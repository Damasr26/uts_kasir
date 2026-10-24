package id.josjismart.ui;

import id.josjismart.model.*;
import id.josjismart.repo.*;
import java.awt.*;
import java.io.*;
import java.math.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public final class ReportPanel extends JPanel {
    private final SaleRepository sales;
    private final JSpinner from=new JSpinner(new SpinnerDateModel()),to=new JSpinner(new SpinnerDateModel());
    private final JTextField search=Skin.field(17);
    private final DefaultTableModel historyModel=Skin.model("Invoice","Waktu","Pembeli","Kasir","Total","Bayar","Kembali");
    private final JTable table=Skin.table(historyModel);
    private java.util.List<Sale> results=java.util.List.of();
    private final JLabel revenue=Skin.text("Rp0",22,true,Skin.NAVY),tx=Skin.text("0",25,true,Skin.NAVY),
            average=Skin.text("Rp0",22,true,Skin.NAVY),units=Skin.text("0",25,true,Skin.NAVY);
    private final RevenueChart chart=new RevenueChart();
    private final DefaultTableModel bestModel=Skin.model("Produk (Snapshot)","SKU","Unit Terjual","Omzet");
    private final JLabel note=Skin.text("0 transaksi ditemukan",12,false,Skin.MUTED);
    public ReportPanel(MainFrame frame,SaleRepository sales) {
        super(new BorderLayout(0,13));this.sales=sales;setOpaque(false);setBorder(new EmptyBorder(0,27,26,27));
        LocalDate today=LocalDate.now();
        from.setValue(java.sql.Date.valueOf(today.withDayOfMonth(1)));
        to.setValue(java.sql.Date.valueOf(today));
        from.setEditor(new JSpinner.DateEditor(from,"dd/MM/yyyy"));to.setEditor(new JSpinner.DateEditor(to,"dd/MM/yyyy"));
        from.setPreferredSize(new Dimension(128,38));to.setPreferredSize(new Dimension(128,38));
        Skin.Surface filter=Skin.card();filter.setLayout(new BorderLayout(7,9));
        JPanel dateControls=Skin.wrapRow(Skin.text("DARI",11,true,Skin.INK),from,Skin.text("SAMPAI",11,true,Skin.INK),to);
        JButton apply=Skin.button("Tampilkan Laporan");dateControls.add(apply);filter.add(dateControls,BorderLayout.NORTH);
        JPanel hint=Skin.spaced(new BorderLayout(),0,3,0,3);
        hint.add(Skin.text("Periode berlaku untuk riwayat dan analisis. Pencarian hanya memfilter tabel transaksi.",12,false,Skin.MUTED),BorderLayout.WEST);
        filter.add(hint,BorderLayout.SOUTH);add(filter,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.setFont(new Font("Segoe UI",Font.BOLD,13));
        tabs.setBorder(null);tabs.setOpaque(false);
        tabs.addTab("RIWAYAT TRANSAKSI",historyTab());tabs.addTab("ANALISIS PENJUALAN",analyticsTab());
        add(tabs,BorderLayout.CENTER);
        apply.addActionListener(e->refresh());
    }
    private JPanel historyTab() {
        JPanel p=Skin.spaced(new BorderLayout(0,11),9,0,0,0);
        Skin.Surface card=Skin.card();card.setLayout(new BorderLayout(0,11));
        JPanel header=new JPanel(new BorderLayout(9,0));header.setOpaque(false);
        JPanel labels=new JPanel(new GridLayout(2,1,0,4));labels.setOpaque(false);
        labels.add(Skin.text("Arsip Penjualan",18,true,Skin.NAVY));labels.add(note);
        header.add(labels,BorderLayout.WEST);
        search.putClientProperty("JTextField.placeholderText","Cari invoice / pembeli / kasir");
        header.add(search,BorderLayout.CENTER);
        JButton find=Skin.secondary("Cari");header.add(find,BorderLayout.EAST);card.add(header,BorderLayout.NORTH);
        table.setAutoCreateRowSorter(true);card.add(Skin.scroll(table),BorderLayout.CENTER);
        JButton detail=Skin.button("Lihat Detail / Struk"),export=Skin.secondary("Ekspor CSV Rincian"),reload=Skin.secondary("Refresh");
        JPanel actions=Skin.wrapRow(detail,export,reload);card.add(actions,BorderLayout.SOUTH);
        p.add(card,BorderLayout.CENTER);
        find.addActionListener(e->refreshHistory());search.addActionListener(e->refreshHistory());
        detail.addActionListener(e->showDetail());export.addActionListener(e->exportCsv());reload.addActionListener(e->refresh());
        table.addMouseListener(new java.awt.event.MouseAdapter(){@Override public void mouseClicked(java.awt.event.MouseEvent e){if(e.getClickCount()==2)showDetail();}});
        return p;
    }
    private JPanel analyticsTab() {
        JPanel p=Skin.spaced(new BorderLayout(0,13),9,0,0,0);
        JPanel stats=new JPanel(new GridLayout(1,4,11,0));stats.setOpaque(false);
        stats.add(stat("TOTAL OMZET",revenue));stats.add(stat("TRANSAKSI",tx));
        stats.add(stat("RATA-RATA NOTA",average));stats.add(stat("UNIT TERJUAL",units));
        p.add(stats,BorderLayout.NORTH);
        JPanel graphs=new JPanel(new GridLayout(1,2,13,0));graphs.setOpaque(false);
        Skin.Surface graph=Skin.card();graph.setLayout(new BorderLayout(0,7));
        graph.add(Skin.heading("Tren Penjualan","Omzet 14 hari terakhir dari periode terpilih"),BorderLayout.NORTH);
        graph.add(chart,BorderLayout.CENTER);graphs.add(graph);
        Skin.Surface popular=Skin.card();popular.setLayout(new BorderLayout(0,11));
        popular.add(Skin.heading("Barang Terlaris","Berdasarkan snapshot produk saat transaksi"),BorderLayout.NORTH);
        popular.add(Skin.scroll(Skin.table(bestModel)),BorderLayout.CENTER);graphs.add(popular);
        p.add(graphs,BorderLayout.CENTER);
        return p;
    }
    private Skin.Surface stat(String title,JLabel value) {
        Skin.Surface card=Skin.card();card.setLayout(new GridLayout(2,1,0,8));
        card.add(Skin.text(title,11,true,Skin.GOLD));card.add(value);return card;
    }
    private LocalDate date(JSpinner s) {
        return Instant.ofEpochMilli(((java.util.Date)s.getValue()).getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }
    private void validateDates() {
        if(date(to).isBefore(date(from)))throw new IllegalArgumentException("Tanggal akhir harus setelah atau sama dengan tanggal awal.");
    }
    public void refresh() {
        try {
            from.commitEdit();to.commitEdit();validateDates();
            LocalDate start=date(from),end=date(to);
            SaleRepository.Summary summary=sales.summary(start,end);
            revenue.setText(Skin.money(summary.revenue()));tx.setText(""+summary.transactions());
            average.setText(Skin.money(summary.average()));units.setText(""+summary.units());
            chart.setValues(sales.dailyRevenue(start,end));bestModel.setRowCount(0);
            for(var b:sales.bestSellers(start,end))bestModel.addRow(new Object[]{b.name(),b.sku(),b.sold(),Skin.money(b.revenue())});
            refreshHistory();
        }catch(Exception ex){Skin.error(this,ex);}
    }
    private void refreshHistory() {
        try {
            from.commitEdit();to.commitEdit();validateDates();
            results=sales.listSales(date(from),date(to),search.getText());
            historyModel.setRowCount(0);
            for(Sale s:results)historyModel.addRow(new Object[]{s.invoice(),
                s.dateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),s.customer(),s.cashier(),
                Skin.money(s.total()),Skin.money(s.paid()),Skin.money(s.change())});
            note.setText(results.size()+" transaksi (maksimal 2.000 baris per pencarian)");
        }catch(Exception ex){Skin.error(this,ex);}
    }
    private void showDetail() {
        int row=table.getSelectedRow();
        if(row<0){Skin.info(this,"Pilih salah satu transaksi untuk melihat detail.");return;}
        int index=table.convertRowIndexToModel(row);if(index>=results.size())return;
        try {Sale s=results.get(index);Receipt.show(this,s,sales.findSaleItems(s.id()));}
        catch(Exception ex){Skin.error(this,ex);}
    }
    private static String csv(String value) {
        if(value==null)value="";
        // Hindari CSV formula injection ketika membuka file memakai Excel/LibreOffice.
        if(!value.isEmpty()&&"=+-@".indexOf(value.charAt(0))>=0)value="'"+value;
        return "\""+value.replace("\"","\"\"")+"\"";
    }
    private void exportCsv() {
        try {
            JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new File("josjismart-laporan-"+LocalDate.now()+".csv"));
            if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
            File file=chooser.getSelectedFile();
            if(file.exists()&&JOptionPane.showConfirmDialog(this,"File sudah ada. Timpa?","Konfirmasi",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            try(BufferedWriter w=Files.newBufferedWriter(file.toPath(),StandardCharsets.UTF_8)) {
                w.write('\uFEFF');
                w.write("Invoice;Tanggal;Pembeli;Kasir;SKU Snapshot;Nama Snapshot;Kategori;Satuan;Qty;Harga Satuan;Subtotal;Total Nota;Bayar;Kembali\n");
                for(Sale sale:results)for(SaleItem item:sales.findSaleItems(sale.id())) {
                    String[] cells={sale.invoice(),sale.dateTime().toString(),sale.customer(),sale.cashier(),item.sku(),item.name(),
                        item.category(),item.unit(),""+item.qty(),item.price().toPlainString(),item.total().toPlainString(),
                        sale.total().toPlainString(),sale.paid().toPlainString(),sale.change().toPlainString()};
                    for(int i=0;i<cells.length;i++){if(i>0)w.write(';');w.write(csv(cells[i]));}
                    w.write('\n');
                }
            }
            Skin.info(this,"Laporan rincian transaksi berhasil diekspor:\n"+file.getAbsolutePath()+
                    "\n\nData berisi snapshot barang historis, bukan hasil join ke produk saat ini.");
        }catch(Exception ex){Skin.error(this,ex);}
    }
}
