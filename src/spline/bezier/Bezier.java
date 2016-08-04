package spline.bezier;

import java.util.ArrayList;
import java.util.List;

import pair.Pair;
import spline.mvc.Model;

public class Bezier extends Model {

    private float            ERROR        = -1e6f;

    private int              spanIdGen    = 0;
    private List<Span>       spans        = new ArrayList<Span>();
    private List<Pair>       ctrlPts      = new ArrayList<Pair>();
    private int              knotCount;
    private int              spanCount;
    private final static int PTS_PER_SPAN = 4;

    private int              nextX;
    private int              nextY;

    public Bezier() {
        throw new UnsupportedOperationException();
    }

    public List<Span> getSpans() {
        return this.spans;
    }

    public void setSpans(List<Span> spans) {
        this.spans = spans;
    }

    public Bezier(List<Pair> ctrlPts) {
        throw new UnsupportedOperationException();
    }

    public void init(List<Pair> ctrlPts, int knotCount) {
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
        int ptCount = (PTS_PER_SPAN - 1) * knotCount + 1;
        for (int i = 0; i < ptCount; i++) {
            ctrlPts.add(new Pair());
        }
    }

    public int getKnotCount() {
        return knotCount;
    }

    public void setNextX(float x) {
        ctrlPts.get(nextX).x(x);
        nextX++;
        initSpans();
    }

    public void setNextY(float y) {
        ctrlPts.get(nextY).y(y);
        nextY++;
        initSpans();
    }

    private void initSpans() {

        // Don't initialize the spans till all the control points have been set
        if (nextX != ctrlPts.size() && nextY != ctrlPts.size())
            return;

        /**
         * Select starting control point for each span and create new span
         * objects
         **/

        int SPAN_PT_CT = 4; // Number of control points that define a span
        int INC = SPAN_PT_CT - 1; // Number of points to increment when defining
                                  // new span (last point of span n should ==
                                  // first point of span n+1)

        for (int i = 0; i < spanCount; i++) {

            // If this is the first span, set the previous span to null
            Span prevSpan = i == 0 ? null : spans.get(i - 1);

            // Extract the proper subset of control points for this span
            Pair[] spanPts = new Pair[SPAN_PT_CT];
            for (int j = 0; j < SPAN_PT_CT; j++) {
                spanPts[j] = ctrlPts.get(i * INC);
            }

            // Create the new span and add it to the list
            Span newSpan = new Span(spanPts, prevSpan);
            spans.add(newSpan);
        }
    }

    float positionAtX(float x) {
        Span thisSpan = spanContainingX(x);
        if (thisSpan != null)
            return thisSpan.positionAtX(x);
        else
            return ERROR;
    }

    float velocityAtX(float x) {
        Span thisSpan = spanContainingX(x);
        if (thisSpan != null)
            return thisSpan.velocityAtX(x);
        else
            return ERROR;
    }

    float accelAtX(float x) {
        Span thisSpan = spanContainingX(x);
        if (thisSpan != null)
            return thisSpan.accelAtX(x);
        else
            return ERROR;
    }

    Span spanContainingX(float x) {
        for (int i = 0; i < spans.size(); i++) {
            if (spans.get(i).containsX(x)) {
                return spans.get(i);
            }
        }
        return null;
    }

    float startX() {
        if (spans.size() != 0)
            return spans.get(spans.size() - 1).startX();
        else
            return 0f;
    }

    float stopX() {
        if (spans.size() != 0)
            return spans.get(spans.size() - 1).stopX();
        else
            return 0f;
    }

    float startY() {
        if (spans.size() != 0)
            return spans.get(spans.size() - 1).startY();
        else
            return 0f;
    }

    float stopY() {
        if (spans.size() != 0)
            return spans.get(spans.size() - 1).stopY();
        else
            return 0f;
    }

    Span getSpan(int which) {
        return spans.get(which);
    }

    private class Span {
        private final static int MAX_CTRL_PTS = 4;

        private int              id;
        private Span             nextSpan;
        private Span             prevSpan;
        private Pair             coeffA;
        private Pair             coeffB;
        private Pair             coeffC;
        private Pair             coeffD;
        private int              recursionIndex;
        private Pair[]           ctrlPts      = new Pair[4];

        /**
         * Default constructor
         */
        public Span() {
            id = spanIdGen++;
        }

