package id.josjismart;

import id.josjismart.db.Database;
import id.josjismart.ui.MainFrame;
import javax.swing.*;
import java.awt.*;

public final class App {
    private App() {}
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch(Exception ignored) {}
        UIManager.put("OptionPane.messageFont",new Font("Segoe UI",Font.PLAIN,13));
        UIManager.put("Label.font",new Font("Segoe UI",Font.PLAIN,13));
        SwingUtilities.invokeLater(()->{
            try { Database.initialize(); new MainFrame().setVisible(true); }
            catch(Exception e) {
                JTextArea details=new JTextArea("Koneksi / inisialisasi MySQL gagal.\n\n" + e.getMessage() +
                        "\n\nPeriksa:\n1. XAMPP > MySQL START\n2. config.properties di folder proyek\n3. Port MySQL (biasa 3306)\n4. User dan password MySQL\n5. Maven sudah mengunduh driver MySQL");
                details.setFont(new Font("Segoe UI",Font.PLAIN,13));details.setEditable(false);
                details.setBackground(UIManager.getColor("Panel.background"));
                JOptionPane.showMessageDialog(null,details,"JOSJISMART | Gagal memulai",JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}
