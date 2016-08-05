package spline.bezier;

import java.util.ArrayList;
import java.util.List;

import math.geom2d.Point2D;
import spline.mvc.Model;

public class Bezier extends Model {

    private double           ERROR        = -1e6f;

    private int              spanIdGen    = 0;
    private List<Span>       spans        = new ArrayList<Span>();
    private List<CtrlPt>     ctrlPts      = new ArrayList<CtrlPt>();
    private int              knotCount;
    private int              spanCount;
    private final static int PTS_PER_SPAN = 4;

    private int              nextX;
    private int              nextY;
    private AbstractBezierUI view;

    public Bezier() {
        this.view = null;
        this.ctrlPts = null;
    }

    public Bezier(List<Point2D> ctrlPts) {
        setCtrlPts(ctrlPts);
        this.view = null;
    }

    public Bezier(AbstractBezierUI view) {
        this.view = view;
        this.ctrlPts = null;
    }

    public Bezier(List<Point2D> ctrlPts, AbstractBezierUI view) {
        setCtrlPts(ctrlPts);
        this.view = view;
    }

    public void setView(AbstractBezierUI view) {
        this.view = view;
        for(CtrlPt p : ctrlPts){
            p.set
        }
    }

    public List<Span> getSpans() {
        return this.spans;
    }

    public void setSpans(List<Span> spans) {
        this.spans = spans;
    }

    public void setCtrlPts(List<Point2D> ctrlPtVals) {
        for (Point2D p : ctrlPtVals) {
            ctrlPts.add(new CtrlPt(p, view));
        }
        this.spanCount = (ctrlPts.size() - 1) / (PTS_PER_SPAN - 1);
        this.knotCount = spanCount + 1;
        nextX = ctrlPts.size();
        nextY = ctrlPts.size();
    }

    public void init(List<CtrlPt> ctrlPts, int knotCount) {
        this.ctrlPts = ctrlPts;
        this.knotCount = knotCount;
        this.spanCount = knotCount - 1;
    }

    public void setKnotCount(int knotCount) {
        this.knotCount = knotCount;
        this.spanCount = knotCount - 1;
        nextX = 0;
        nextY = 0;

        // Fill the point count list with empty pairs
        int ptCount = (PTS_PER_SPAN - 1) * spanCount + 1;
        for (int i = 0; i < ptCount; i++) {
            ctrlPts.add(new CtrlPt(view));
        }
    }

    public int getKnotCount() {
        return knotCount;
    }

    public void setNextX(double x) {
        CtrlPt p = ctrlPts.get(nextX);
        double y = p.getVal().y();
        p.setVal(new Point2D(x, y));
        nextX++;
        initSpans();
    }

    public void setNextY(double y) {
        CtrlPt p = ctrlPts.get(nextY);
        double x = p.getVal().x();
        p.setVal(new Point2D(x, y));
        nextY++;
        initSpans();
    }

    private void initSpans() {

        // Don't initialize the spans till all the control points have been set
        if (nextX != ctrlPts.size() || nextY != ctrlPts.size())
            return;

        /**
         * Select starting control point for each span and create new span
         * objects
         **/

        int SPAN_PT_CT = 4; // Number of control points that define a span
        int INC = SPAN_PT_CT - 1; // Number of points to increment when defining
                                  // new span (last point of span n should ==
                                  // first point of span n+1)

        // The control points might be reinitialized at some point, so clear any
        // existing spans
        spans.clear();
        for (int i = 0; i < spanCount; i++) {

            // If this is the first span, set the previous span to null
            Span prevSpan = i == 0 ? null : spans.get(i - 1);

            // Extract the proper subset of control points for this span
            CtrlPt[] spanPts = new CtrlPt[SPAN_PT_CT];
            for (int j = 0; j < SPAN_PT_CT; j++) {
                spanPts[j] = ctrlPts.get(j + i * INC);
            }

            // Create the new span and add it to the list
            Span newSpan = new Span(spanPts, prevSpan);
            spans.add(newSpan);
        }

        super.notifyObservers();
    }

    public double positionAtX(double x) {
        Span thisSpan = spanContainingX(x);
        if (thisSpan != null)
            return thisSpan.positionAtX(x);
        else
            return ERROR;
    }

    public double velocityAtX(double x) {
        Span thisSpan = spanContainingX(x);
        if (thisSpan != null)
            return thisSpan.velocityAtX(x);
        else
            return ERROR;
    }

    public double accelAtX(double x) {
        Span thisSpan = spanContainingX(x);
        if (thisSpan != null)
            return thisSpan.accelAtX(x);
        else
            return ERROR;
    }

