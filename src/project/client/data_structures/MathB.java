package project.client.data_structures;

public class MathB
{

	public static double distance_sqr(double x1, double y1, double x2, double y2)
	{
		double dx = x1 - x2;
		double dy = y1 - y2;
		
		return dx*dx + dy*dy;
	}
	
	public static double distance(double x1, double y1, double x2, double y2)
	{
		double dx = x1 - x2;
		double dy = y1 - y2;
		
		return Math.sqrt(dx*dx + dy*dy);
	}
	
}
