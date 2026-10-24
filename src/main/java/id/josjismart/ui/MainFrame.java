package id.josjismart.ui;

import java.awt.*;
import java.time.LocalDate;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import id.josjismart.repo.*;

public final class MainFrame extends JFrame {
    private String cashier;
    private final JLabel cashierLabel=Skin.text("",12,false,new Color(221,234,237));
    private final StoreRepository store=new StoreRepository();
    private final SaleRepository sales=new SaleRepository();
    private final CardLayout cardLayout=new CardLayout();
    private final JPanel pages=new JPanel(cardLayout);
    private final JLabel heading=Skin.text("Ringkasan Toko",25,true,Skin.NAVY);
    private final JLabel subheading=Skin.text("Kondisi toko hari ini",13,false,Skin.MUTED);
    private final DashboardPanel dashboard;
    private final ProductPanel products;
    private final PosPanel pos;
    private final ReportPanel reports;
    private final SettingsPanel settings;

    public MainFrame() throws SQLException {
        super("JOSJISMART | Kasir & Manajemen Toko");
        this.cashier=store.getSetting("cashier_name","Kasir JOSJISMART");
        cashierLabel.setText("Kasir: " + this.cashier);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1070,690));setSize(1380,830);setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel menu=new JPanel(new BorderLayout(0,12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D x=(Graphics2D)g.create();
                x.setPaint(new GradientPaint(0,0,new Color(35,66,88),getWidth(),getHeight(),new Color(11,27,47)));
                x.fillRect(0,0,getWidth(),getHeight());
                x.setColor(new Color(255,255,255,11));
                for(int i=3;i<getHeight();i+=6)x.drawLine(0,i,getWidth(),i);
                x.setColor(new Color(209,167,104,100));x.drawLine(getWidth()-2,0,getWidth()-2,getHeight());
                x.dispose();super.paintComponent(g);
            }
        };
        menu.setOpaque(false);menu.setPreferredSize(new Dimension(236,0));
        menu.setBorder(new EmptyBorder(30,18,22,18));
        JPanel brand=new JPanel(new GridLayout(3,1,0,3));brand.setOpaque(false);
        brand.add(Skin.text("◆  JOSJISMART",23,true,new Color(237,244,246)));
        brand.add(Skin.text("DIAMOND REGISTER",11,true,new Color(213,174,109)));
        brand.add(Skin.text("EST. 2026  /  POS SYSTEM",10,false,new Color(139,174,195)));
        menu.add(brand,BorderLayout.NORTH);
        JPanel nav=new JPanel(new GridLayout(0,1,0,9));nav.setOpaque(false);
        JButton home=navButton("◈   Dashboard"), productsB=navButton("▦   Data Produk"),
                posB=navButton("▣   Point of Sale"), reportsB=navButton("▤   Laporan & Analisis"),
                settingsB=navButton("⚙   Pengaturan");
        nav.add(home);nav.add(productsB);nav.add(posB);nav.add(reportsB);nav.add(settingsB);
        JPanel navHolder=new JPanel(new BorderLayout());navHolder.setOpaque(false);
        navHolder.setBorder(new EmptyBorder(35,0,0,0));navHolder.add(nav,BorderLayout.NORTH);
        menu.add(navHolder,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new GridLayout(3,1,0,8));bottom.setOpaque(false);
        bottom.add(Skin.text("●  DATABASE CONNECTED",10,true,new Color(117,203,171)));
        bottom.add(cashierLabel);
        JButton exit=navButton("↪   Tutup Aplikasi");bottom.add(exit);
        menu.add(bottom,BorderLayout.SOUTH);add(menu,BorderLayout.WEST);

        Skin.WorkshopBackground right=new Skin.WorkshopBackground(new BorderLayout());
        JPanel header=new JPanel(new BorderLayout());header.setOpaque(false);header.setBorder(new EmptyBorder(26,29,18,29));
        JPanel headline=new JPanel(new GridLayout(2,1,0,5));headline.setOpaque(false);
        headline.add(heading);headline.add(subheading);header.add(headline,BorderLayout.WEST);
        header.add(Skin.text("JOSJISMART   /   " + LocalDate.now(),12,true,Skin.GOLD),BorderLayout.EAST);
        right.add(header,BorderLayout.NORTH);
        pages.setOpaque(false);right.add(pages,BorderLayout.CENTER);add(right,BorderLayout.CENTER);

        dashboard=new DashboardPanel(this,sales);products=new ProductPanel(this,store);
        pos=new PosPanel(this,store,sales);reports=new ReportPanel(this,sales);
        settings=new SettingsPanel(this,store);
        pages.add(dashboard,"home");pages.add(products,"products");pages.add(pos,"pos");
        pages.add(reports,"reports");pages.add(settings,"settings");
        home.addActionListener(e->showPage("home"));
        productsB.addActionListener(e->showPage("products"));
        posB.addActionListener(e->showPage("pos"));
        reportsB.addActionListener(e->showPage("reports"));
        settingsB.addActionListener(e->showPage("settings"));
        exit.addActionListener(e->{
            if(JOptionPane.showConfirmDialog(this,"Tutup aplikasi JOSJISMART?","Konfirmasi Keluar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                dispose();
            }
        });
        showPage("home");
    }
    private JButton navButton(String label) {
        JButton b=new JButton(label);b.setFont(new Font("Segoe UI",Font.BOLD,13));
        b.setForeground(new Color(222,235,242));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(11,12,11,8));b.setOpaque(false);b.setContentAreaFilled(false);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {b.setForeground(new Color(244,198,117));}
            @Override public void mouseExited(java.awt.event.MouseEvent e) {b.setForeground(new Color(222,235,242));}
        });return b;
    }
    public void showPage(String name) {
        cardLayout.show(pages,name);
        switch(name) {
            case "home" -> {heading.setText("Dashboard JOSJISMART");subheading.setText("Ringkasan performa dan kondisi toko");dashboard.refresh();}
            case "products" -> {heading.setText("Inventaris Produk");subheading.setText("Tambah, ubah, hapus, dan cari item toko");products.refresh();}
            case "pos" -> {heading.setText("Point of Sale");subheading.setText("Keranjang transaksi dan pembayaran pelanggan");pos.refreshProducts();}
            case "reports" -> {heading.setText("Laporan & Analisis");subheading.setText("Riwayat permanen, insight, dan ekspor CSV");reports.refresh();}
            default -> {heading.setText("Pengaturan Toko");subheading.setText("Atur nama kasir pada transaksi dan struk");settings.refresh();}
        }
    }
    public String cashierName() {return cashier;}
    public void setCashierName(String name) {this.cashier=name;cashierLabel.setText("Kasir: " + name);}
    public void onSaleCompleted() {products.refresh();pos.refreshProducts();dashboard.refresh();reports.refresh();}
}
