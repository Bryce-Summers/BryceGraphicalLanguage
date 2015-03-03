package project.client.Physics;

import java.util.ArrayList;

import project.client.data_structures.Vector;
import project.client.interfaces.Body;

public class BodyPhysics
{
	// Simulates forces between each pair of bodies.
	public static void NBody(ArrayList<? extends Body> bodies, double attraction_coef, boolean attract, double min_dist)
	{
		int len = bodies.size();
		for(int a = 0; a < len; a++)
		for(int b = a; b < len; b++)
		{
			Body b1 = bodies.get(a);
			Body b2 = bodies.get(b);
			
			NBody2(b1, b2, attraction_coef, attract, min_dist);
		}
	}
	
	public static void NBody2(Body b1, Body b2, double attraction_coef, boolean attract, double min_dist)
	{
		Vector x1 = b1.getPosition();
		Vector x2 = b2.getPosition();
		
		Vector diff = x2.sub(x1);
		
		// Repel.
		if(!attract)
		{
			double mag = diff.mag();
			diff = diff.mult(-1*Math.max(min_dist - mag, 0));
		}
		else
		{
			// Make the attraction only bring the nodes to the minimum distance.
			double mag = diff.mag();
			if(mag != 0)
			{
				diff = diff.div(mag).mult(mag - min_dist);
			}
		}
		
		Vector force = diff.mult(attraction_coef);
		
		b1.addForce(force);
		b2.addForce(force.mult(-1));
	}
	
	// FIXME : Change to RK5.
	public static void Integrate(Body body)
	{
		Vector force = body.getForce();
		Vector acceleration = force.div(body.getMass());
		
		Vector velocity = body.getVelocity().add(acceleration);
		
		Vector position = body.getPosition().add(velocity);
		
		body.setVelocity(velocity);
		body.setPosition(position);
	}
	
	// REQUIRES : coef in [0, 1.0]
	// Ensures : Slows a body down.
	public static void Friction(Body body, double coef)
	{
		Vector velocity = body.getVelocity().mult(coef);
		body.setVelocity(velocity);
	}
	
}

