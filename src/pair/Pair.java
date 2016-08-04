package pair;

public class Pair {
    private float x;
    private float y;

    public Pair() {

    }

    public Pair(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float x() {
        return x;
    }

    public void x(float x) {
        this.x = x;
    }

    public float y() {
        return y;
    }

    public void y(float y) {
        this.y = y;
    }

    public float getVal(int which) {
        if (which == 0) {
            return this.x;
        }
        else if (which == 1) {
            return this.y;
        }
        else {
            throw new IndexOutOfBoundsException();
        }
    }

    public void setVal(float val, int which) {
        if (which == 0) {
            this.x = val;
        }
        else if (which == 1) {
            this.y = val;
        }
        else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Adds two Pairs
     * 
     * @param a
     *            First Pair to be added
     * @param b
     *            Second Pair to be added
     * @return New Pair where x = a.x + b.x and y = a.y + b.y
     */
    public static Pair add(Pair a, Pair b) {
        return new Pair((a.x() + b.x()), (a.y() + b.y()));
    }

    public static Pair add(Pair a, float val) {
        return new Pair(a.x() + val, a.y() + val);
    }

    /**
     * Subtracts Pair b from Pair a
     * 
     * @param a
     *            Pair from which second pair will be subtracted
     * @param b
     *            Pair that will be subtracted from first Pair
     * @return New Pair where x = a.x - b.x and y = a.y - b.y
     */
    public static Pair sub(Pair a, Pair b) {
        return new Pair((a.x() - b.x()), (a.y() - b.y()));
    }

    public static Pair sub(Pair a, float val) {
        return new Pair(a.x() - val, a.y() - val);
    }

    /**
     * Multiplies two Pairs
     * 
     * @param a
     *            First Pair to be multiplied
     * @param b
     *            Second Pair to be multiplied
     * @return New Pair where x = a.x * b.x and y = a.y * b.y
     */
    public static Pair mult(Pair a, Pair b) {
        return new Pair((a.x() * b.x()), (a.y() * b.y()));
    }

    public static Pair mult(Pair a, float val) {
        return new Pair(a.x() * val, a.y() * val);
    }

    /**
     * Divides first Pair by second Pair
     * 
     * @param dividend
     *            Pair to be divided
     * @param divisor
     *            Pair that will be divided by
     * @return New Pair where x = divisor.x + dividend.x and y = divisor.y +
     *         dividend.y
     */
    public static Pair divide(Pair dividend, Pair divisor) {
        return new Pair((dividend.x() / divisor.x()), (dividend.y() / divisor.y()));
    }

    public static Pair divide(Pair a, float val) {
        return new Pair(a.x() / val, a.y() / val);
    }

    public Pair negate() {
        return new Pair(-this.x(), -this.y());
    }

}
