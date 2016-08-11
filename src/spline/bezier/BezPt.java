package spline.bezier;

import java.awt.event.MouseEvent;

import math.geom2d.Point2D;

public class BezPt {

    AbstractBezierUI view;
    Point2D          px;
    Point2D          val;
    boolean          pxLocked;
    boolean          contVel;
    PtTyp            type;
    BezPt            nextPt;
    BezPt            prevPt;
    int              pxBuff = 5;

    /**
     * This enum type represents the different types of control points composing
     * a cubic Bezier curve. The first and fourth points, through which the
     * spline curve passes are knots. The second point is the
     * "leading control point" and the third point is the
     * "trailing control point".
     * 
     * @author Michael
     *
     */
    static public enum PtTyp {
        KNOT, LEAD_CTRL, TRAIL_CTRL, INVALID_TYP;
    }

    BezPt(Point2D val) {
        this.val = val;
        this.view = null;
        this.type = PtTyp.INVALID_TYP;
    }

    BezPt(AbstractBezierUI view) {
        this.view = view;
        this.type = PtTyp.INVALID_TYP;
    }

    BezPt(Point2D val, AbstractBezierUI view, BezPt pt) {
        this.view = view;
        this.setVal(val);
        this.type = PtTyp.INVALID_TYP;
        this.setPrevPt(pt);
    }

    public void setView(AbstractBezierUI view) {
        this.view = view;
        updatePx();
    }

    public void setXPx(int x) {
        if (view == null || pxLocked)
            return;
        this.px = new Point2D(x, px.y());
        updateVal();
    }

    public void setYPx(int y) {
        if (view == null || pxLocked)
            return;
        this.px = new Point2D(px.x(), y);
        updateVal();
    }

    public void setContVel(boolean continuous) {
        this.contVel = continuous;
    }

    public boolean isContVel() {
        return contVel;
    }

    void setType(PtTyp type) {
        this.type = type;
    }

    PtTyp getType() {
        return type;
    }

    public Point2D getPx() {
        return px;
    }

    public Point2D setLocation(Point2D loc) {
        if (pxLocked || view == null)
            return this.px;

        // Bound control points between neighboring knots
        if (this.isCtrlPt()) {
            if (!this.isFirstKnot() && loc.x() < this.getPrevKnot().getPx().x())
                loc = new Point2D(this.getPrevKnot().getPx().x(), loc.y());
            if (!this.isLastKnot() && loc.x() > this.getNextKnot().getPx().x())
                loc = new Point2D(this.getNextKnot().getPx().x(), loc.y());
        }
        // Bound knots between neighboring knots with buffer, and also nudge any
        // control points they run into
        else if (this.isKnot()) {
            // Bound first and last knots by view edges
            if (this.isFirstKnot() && loc.x() < pxBuff)
                loc = new Point2D(pxBuff, loc.y());
            if (this.isLastKnot() && loc.x() > view.getWidth() - pxBuff)
                loc = new Point2D(view.getWidth() - pxBuff, loc.y());

            // Bound by other knots
            if (!this.isFirstKnot() && loc.x() < this.getPrevKnot().getPx().x() + pxBuff)
                loc = new Point2D(this.getPrevKnot().getPx().x() + pxBuff, loc.y());
            if (!this.isLastKnot() && loc.x() > this.getNextKnot().getPx().x() - pxBuff)
                loc = new Point2D(this.getNextKnot().getPx().x() - pxBuff, loc.y());

            // Move nearby control points if necessary
            Point2D ctrlLoc;
            BezPt ctrlPt;
            // If bumping into the previous lead control point
            if (!this.isFirstKnot() && loc.x() < this.getPrevLeadPt().getPx().x() + pxBuff) {
                ctrlPt = this.getPrevLeadPt();
                ctrlLoc = new Point2D(loc.x() - pxBuff, ctrlPt.getPx().getY());
                ctrlPt.setLocation(ctrlLoc);
            }
            // If bumping into the next trailing control point
            if (!this.isLastKnot() && loc.x() > this.getNextTrailPt().getPx().x() - pxBuff) {
                ctrlPt = this.getNextTrailPt();
                ctrlLoc = new Point2D(loc.x() + pxBuff, ctrlPt.getPx().getY());
                ctrlPt.setLocation(ctrlLoc);
            }
        }
        this.px = loc;
        return this.px;
    }

    public Point2D getVal() {
        return val;
    }

    /**
     * Sets the control point actual value. When this method is called, the
     * pixel value is updated and the observing view is updated.
     * 
     * @param val
     */
    public void setVal(Point2D val) {
        this.val = val;
        updatePx();
    }

    /**
     * Updates the control point's pixel value, based upon the currently
     * attached view, to reflect the current model value. If no view is
     * attached, the method immediately returns.
     */
    void updatePx() {
        if (view == null)
            return;
        double x = (val.x() - view.minX()) / view.rangeX() * view.getWidth();
        double y = (1.0 - (val.y() - view.minY()) / view.rangeY()) * view.getHeight();
        this.px = new Point2D(x, y);
        view.repaint();
    }

