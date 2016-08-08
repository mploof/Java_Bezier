package spline.bezier;

import java.awt.event.MouseEvent;

import math.geom2d.Point2D;

public class CtrlPt {

    AbstractBezierUI view;
    Point2D          location;
    Point2D          val;

    CtrlPt(Point2D val) {
        this.val = val;
        this.view = null;
    }

    CtrlPt(AbstractBezierUI view) {
        this.view = view;
    }

    CtrlPt(Point2D val, AbstractBezierUI view) {
        this.view = view;
        this.setVal(val);
    }

    public void setView(AbstractBezierUI view) {
        this.view = view;
        updatePx();
    }

    public void setXPx(int x) {
        if (view == null)
            return;
        this.location = new Point2D(x, location.y());
        updateVal();
    }

    public void setYPx(int y) {
        if (view == null)
            return;
        this.location = new Point2D(location.x(), y);
        updateVal();
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public Point2D getVal() {
        return val;
    }

    /**
     * Sets the control point actual value. When this method is called, the
     * pixel location is updated and the observing view is updated.
     * 
     * @param val
     */
    public void setVal(Point2D val) {
        this.val = val;
        updatePx();
    }

    void updatePx() {
        if (view == null)
            return;
        double x = (val.x() - view.minX()) / view.rangeX() * view.getWidth();
        double y = (1.0 - (val.y() - view.minY()) / view.rangeY()) * view.getHeight();
        this.location = new Point2D(x, y);
        view.repaint();
    }

    void updateVal() {
        if (view == null)
            return;
        double x = location.x() / view.getWidth() * view.rangeX() + view.minX();
        double y = (1.0 - location.y() / view.getHeight()) * view.rangeY() + view.minY();
        this.val = new Point2D(x, y);
    }

    int mouseDistance(MouseEvent e) {
        Point2D p = new Point2D(e.getX(), e.getY());
        return (int) Math.round(Point2D.distance(p, this.location));
    }
}
