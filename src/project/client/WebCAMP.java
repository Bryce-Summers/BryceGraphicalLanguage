package project.client;


/*
 * Dear Bryce, you need to add source module lines to the xml files in order
 * to get the gwt to automatically translate them for you.
 * 
 * 
 * This class provides all of the code that interfaces with the Google Windowing Toolkit.
 * 
 * 
 * TODO:
 * 
 * Finish the Graph Visualizer.
 * 
 * Implement a N-body simulation to keep the nodes and edges apart from each other.
 * <!> Use the context2d.scale() functions and translation functions to always keep the graph in view.
 * 
 * Implement interactivity by allowing the use to click and drag the various nodes.
 * 
 * Implement GRAPH (ical) programming language for constructing and manipulating graphs.
 * 
 * Use this information visualization to demonstrate how data structures work.
 * 
 */

import java.util.ArrayList;

import project.client.CodeStructures.Processor;
import project.client.GraphStructures.Edge;
import project.client.GraphStructures.Node;
import project.client.Graphics.Color;
import project.client.interfaces.Body;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
 
public class WebCAMP implements EntryPoint, MouseWheelHandler, MouseDownHandler,
			MouseMoveHandler, MouseUpHandler, KeyDownHandler, KeyUpHandler, KeyPressHandler
{
     
    Canvas canvas;
    Context2d context;
    static final int canvasWidth  = 1200;
    static final int canvasHeight = 800;
    static final String divTagId = "WebCAMP"; // must match div tag in html file

    // Graph Visualization code.
    public ArrayList<Node> vertices = new ArrayList<Node>();
    public ArrayList<Edge> edges    = new ArrayList<Edge>();
    
    Processor processor;
    int code_example_num = 0;
        
    public void onModuleLoad() {
         
        canvas = Canvas.createIfSupported();
         
        if (canvas == null) {
              RootPanel.get().add(new Label("Sorry, your browser doesn't support the HTML5 Canvas element"));
              return;
        }
         
        canvas.setStyleName("mainCanvas");     // *** must match the div tag in CanvasExample.html ***
        canvas.setWidth(canvasWidth + "px");
        canvas.setCoordinateSpaceWidth(canvasWidth);
         
        canvas.setHeight(canvasHeight + "px");      
        canvas.setCoordinateSpaceHeight(canvasHeight);
         
        RootPanel.get( divTagId ).add(canvas);
        context = canvas.getContext2d();
         
        final Timer timer = new Timer() {           
            @Override
            public void run() {
                draw();
            }
        };
        timer.scheduleRepeating(100);
        
        final Timer timer2 = new Timer() {           
            @Override
            public void run() {
                update();
            }
        };
        timer2.scheduleRepeating(100);
        
               
        BuildCode();
        
        
        // -- Add Mouse functionality.
        canvas.addMouseWheelHandler(this);
        canvas.addMouseMoveHandler(this);
        canvas.addMouseDownHandler(this);
        canvas.addMouseUpHandler(this);
        
        canvas.addKeyDownHandler(this);
        canvas.addKeyUpHandler(this);
        canvas.addKeyPressHandler(this);
        
    }
    
    // Builds the code and starts up the processor.
    public void BuildCode()
    {
    	ArrayList<String> code = new ArrayList<String>();
    	
    	// Try to Load a file. FIXME : This is no where near done.
    	try {
			new RequestBuilder(RequestBuilder.GET, "code.txt").sendRequest("", new RequestCallback() {
				  
				public void onResponseReceived(Request req, Response resp) {
				    String text = resp.getText();
				    // do stuff with the text
				  }

				    public void onError(Request res, Throwable throwable) {
				    // handle errors
				  }

			});
		} catch (RequestException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	    	
    	if(code_example_num == 6)
       	{
    		code.add("// Graph Building.");
    		code.add("A -> B");
    		code.add("B <- C");
    		code.add("C <-> D");
    		code.add("A <- E");
       	}
    	
    	if(code_example_num == 5)
       	{
    	code.add("//Operations.");	
    	code.add("A = (1+2)");
    	code.add("A = (5|0)");
    	code.add("A = 1 ==6 ");
    	code.add("A = 1 + 1");
    	code.add("A = 5 % 2");
    	code.add("A = 2 << 5");
    	code.add("A = 8 >> 2");
    	code.add("A = 1 + 1 + 1");
    	code.add("A = 2*3 + 1*1");
    	code.add("A = (5 | 0) & (1|6)");
    	code.add("A = (5 | 0) & 1 ==6");
    	code.add("A = (5 | 0) & (1|6) & 1 ==6 ");
    	code.add("A = (((1) + 3)) + 3");
       	}
    	
    	

    	
    	if(code_example_num == 0)
    	{
    	
	    	code.add("def.fib(n)");
	    	code.add("  if(n <= 1)");
	    	code.add("    return = n");
	    	code.add("    return");
	    	code.add("  end");
	    	code.add("");
	    	code.add("  var.a = fib(n - 1)");
	    	code.add("  var.b = fib(n - 2)");
	    	code.add("  return = a + b");
	    	code.add("  return");
	    	code.add("end");
	    	code.add("");
	    	code.add("// End of function");
	    	code.add("result = fib(4)");
    	
    	}
    	
   	
    	// Scope and Shadowing Example.

    	if(code_example_num == 1)
    	{
        	code.add("// Shadowing");
	    	code.add("VAR = \"GLOBAL\"");
	    	code.add("if(1)");
	    	code.add("  var.VAR = \"CLASS\"");
	    	code.add("  for(i = 0; i < 5; i++)");
	    	code.add("      var. VAR[i] = \"LOCAL\"");
	    	code.add("  end");
	    	code.add("end");
    	}
    	    	
    	    	
    	if(code_example_num == 2)
    	{
    		
    	code.add("// Conditionals");
    	code.add("// and Loops.");
    	code.add("A = 0");
    	code.add("if(A ==0)");
    	code.add("  B = 2");
    	code.add("end");
    	
    	code.add("if(A != 0)");
    	code.add("end");
    	
    	
    	code.add("while(A <= 1)");
    	code.add("   A++");
    	code.add("end");
    	
    	
    	code.add("A = 0");
    	code.add("for(i=0;i < 2; i++)");
    	code.add("for(i2=0;i2 < 2; i2++)");
    	code.add("   A++");
    	code.add("   end");
    	code.add("end");
    	}

    	if(code_example_num == 3)
       	{
       		code.add("// Recursive Counting");
	    	code.add("def.foo(n)");
	    	code.add("  if(n == 0)");
	    	code.add("    return = 0");
	    	code.add("    return");
	    	code.add("  end");
	    	code.add("  result = foo(n - 1) + 1");
	    	code.add("  return = result");
	    	code.add("  return");
	    	code.add("end");
	    	code.add("");
	    	code.add("result = foo(5)");
       	}
    	
    	
       	if(code_example_num == 4)
       	{
       		code.add("//Linked List");
       		code.add("head = head");
       		code.add("size = 0");
       		code.add("");
       		code.add("def.push(data)");
       		code.add("  node.data = data");
       		code.add("  node.next = head");
       		code.add("  head = &node");
       		code.add("  return = 0");
       		code.add("  return");
       		code.add("end");
       		code.add("");
       		code.add("def.pop()");
       		code.add("  return = head.data");
       		code.add("  head = &head.next");
       		code.add("  return = 0");
       		code.add("  return");
       		code.add("end");
       		code.add("");
       		code.add("push(0)");
       		code.add("push(1)");
       		code.add("push(2)");
       		code.add("");
       		code.add("A = pop()");
       		code.add("B = pop()");
       		code.add("C = pop()");
       	}
    	
    	
    	code_example_num = (code_example_num + 1) % 7;
    	
    	processor = new Processor(code);
    	
    }
    

    public void update()
    {
       processor.update();
    }
    
    public void draw()
    {
    	// White Background.
    	context.setFillStyle(Color.RGB(255,  255,  255));
        context.fillRect( 0, 0, canvasWidth, canvasHeight);
        
        processor.draw(context);
                
    }
    
    
    // -- Mouse Interaction Code.

    public static Body clicked_on;
    
	public void onMouseUp(MouseUpEvent event)
	{
		clicked_on = null;
	}

	public void onMouseMove(MouseMoveEvent event)
	{
		double mouseX = (event.getRelativeX(canvas.getElement()));
		double mouseY = (event.getRelativeY(canvas.getElement()));
		
		if(clicked_on != null)
		{
			clicked_on.x = mouseX;
			clicked_on.y = mouseY;
		}
		
	}

	public void onMouseDown(MouseDownEvent event)
	{

		double mouseX = (event.getRelativeX(canvas.getElement()));
		double mouseY = (event.getRelativeY(canvas.getElement()));
		
		for(Body b : processor.graph.current_vertices)
		{
			if(b.containsPoint(mouseX, mouseY))
			{
				clicked_on = b;
			}
		}
		
	}

	public void onMouseWheel(MouseWheelEvent event)
	{
		// TODO Auto-generated method stub
		
	}

	/* Key Presses Handlers. */
	
	public void onKeyPress(KeyPressEvent event)
	{
		// TODO Auto-generated method stub
		
	}

	public void onKeyUp(KeyUpEvent event)
	{
		if(event.getNativeKeyCode() == ' ')
		{
			BuildCode();
		}
	}

	// Communicates Keyboard Input to the Processor class.
	public void onKeyDown(KeyDownEvent event)
	{
		// TODO Auto-generated method stub
		if(event.isLeftArrow())
		{
			processor.prev();
		}

		if(event.isRightArrow())
		{
			processor.next();
		}
		
		if(event.isUpArrow())
		{
			processor.viewUp();
		}

		if(event.isDownArrow())
		{
			processor.viewDown();
		}

		
	}
    
}
