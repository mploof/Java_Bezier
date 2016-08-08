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

public abstract class AbstractBezierUI extends JPanel {
    int              minX, maxX, minY, maxY;
    int              pointRad     = 5;
    int              selectedPt;
    static final int PTS_PER_SPAN = 4;
    Bezier           model;

    static enum PtTyp {
        KNOT, LEAD_CTRL, TRAIL_CTRL, INVALID_TYP;
    }

    public AbstractBezierUI() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent arg0) {
                if (model != null) {
                    model.updatePx();
                }
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
                System.out.println("Selected point " + selectedPt);
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                if (model != null)
                    model.updateVals();
                selectedPt = -1;
            }
        });
    }

    void setModel(Bezier model) {
        this.model = model;
        model.setView(this);
    }

    private PtTyp selectedPointType() {
        int typ = selectedPt % 3;
        switch (typ) {
        case 0:
            return PtTyp.KNOT;
        case 1:
            return PtTyp.LEAD_CTRL;
        case 2:
            return PtTyp.TRAIL_CTRL;
        default:
            return PtTyp.INVALID_TYP;
        }
    }

    public int getPointRad() {
        return pointRad;
    }

    public void setPointRad(int rad) {
        pointRad = rad;
    }

    int onCtrlPt(MouseEvent e) {
        int index = 0;
        for (CtrlPt p : model.getCtrlPts()) {
            if (p.mouseDistance(e) <= pointRad) {
                selectedPt = index;
                return selectedPt;
            }
            index++;
        }
        selectedPt = -1;
        return selectedPt;
    }

    void dragPoint(MouseEvent e) {

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

        // Fetch the knot, find new location, and motion vector
        CtrlPt pt = model.getCtrlPts().get(selectedPt);
        Point2D newLocation = new Point2D(e.getX(), e.getY());
        Vector2D v = new Vector2D(pt.getLocation(), newLocation);

        // Update the knot with the new location
        pt.setLocation(newLocation);

        // If not the first knot, move the trailing control point
        if (selectedPt > 0) {
            CtrlPt c0 = model.getCtrlPts().get(selectedPt - 1);
            Point2D newLoc = c0.getLocation().plus(v);
            c0.setLocation(newLoc);
        }
        // If not the last knot, move the leading control point
        if (selectedPt != model.getCtrlPts().size() - 1) {
            CtrlPt c1 = model.getCtrlPts().get(selectedPt + 1);
            Point2D newLoc = c1.getLocation().plus(v);
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

        // Fetch the control point update it with the new location
        CtrlPt pt = model.getCtrlPts().get(selectedPt);
        Point2D newLocation = new Point2D(e.getX(), e.getY());
        pt.setLocation(newLocation);

        // Check whether an opposing control point needs adjustment
        boolean leadAdj = (selectedPointType() == PtTyp.TRAIL_CTRL
                && selectedPt != model.getCtrlPts().size() - 2) ? true : false;
        boolean trailAdj = (selectedPointType() == PtTyp.LEAD_CTRL
                && selectedPt != 1) ? true : false;

        // Adjust the opposite control point if necessary
        if (leadAdj || trailAdj) {
            Point2D c0;
            Point2D k;
            Point2D c1;
            double rho;
            double theta;
            int sign = 1;

            // Is leading control point, adjust trailing control point
            if (trailAdj) {
                c0 = model.getCtrlPts().get(selectedPt - 2).getLocation();
                k = model.getCtrlPts().get(selectedPt - 1).getLocation();
                c1 = newLocation;
                rho = Point2D.distance(c0, k);
                theta = new Vector2D(c1, k).angle();
                sign = -1;
            }
            // Is trailing control point, adjust leading control point
            else {
                c0 = newLocation;
                k = model.getCtrlPts().get(selectedPt + 1).getLocation();
                c1 = model.getCtrlPts().get(selectedPt + 2).getLocation();
                rho = Point2D.distance(c1, k);
                theta = new Vector2D(c0, k).angle();
            }

            /*
             * Create a vector from knot to new opposite control
             * point location and update the proper point
             */
            Vector2D oppV = Vector2D.createPolar(rho, theta);
            int adjPt = selectedPt + 2 * sign;
            System.out.println("selPt: " + selectedPt + " adjPt: " + adjPt);
            model.getCtrlPts().get(adjPt).setLocation(k.plus(oppV));
        }

        // Update the view
        repaint();
    }

    public int minX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
        updateModel();
    }

    public int maxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
        updateModel();
    }

    public int minY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
        updateModel();
    }

    public int maxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
        updateModel();
    }

    public int rangeX() {
        return maxX - minX;
    }

    public int rangeY() {
        return maxY - minY;
    }

    private void updateModel() {
        if (model != null)
            model.updatePx();
    }

    @Override
    abstract public void paintComponent(Graphics g);
}
