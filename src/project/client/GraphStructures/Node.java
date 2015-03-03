package project.client.GraphStructures;

import com.google.gwt.canvas.dom.client.Context2d;

import project.client.Graphics.Color;
import project.client.Graphics.Drawing;
import project.client.data_structures.MathB;
import project.client.interfaces.Body;
import project.client.interfaces.OBJ;

/*
 * Represents a Node in the Graph.
 */

public class Node extends Body implements OBJ
{
	
	public Object data;
	public int radius;
	
	public Node(double x, double y, Object val, int radius)
	{
		super(x, y);
		
		data = val;

		this.radius = radius;
	}
	
	// The Node Drawing Pipeline.
	
	
	public void draw(Context2d g)
	{
		Drawing draw = new Drawing(g);
		
	
		// Draw This node.
		drawNode(draw);
	}

	// Draws the actual node and its data to the screen.
	private void drawNode(Drawing draw)
	{
		// Draw the Circle.
		draw.color(Color.BLACK);
		draw.circle(x, y, radius - 5 - 1, false);
		draw.color(Color.WHITE);
		draw.circle(x, y, radius - 10);
						
		draw.color(Color.BLACK);
		draw.fontSize(24);
		draw.centered_text(data.toString(), x, y, radius*4/3);
	}

	@Override
	public boolean containsPoint(double mouseX, double mouseY)
	{
		return MathB.distance(x, y, mouseX, mouseY) <= radius;
	}

	public void update()
	{
		/* Do not do Anything */
	}

	
	// -- Explicit restating of the built in  hash functions and equality comparisons.
	
	@Override
	public int hashCode()
	{
		return super.hashCode();		
	}
	
	// Edges must contain the same particular Node Objects.
	@Override
	public boolean equals(Object o)
	{
		return o == this;
	}
	
	public String toString()
	{
		return "NODE : " + ((data != null) ? data.toString() : "[NULL]");
	}
}
