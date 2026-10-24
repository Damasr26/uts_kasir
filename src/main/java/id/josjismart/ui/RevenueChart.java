package id.josjismart.ui;

import id.josjismart.repo.SaleRepository.SeriesPoint;
import java.awt.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.*;

public final class RevenueChart extends JPanel {
    private java.util.List<SeriesPoint> values=java.util.List.of();
    public RevenueChart() {setOpaque(false);setPreferredSize(new Dimension(360,195));}
    public void setValues(java.util.List<SeriesPoint> next) {values=next==null?java.util.List.of():new ArrayList<>(next);repaint();}
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int w=getWidth(),h=getHeight(),left=16,top=20,right=w-15,bottom=h-37;
        x.setFont(new Font("Segoe UI",Font.PLAIN,10));
        x.setColor(new Color(95,111,124,32));
        for(int i=0;i<5;i++){int yy=top+(bottom-top)*i/4;x.drawLine(left,yy,right,yy);}
        if(values.isEmpty()) {
            x.setFont(Skin.FONT);x.setColor(Skin.MUTED);x.drawString("Belum ada transaksi dalam rentang ini.",left+14,h/2);x.dispose();return;
        }
        BigDecimal max=BigDecimal.ONE;
        for(SeriesPoint p:values)if(p.amount().compareTo(max)>0)max=p.amount();
        int count=values.size(),available=Math.max(10,right-left);
        float slice=(float)available/count;
        for(int i=0;i<count;i++){
            SeriesPoint p=values.get(i);
            int bh=(int)(p.amount().divide(max,6,java.math.RoundingMode.HALF_UP).doubleValue()*(bottom-top));
            int bx=(int)(left+i*slice+slice*.17f);
            int barW=Math.max(3,(int)(slice*.65f));
            x.setColor(new Color(12,43,66,35));x.fillRoundRect(bx+2,bottom-bh+3,barW,bh,6,6);
            x.setPaint(new GradientPaint(bx,bottom-bh,new Color(121,186,216),bx,bottom,new Color(37,95,141)));
            x.fillRoundRect(bx,bottom-bh,barW,Math.max(bh,1),6,6);
            x.setColor(new Color(250,253,255,130));x.drawLine(bx+2,bottom-bh+2,bx+barW-3,bottom-bh+2);
            if(count<=7||i%2==0){x.setColor(Skin.MUTED);x.drawString(p.label(),bx-3,bottom+17);}
        }
        x.dispose();
    }
}
