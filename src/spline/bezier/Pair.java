package spline.bezier;

public class Pair {

	public float x;
	public float y;

	public Pair(float x, float y){
		this.x = x;
		this.y = y;
	}

	public float getVal(int which) {
		throw new UnsupportedOperationException();
	}

	public Pair sum(Pair a, Pair b) {
		return new Pair((a.x + b.x), (a.y + b.y));
	}

	public Pair mult(Pair a, Pair b) {
		return new Pair((a.x * b.x), (a.y * b.y));
	}

	public Pair diff(Pair a, Pair b) {
		return new Pair((a.x - b.x), (a.y - b.y));
	}

	public Pair divide(Pair dividend, Pair divisor) {
		return new Pair((dividend.x / divisor.x), (dividend.y / divisor.y));
	}
}
