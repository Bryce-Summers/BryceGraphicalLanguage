package project.client.Graphics;

import project.client.data_structures.Vector;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;

/*
 * Bryce's Personal Drawing library built on top of Context2d;
 * 
 * FIXME : Make things in terms of points.
 * 
 * 
 * FIXME : Make this more performant.
 * 
 * 	We can draw only to integer coordinates and it may improve the performance.
 */

public class Drawing
{

	// The Drawing Context.
	public Context2d g;
	
	public Drawing(Context2d context)
	{
		g = context;
	}
	
	public void color(CssColor c)
	{
		g.setFillStyle(c);
		g.setStrokeStyle(c);
	}
	
	public void linesize(int s)
	{
		g.setLineWidth(s);
	}
	
	// Draws a Circle. // FIXME : Do not draw the fill. use stroke instead.
	
	public void circle(double x, double y, double radius)
	{
		circle(x, y, radius, true);
	}
	
	public void circle(double x, double y, double radius, boolean fill)
	{
		g.beginPath();
		g.arc(x, y, radius, 0, Math.PI * 2.0);
		g.closePath();
		
		if(fill)
		{
			g.fill();
		}
		else
		{
			g.stroke();
		}
	}
	
	public void fontSize(int size)
	{
		g.setFont("bold " + 24 + "px sans-serif");
	}
	
	public void centered_text(String str, double x, double y, double max_width)
	{
		g.setTextAlign(Context2d.TextAlign.CENTER);
		g.setTextBaseline(Context2d.TextBaseline.MIDDLE);
		g.fillText(str, x, y, max_width);
	}
	
	public void left_text(String str, double x, double y, double max_width)
	{
		g.setTextAlign(Context2d.TextAlign.LEFT);
		g.setTextBaseline(Context2d.TextBaseline.TOP);
		g.fillText(str, x, y, max_width);
	}
	
	// Draws a Bezier curve with the given points and tangent vectors.
	public void bezier(
			double x1,  double y1, 
			double tx1, double ty1, 
			double x2,  double y2,
			double tx2, double ty2)
	{
		g.beginPath();
		g.moveTo(x1, y1);
		g.bezierCurveTo(x1 + tx1, y1 + ty1, x2 + tx2, y2 + ty2, x2, y2);
		g.closePath();
		g.stroke();
				
	}
	
	public void line(double x1, double y1, double x2, double y2)
	{
		g.beginPath();
		g.moveTo(x1, y1);
		g.lineTo(x2, y2);
		g.closePath();
		
		// Stroke is used for line drawing.
		g.stroke();

	}

	// FIXME : Have this be the internal function.
	public void arrow(Vector v1, Vector v2)
	{
		arrow(v1.getX(), v1.getY(), v2.getX(), v2.getY());
	}
	
	public void arrow(double x1, double y1, double x2, double y2)
	{
		
		
		// A Vector pointing back to the original node.
		Vector diff = new Vector((x1 - x2), y1 - y2);
		
		diff = diff.norm();
		
		int arrow_size = 15;
		
		Vector back = diff.mult(arrow_size);
		
		Vector left  = diff.rotate2D(90).mult(arrow_size);
		Vector right = left.mult(-1);
		
		Vector p1 = new Vector(x2, y2);
		Vector line_end = p1.add(back);
		
		Vector p2 = line_end.add(left);
		Vector p3 = line_end.add(right);
				
		
		line(x1, y1, line_end.getX(), line_end.getY());
		
		g.beginPath();
		g.moveTo(x2, y2);
		g.lineTo(p2.getX(), p2.getY());
		g.lineTo(p3.getX(), p3.getY());
		g.fill();
		
	}
	
}
