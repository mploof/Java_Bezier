package spline.bezier;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import math.geom2d.Point2D;

public class BezierUI extends AbstractBezierUI {

    BezierUI(Bezier model) {
        model.setView(this);

        this.setOpaque(true);
        this.setBackground(Color.BLACK);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dragPoint(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onCtrlPt(e);
                System.out.println("Selected point " + selectedPt);
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                selectedPt = -1;
            }
        });
        setMinX(-10);
        setMaxX(110);
        setMinY(-10);
        setMaxY(110);
        selectedPt = -1;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);

        if (ctrlPts.size() < PTS_PER_SPAN)
            return;

        for (CtrlPt p : ctrlPts) {
            int dia = pointRad * 2;
            Ellipse2D dot = new Ellipse2D.Double(p.getLocation().x() - pointRad,
                    p.getLocation().y() - pointRad, dia, dia);
            g2.draw(dot);
        }

        final int SPAN_INC = PTS_PER_SPAN - 1;
        int spanCount = (ctrlPts.size() - 1) / SPAN_INC;
        for (int i = 0; i < spanCount; i++) {
            // create new CubicCurve2D.Double
            CubicCurve2D c = new CubicCurve2D.Double();
            // draw CubicCurve2D.Double with set coordinates
            Point2D p0 = ctrlPts.get(0 + SPAN_INC * i).getLocation();
            Point2D p1 = ctrlPts.get(1 + SPAN_INC * i).getLocation();
            Point2D p2 = ctrlPts.get(2 + SPAN_INC * i).getLocation();
            Point2D p3 = ctrlPts.get(3 + SPAN_INC * i).getLocation();
            c.setCurve(p0.x(), p0.y(), p1.x(), p1.y(), p2.x(), p2.y(), p3.x(), p3.y());
            Stroke s = new BasicStroke(3);
            g2.setStroke(s);
            g2.setColor(Color.RED);
            g2.draw(c);

            Line2D l = new Line2D.Double();
            g2.setColor(Color.cyan);
            l.setLine(p0.x(), p0.y(), p1.x(), p1.y());
            g2.draw(l);
            l.setLine(p3.x(), p3.y(), p2.x(), p2.y());
            g2.draw(l);
        }

        // Draw a border around everything
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

}
