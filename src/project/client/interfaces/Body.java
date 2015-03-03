package project.client.interfaces;

import project.client.data_structures.Vector;

public abstract class Body
{
	// -- Private Variables.
	public double x, y;
		
	Vector velocity = Vector.zero(2);
	Vector force 	= Vector.zero(2);
	
	// -- Constructor.
	public Body(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	// -- Body simulation code.
		
	public Vector getPosition()
	{
		return new Vector(x, y);
	}

	public Vector getVelocity() 
	{
		return velocity;
	}

	public void setPosition(Vector pos)
	{
		x = pos.getX();
		y = pos.getY();
	}

	public void setVelocity(Vector vel)
	{
		velocity = vel;
	}

	public void addForce(Vector input)
	{
		force = force.add(input);
	}

	public Vector getForce()
	{
		return force;
	}

	public double getMass()
	{
		return 1.0;
	}

	public void resetForce()
	{
		force = Vector.zero(2);
	}

	
	public abstract boolean containsPoint(double mouseX, double mouseY);


}