    /**
     * Updates the control point's model value to reflect the current px
     * in the attached view. If no view is attached, this method immediately
     * returns.
     */
    void updateVal() {
        if (view == null)
            return;
        double x = px.x() / view.getWidth() * view.rangeX() + view.minX();
        double y = (1.0 - px.y() / view.getHeight()) * view.rangeY() + view.minY();
        this.val = new Point2D(x, y);
    }

    /**
     * Returns whether the control point's pixel value is locked. If it is
     * locked, it may not be changed directly, but it may be updated by changing
     * the model value. This allows for changes to the model by objects other
     * than the attached view.
     * 
     * @return A boolean indicating the lock state.
     */
    boolean isPxLocked() {
        return pxLocked;
    }

    /**
     * Determines the distance of the mouse from the control point's pixel
     * px
     * 
     * @param e
     *            A mouse event
     * @return The distance from the mouse event to the control point's pixel
     *         px
     */
    int mouseDistance(MouseEvent e) {
        Point2D p = new Point2D(e.getX(), e.getY());
        return (int) Math.round(Point2D.distance(p, this.px));
    }

    // Linked list set and get methods

    public void setNextPt(BezPt pt) {
        this.nextPt = pt;
    }

    public BezPt getNextPt() {
        return nextPt;
    }

    public void setPrevPt(BezPt pt) {
        this.prevPt = pt;
        if (this.prevPt != null)
            prevPt.setNextPt(this);
    }

    public BezPt getPrevPt() {
        return prevPt;
    }

    // Linked list navigation methods

    public BezPt getNextKnot() {
        return getNextPtTyp(PtTyp.KNOT);
    }

    public BezPt getPrevKnot() {
        return getPrevPtTyp(PtTyp.KNOT);
    }

    public BezPt getNextLeadPt() {
        return getNextPtTyp(PtTyp.LEAD_CTRL);

    }

    public BezPt getPrevLeadPt() {
        return getPrevPtTyp(PtTyp.LEAD_CTRL);

    }

    public BezPt getNextTrailPt() {
        return getNextPtTyp(PtTyp.TRAIL_CTRL);
    }

    public BezPt getPrevTrailPt() {
        return getPrevPtTyp(PtTyp.TRAIL_CTRL);
    }

    public boolean isFirstLead() {
        if (this.type != PtTyp.KNOT || getPrevLeadPt() != null)
            return false;
        else
            return true;
    }

    public boolean isLastTrail() {
        if (this.type != PtTyp.TRAIL_CTRL || getNextTrailPt() != null)
            return false;
        else
            return true;
    }

    public boolean isFirstKnot() {
        if (this.type != PtTyp.KNOT || getPrevKnot() != null)
            return false;
        else
            return true;
    }

    public boolean isLastKnot() {
        if (this.type != PtTyp.KNOT || getNextKnot() != null)
            return false;
        else
            return true;
    }

    public BezPt getNextCtrlPt() {
        BezPt p = this.getNextPt();
        while (p != null) {
            if (p.getType() == PtTyp.TRAIL_CTRL || p.getType() == PtTyp.LEAD_CTRL)
                return p;
            else
                p = p.getNextPt();
        }
        return null;
    }

    public BezPt getPrevCtrlPt() {
        BezPt p = this.getPrevPt();
        while (p != null) {
            if (p.getType() == PtTyp.TRAIL_CTRL || p.getType() == PtTyp.LEAD_CTRL)
                return p;
            else
                p = p.getPrevPt();
        }
        return null;
    }

    public BezPt getNextPtTyp(PtTyp typ) {
        BezPt p = this.getNextPt();
        while (p != null) {
            if (p.getType() == typ)
                return p;
            else
                p = p.getNextPt();
        }
        return null;
    }

    public BezPt getPrevPtTyp(PtTyp typ) {
        BezPt p = this.getPrevPt();
        while (p != null) {
            if (p.getType() == typ)
                return p;
            else
                p = p.getPrevPt();
        }
        return null;
    }

    public boolean isKnot() {
        if (this.type == PtTyp.KNOT)
            return true;
        else
            return false;
    }

    public boolean isCtrlPt() {
        if (this.isLeadPt() || this.isTrailPt())
            return true;
        else
            return false;
    }

    public boolean isLeadPt() {
        if (this.type == PtTyp.LEAD_CTRL)
            return true;
        else
            return false;
    }

    public boolean isTrailPt() {
        if (this.type == PtTyp.TRAIL_CTRL)
            return true;
        else
            return false;
    }

    @Override
    public String toString() {
        return "Type: " + this.type + " xPX: " + px.x() + " yPx: " + px.y();
    }

}
