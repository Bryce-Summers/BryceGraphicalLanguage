package project.client.CodeStructures;

import java.util.ArrayList;
import java.util.Stack;

import com.google.gwt.canvas.dom.client.Context2d;

import project.client.GraphStructures.Node;
import project.client.Graphics.Color;
import project.client.Graphics.Drawing;
import project.client.Operations.StringOps;

import project.client.interfaces.OBJ;

/*
 * Visualizes and executes processing functions.
 * 
 * Written By Bryce Summers on 2/18/2015.
 * 
 * 
 * 
 * Bryce Graphical Language Virtual Machine.
 * 
 * 
 * Language Implementation and Semantics Notes.
 * 
 * My language works on a dynamic in scope variable instantiation model,
 * so queries to assign or find Nodes that relate to null nodes that do not yet exist are
 * handled by creating the nodes and pretending that they had always been there.
 * 
 * 
 * NOTES on variable initialization.
 * 
 * NAME; --> adds an edge(NAME, NAME); // This is used to ensure that the node
 * 									   // is not de initialized while running backwards.
 * NAME1 = NAME2; --> adds edge(NAME1, NAME2);
 * 
 * // FIXME : Visualize self looping edges, and add functionality for supporting the drawing of such edges. 
 * 
 */

public class Processor implements OBJ
{
	
	// -- Data fields.
	// Whitespace cleansed pure syntactic code.
	final ArrayList<String> code;
	// The original code with whitespace to draw to the screen.
	final ArrayList<String> code_original;
	int line = 0;
	
	// The index of the first line that is visible inside of the terminal.
	int view_line = 0;
	int view_size = 20;
	
	
	// -- A stack of the line numbers encountered.
	ArrayList<Integer> line_numbers = new ArrayList<Integer>();
	
	// The indices of lines that the processor should return to
	// end of functions.
	// end of loops.
	// return statements.
	ArrayList<Integer> return_line_numbers = new ArrayList<Integer>();
	
	// Stores the inputs Nodes corresponding to the current input.
	ArrayList<Node> current_input = new ArrayList<Node>();

	
	// The Graph that draws the state.
	public state_graph graph;
	
	ExecutionUnit exec;
	
	// Returned --> processor just returned from a loop or statement.
	// normal --> processor just executed the line above this one.
	private enum State{RETURNED,NORMAL, FUNCTION_CALL;}
	
	
	// Stores the line numbers that the program should return to.
	// Never pop from this stack unless your are executing a call to prev().
	Stack<Integer> return_stack = new Stack<Integer>();
	
	// Stores the current state of the processor.
	State STATE = State.NORMAL;
	Stack<State>   state_stack  = new Stack<State>();
	
	
	// The value that functions that do not set the return variable will return.
	public static final Integer DEFAULT_RETURN_VALUE = 0;
	// The name of the node the will store the return value.
	public static final String RETURN_NAME = "return";
	
	
	
	// -- Constructor.
	public Processor(ArrayList<String> code_input)
	{
		code = new ArrayList<String>();

		// -- Sanitize the instruction string.
		for(String instruction:code_input)
		{
			// Remove Spaces.
			instruction = instruction.replaceAll(" ", "");
			// Remove Tabs.
			instruction = instruction.replaceAll('\t' + "", "");
			code.add(instruction);
		}
		
		code.add("[END OF CODE FILE]");
				
		code_original = code_input;

		
		graph = new state_graph();
		
		// Add return with a value of null.
		
		Node RETURN = graph.newVariable(RETURN_NAME);
		graph.addDirectedEdge(RETURN, RETURN, RETURN);
		
		
		
		exec = new ExecutionUnit(graph);
		
		// TODO Presearch and index the locations of Types and Functions.
		
		// findTypes();
		// findFunctions();
		
		
	}

	// -- Public Interface.

