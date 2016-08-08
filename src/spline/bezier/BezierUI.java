package spline.bezier;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import math.geom2d.Point2D;

public class BezierUI extends AbstractBezierUI {

    BezierUI() {
        init();
    }

    BezierUI(Bezier model) {
        model.setView(this);
        init();
    }

    private void init() {
        this.setOpaque(true);

        // Set initial graph extents
        setMinX(-10);
        setMaxX(110);
        setMinY(-10);
        setMaxY(110);
        selectedPt = -1;
    }

    @Override
    public void paintComponent(Graphics g) {
        // Turn on antialiasing, otherwise curves will look chunky
        Graphics2D g2 = (Graphics2D) g;

        // Draw a solid background. If this isn't done, artifacts from previous
        // repaints will remain
        g.setColor(Color.darkGray);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);

        // Don't draw if the model is not yet set or there isn't at least 1
        // control point
        if (model == null || model.getCtrlPts().size() < 1) {
            if (model == null)
                System.out.println("No model");
            else
                System.out.println("Not enough control points: " + model.getCtrlPts().size());
            return;
        }

        // Draw the control points
        for (CtrlPt p : model.getCtrlPts()) {
            int dia = pointRad * 2;
            Ellipse2D dot = new Ellipse2D.Double(p.getLocation().x() - pointRad,
                    p.getLocation().y() - pointRad, dia, dia);
            g2.draw(dot);
        }
        System.out.println("Control point count: " + model.getCtrlPts().size());

        // Draw the spans
        final int SPAN_INC = PTS_PER_SPAN - 1;
        int spanCount = (model.getCtrlPts().size() - 1) / SPAN_INC;

        System.out.println("Span count: " + spanCount);
        if (spanCount >= 1) {
            for (int i = 0; i < spanCount; i++) {
                // create new CubicCurve2D.Double
                CubicCurve2D c = new CubicCurve2D.Double();
                // draw CubicCurve2D.Double with set coordinates
                Point2D p0 = model.getCtrlPts().get(0 + SPAN_INC * i).getLocation();
                Point2D p1 = model.getCtrlPts().get(1 + SPAN_INC * i).getLocation();
                Point2D p2 = model.getCtrlPts().get(2 + SPAN_INC * i).getLocation();
                Point2D p3 = model.getCtrlPts().get(3 + SPAN_INC * i).getLocation();
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
        }

        // Draw a border around everything
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

}
