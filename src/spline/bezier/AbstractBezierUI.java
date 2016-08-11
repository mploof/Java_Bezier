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
import spline.bezier.BezPt.PtTyp;

@SuppressWarnings("serial")
public abstract class AbstractBezierUI extends JPanel {

    AbstractBezierUI thisUI       = this;
    int              minX, maxX, minY, maxY;
    int              pointRad     = 5;
    static final int PTS_PER_SPAN = 4;
    Bezier           model        = null;
    BezPt            selectedPt   = null;
    int              scrubberPx   = 0;

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
                dragAction(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // If no control point is selected, move the scrubber
                if (onCtrlPt(e) == -1) {
                    updateScrubber(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                if (model != null)
                    model.updateVals();
                if (selectedPt != null) {
                    int modelRangeBuff = 10;
                    if (selectedPt.getVal().y() > maxY) {
                        thisUI.setMaxY((int) (selectedPt.getVal().y() + modelRangeBuff));
                    }
                    else if (selectedPt.getVal().y() < minY) {
                        thisUI.setMinY((int) (selectedPt.getVal().y() - modelRangeBuff));
                    }
                }
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
            return BezPt.PtTyp.INVALID_TYP;
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
            for (BezPt p : model.getCtrlPts()) {
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
    void dragAction(MouseEvent e) {
        // Update knot
        if (selectedPointType() == PtTyp.KNOT) {
            updateKnot(e);
        }
        // Update control point
        else if (selectedPointType() != PtTyp.INVALID_TYP) {
            updateControlPoint(e);
        }
        // Move the scrubber
        else {
            updateScrubber(e);
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
        System.out.println("X: " + newLocation.x() + " Y: " + newLocation.y());

        // Update the knot with the new location
        selectedPt.setLocation(newLocation);

        // If not the first knot, move the trailing control point
        if (!selectedPt.isFirstKnot()) {
            BezPt c0 = selectedPt.getPrevCtrlPt();
            Point2D newLoc = c0.getPx().plus(v);
            c0.setLocation(newLoc);
        }
        // If not the last knot, move the leading control point
        if (!selectedPt.isLastKnot()) {
            BezPt c1 = selectedPt.getNextCtrlPt();
            Point2D newLoc = c1.getPx().plus(v);
            c1.setLocation(newLoc);
        }

        // Update the view
        repaint();
    }

    /**
     * Updates the scrubber location
     * 
     * @param e
     *            A MouseEvent
     */
    private void updateScrubber(MouseEvent e) {
        scrubberPx = e.getX();
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
        newLoc = selectedPt.setLocation(newLoc);
        System.out.println("X: " + newLoc.x() + " Y: " + newLoc.y());

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

        System.out.println(selectedPt);

        // Adjust the opposite control point if necessary
        if (leadAdj || trailAdj) {
            Point2D c0;
            Point2D k;
            Point2D c1;
            double rho;
            double theta;

            // Is leading control point, adjust trailing control point
            BezPt adjPt;
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
            Point2D newOppPtLocation = k.plus(oppV);

            // Set the opposite point location
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