	// Execute 1 step forward.
	public synchronized void next()
	{
					
		// We cannot go forward if we have executed every line of code.
		// The -1 is because we have an extra end of file token at the end of the code for degenerate forward by 1 jumps.
		if(line >= code.size() - 1)
		{
			return;
		}
		
		// Group a new set of instructions for the graph.
		// This is how we associate particular operations with a particular line of code.
		// graph.undo() undoes all of the operations that have happened since the latest call to graph.push();
		graph.push();
		
		String instruction = code.get(line);
	
		
		// Handle While Loops.
		if(isWhileLoop(instruction))
		{
			// while([expression]) --> [expression] 
			instruction = instruction.substring(6, instruction.length() - 1);
			
			// Entering the while loop for the first time.
			if(STATE == State.NORMAL)
			{
				graph.pushScope(new Scope_Type(Scope_Type.Type.WHILE_LOOP, line));
			}
			
			// Loop Guard.
			if(exec.execute_IF(instruction))
			{
				// Make sure we know where to return to later in the loop.
				return_stack.add(line);
				nextLine(line + 1, State.NORMAL);
				console("WHILE IF SUCEEDED");
			}
			else
			{
				// Loop exit.
				console("WHILE IF FAILED");
				graph.popScope();
				nextLine(getEndLine() + 1, State.NORMAL);
			}
			return;
		}
		
		// Handle For loops.
		if(isForLoop(instruction))
		{
			// for(x=1;x<2;x++) --> x=1;x<2;x++ 
			instruction = instruction.substring(4, instruction.length() - 1);
			
			//x=1;x<2;x++ --> [x=1, x<2, x++]
			String[] clauses = instruction.split(";");
			
			if(STATE == State.NORMAL)
			{
				// Initialization.
				graph.pushScope(new Scope_Type(Scope_Type.Type.FOR_LOOP, line));
				exec.execute(clauses[0]);
			}
			else if(STATE == State.RETURNED)
			{
				// Iteration instruction.
				//exec.execute(clauses[2]);
			}
			else
			{
				throw new Error("We cannot handle the current processor state: " + STATE);
			}
			
			// Loop Guard.
			if(exec.execute_IF(clauses[1]))
			{
				// Make sure we know where to return to later in the loop.
				return_stack.add(line);
				nextLine(line + 1, State.NORMAL);
				console("IF SUCEEDED");
			}
			else
			{
				// Loop exit.
				console("IF FAILED");
				graph.popScope();
				nextLine(getEndLine() + 1, State.NORMAL);
			}
			return;
		}
		
		// Handle return loop return statements.
		// FIXME : Continue should go back to the last loop, not the last if statement.
		if(instruction.equals("end") || instruction.equals("continue"))
		{
			
			int return_line = return_stack.pop();
			if(return_line == line + 1)
			{
				graph.popScope();// FIXME
			}
			
			// Loop at the new line that we have returned to.
			instruction = code.get(return_line);
			
			// Call the 3rd clause of the for loop so that the variable is
			// already in its loop guard comparison state 
			// when the user looks at the graph.
			if(isForLoop(instruction))
			{
				// Get rid of ending parentheses.
				instruction = instruction.substring(4, instruction.length() - 1);
				String[] clauses = instruction.split(";");
				exec.execute(clauses[2]);
			}
			
			nextLine(return_line, State.RETURNED);
			
			return;
		}
		
		// FIXME : Handle lines with multiple function calls.
		// FIXME : Handle possible function calls for all execution calls.
		
		if(instruction.equals(RETURN_NAME))
		{			
			/*
			// Default return value will be 0.
			if(graph.dereference(RETURN) == null)
			{
				graph.addDirectedEdge(RETURN, graph.NEW_CONSTANT_NODE(DEFAULT_RETURN_VALUE), RETURN);
			}
			*/
			
			// FIXME : Keep on returning until we have left the current function.
			
						
			// Keep popping scopes until we pop a function call.
			// Search through the return stack for 
			
			Scope_Type scope;
			
			do
			{
				scope = graph.popScope();
			}
			while(scope.type != Scope_Type.Type.FUNCTION_CALL);
			
			
			int return_line = scope.return_line;
			
			nextLine(return_line, State.RETURNED);
			
			return;
		}		
		
		// Handle IF statements.
		if(is_if_statement(instruction))
		{
			if(exec.execute_IF(instruction.substring(3, instruction.length() - 1)))
			{
				// We need to direct the end line to just proceed as normal.
				return_stack.add(getEndLine() + 1);
				
				// IF statements impose a scope.
				graph.pushScope(new Scope_Type(Scope_Type.Type.IF_STATEMENT, line));
				nextLine(line + 1, State.NORMAL);
			}
			else
			{
				nextLine(getEndLine() + 1, State.NORMAL);
			}
			
			return;
		}
		
		// Function Definition.
		if(StringOps.hasPrefix(instruction, "def."))
		{
			// Handle A function CALL.
			
			if(STATE == State.FUNCTION_CALL)
			{
				//graph.pushScope();
				
				int index_leftp = instruction.indexOf('(');				
				String[] variables = instruction.substring(index_leftp + 1, instruction.length() - 1).split(",");
				
				String[] arguments = graph.getArguments();
				
				// Match as many of the variables to arguments as we can.
				int len = Math.min(variables.length,  arguments.length);
				for(int i = 0; i < len; i++)
				{
					if(!variables[i].equals(""))
					exec.execute("var." + variables[i] + "=" + arguments[i]);
				}
				
								
				nextLine(line + 1, State.NORMAL);	
				return;
			}
			
			// -- Handle a Function definition.
			
			int index = instruction.indexOf('(');
			String function_name_str = instruction.substring(4, index);
			
			// Have the given function name now point to a node containing the line number.
			Node function_name = graph.findVariable(function_name_str);
			Node function_line_number = graph.NEW_NODE(line);
					
			graph.addDirectedEdge(function_name, function_line_number, function_name);
			
			// Skip the contents of the function for now.
			nextLine(getEndLine() + 1, State.NORMAL);
			return;
			
		}
		
		// FIXME : Handle Function Calls in all calls to exec.execute.
			
		/**********************************
		 *                                *
		 *      Normal 1 line execution.  *	
		 *                                *
		 **********************************/

		int function_line = exec.execute(instruction);
		
		if(function_line == ExecutionUnit.NO_FUNCTION_CALL)
		{
			nextLine(line + 1, State.NORMAL);
			return;
		}
		else
		{
			// -- Function Call.
			
			// Go to the line where the function is defined.
			graph.pushScope(new Scope_Type(Scope_Type.Type.FUNCTION_CALL, line));
			return_stack.add(line);
			nextLine(function_line, State.FUNCTION_CALL);
			return;
		}
		
	}
	

