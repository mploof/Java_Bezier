package spline.bezier;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import math.geom2d.Point2D;
import spline.mvc.View;

public class BezierUI extends View {
    List<Point2D> splinePts = new ArrayList<Point2D>();
    List<Point2D> ctrlPts   = new ArrayList<Point2D>();
    List<Point2D> sp        = new ArrayList<Point2D>();
    int           minX, maxX, minY, maxY;
    boolean       scaled;
    int           curvePtCount;
    Bezier        model;
    final int     PT_RAD    = 5;
    int           selectedPt;

    BezierUI() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedPt < 0)
                    return;
                else {
                    Point2D thisPt = new Point2D(e.getX(), e.getY());
                    sp.set(selectedPt, thisPt);
                    repaint();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedPt = onCtrlPt(e);
                System.out.println("Selected point " + selectedPt);
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                ctrlPts.clear();
                for (Point2D p : sp) {
                    ctrlPts.add(unscalePoint(p));
                }
                model.setCtrlPts(ctrlPts);
                selectedPt = -1;
            }
        });
        scaled = false;
        curvePtCount = 200;
        minX = -10;
        maxX = 110;
        minY = -10;
        maxY = 110;
        selectedPt = -1;
    }

    private int onCtrlPt(MouseEvent e) {
        double dist;
        int index = 0;
        for (Point2D p : ctrlPts) {
            Point2D ps = scalePoint(p);
            dist = Math.sqrt(Math.pow((e.getX() - ps.x()), 2) + Math.pow((e.getY() - ps.y()), 2));
            if (dist <= PT_RAD) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
        repaint();
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
        repaint();
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
        repaint();
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
        repaint();
    }

    public int rangeX() {
        return maxX - minX;
    }

    public int rangeY() {
        return maxY - minY;
    }

    Point2D scalePoint(Point2D p) {
        double x = (p.x() - minX) / rangeX() * this.getWidth();
        double y = (1.0 - (p.y() - minY) / rangeY()) * this.getHeight();
        return new Point2D(x, y);
    }

    Point2D unscalePoint(Point2D p) {
        double x = p.x() / this.getWidth() * rangeX() + minX;
        double y = (1.0 - p.y() / this.getHeight()) * rangeY() + minY;
        return new Point2D(x, y);
    }

    @Override
    public void update() {
        model = (Bezier) super.getModel();
        if (scaled)
            splinePts = model.getScaledCurvePoints(curvePtCount);
        else {
            splinePts = model.getCurvePoints(curvePtCount);
            ctrlPts = model.getCtrlPts();
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);

        if (splinePts.size() < 2)
            return;

        // Re-scale the spline to fit the window
        List<Point2D> paintPts = new ArrayList<Point2D>();

        // If starting with scaled values, fit to the window...
        if (scaled) {
            for (Point2D p : splinePts) {
                double x = p.x() * this.getWidth();
                double y = (1.0 - p.y()) * this.getHeight();
                paintPts.add(new Point2D(x, y));
            }
        }
        // ...otherwise fit the values to the display range
        else {
            for (Point2D p : splinePts) {
                paintPts.add(scalePoint(p));
            }
        }

        // // Paint the spline points
        // Point2D p0 = paintPts.get(0);
        // Point2D p1 = paintPts.get(1);
        //
        // for (Point2D p : paintPts) {
        // System.out.println(p);
        // }
        //
        // for (int i = 1; i < splinePts.size(); i++) {
        // int x1 = (int) Math.round(p0.x());
        // int y1 = (int) Math.round(p0.y());
        // int x2 = (int) Math.round(p1.x());
        // int y2 = (int) Math.round(p1.y());
        // g2.drawLine(x1, y1, x2, y2);
        //
        // if (i < splinePts.size() - 1) {
        // p0 = p1;
        // p1 = paintPts.get(i + 1);
        // }
        // }

        // If this isn't a scaled curve, paint the control points
        if (!scaled) {

            // If not point is selected, update from the model
            if (selectedPt == -1) {
                sp = new ArrayList<Point2D>(); // The scaled control points
                for (Point2D p : ctrlPts) {
                    Point2D ps = scalePoint(p);
                    sp.add(ps);
                }
            }

            for (Point2D p : sp) {
                int dia = PT_RAD * 2;
                g2.draw(new Ellipse2D.Double(p.x() - PT_RAD, p.y() - PT_RAD, dia, dia));
            }

            int spanCount = model.getSpans().size();
            final int SPAN_INC = 3;
            for (int i = 0; i < spanCount; i++) {
                // create new CubicCurve2D.Double
                CubicCurve2D c = new CubicCurve2D.Double();
                // draw CubicCurve2D.Double with set coordinates
                double x0 = sp.get(0 + SPAN_INC * i).x();
                double y0 = sp.get(0 + SPAN_INC * i).y();

                double x1 = sp.get(1 + SPAN_INC * i).x();
                double y1 = sp.get(1 + SPAN_INC * i).y();

                double x2 = sp.get(2 + SPAN_INC * i).x();
                double y2 = sp.get(2 + SPAN_INC * i).y();

                double x3 = sp.get(3 + SPAN_INC * i).x();
                double y3 = sp.get(3 + SPAN_INC * i).y();
                c.setCurve(x0, y0, x1, y1, x2, y2, x3, y3);
                g2.draw(c);
            }
        }

        // Draw a border around everything
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

}
