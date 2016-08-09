package spline.bezier;

import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;

import math.geom2d.Point2D;
import math.geom2d.Vector2D;
import spline.bezier.CtrlPt.PtTyp;

@SuppressWarnings("serial")
public abstract class AbstractBezierUI extends JPanel {

    int              minX, maxX, minY, maxY;
    int              pointRad     = 5;
    static final int PTS_PER_SPAN = 4;
    Bezier           model;
    CtrlPt           selectedPt;

    public AbstractBezierUI() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent arg0) {
                // Inform the model of the new UI size
                updateModel();
            }
        });
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
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                if (model != null)
                    model.updateVals();
                selectedPt = null;
            }
        });
    }

    /**
     * Attaches a Bezier object model to this view and updates the model with
     * information about the view.
     * 
     * @param model
     *            A Bezier object
     */
    void setModel(Bezier model) {
        this.model = model;
        model.setView(this);
    }

    /**
     * This method returns the type of point currently selected. Knots are the
     * points through which the ends of each span pass, while control points may
     * be defined as leading and trailing, indicating whether they are in front
     * of the first knot or behind the second knot. If no point is selected, an
     * invalid type enum is returned.
     * 
     * @return The type of point currently selected.
     */
    private PtTyp selectedPointType() {
        if (selectedPt == null)
            return CtrlPt.PtTyp.INVALID_TYP;
        else
            return selectedPt.getType();
    }

    /**
     * @return The radius in px of the drawn circles representing control points
     */
    public int getPointRad() {
        return pointRad;
    }

    /**
     * Sets the radius in px of the drawn circles representing control points
     * 
     * @param rad
     *            Size in px
     */
    public void setPointRad(int rad) {
        pointRad = rad;
    }

    /**
     * This method returns the integer value of a control point that the mouse
     * is currently near.
     * 
     * @param e
     *            A mouse click event
     * @return Which point the mouse has just clicked. Returns -1 if no point is
     *         clicked.
     */
    int onCtrlPt(MouseEvent e) {
        if (model != null) {
            int index = 0;
            for (CtrlPt p : model.getCtrlPts()) {
                if (p.mouseDistance(e) <= pointRad) {
                    selectedPt = p;
                    System.out.println("Selected point " + model.getCtrlPts().indexOf(selectedPt));
                    return index;
                }
                index++;
            }
        }
        selectedPt = null;
        return -1;
    }

    /**
     * Updates the positions of knots and control points as they are dragged by
     * the mouse.
     * 
     * @param e
     *            A mouse drag event
     */
    void dragPoint(MouseEvent e) {
        System.out.println("Dragging point");
        // Update knot
        if (selectedPointType() == PtTyp.KNOT) {
            updateKnot(e);
        }
        // Update control point
        else if (selectedPointType() != PtTyp.INVALID_TYP) {
            updateControlPoint(e);
        }
    }

    /**
     * Updates a knot location and its control points
     * 
     * @param e
     *            A MouseEvent
     */
    private void updateKnot(MouseEvent e) {

        // Don't update a locked point
        if (selectedPt.isPxLocked()) {
            return;
        }
        Point2D newLocation = new Point2D(e.getX(), e.getY());
        Vector2D v = new Vector2D(selectedPt.getPx(), newLocation);

        // Update the knot with the new location
        selectedPt.setLocation(newLocation);

        // If not the first knot, move the trailing control point
        if (!selectedPt.isFirstKnot()) {
            CtrlPt c0 = selectedPt.getPrevCtrlPt();
            Point2D newLoc = c0.getPx().plus(v);
            c0.setLocation(newLoc);
        }
        // If not the last knot, move the leading control point
        if (!selectedPt.isLastKnot()) {
            CtrlPt c1 = selectedPt.getNextCtrlPt();
            Point2D newLoc = c1.getPx().plus(v);
            c1.setLocation(newLoc);
        }

        // Update the view
        repaint();
    }

    /**
     * Update's a control point's location and the position of the opposite
     * control point.
     * 
     * @param e
     *            A MouseEvent
     */
    private void updateControlPoint(MouseEvent e) {

        // Update point location, so long as it's not locked
        if (selectedPt.isPxLocked())
            return;
        Point2D newLoc = new Point2D(e.getX(), e.getY());
        // Bound the new location between surrounding knots
        if (!selectedPt.isFirstKnot() && newLoc.x() < selectedPt.getPrevKnot().getPx().x())
            newLoc = new Point2D(selectedPt.getPrevKnot().getPx().x(), newLoc.y());
        if (!selectedPt.isLastKnot() && newLoc.x() > selectedPt.getNextKnot().getPx().x())
            newLoc = new Point2D(selectedPt.getNextKnot().getPx().x(), newLoc.y());
        selectedPt.setLocation(newLoc);

        // Check whether an opposing control point needs adjustment
        PtTyp type = selectedPointType();
        boolean leadAdj = false;
        boolean trailAdj = false;

        /*
         * If the point's associated knot does not require continuous velocity,
         * we're done, otherwise check to make sure it's not the first or last
         * control point
         */
        if (type == PtTyp.TRAIL_CTRL && selectedPt.getNextKnot().isContVel()) {
            leadAdj = !selectedPt.isLastTrail();
        }
        else if (type == PtTyp.LEAD_CTRL && selectedPt.getPrevKnot().isContVel()) {
            trailAdj = !selectedPt.isFirstLead();
        }

        // Adjust the opposite control point if necessary
        if (leadAdj || trailAdj) {
            Point2D c0;
            Point2D k;
            Point2D c1;
            double rho;
            double theta;

            // Is leading control point, adjust trailing control point
            CtrlPt adjPt;
            if (trailAdj) {
                c0 = selectedPt.getPrevCtrlPt().getPx();
                k = selectedPt.getPrevKnot().getPx();
                c1 = newLoc;
                rho = Point2D.distance(c0, k);
                theta = new Vector2D(c1, k).angle();
                adjPt = selectedPt.getPrevCtrlPt();
            }
            // Is trailing control point, adjust leading control point
            else {
                c0 = newLoc;
                k = selectedPt.getNextKnot().getPx();
                c1 = selectedPt.getNextCtrlPt().getPx();
                rho = Point2D.distance(c1, k);
                theta = new Vector2D(c0, k).angle();
                adjPt = selectedPt.getNextCtrlPt();
            }

            /*
             * Create a vector from knot to new opposite control
             * point location and update the proper point
             */
            Vector2D oppV = Vector2D.createPolar(rho, theta);
            System.out.println("selPt: " + model.getCtrlPts().indexOf(selectedPt) + " adjPt: " + adjPt);
            Point2D newOppPtLocation = k.plus(oppV);

            adjPt.setLocation(newOppPtLocation);
        }

        // Update the view
        repaint();
    }

    /**
     * @return The minimum graph X value
     */
    public int minX() {
        return minX;
    }

    /**
     * Sets the minimum graph X value and informs the model of the change
     * 
     * @param minX
     */
    public void setMinX(int minX) {
        this.minX = minX;
        updateModel();
    }

    /**
     * @return The maximum graph X value
     */
    public int maxX() {
        return maxX;
    }

    /**
     * Sets the maximum graph X value and informs the model of the change
     * 
     * @param maxX
     */
    public void setMaxX(int maxX) {
        this.maxX = maxX;
        updateModel();
    }

    /**
     * @return The minimum graph Y value
     */
    public int minY() {
        return minY;
    }

    /**
     * Sets the minimum graph Y value and informs the model of the change
     * 
     * @param minY
     */
    public void setMinY(int minY) {
        this.minY = minY;
        updateModel();
    }

    /**
     * @return The maximum graph Y value
     */
    public int maxY() {
        return maxY;
    }

    /**
     * Sets the maximum graph Y value and informs the model of the change
     * 
     * @param maxY
     */
    public void setMaxY(int maxY) {
        this.maxY = maxY;
        updateModel();
    }

    /**
     * @return The difference between the maximum and minimum X Values
     */
    public int rangeX() {
        return maxX - minX;
    }

    /**
     * @return The difference between the maximum and minimum Y Values
     */
    public int rangeY() {
        return maxY - minY;
    }

    /**
     * Informs the model of a change in view parameters that requires updating
     * the model's pixel locations
     */
    private void updateModel() {
        if (model != null)
            model.updatePx();
    }

    /**
     * A concrete implementation of this abstract class must implement the
     * paintComponent method in order to display the control points and / or
     * Bezier curve in some way.
     * 
     * @param g
     *            An inherited Graphics object
     */
    @Override
    abstract public void paintComponent(Graphics g);
}
