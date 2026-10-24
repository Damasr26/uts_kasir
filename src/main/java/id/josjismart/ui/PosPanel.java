package id.josjismart.ui;

import id.josjismart.model.*;
import id.josjismart.repo.*;
import java.awt.*;
import java.math.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

public final class PosPanel extends JPanel {
    private final MainFrame frame;
    private final StoreRepository store;
    private final SaleRepository sales;
    private java.util.List<Product> results=java.util.List.of();
    private final Map<Long,CartLine> cart=new LinkedHashMap<>();
    private final DefaultTableModel catalogModel=Skin.model("SKU","Produk","Stok","Harga");
    private final DefaultTableModel cartModel=Skin.model("Barang","Qty","Harga","Subtotal");
    private final JTable catalog=Skin.table(catalogModel),cartTable=Skin.table(cartModel);
    private final JTextField search=Skin.field(18),customer=Skin.field(18),payment=Skin.field(18);
    private final JLabel grandTotal=Skin.text("Rp0",31,true,Skin.NAVY),change=Skin.text("Kembalian: Rp0",13,true,Skin.BLUE),
            cartCount=Skin.text("0 item",12,false,Skin.MUTED);
    private Sale lastSale;
    public PosPanel(MainFrame frame,StoreRepository store,SaleRepository sales) {
        super(new BorderLayout(14,0));this.frame=frame;this.store=store;this.sales=sales;
        setOpaque(false);setBorder(new EmptyBorder(0,27,26,27));
        Skin.Surface left=Skin.card();left.setLayout(new BorderLayout(0,12));
        left.add(Skin.heading("Pilih Produk","Klik dua kali atau gunakan tombol tambah"),BorderLayout.NORTH);
        JPanel catalogArea=new JPanel(new BorderLayout(0,11));catalogArea.setOpaque(false);
        catalogArea.add(search,BorderLayout.NORTH);
        catalog.setAutoCreateRowSorter(true);catalogArea.add(Skin.scroll(catalog),BorderLayout.CENTER);
        JButton add=Skin.button("+ Masukkan ke Keranjang"),refresh=Skin.secondary("Muat Katalog");
        JPanel choices=Skin.wrapRow(add,refresh);catalogArea.add(choices,BorderLayout.SOUTH);
        left.add(catalogArea,BorderLayout.CENTER);
        add(left,BorderLayout.CENTER);

        Skin.Surface right=Skin.card();right.setLayout(new BorderLayout(0,12));
        right.setPreferredSize(new Dimension(480,100));
        JPanel rtop=new JPanel(new BorderLayout());rtop.setOpaque(false);
        rtop.add(Skin.heading("Keranjang Belanja","Rincian pembelian pelanggan"),BorderLayout.CENTER);
        rtop.add(cartCount,BorderLayout.EAST);right.add(rtop,BorderLayout.NORTH);
        JPanel middle=new JPanel(new BorderLayout(0,11));middle.setOpaque(false);
        middle.add(Skin.scroll(cartTable),BorderLayout.CENTER);
        JPanel cartButtons=Skin.wrapRow();
        JButton qty=Skin.secondary("Ubah Qty"),remove=Skin.secondary("Hapus Item"),clear=Skin.secondary("Kosongkan");
        cartButtons.add(qty);cartButtons.add(remove);cartButtons.add(clear);middle.add(cartButtons,BorderLayout.SOUTH);
        right.add(middle,BorderLayout.CENTER);
        JPanel payArea=new JPanel(new BorderLayout(0,11));payArea.setOpaque(false);
        JPanel form=new JPanel(new GridLayout(2,1,0,9));form.setOpaque(false);
        customer.setText("Pelanggan Umum");
        form.add(Skin.labeled("NAMA PEMBELI",customer));form.add(Skin.labeled("UANG BAYAR (RUPIAH)",payment));
        payArea.add(form,BorderLayout.NORTH);
        JPanel totalBox=new JPanel(new GridLayout(3,1,0,5));totalBox.setOpaque(false);
        totalBox.add(Skin.text("TOTAL PEMBAYARAN",11,true,Skin.GOLD));totalBox.add(grandTotal);totalBox.add(change);
        payArea.add(totalBox,BorderLayout.CENTER);
        JButton checkout=Skin.button("PROSES PEMBAYARAN  ✓"),receipt=Skin.secondary("Cetak Struk Terakhir");
        JPanel payBtns=new JPanel(new GridLayout(2,1,0,8));payBtns.setOpaque(false);
        payBtns.add(checkout);payBtns.add(receipt);payArea.add(payBtns,BorderLayout.SOUTH);
        right.add(payArea,BorderLayout.SOUTH);add(right,BorderLayout.EAST);
        add.addActionListener(e->addSelected());
        catalog.addMouseListener(new java.awt.event.MouseAdapter() { @Override public void mouseClicked(java.awt.event.MouseEvent e) {if(e.getClickCount()==2)addSelected();} });
        refresh.addActionListener(e->refreshProducts());
        qty.addActionListener(e->changeQty());remove.addActionListener(e->removeSelected());
        clear.addActionListener(e->{if(!cart.isEmpty()&&JOptionPane.showConfirmDialog(this,"Kosongkan keranjang?","Konfirmasi",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){cart.clear();drawCart();}});
        checkout.addActionListener(e->checkout());
        receipt.addActionListener(e->showLastReceipt());
        payment.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){updateChange();}
            public void removeUpdate(DocumentEvent e){updateChange();}
            public void changedUpdate(DocumentEvent e){updateChange();}
        });
        search.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){refreshProducts();}
            public void removeUpdate(DocumentEvent e){refreshProducts();}
            public void changedUpdate(DocumentEvent e){refreshProducts();}
        });
        drawCart();
    }
    public void refreshProducts() {
        try {
            results=store.findProducts(search.getText());catalogModel.setRowCount(0);
            for(Product p:results)catalogModel.addRow(new Object[]{p.sku(),p.name(),p.stock(),Skin.money(p.price())});
        }catch(Exception ex){Skin.error(this,ex);}
    }
    private void addSelected() {
        int row=catalog.getSelectedRow();if(row<0){Skin.info(this,"Pilih produk dahulu.");return;}
        int index=catalog.convertRowIndexToModel(row);
        if(index>=results.size())return;
        Product p=results.get(index);
        CartLine line=cart.get(p.id());
        int next=(line==null?0:line.quantity())+1;
        if(next>p.stock()){Skin.info(this,"Stok " + p.name() + " hanya " + p.stock() + ".");return;}
        if(line==null)cart.put(p.id(),new CartLine(p,1));else line.setQuantity(next);
        drawCart();
    }
    private java.util.List<CartLine> cartRows() {return new ArrayList<>(cart.values());}
    private CartLine selectedCart() {
        int row=cartTable.getSelectedRow();
        if(row<0){Skin.info(this,"Pilih item pada keranjang.");return null;}
        var rows=cartRows();return row<rows.size()?rows.get(row):null;
    }
    private void changeQty() {
        CartLine line=selectedCart();if(line==null)return;
        String input=JOptionPane.showInputDialog(this,"Jumlah untuk " + line.product().name(),line.quantity());
        if(input==null)return;
        try{
            int q=Integer.parseInt(input.trim());
            if(q<1||q>line.product().stock())throw new IllegalArgumentException("Jumlah minimal 1 dan maksimal " + line.product().stock());
            line.setQuantity(q);drawCart();
        }catch(Exception e){Skin.error(this,e);}
    }
    private void removeSelected() {CartLine line=selectedCart();if(line!=null){cart.remove(line.product().id());drawCart();}}
    private BigDecimal total() {
        BigDecimal total=BigDecimal.ZERO;for(CartLine line:cart.values())total=total.add(line.total());return total;
    }
    private static BigDecimal parsePayment(String str) {
        if(str==null||str.isBlank())return BigDecimal.ZERO;
        String n=str.trim().replace(".","").replace(",","");
        if(!n.matches("\\d+"))throw new IllegalArgumentException("Uang bayar harus angka, contoh: 50000");
        return new BigDecimal(n);
    }
    private void drawCart() {
        cartModel.setRowCount(0);int qty=0;
        for(CartLine line:cart.values()) {
            cartModel.addRow(new Object[]{line.product().name(),line.quantity(),Skin.money(line.product().price()),Skin.money(line.total())});
            qty+=line.quantity();
        }
        grandTotal.setText(Skin.money(total()));cartCount.setText(qty+" unit");updateChange();
    }
    private void updateChange() {
        try {
            BigDecimal diff=parsePayment(payment.getText()).subtract(total());
            change.setText(diff.signum()<0?"Kurang bayar: "+Skin.money(diff.negate()):"Kembalian: "+Skin.money(diff));
        }catch(Exception e){change.setText("Nominal pembayaran tidak valid");}
    }
    private void checkout() {
        try {
            if(cart.isEmpty())throw new IllegalArgumentException("Keranjang kosong.");
            BigDecimal paid=parsePayment(payment.getText());
            if(paid.compareTo(total())<0)throw new IllegalArgumentException("Pembayaran kurang. Total: "+Skin.money(total()));
            if(JOptionPane.showConfirmDialog(this,"Pembeli: "+(customer.getText().isBlank()?"Pelanggan Umum":customer.getText())+
                    "\nTotal: "+Skin.money(total())+"\nBayar: "+Skin.money(paid)+"\n\nSelesaikan transaksi?",
                    "Konfirmasi Pembayaran",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            Sale completed=sales.checkout(cartRows(),customer.getText(),frame.cashierName(),paid);
            lastSale=completed;cart.clear();payment.setText("");customer.setText("Pelanggan Umum");drawCart();
            frame.onSaleCompleted();
            Skin.info(this,"Transaksi berhasil!\nNo nota: " + completed.invoice() + "\nKembalian: " + Skin.money(completed.change()));
            showLastReceipt();
        }catch(Exception ex){Skin.error(this,ex);}
    }
    private void showLastReceipt() {
        if(lastSale==null){Skin.info(this,"Belum ada transaksi yang dibuat dari sesi ini.\nStruk lama tersedia di menu Laporan.");return;}
        try{Receipt.show(this,lastSale,sales.findSaleItems(lastSale.id()));}catch(Exception ex){Skin.error(this,ex);}
    }
}
