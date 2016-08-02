package spline.mvc;

public abstract class View {

	
	private Controller controller;
	
	protected View(){
		
	}
	protected View(Controller controller){
		this.controller = controller;
	}
	public void ContextInterface() {
		controller.algorithmInterface();
	}
	
	public abstract void update();
}