    private Span spanContainingX(double x) {
        for (int i = 0; i < spans.size(); i++) {
            if (spans.get(i).containsX(x)) {
                return spans.get(i);
            }
        }
        return null;
    }

    double getStartX() {
        if (spans.size() != 0)
            return spans.get(spans.size() - 1).getStartX();
        else
            return 0f;
    }

    double getStopX() {
        if (spans.size() != 0)
            return spans.get(spans.size() - 1).getStopX();
        else
            return 0f;
    }

    double getRangeX() {
        return getStopX() - getStartX();
    }

    double getRangeY() {
        return getMaxY() - getMinY();
    }

    double getStartY() {
        if (spans.size() != 0)
            return spans.get(spans.size() - 1).getStartY();
        else
            return 0f;
    }

    double getStopY() {
        if (spans.size() != 0)
            return spans.get(spans.size() - 1).getStopY();
        else
            return 0f;
    }

    Span getSpan(int which) {
        return spans.get(which);
    }

    List<CtrlPt> getCtrlPts() {
        return ctrlPts;
    }

    /**
     * 
     * @param points
     *            The number of points equally spaced along the X axis to
     *            represent the curve
     * @return
     *         A list of points representing all segments of the initialized
     *         Bezier curve
     */
    public List<Point2D> getCurvePoints(int points) {
        List<Point2D> ret = new ArrayList<Point2D>();

        double increment = getStopX() / points;
        for (int i = 0; i <= points; i++) {
            double x = i * increment;
            double y = positionAtX(x);
            ret.add(new Point2D(x, y));
        }
        return ret;
    }

    public List<Point2D> getScaledCurvePoints(int points) {
        List<Point2D> pts = getCurvePoints(points);
        double minX = getStartX();
        double rangeX = getStopX() - minX;
        double minY = getMinY();
        double rangeY = getMaxY() - minY;

        int signX = minX > 0 ? -1 : 1;
        int signY = minY > 0 ? -1 : 1;

        for (int i = 0; i < pts.size(); i++) {
            Point2D p = pts.get(i);

            // Move the X start and min Y values to 0
            double x = (p.getX() + minX * signX);
            double y = (p.getY() + minY * signY);

            // Scale the values based on the spline range
            x /= rangeX;
            y /= rangeY;

            pts.set(i, new Point2D(x, y));
        }
        return pts;
    }

    private double getMinY() {
        double ret = 0;
        for (Span s : spans) {
            double val = s.getMinY();
            if (val < ret)
                ret = val;
        }
        return ret;
    }

    private double getMaxY() {
        double ret = 0;
        for (Span s : spans) {
            double val = s.getMaxY();
            if (val > ret)
                ret = val;
        }
        return ret;
    }

    private class Span {
        private final static int MAX_CTRL_PTS = 4;

        private int              id;
        private Span             nextSpan;
        private Span             prevSpan;
        private Point2D          coeffA;
        private Point2D          coeffB;
        private Point2D          coeffC;
        private Point2D          coeffD;
        private int              recursionIndex;
        private Point2D[]        ctrlPts      = new Point2D[4];
        private final static int SEARCH_COUNT = 500;

        /**
         * Default constructor
         */
        public Span() {
            id = spanIdGen++;
        }

        /**
         * 
         * @param ctrlPts
         *            An array of size 4 of Point2D objects representing the
         *            control point locations
         * @param prevSpan
         *            A reference to the previous span to which this span is
         *            attached. If this is the first span in a Bezier, set
         *            prevSpan to null.
         */
        public Span(CtrlPt[] ctrlPts, Span prevSpan) {
            id = spanIdGen++;
            recursionIndex = 0;
            this.nextSpan = null;
            for (int i = 0; i < ctrlPts.length; i++) {
                this.ctrlPts[i] = ctrlPts[i].getVal();
            }
            setPrevSpan(prevSpan);
            setCoeffs();
        }

        public int getId() {
            return this.id;
        }

        public Span getNextSpan() {
            return this.nextSpan;
        }

        public void setNextSpan(Span nextSpan) {
            this.nextSpan = nextSpan;
        }

        public Span getPrevSpan() {
            return this.prevSpan;
        }

        public void setPrevSpan(Span prevSpan) {
            this.prevSpan = prevSpan;
            if (prevSpan != null)
                prevSpan.setNextSpan(this);
        }

