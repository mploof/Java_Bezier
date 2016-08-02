package spline.bezier;

import java.util.ArrayList;
import java.util.List;

import spline.bezier.Bezier.*;
import spline.mvc.Model;

import java.util.*;
import spline.mvc.*;

public class Bezier extends Model {
	
	private static int spanIdGen = 0;
	

	private List<spline.bezier.Bezier.Span> spans = new ArrayList<Span>();

	private int knotCount;


	private final static int PTS_PER_SPAN = 4;

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

	public void init() {
		throw new UnsupportedOperationException();
	}

	public void setKnotCount(int count) {
		this.knotCount = count;
	}

	public void getKnotCount() {
		throw new UnsupportedOperationException();
	}

	private class Span {
		private int id;
		private Span nextSpan;
		private Span prevSpan;
		private Pair coeffA;
		private Pair coeffB;
		private Pair coeffC;
		private Pair coeffD;
		private final static int MAX_CTRL_PTS = 4;		

		public Span() {
			id = spanIdGen++;
		}

		public Span(List<spline.bezier.Pair> ctrlPts, spline.bezier.Bezier.Span prevSpan) {
			throw new UnsupportedOperationException();
		}
		
		public int getId() {
			return this.id;
		}

		public spline.bezier.Bezier.Span getNextSpan() {
			return this.nextSpan;
		}

		public void setNextSpan(spline.bezier.Bezier.Span nextSpan) {
			this.nextSpan = nextSpan;
		}

		public spline.bezier.Bezier.Span getPrevSpan() {
			return this.prevSpan;
		}

		public void setPrevSpan(spline.bezier.Bezier.Span prevSpan) {
			this.prevSpan = prevSpan;
		}

		public float solveCubic(float t, boolean isX, float offset) {
			throw new UnsupportedOperationException();
		}

		public float solveCubicPrime(float t, boolean isX) {
			throw new UnsupportedOperationException();
		}

		public float solveCubicDoublePrime(float t, boolean isX) {
			throw new UnsupportedOperationException();
		}

		public float tOfX(float x, float guess, int recursionLimit) {
			throw new UnsupportedOperationException();
		}




	}

}
