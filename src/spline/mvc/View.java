package spline.mvc;

import javax.swing.JPanel;

public abstract class View extends JPanel {

    private Controller controller;
    private Model      model;

    protected View() {

    }

    protected View(Controller controller) {
        this.controller = controller;
    }

    public void ContextInterface() {
        controller.algorithmInterface();
    }

    public void registerModel(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

    public abstract void update();
}
