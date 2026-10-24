package id.josjismart.ui;

import java.awt.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/** Komponen custom-painted: bevel, highlight kaca, gradient logam, dan bayangan timbul. */
public final class Skin {
    public static final Color NAVY=new Color(17,36,57), INK=new Color(34,52,67), BLUE=new Color(43,104,152),
        SKY=new Color(89,156,198), GOLD=new Color(191,134,62), PAPER=new Color(238,235,224), MUTED=new Color(109,124,131);
    public static final Font FONT=new Font("Segoe UI",Font.PLAIN,14);
    private Skin() {}
    public static String money(BigDecimal value) {
        NumberFormat n=NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
        n.setMaximumFractionDigits(0);n.setMinimumFractionDigits(0);
        return n.format(value==null?BigDecimal.ZERO:value);
    }
    public static JLabel text(String value,int size, boolean bold,Color color) {
        JLabel l=new JLabel(value);l.setFont(new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,size));l.setForeground(color);return l;
    }
    public static JPanel spaced(LayoutManager layout,int top,int left,int bottom,int right) {
        JPanel p=new JPanel(layout);p.setOpaque(false);p.setBorder(new EmptyBorder(top,left,bottom,right));return p;
    }
    public static JTextField field(int columns) {
        JTextField f=new JTextField(columns);f.setFont(FONT);f.setForeground(INK);f.setCaretColor(BLUE);
        f.setBackground(new Color(254,253,249));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(177,183,183),1,true),new EmptyBorder(10,12,10,12)));
        return f;
    }
    public static JPasswordField password(int columns) {
        JPasswordField f=new JPasswordField(columns);f.setFont(FONT);f.setBackground(new Color(254,253,249));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(177,183,183),1,true),new EmptyBorder(10,12,10,12)));
        return f;
    }
    public static JTextArea area() {
        JTextArea area=new JTextArea();area.setEditable(false);area.setBackground(new Color(252,251,245));
        area.setForeground(INK);area.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));
        area.setBorder(new EmptyBorder(16,16,16,16));return area;
    }
    public static JButton button(String label) {return new BevelButton(label,true);}
    public static JButton secondary(String label) {return new BevelButton(label,false);}
    public static JScrollPane scroll(Component content) {
        JScrollPane scroll=new JScrollPane(content);
        scroll.setBorder(new CompoundBorder(new LineBorder(new Color(178,180,175),1,true),new EmptyBorder(2,2,2,2)));
        scroll.getViewport().setBackground(new Color(250,249,245));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }
    public static JTable table(DefaultTableModel model) {
        JTable t=new JTable(model);t.setFont(FONT);t.setRowHeight(36);t.setForeground(INK);
        t.setBackground(new Color(252,251,247));t.setSelectionBackground(new Color(202,226,240));
        t.setSelectionForeground(NAVY);t.setShowVerticalLines(false);t.setGridColor(new Color(226,225,218));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setPreferredSize(new Dimension(0,38));
        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            {setHorizontalAlignment(SwingConstants.LEFT);setBorder(new EmptyBorder(0,10,0,10));setFont(new Font("Segoe UI",Font.BOLD,12));}
            @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focus,int row,int col) {
                super.getTableCellRendererComponent(table,value,selected,focus,row,col);
                setBackground(new Color(224,227,224));setForeground(INK);return this;
            }
        });
        DefaultTableCellRenderer cell=new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl,Object value,boolean selected,boolean focus,int row,int col) {
                super.getTableCellRendererComponent(tbl,value,selected,focus,row,col);
                if(!selected)setBackground(row%2==0?new Color(252,251,247):new Color(243,244,239));
                setBorder(new EmptyBorder(1,10,1,10));return this;
            }
        };
        t.setDefaultRenderer(Object.class,cell);return t;
    }
    public static DefaultTableModel model(String... names) {
        return new DefaultTableModel(names,0) { @Override public boolean isCellEditable(int row,int col) {return false;} };
    }
    public static Surface card() {return new Surface(false);}
    public static Surface darkCard() {return new Surface(true);}
    public static void error(Component parent,Exception ex) {
        String message=ex.getMessage();
        if(ex instanceof java.sql.SQLIntegrityConstraintViolationException)message="SKU sudah ada / data melanggar aturan unik.\n"+message;
        JOptionPane.showMessageDialog(parent,message==null?ex.toString():message,"JOSJISMART — Perhatian",JOptionPane.ERROR_MESSAGE);
    }
    public static void info(Component parent,String message) {JOptionPane.showMessageDialog(parent,message,"JOSJISMART",JOptionPane.INFORMATION_MESSAGE);}
    public static class Surface extends JPanel {
        private final boolean dark;
        public Surface(boolean dark) { this.dark=dark;setOpaque(false);setBorder(new EmptyBorder(22,22,22,22)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight();
            x.setColor(new Color(25,30,35,35));x.fillRoundRect(4,6,w-8,h-9,25,25);
            Color top=dark?new Color(50,85,110):new Color(255,254,250);
            Color bottom=dark?new Color(21,45,68):new Color(224,225,216);
            x.setPaint(new GradientPaint(2,0,top,2,h,bottom));x.fillRoundRect(2,2,w-7,h-9,22,22);
            x.setColor(dark?new Color(139,181,205,105):new Color(255,255,255,210));x.setStroke(new BasicStroke(1.8f));
            x.drawRoundRect(3,3,w-10,h-12,21,21);
            x.setColor(dark?new Color(4,23,41,150):new Color(111,112,108,65));x.setStroke(new BasicStroke(1f));
            x.drawRoundRect(5,5,w-14,h-16,19,19);
            x.dispose();super.paintComponent(g);
        }
    }
    public static class BevelButton extends JButton {
        private final boolean primary;
        public BevelButton(String title,boolean primary) {
            super(title);this.primary=primary;
            setFont(new Font("Segoe UI",Font.BOLD,13));setForeground(primary?Color.WHITE:INK);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);setContentAreaFilled(false);setOpaque(false);setBorderPainted(false);
            setMargin(new Insets(8,14,8,14));setPreferredSize(new Dimension(Math.max(105,title.length()*9+24),40));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight();
            x.setColor(new Color(0,0,0,38));x.fillRoundRect(2,4,w-4,h-5,14,14);
            boolean over=getModel().isRollover(),down=getModel().isPressed();
            Color a=primary?new Color(90,156,196):new Color(250,250,245);
            Color b=primary?new Color(29,83,128):new Color(190,198,198);
            if(over){a=a.brighter();b=b.brighter();} if(down){Color tmp=a;a=b;b=tmp;}
            x.setPaint(new GradientPaint(0,1,a,0,h,b));x.fillRoundRect(1,1,w-3,h-5,13,13);
            x.setColor(primary?new Color(192,227,245,175):new Color(255,255,255,205));
            x.drawRoundRect(2,2,w-5,h-8,12,12);
            x.setColor(primary?new Color(13,51,84,140):new Color(96,105,108,120));
            x.drawRoundRect(1,1,w-3,h-5,13,13);
            x.dispose();super.paintComponent(g);
        }
    }
    public static class WorkshopBackground extends JPanel {
        public WorkshopBackground(LayoutManager layout) {super(layout);setOpaque(false);}
        @Override protected void paintComponent(Graphics g) {
            Graphics2D x=(Graphics2D)g.create();
            x.setPaint(new GradientPaint(0,0,new Color(246,244,234),getWidth(),getHeight(),new Color(212,219,217)));
            x.fillRect(0,0,getWidth(),getHeight());
            x.setColor(new Color(91,116,122,12));
            for(int i=0;i<getHeight();i+=5)x.drawLine(0,i,getWidth(),i);
            x.dispose();super.paintComponent(g);
        }
    }
    public static JPanel heading(String title,String detail) {
        JPanel p=new JPanel(new GridLayout(2,1,0,4));p.setOpaque(false);
        p.add(text(title,25,true,NAVY));p.add(text(detail,13,false,MUTED));return p;
    }
    public static JPanel wrapRow(Component... children) {
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,9,3));p.setOpaque(false);
        for(Component c:children)p.add(c);return p;
    }
    public static JPanel labeled(String label,JComponent input) {
        JPanel p=new JPanel(new BorderLayout(0,6));p.setOpaque(false);
        p.add(text(label,12,true,INK),BorderLayout.NORTH);p.add(input,BorderLayout.CENTER);return p;
    }
}
