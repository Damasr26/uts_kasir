package id.josjismart.ui;

import id.josjismart.repo.StoreRepository;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Pengaturan toko tanpa autentikasi. Nama kasir disalin ke nota saat transaksi dilakukan. */
public final class SettingsPanel extends JPanel {
    private final MainFrame frame;
    private final StoreRepository store;
    private final JTextField cashierName=Skin.field(28);
    public SettingsPanel(MainFrame frame,StoreRepository store) {
        super(new BorderLayout(0,18));this.frame=frame;this.store=store;
        setOpaque(false);setBorder(new EmptyBorder(0,27,27,27));
        Skin.Surface card=Skin.card();card.setLayout(new BorderLayout(0,20));
        card.add(Skin.heading("Pengaturan Toko","Tanpa akun, tanpa password, langsung masuk kasir."),BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(0,16));body.setOpaque(false);
        body.add(Skin.labeled("NAMA KASIR (DITAMPILKAN PADA NOTA)",cashierName),BorderLayout.NORTH);
        JTextArea note=new JTextArea("Nama kasir akan dicatat sebagai snapshot saat pembayaran. " +
                "Mengganti nama kasir di sini tidak mengubah riwayat transaksi lama.\n\n" +
                "Nama pembeli diisi melalui menu Point of Sale. " +
                "Koneksi MySQL tetap diatur melalui config.properties.");
        note.setEditable(false);note.setOpaque(false);note.setLineWrap(true);note.setWrapStyleWord(true);
        note.setFont(new Font("Segoe UI",Font.PLAIN,14));note.setForeground(Skin.MUTED);
        body.add(note,BorderLayout.CENTER);card.add(body,BorderLayout.CENTER);
        JButton save=Skin.button("Simpan Nama Kasir");card.add(Skin.wrapRow(save),BorderLayout.SOUTH);
        save.addActionListener(e->{
            try {
                String name=cashierName.getText().trim();
                store.saveSetting("cashier_name",name);
                frame.setCashierName(name);
                Skin.info(this,"Nama kasir berhasil disimpan. Transaksi baru menggunakan nama ini.");
            }catch(Exception ex){Skin.error(this,ex);}
        });
        JPanel content=new JPanel(new BorderLayout());content.setOpaque(false);
        content.add(card,BorderLayout.NORTH);add(content,BorderLayout.CENTER);
        add(Skin.text("JOSJISMART 1.1  •  Tanpa login  •  Java 21  •  MySQL XAMPP",12,false,Skin.MUTED),BorderLayout.SOUTH);
        refresh();
    }
    public void refresh() {cashierName.setText(frame.cashierName());}
}
