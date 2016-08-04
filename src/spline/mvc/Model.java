package spline.mvc;

import java.util.ArrayList;
import java.util.List;

public abstract class Model {

    private List<spline.mvc.View> observers = new ArrayList<View>();

    public void registerObserver(View observer) {
        observers.add(observer);
        observer.registerModel(this);
    }

    public void detachObserver(View observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        for (View o : observers) {
            o.update();
        }
    }
}
