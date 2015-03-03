package project.client.GraphStructures;

import com.google.gwt.canvas.dom.client.Context2d;

import project.client.Graphics.Color;
import project.client.Graphics.Drawing;
import project.client.data_structures.Vector;
import project.client.interfaces.Body;
import project.client.interfaces.OBJ;

/*
 * Edge class for representing my graphs.
 * 
 * Written by Bryce Summers ~ 2/18/2015.
 * 
 * Every edge represents an ***directed*** edge. Therefore an edge A,B will has differently than B,A.
 * 
 * FIXME : Have edges draw directional arrows.
 * 
 */

public class Edge extends Body implements OBJ
{
	// The vertices that this edge connects.
	public Node n1;
	public Node n2;
	
	public Edge(Node n1, Node n2)
	{
		super(0, 0);
		
		this.n1 = n1;
		this.n2 = n2;
		
		if(n1 != null && n2 != null)
		{
			// Have the edge start at the linear average of the node's positions.
			x = n1.x + n2.x;
			y = n1.y + n2.y;
			x/=2;
			y/=2;
		}
	}

	public void draw(Context2d g) 
	{

		Drawing draw = new Drawing(g);
		
		// FIXME : Upgrade this to a bezier based edge,
		// where the two lines flow smoothly through the edge point.

		
		// Compute the start and end points of arrows that go from the 
		// edge of one circle to the edge of another.
		Vector p1 = new Vector(n1.x, n1.y);
		Vector p2 = new Vector(n2.x, n2.y);
		
		Vector diff = p2.sub(p1);
		Vector forward = diff.norm();
		
		p1 = p1.add(forward.mult(n1.radius));
		p2 = p2.sub(forward.mult(n2.radius));
		
		
		// --
		
		
		draw.color(Color.WHITE);
		draw.linesize(12);
		draw.arrow(p1, p2);
		
		draw.color(Color.GRAY);
		draw.linesize(9);
		draw.arrow(p1, p2);
		
		/*
		draw.arrow( n1.x, n1.y, x, y);
		draw.arrow(x, y, n2.x, n2.y);
		*/
		
		//drawKnob(draw);
	}
	
	
	// Draws this edge's control knob.
	public void drawKnob(Drawing draw)
	{
		draw.color(Color.BLACK);
		draw.circle(x, y, 5);
	}

	@Override
	// FIXME : Implement point in edge test.
	public boolean containsPoint(double mouseX, double mouseY)
	{
		return false;
	}

	public void update()
	{
		// FIXME: Have edges dynamically control their bending and drawing.		
	}
	
	// hashCode based on particular instantiated node objects, rather than the data represented therein.
	@Override
	public int hashCode()
	{
		return n1.hashCode()*7 + n2.hashCode();
		
	}
	
	// Edges must contain the same particular Node Objects.
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Edge)
		{
			Edge e = (Edge)o;
			return e.n1 == n1 && e.n2 == n2;
		}
		
		return false;
	}

	// Returns true iff the edge links a node with itself.
	public boolean isReflexive()
	{
		return n1 == n2;
	}
}
