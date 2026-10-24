package id.josjismart.ui;

import id.josjismart.repo.SaleRepository;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class DashboardPanel extends JPanel {
    private final MainFrame parent;
    private final SaleRepository sales;
    private final JLabel revenue=Skin.text("Rp0",24,true,Skin.NAVY), count=Skin.text("0",27,true,Skin.NAVY),
            units=Skin.text("0",27,true,Skin.NAVY), stock=Skin.text("0",27,true,Skin.NAVY);
    private final RevenueChart chart=new RevenueChart();
    private final JPanel bests=new JPanel();
    public DashboardPanel(MainFrame parent,SaleRepository sales) {
        super(new BorderLayout(0,19));this.parent=parent;this.sales=sales;setOpaque(false);
        setBorder(new EmptyBorder(0,27,26,27));
        JPanel stats=new JPanel(new GridLayout(1,4,13,0));stats.setOpaque(false);
        stats.add(tile("OMZET HARI INI",revenue,"Total uang penjualan"));
        stats.add(tile("TRANSAKSI",count,"Jumlah struk hari ini"));
        stats.add(tile("UNIT TERJUAL",units,"Total barang terjual"));
        stats.add(tile("STOK MENIPIS",stock,"Item dengan stok ≤ 5"));
        add(stats,BorderLayout.NORTH);
        JPanel lower=new JPanel(new GridLayout(1,2,18,0));lower.setOpaque(false);
        Skin.Surface revenueCard=Skin.card();revenueCard.setLayout(new BorderLayout(0,12));
        revenueCard.add(Skin.heading("Grafik Omzet","7 hari terakhir • penjualan harian"),BorderLayout.NORTH);
        revenueCard.add(chart,BorderLayout.CENTER);
        lower.add(revenueCard);
        Skin.Surface popular=Skin.card();popular.setLayout(new BorderLayout(0,8));
        popular.add(Skin.heading("Produk Terlaris","Peringkat berdasarkan unit terjual, 30 hari"),BorderLayout.NORTH);
        bests.setLayout(new BoxLayout(bests,BoxLayout.Y_AXIS));bests.setOpaque(false);
        popular.add(bests,BorderLayout.CENTER);lower.add(popular);add(lower,BorderLayout.CENTER);
        JButton toPOS=Skin.button("BUAT TRANSAKSI BARU    →");toPOS.addActionListener(e->parent.showPage("pos"));
        JPanel foot=Skin.spaced(new FlowLayout(FlowLayout.RIGHT),0,0,0,0);foot.add(toPOS);add(foot,BorderLayout.SOUTH);
    }
    private Skin.Surface tile(String title,JLabel number,String caption) {
        Skin.Surface s=Skin.card();s.setLayout(new GridLayout(3,1,0,8));
        s.add(Skin.text(title,11,true,Skin.GOLD));s.add(number);s.add(Skin.text(caption,12,false,Skin.MUTED));return s;
    }
    public void refresh() {
        try {
            LocalDate now=LocalDate.now();SaleRepository.Summary summary=sales.summary(now,now);
            revenue.setText(Skin.money(summary.revenue()));count.setText(""+summary.transactions());
            units.setText(""+summary.units());stock.setText(""+summary.lowStock());
            chart.setValues(sales.dailyRevenue(now.minusDays(6),now));
            bests.removeAll();int index=1;
            for(var item:sales.bestSellers(now.minusDays(29),now)) {
                JPanel row=Skin.spaced(new BorderLayout(8,0),9,3,9,3);
                row.add(Skin.text(index+".  "+item.name(),13,true,Skin.INK),BorderLayout.CENTER);
                row.add(Skin.text(item.sold()+" unit",13,true,Skin.BLUE),BorderLayout.EAST);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));bests.add(row);index++;
            }
            if(index==1)bests.add(Skin.text("Belum ada transaksi. Mulai jualan sekarang!",13,false,Skin.MUTED));
            bests.revalidate();bests.repaint();
        }catch(Exception ex){Skin.error(this,ex);}
    }
}