        private void setCoeffs() {

            // A = (-pt0) + (3 * pt1) + (-3 * pt2) + pt3
            coeffA = ctrlPts[0].scale(-1);
            coeffA = coeffA.plus(ctrlPts[1].scale(3));
            coeffA = coeffA.plus(ctrlPts[2].scale(-3));
            coeffA = coeffA.plus(ctrlPts[3]);

            // B = (3 * pt0) + (-6 * pt1) + (3 * pt2)
            coeffB = ctrlPts[0].scale(3);
            coeffB = coeffB.plus(ctrlPts[1].scale(-6));
            coeffB = coeffB.plus(ctrlPts[2].scale(3));

            // C = (-3 * pt0) + (3 * pt1)
            coeffC = ctrlPts[0].scale(-3);
            coeffC = coeffC.plus(ctrlPts[1].scale(3));

            // D = pt0
            coeffD = ctrlPts[0];
        }

        public double solveCubic(double val, boolean isX) {
            return solveCubic(val, isX, 0);
        }

        public double solveCubic(double t, boolean isX, double offset) {
            double ret;

            // P(t) = At^3 + Bt^2 + Ct + D
            // Calculate via Horner's rule
            double A = isX ? coeffD.getX() : coeffA.getY();
            double B = isX ? coeffB.getX() : coeffB.getY();
            double C = isX ? coeffC.getX() : coeffC.getY();
            double D = isX ? coeffD.getX() : coeffD.getY();

            double term = t;
            ret = D - offset;
            ret += term * C;
            term *= t;
            ret += term * B;
            term *= t;
            ret += term * A;
            return ret;

        }

        public double solveCubicPrime(double t, boolean isX) {
            double ret;

            double A = isX ? coeffD.getX() : coeffA.getY();
            double B = isX ? coeffB.getX() : coeffB.getY();
            double C = isX ? coeffC.getX() : coeffC.getY();

            // P'(t) = 3At^2 + 2Bt + C
            // Calculate via Horner's rule
            double term = t;
            ret = C;
            ret += term * B * 2;
            term *= t;
            ret += term * A * 3;
            return ret;
        }

        public double solveCubicDoublePrime(double t, boolean isX) {
            double ret;

            double A = isX ? coeffD.getX() : coeffA.getY();
            double B = isX ? coeffB.getX() : coeffB.getY();

            // P'(t) = 6At + 2B
            double term = t;
            ret = 6 * A * term + 2 * B;
            return ret;
        }

        public double tOfX(double x, double guess, int recursionLimit) {
            if (x == 0)
                return 0f;

            double convergenceThreshold = 1.5e-5f;

            // Find f(guess) and f'(guess)
            double fOfGuess = solveCubic(guess, true, x);
            double fPrimeOfGuess = solveCubicPrime(guess, true);
            // Newton's method
            double newGuess = guess - (fOfGuess / fPrimeOfGuess);
            recursionIndex++;
            // Return when close enough or exceeding recursion limit
            if (Math.abs(newGuess - guess) < convergenceThreshold || recursionIndex == recursionLimit) {
                // Be sure to reset the recursion index, otherwise the function
                // will return after too few iterations upon future calls
                recursionIndex = 0;
                return newGuess;
            }
            // Otherwise do another iteration
            else {
                return tOfX(x, newGuess, recursionLimit);
            }
        }

        public boolean containsX(double x) {
            if (x >= this.getStartX() && x <= this.getStopX())
                return true;
            else
                return false;
        }

        public boolean containsY(double y) {
            if (getStartY() <= y && y <= getStopY())
                return true;
            else
                return false;
        }

        public double getStartX() {
            return ctrlPts[0].x();
        }

        public double getStopX() {
            return ctrlPts[MAX_CTRL_PTS - 1].x();
        }

        public double getMinY() {
            double inc = getRangeX() / SEARCH_COUNT;
            double ret = 0;
            for (int i = 0; i <= SEARCH_COUNT; i++) {
                double pos = positionAtX(i * inc);
                if (pos < ret)
                    ret = pos;
            }
            return ret;
        }

        public double getMaxY() {
            double inc = getRangeX() / SEARCH_COUNT;
            double ret = 0;
            for (int i = 0; i <= SEARCH_COUNT; i++) {
                double pos = positionAtX(i * inc);
                if (pos > ret)
                    ret = pos;
            }
            return ret;
        }

        public double getRangeX() {
            return this.getStopX() - this.getStartX();
        }

        public double getStartY() {
            return ctrlPts[0].y();
        }

        public double getStopY() {
            return ctrlPts[MAX_CTRL_PTS - 1].y();
        }

        public double getRangeY() {
            return this.getStopY() - this.getStartY();
        }

        double positionAtX(double x) {
            double t = tOfX(x, 0.5f, 15);
            return solveCubic(t, false);
        };

        double velocityAtX(double x) {
            double t = tOfX(x, 0.5f, 15);
            return solveCubicPrime(t, false);
        };

        double accelAtX(double p_x) {
            double t = tOfX(p_x, 0.5f, 15);
            return solveCubicDoublePrime(t, false);
        };

    }

}