        /**
         * 
         * @param ctrlPts
         *            An array of size 4 of Pair objects representing the
         *            control point locations
         * @param prevSpan
         *            A reference to the previous span to which this span is
         *            attached. If this is the first span in a Bezier, set
         *            prevSpan to null.
         */
        public Span(Pair[] ctrlPts, Span prevSpan) {
            id = spanIdGen++;
            recursionIndex = 0;
            this.nextSpan = null;
            this.ctrlPts = ctrlPts;
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
        }

        private void setCoeffs() {

            // A = (-pt0) + (3 * pt1) + (-3 * pt2) + pt3
            coeffA = Pair.add((ctrlPts[0].negate()), (Pair.mult(ctrlPts[1], 3)));
            coeffA = Pair.add(coeffA, Pair.mult(ctrlPts[2], -3));
            coeffA = Pair.add(coeffA, ctrlPts[3]);

            // B = (3 * pt0) + (-6 * pt1) + (3 * pt2)
            coeffB = Pair.add(Pair.mult(ctrlPts[0], 3), Pair.mult(ctrlPts[1], -6));
            coeffB = Pair.add(coeffB, Pair.mult(ctrlPts[2], 3));

            // C = (-3 * pt0) + (3 * pt1)
            coeffC = Pair.add(Pair.mult(ctrlPts[0], -3), Pair.mult(ctrlPts[1], 3));

            // D = pt0
            coeffD = ctrlPts[0];
        }

        public float solveCubic(float val, boolean isX) {
            return solveCubic(val, isX, 0);
        }

        public float solveCubic(float t, boolean isX, float offset) {
            float ret;
            int which = isX ? 0 : 1;

            // P(t) = At^3 + Bt^2 + Ct + D
            // Calculate via Horner's rule
            float term = t;
            ret = coeffD.getVal(which) - offset;
            ret += term * coeffC.getVal(which);
            term *= t;
            ret += term * coeffB.getVal(which);
            term *= t;
            ret += term * coeffA.getVal(which);
            return ret;

        }

        public float solveCubicPrime(float t, boolean isX) {
            float ret;
            int which = isX ? 0 : 1;

            // P'(t) = 3At^2 + 2Bt + C
            // Calculate via Horner's rule
            float term = t;
            ret = coeffC.getVal(which);
            ret += term * coeffB.getVal(which) * 2;
            term *= t;
            ret += term * coeffA.getVal(which) * 3;
            return ret;
        }

        public float solveCubicDoublePrime(float t, boolean isX) {
            float ret;
            int which = isX ? 0 : 1;

            // P'(t) = 6At + 2B
            float term = t;
            ret = 6 * coeffA.getVal(which) * term + 2 * coeffB.getVal(which);
            return ret;
        }

        public float tOfX(float x, float guess, int recursionLimit) {
            if (x == 0)
                return 0f;

            float convergenceThreshold = 1.5e-5f;

            // Find f(guess) and f'(guess)
            float fOfGuess = solveCubic(guess, true, x);
            float fPrimeOfGuess = solveCubicPrime(guess, true);
            // Newton's method
            float newGuess = guess - (fOfGuess / fPrimeOfGuess);
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

        public boolean containsX(float x) {
            if (x >= startX() && x <= stopX())
                return true;
            else
                return false;
        }

        public boolean containsY(float y) {
            if (startY() <= y && y <= stopY())
                return true;
            else
                return false;
        }

        public float startX() {
            return ctrlPts[0].x();
        }

        public float stopX() {
            return ctrlPts[MAX_CTRL_PTS - 1].x();
        }

        public float rangeX() {
            return stopX() - startX();
        }

        public float startY() {
            return ctrlPts[0].y();
        }

        public float stopY() {
            return ctrlPts[MAX_CTRL_PTS - 1].y();
        }

        public float rangeY() {
            return stopY() - startY();
        }

        float positionAtX(float x) {
            float t = tOfX(x, 0.5f, 15);
            return solveCubic(t, false);
        };

        float velocityAtX(float x) {
            float t = tOfX(x, 0.5f, 15);
            return solveCubicPrime(t, false);
        };

        float accelAtX(float p_x) {
            float t = tOfX(p_x, 0.5f, 15);
            return solveCubicDoublePrime(t, false);
        };

    }

}