	// Enables printing to the internet console.
	public static native void console(String text)
	/*-{
	    console.log(text);
	}-*/;
	
	// Reverse the last step.
	public synchronized void prev()
	{
		// We cannot go back if we are at the start of the execution.
		if(line_numbers.isEmpty())
		{
			return;
		}
		
	
		// Normal Assignment undo.
		exec.undoInstruction();
		
		// Naturally, we need to first go back to the last executed line.
		prevLine();
				

	}
	
	
	public synchronized void viewDown()
	{
		view_line = Math.min(code.size() - 1, view_line + 1);
	}
	
	public synchronized void viewUp()
	{
		view_line = Math.max(0,  view_line - 1);
	}
	
	
	// Processor Visualization.
	public void draw(Context2d g)
	{
		// Draw the Graph Visualization.
		graph.draw(g);
		
		// -- Now draw the code and prossessor location state info.
		Drawing draw = new Drawing(g);
		
		draw.fontSize(24);
		
		for(int i = view_line; i < view_line + view_size && i <= code_original.size(); i++)
		{
			double y = (i - view_line)*24*2;
			
			if(i == line)
			{
				draw.color(Color.BLACK);
				draw.circle(12, y + 12, 5);
				
				draw.color(Color.RED);
			}
			else
			{
				draw.color(Color.BLACK);
			};
			
			String message;
			
			if(i < code_original.size())
			{
				message = code_original.get(i);
			}
			else
			{
				message = "[END OF FILE]";
			}
			
			draw.left_text(message, 20, y, 300);
		}
		
	}

	public void update()
	{
		graph.update();		
	}
	
	
	// -- Line State handling functions.
	
	private void nextLine(int line_new, State state_new)
	{
		line_numbers.add(line);
		line = line_new;		
		view_line = Math.max(0, line - view_size/2);
		updateViewLine();
		
		// Manage the state history.
		setState(state_new);
		
		graph.updateCurrentlyDisplayedGraph();
	}
	
	private void prevLine()
	{
		int line_new = line_numbers.remove(line_numbers.size() - 1);
		
		if(code.get(line_new).equals("end"))
		{
			return_stack.push(line);
		}
		
		line = line_new;
		
		updateViewLine();
		
		// Manage the state history.
		popState();
		
		// Reresh the graph.
		graph.updateCurrentlyDisplayedGraph();
		
	}
	
	private void updateViewLine()
	{
		view_line = Math.max(0, line - view_size/2);
	}
	
	private void setState(State state_new)
	{
		state_stack.push(STATE);
		STATE = state_new;
	}
	
	private void popState()
	{
		STATE = state_stack.pop();
	}
	
	// Returns the line number for the end statement cooresponding to the current loop or function call.
	private int getEndLine() 
	{
		int count = 1;
		
		int line_index = line;
		
		while(count > 0)
		{
			line_index++;
			
			String instr = code.get(line_index);
			if(isEndable(instr))
			{
				count++;
				continue;
			}
			
			if(instr.endsWith("end"))
			{
				count--;
				continue;
			}
		}
		
		return line_index;
	}
	
	// Returns true iff this line of code should be associated with an end statement.
	// This demarcates the entrance to scoped blocks and will be used in computations that try to 
	// have the processor skip an entire block of code.
	private boolean isEndable(String code)
	{
		return isForLoop(code) || isFunctionCall(code) || isWhileLoop(code) || is_if_statement(code);
	}
	
	private boolean isForLoop(String code)
	{
		return StringOps.hasPrefix(code, "for(");
	}
	
	private boolean isFunctionCall(String code)
	{
		return false;
	}
	
	private boolean isWhileLoop(String code)
	{
		return StringOps.hasPrefix(code, "while(");
	}
	
	private boolean is_if_statement(String instruction)
	{
		return StringOps.hasPrefix(instruction, "if(");
	}

}