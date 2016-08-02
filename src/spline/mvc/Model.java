package spline.mvc;

import java.util.ArrayList;
import java.util.List;
import spline.bezier.*;
import java.util.*;

public abstract class Model {

	private List<spline.mvc.View> observers = new ArrayList<View>();

	public void attach(View observer) {
		observers.add(observer);
	}

	public void detach(View observer) {
		observers.remove(observer);
	}

	public void notifyObservers() {
		for(View o : observers){
			o.update();
		}
	}
}
