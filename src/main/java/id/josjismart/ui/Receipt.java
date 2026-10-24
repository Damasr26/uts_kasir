package id.josjismart.ui;

import id.josjismart.model.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import java.awt.*;

public final class Receipt {
    private Receipt() {}
    private static String cut(String value,int width) {return value.length()<=width?value:value.substring(0,width-1)+"~";}
    public static String build(Sale sale,List<SaleItem> items) {
        StringBuilder b=new StringBuilder();
        b.append("             JOSJISMART\n").append("       DIAMOND POINT OF SALE\n")
         .append("============================================\n")
         .append("No. Nota : ").append(sale.invoice()).append("\n")
         .append("Tanggal  : ").append(sale.dateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n")
         .append("Pembeli  : ").append(sale.customer()).append("\n")
         .append("Kasir    : ").append(sale.cashier()).append("\n")
         .append("--------------------------------------------\n");
        for(SaleItem i:items) {
            b.append(cut(i.name(),42)).append("\n")
             .append(String.format("%3d %-6s x %-14s %s%n",i.qty(),cut(i.unit(),6),Skin.money(i.price()),Skin.money(i.total())));
        }
        b.append("--------------------------------------------\n")
         .append("TOTAL   : ").append(Skin.money(sale.total())).append("\n")
         .append("BAYAR   : ").append(Skin.money(sale.paid())).append("\n")
         .append("KEMBALI : ").append(Skin.money(sale.change())).append("\n")
         .append("============================================\n")
         .append("  Terima kasih telah belanja di JOSJISMART\n")
         .append("          Sampai jumpa kembali!\n");
        return b.toString();
    }
    public static void show(Component parent,Sale sale,List<SaleItem> items) {
        JTextArea text=Skin.area();text.setText(build(sale,items));text.setCaretPosition(0);
        JButton print=Skin.button("Cetak Struk"),close=Skin.secondary("Tutup");
        JDialog dialog=new JDialog(SwingUtilities.getWindowAncestor(parent),"Struk " + sale.invoice(),Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(0,10));dialog.getContentPane().setBackground(new Color(234,230,218));
        dialog.add(Skin.scroll(text),BorderLayout.CENTER);
        JPanel actions=Skin.wrapRow(print,close);dialog.add(actions,BorderLayout.SOUTH);
        print.addActionListener(e->{try{text.print();}catch(Exception ex){Skin.error(dialog,ex);}});
        close.addActionListener(e->dialog.dispose());
        dialog.setSize(530,610);dialog.setLocationRelativeTo(parent);dialog.setVisible(true);
    }
}
