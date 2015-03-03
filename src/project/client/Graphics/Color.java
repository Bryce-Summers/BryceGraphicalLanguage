package project.client.Graphics;

import com.google.gwt.canvas.dom.client.CssColor;

public class Color 
{
	
	public static CssColor BLACK = RGB(0, 0, 0);
	public static CssColor WHITE = RGB(255, 255, 255);
	public static CssColor GRAY  = grayscale(100);
	public static CssColor RED = RGB(255, 0, 0);

	public static CssColor grayscale(int val)
	{
		return RGBA(val, val, val, 255);
	}
	
	// Bounds are [0, 255]
	public static CssColor RGB(int red, int green, int blue)
	{
		return RGBA(red, green, blue, 255);
	}
	
	// RGB with alpha component.
	public static CssColor RGBA(int red, int green, int blue, int alpha)
	{
	    // Get a random color and alpha transparency

	    CssColor color = CssColor.make("rgba(" + red + ", "
	    									   + green + ","
	    									   + blue + ", "
	    									   + alpha + ")");
	    
	    return color;
	}
	

	
}
