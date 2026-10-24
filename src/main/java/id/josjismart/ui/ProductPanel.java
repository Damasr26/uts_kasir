package id.josjismart.ui;

import id.josjismart.model.Product;
import id.josjismart.repo.StoreRepository;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public final class ProductPanel extends JPanel {
    private final MainFrame frame;
    private final StoreRepository store;
    private final JTextField search=Skin.field(25);
    private final DefaultTableModel model=Skin.model("SKU","Nama Produk","Kategori","Satuan","Harga","Stok");
    private final JTable table=Skin.table(model);
    private java.util.List<Product> results=java.util.List.of();
    private final JLabel count=Skin.text("0 produk",12,false,Skin.MUTED);
    public ProductPanel(MainFrame frame,StoreRepository store) {
        super(new BorderLayout(0,16));this.frame=frame;this.store=store;setOpaque(false);
        setBorder(new EmptyBorder(0,27,27,27));
        Skin.Surface toolbar=Skin.card();toolbar.setLayout(new BorderLayout(16,0));
        JPanel label=new JPanel(new GridLayout(2,1,0,4));label.setOpaque(false);
        label.add(Skin.text("Katalog Barang",17,true,Skin.NAVY));label.add(count);toolbar.add(label,BorderLayout.WEST);
        toolbar.add(search,BorderLayout.CENTER);search.putClientProperty("JTextField.placeholderText","Cari SKU / nama / kategori");
        JButton add=Skin.button("+ Tambah Produk");toolbar.add(add,BorderLayout.EAST);add(toolbar,BorderLayout.NORTH);
        table.setAutoCreateRowSorter(true);
        Skin.Surface area=Skin.card();area.setLayout(new BorderLayout(0,12));
        area.add(Skin.text("INVENTARIS JOSJISMART",12,true,Skin.GOLD),BorderLayout.NORTH);
        area.add(Skin.scroll(table),BorderLayout.CENTER);
        JPanel action=Skin.wrapRow();
        JButton edit=Skin.secondary("✎  Edit Terpilih"),delete=Skin.secondary("✕  Hapus Terpilih"),reload=Skin.secondary("↻  Muat Ulang");
        action.add(edit);action.add(delete);action.add(reload);area.add(action,BorderLayout.SOUTH);add(area,BorderLayout.CENTER);
        add.addActionListener(e->openEditor(null));
        edit.addActionListener(e->{Product p=selected();if(p!=null)openEditor(p);});
        delete.addActionListener(e->deleteSelected());
        reload.addActionListener(e->refresh());
        table.addMouseListener(new java.awt.event.MouseAdapter(){@Override public void mouseClicked(java.awt.event.MouseEvent e){if(e.getClickCount()==2){Product p=selected();if(p!=null)openEditor(p);}}});
        search.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){refresh();}
            public void removeUpdate(DocumentEvent e){refresh();}
            public void changedUpdate(DocumentEvent e){refresh();}
        });
    }
    private Product selected() {
        int row=table.getSelectedRow();
        if(row<0){Skin.info(this,"Pilih satu produk di tabel terlebih dahulu.");return null;}
        int index=table.convertRowIndexToModel(row);
        return index>=results.size()?null:results.get(index);
    }
    public void refresh() {
        try {
            results=store.findProducts(search.getText());model.setRowCount(0);
            for(Product p:results)model.addRow(new Object[]{p.sku(),p.name(),p.category(),p.unit(),Skin.money(p.price()),p.stock()});
            count.setText(results.size()+" produk ditemukan");
        }catch(Exception ex){Skin.error(this,ex);}
    }
    private void openEditor(Product selected) {
        boolean edit=selected!=null;
        JTextField sku=Skin.field(18),name=Skin.field(22),cat=Skin.field(17),unit=Skin.field(12),price=Skin.field(12),stock=Skin.field(12);
        if(edit) {
            sku.setText(selected.sku());name.setText(selected.name());cat.setText(selected.category());
            unit.setText(selected.unit());price.setText(selected.price().stripTrailingZeros().toPlainString());
            stock.setText(""+selected.stock());
        } else {cat.setText("Umum");unit.setText("pcs");stock.setText("0");}
        JPanel form=new JPanel(new GridLayout(6,1,0,11));form.setBackground(new Color(244,242,233));
        form.setBorder(new EmptyBorder(10,12,10,12));
        form.add(Skin.labeled("SKU / KODE BARANG",sku));form.add(Skin.labeled("NAMA BARANG",name));
        form.add(Skin.labeled("KATEGORI",cat));form.add(Skin.labeled("SATUAN",unit));
        form.add(Skin.labeled("HARGA RUPIAH (contoh 12500)",price));form.add(Skin.labeled("STOK",stock));
        while(true) {
            int answer=JOptionPane.showConfirmDialog(this,form,edit?"Edit Produk":"Tambah Produk",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
            if(answer!=JOptionPane.OK_OPTION)return;
            try {
                String harga=price.getText().trim().replace(".","").replace(",","");
                Product p=new Product(edit?selected.id():0,sku.getText().trim(),name.getText().trim(),
                        cat.getText().trim(),unit.getText().trim(),new BigDecimal(harga),Integer.parseInt(stock.getText().trim()));
                if(p.price().scale()>2)throw new IllegalArgumentException("Harga maksimal 2 angka desimal.");
                store.saveProduct(p);refresh();Skin.info(this,edit?"Data produk diperbarui.":"Produk ditambahkan.");return;
            }catch(Exception ex){Skin.error(this,ex);}
        }
    }
    private void deleteSelected() {
        Product p=selected();if(p==null)return;
        int answer=JOptionPane.showConfirmDialog(this,"Hapus produk '"+p.name()+"'?\n\nRiwayat transaksi lama tetap utuh karena tersimpan sebagai snapshot.",
                "Konfirmasi Hapus",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(answer!=JOptionPane.YES_OPTION)return;
        try {store.deleteProduct(p.id());refresh();Skin.info(this,"Produk dihapus. Riwayat tidak berubah.");}
        catch(Exception ex){Skin.error(this,ex);}
    }
}
