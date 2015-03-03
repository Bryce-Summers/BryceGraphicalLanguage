package project.client.interfaces;

import com.google.gwt.canvas.dom.client.Context2d;

public interface OBJ
{
	// Called once per frame.
	public abstract void draw(Context2d g);
	
	// Called once per update step. (Perhaps more often than drawing.)
	public abstract void update();
}

