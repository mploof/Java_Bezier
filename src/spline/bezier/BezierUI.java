package spline.bezier;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import pair.Pair;
import spline.mvc.View;

public class BezierUI extends View {
    List<Pair> splinePts = new ArrayList<Pair>();
    int        minX, maxX, minY, maxY;
    boolean    scaled;
    int        curvePtCount;

    BezierUI() {
        scaled = false;
        curvePtCount = 200;
        minX = 0;
        maxX = 100;
        minY = 0;
        maxY = 100;
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

    @Override
    public void update() {
        Bezier model = (Bezier) super.getModel();
        if (scaled)
            splinePts = model.getScaledCurvePoints(curvePtCount);
        else
            splinePts = model.getCurvePoints(curvePtCount);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);

        if (splinePts.size() < 2)
            return;

        // Re-scale the spline to fit the window
        List<Pair> paintPts = new ArrayList<Pair>();

        // If starting with scaled values, fit to the window...
        if (scaled) {
            for (Pair p : splinePts) {
                double x = p.x() * this.getWidth();
                double y = (1.0 - p.y()) * this.getHeight();
                paintPts.add(new Pair(x, y));
            }
        }
        // ...otherwise fit the values to the display range
        else {
            for (Pair p : splinePts) {
                double x = (p.x() - minX) / rangeX() * this.getWidth();
                double y = (1.0 - (p.y() - minY) / rangeY()) * this.getHeight();
                paintPts.add(new Pair(x, y));
            }
        }

        Pair p0 = paintPts.get(0);
        Pair p1 = paintPts.get(1);

        for (Pair p : paintPts) {
            System.out.println(p);
        }

        for (int i = 1; i < splinePts.size(); i++) {
            int x1 = (int) Math.round(p0.x());
            int y1 = (int) Math.round(p0.y());
            int x2 = (int) Math.round(p1.x());
            int y2 = (int) Math.round(p1.y());
            g2.drawLine(x1, y1, x2, y2);

            if (i < splinePts.size() - 1) {
                p0 = p1;
                p1 = paintPts.get(i + 1);
            }
        }

        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

}
