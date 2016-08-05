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
    }

    public void setXPx(int x) {
        if (view == null)
            return;
        this.location = new Point2D(x, location.y());
        this.val = pxToVal(location);
    }

    public void setYPx(int y) {
        if (view == null)
            return;
        this.location = new Point2D(location.x(), y);
        this.val = pxToVal(location);
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
        this.location = valToPx(val);
        if (view != null)
            view.repaint();
    }

    Point2D valToPx(Point2D p) {
        if (view == null)
            return new Point2D(0, 0);
        double x = (p.x() - view.minX()) / view.rangeX() * view.getWidth();
        double y = (1.0 - (p.y() - view.minY()) / view.rangeY()) * view.getHeight();
        return new Point2D(x, y);
    }

    Point2D pxToVal(Point2D p) {
        if (view == null)
            return new Point2D(0, 0);
        double x = p.x() / view.getWidth() * view.rangeX() + view.minX();
        double y = (1.0 - p.y() / view.getHeight()) * view.rangeY() + view.minY();
        return new Point2D(x, y);
    }

    int mouseDistance(MouseEvent e) {
        Point2D p = new Point2D(e.getX(), e.getY());
        return (int) Math.round(Point2D.distance(p, this.location));
    }
}
