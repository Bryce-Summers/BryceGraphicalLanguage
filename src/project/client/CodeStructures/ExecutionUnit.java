package project.client.CodeStructures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

//import org.apache.commons.lang3.StringUtils;

import project.client.GraphStructures.Node;
import project.client.Operations.StringOps;

import project.client.data_structures.Box;

/*
 * Execution Unit. Written by Bryce Summers on 2/22/2015.
 * 
 * Purpose: The Execution Unit class defines how given Strings of 
 * 			code are mapped to forward and backwards operations.
 * 
 * 2/23/2015 : Operator seem to be working quite well.
 * 
 * Please Note that a major assumption is that all syntactic strings will not contain any whitespace characters.
 */


public class ExecutionUnit 
{

	final state_graph graph;
	
	
	HashSet<Character> logical_operators;
	HashSet<Character> name_operators;
	
	// The assignment operators in order of precedance.
	String[] assignment_operators = new String[]{"<->", "->", "<-", "=&", "="};
	
	
	public static final int NO_FUNCTION_CALL = -1;
	public static final int FUNCTION_RETURN  = -2;
	
	public ExecutionUnit(state_graph graph)
	{
		this.graph = graph;
		
		logical_operators = new HashSet<Character>();
		logical_operators.add('+');
		logical_operators.add('-');
		logical_operators.add('*');
		logical_operators.add('/');
		logical_operators.add('%');
		logical_operators.add('&');
		logical_operators.add('^');
		logical_operators.add('|');
		logical_operators.add('(');
		logical_operators.add(')');
		logical_operators.add('!');
		logical_operators.add('=');
		logical_operators.add('<');
		logical_operators.add('>');
		
		// Used in function calls.
		logical_operators.add(',');
	
		// Syntax only found in name expressions. A.b.c[2].h["index"].x;
		name_operators = new HashSet<Character>();
		name_operators.add('.');
		name_operators.add('[');
		name_operators.add(']');
		
	}
	
	/* Executes a given assignment instruction forward or backward by modifying the given state_graph.
	 *
	 * Note: Only handles instructions, function calls, loops,
	 * and control flow should be handled by the processor class.
	 *
	 * FIXME : Handle Function Calls.
	 * FIXME : Handle For Loops.
	 * 
	 * Returns NO_FUNCTION_CALL if no function was called.
	 * Otherwise if a function was called, then it returns the line number of the calling function.
	 */
	public int execute(String instruction)
	{
		
		// Ignore Comments.
		int slash_index = instruction.indexOf("//");
		
		if(slash_index >= 0)
		{
			instruction = instruction.substring(0, slash_index);
		}
		
		if(instruction.equals(""))
		{
			return NO_FUNCTION_CALL;
		}
		
				
		Box<String> instruction_box = new Box<String>();
		instruction_box.val = instruction;
		int line = handle_functions(instruction_box);
		
		// Jumped to a given line.
		if(line >= 0)
		{
			return line;
		}
		
		handle_assignment(instruction_box.val);
		
		// Erase the return value after execution.
		if(line == FUNCTION_RETURN)
		{
			
			Node RETURN = graph.findVariable(Processor.RETURN_NAME);
			graph.addDirectedEdge(RETURN,  RETURN,  RETURN);
			
		}
		
		return NO_FUNCTION_CALL;
		
	}

	public void undoInstruction()
	{
		graph.undo();
	}
	
	// FIXME The are many behaviors that are not yet defined . 1==2;
	// This should be labeled as a no-op.
	private void handle_assignment(String instruction)
	{
		console("Instruction:  " + instruction);
		
		Box<String[]> left_box  = new Box<String[]>();
		Box<String[]> right_box = new Box<String[]>();
		Box<String> op_box = new Box<String>();
		split(instruction, left_box, right_box, op_box);
		
		String[] left  = left_box.val;
		String[] right = right_box.val;
		
		// Add edges in both directions.
		if(op_box.val.equals("<->"))
		{
			Node n1 = parse(left);
			Node n2 = parse(right);
			
			// Non singular assignments.
			graph.addDirectedEdge(n1,  n2, n2);
			graph.addDirectedEdge(n2,  n1, n1);
			return;
		}
		
		if(op_box.val.equals("->"))
		{
			Node n1 = parse(left);
			Node n2 = parse(right);
			
			// Non singular assignments.
			graph.addDirectedEdge(n1,  n2, n2);
			return;
		}
		
		if(op_box.val.equals("<-"))
		{
			Node n1 = parse(left);
			Node n2 = parse(right);
			
			// Non singular assignments.
			graph.addDirectedEdge(n2,  n1, n1);
			return;
		}
		
		// Manipulates a nodes' pointer.
		if(op_box.val.equals("=&"))
		{
			Node n1 = parse(left);
			Node n2 = parse(right);
			
			// Non singular assignments.
			graph.addDirectedEdge(n1,  n2, n1);
			return;
		}
		
		// -- Assignment Instruction: name = name
		if(op_box.val.equals("="))
		{
		
			// For any assignment we want to figure out
			// 1. The Node that will point to something.
			// 2. Whether it is pointing as a dereference or whether it 
			//    is pointing through a named subvariable.
			

			
			//-- The assignment node.
			Node n2 = parse(right);

			// Find the assignTo node.
			Node n1 = parse(left);		
			

			// FIXME : Handle NULL pointer exceptions???
			
			// Dereference the address, because that is the semantics of an assignment operation.
			n2 = graph.dereference(n2);
			
			
			// n2 is now set correctly.
			
			// forward / backwards assignment.
			if(n2 != null)
			{
				// Make n1 singularly point to n2.
				graph.addDirectedEdge(n1, n2, n1);
			}
			
			return;
		}
	}
	
	

	/*******
	 * 
	 * 
	 * Syntactic Parsing and tokenization functions.
	 * 
	 * 
	 */
	
	
	
	// Attempts to parse the given string into an Integer.
	private Integer toInteger(String input)
	{
		if (input.matches("[0-9]+") && input.length() > 0)
		{
			return Integer.parseInt(input);
		}
		
		return null;
	}
	
	
	// Tokenizes a string into NAMES and operators.
	private String[] tokenize(String input, HashSet<Character> operators)
	{
		ArrayList<String> output = new ArrayList<String>();
		
		StringBuilder current = new StringBuilder();
		
		// State machine for separating strings containing operators from strings not containing operators.
		
		
		boolean name_not_op = true;
		
		int len = input.length();
		for(int index = 0; index < len; index++)
		{
			char c = input.charAt(index);
			
			if(c == '(' || c == ')')
			{
				
				// FIXME : Replace these repeated blocks of code with a function call.
				String current_string = current.toString();
				if(current_string.length() > 0)
				{
					output.add(current_string);
					current = new StringBuilder();
				}
				output.add("" + c);
				continue;
			}
			
			if(operators.contains(c))
			{
				if(!name_not_op)
				{
					current.append(c);
				}
				else
				{
					name_not_op = false;
					String current_string = current.toString();
					if(current_string.length() > 0)
					{
						output.add(current_string);
						current = new StringBuilder();
					}
					current.append(c);
				}
			}
			else
			{
				if(name_not_op)
				{
					current.append(c);
				}
				else
				{
					name_not_op = true;
					String current_string = current.toString();
					if(current_string.length() > 0)
					{
						output.add(current_string);
						current = new StringBuilder();
					}
					
					current.append(c);
				}
			}
		}
		
		String current_string = current.toString();
		if(current_string.length() > 0)
		output.add(current_string);
		
		String[] output_array = new String[output.size()];
		
		return output.toArray(output_array);
	}
	
	// Evaluates the given input tokens.
	// REQUIRES : The input tokens should have been tokenized using the logical_operators.
	private Node parse(String... tokens)
	{
			
		ArrayList<Object> logical_tokens = new ArrayList<Object>(tokens.length);
		
		// -- First parse all names.
		for(String str : tokens)
		{
			// Operators --> Strings.
			if(logical_operators.contains(str.charAt(0)))
			{
				logical_tokens.add(str);
				continue;
			}
			
			// Names --> Nodes.
			Node node = parse_name(str);			
			logical_tokens.add(node);			
			
		}
				

		return parse_logical(logical_tokens);
		
	}
	
	
	private Node parse_logical(List<Object> tokens)
	{		
		
		parse_parentheses(tokens);
		
		
		// FIXME : Put these in a better order of precedance.
		// FIXME : Make sure that all operators are supported.

		// http://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html
		
		// FIXME : Operators of the same precedance must be evaluated at the same time from left to right.
		
		
		// Left to Right.
		
		//Postfix: expr++ expr--
		parse_operator_unary_postfix(tokens, "++", "--");
		//Unary: ++expr --expr +expr -expr ~ !
		
		parse_operator_binary_infix(tokens, "*", "/", "%");

		parse_operator_binary_infix(tokens, "+", "-");
		
		parse_operator_binary_infix(tokens, "<<", ">>", ">>>");
		
		parse_operator_binary_infix(tokens, "<", ">", "<=", ">=");// instancof goes here too!
		
		parse_operator_binary_infix(tokens, "==", "!=");

		
		parse_operator_binary_infix(tokens, "&");
		parse_operator_binary_infix(tokens, "^");
		parse_operator_binary_infix(tokens, "|");
		
		parse_operator_binary_infix(tokens, "&&");
		parse_operator_binary_infix(tokens, "||");
		
		// Trunary.
		//? :
		
		// Right to left.
		//Assignment= += -= *= /= %= &= ^= |= <<= >>= >>>=
		
		return (Node)tokens.get(0);

	
	}

	// Parses binary infix operators defined.
	private void parse_operator_binary_infix(List<Object> tokens, String... ops)
	{
		ListIterator<Object> iter = tokens.listIterator();
		
		while(iter.hasNext())
		{
			Object o = iter.next();
			
			if(!(o instanceof String))
			{				
				continue;
			}
			
			String op = null;
			for(String str : ops)
			{
				if(str.equals(o))
				{
					op = str;
					break;
				}
			}
			
			// This operation is not in the set of ops that should be parsed at this time.
			if(op == null)
			{
				continue;
			}
			
			// ASSERT(The iterator is now pointing to the element before the next NODE.
			
			// Iterator is now pointing to the first Node after the operators.
			
			iter.remove();

			Node A = (Node)iter.previous();
			iter.remove();
			Node B = (Node)iter.next();
			iter.remove();
							
			Node A_pointer = graph.dereference(A);
			Node B_pointer = graph.dereference(B);
			
			Integer a = A_pointer == null ? 0 : (Integer)A_pointer.data;
			Integer b = B_pointer == null ? 0 : (Integer)B_pointer.data;
		
			int result = compute(a, b, op);
			
			// Compute the result and box it in a node.
			
			
						
			/* 
			 * Create a temporary node that will serve as an address that points to the node.
			 * 
			 * We can't just return the boxed node, 
			 * because Nodes semantically represent variables and the function that 
			 * parses it will need a variable that
			 * points to the computed result rather than the computed result itself.
			 */
			
			Node result_node = graph.NEW_CONSTANT_NODE(result);
			
			iter.add(result_node);
			
			
		
		}
	}
	
	// Parse all unary postfix operators.
	private void parse_operator_unary_postfix(List<Object> tokens, String ... ops)
	{
		// Get an iterator to the tail of the list.
		ListIterator<Object> iter = tokens.listIterator(tokens.size() - 1);
		
		Object last = iter.next();
		
		for(String op : ops)
		{
			if(last.equals(op))
			{
				iter.remove();
				Node node = (Node)iter.previous();
				int val = (Integer)graph.dereference(node).data;
				iter.remove();
				
				if(op.equals("++"))
				{
					val++;
				}
				
				if(op.equals("--"))
				{
					val--;
				}
				
				Node result_node = graph.NEW_CONSTANT_NODE(val);
				iter.add(result_node);
				return;
			}
		}
		
	}
	
	// Performs a binary computation involving two integers and an operator.
	private int compute(int a, int b, String op)
	{
		if(op.equals("+"))
		{
			return a + b;
		}
		else if(op.equals("-"))
		{
			return a - b;
		}
		else if(op.equals("*"))
		{
			return a * b;
		}
		else if(op.equals("/"))
		{
			return a / b;
		}
		else if(op.equals("%"))
		{
			return a % b;
		}
		else if(op.equals("&"))
		{
			return a & b;
		}
		else if(op.equals("|"))
		{
			return a | b;
		}
		else if(op.equals("&&"))
		{
			return (a & b) != 0 ? 1 : 0;
		}
		else if(op.equals("||"))
		{
			return (a | b) != 0 ? 1 : 0;
		}
		else if(op.equals("<<"))
		{
			return a << b;
		}
		else if(op.equals(">>"))
		{
			return a >> b;
		}
		else if(op.equals(">>>"))
		{
			return a >>> b;
		}
		else if(op.equals(">"))
		{
			return a > b ? 1 : 0;
		}
		else if(op.equals("<"))
		{
			return a < b ? 1 : 0;
		}
		else if(op.equals(">="))
		{
			return a >= b ? 1 : 0;
		}
		else if(op.equals("<="))
		{
			return a <= b ? 1 : 0;
		}
		else if(op.equals("=="))
		{
			return a == b ? 1 : 0;
		}
		else if(op.equals("!="))
		{
			return a != b ? 1 : 0;
		}
		
		/*
		else if(op.equals("^^"))
		{
			return (a ^ b) != 0 ? 1 : 0;
		}*/
		
		console("Cannot parse: " + op);
		
		throw new Error("Cannot parse operator");
		
	}




	// Enables printing to the internet console.
	public static native void console(String text)
	/*-{
	    console.log(text);
	}-*/;
	
	// Modifies the list of tokens by reducing all parenthesized expressions to Nodes.
	private void parse_parentheses(List<Object> tokens)
	{
		ArrayList<Object> subexpression = new ArrayList<Object>();
		ListIterator<Object> iter = tokens.listIterator();
		
		// The current paren count.
		int lparen = 0;
		while(iter.hasNext())
		{
			Object o = iter.next();
			
			if(o.equals("("))
			{
				lparen++;		
			}
			
			if(o.equals(")"))
			{
				lparen--;
			}
			
			// Add all tokens between the parentheses.
			if(lparen != 0)
			{
				subexpression.add(o);
				iter.remove();
			}
			
			// Subexpression parsing code.
			if(lparen == 0 && !subexpression.isEmpty())
			{
				iter.remove();
				// Remove the initial '('.
				subexpression.remove(0);
				iter.add(parse_logical(subexpression));
				subexpression.clear();
			}
			
		}
	}
		
	//	Parses the Node that the given syntactic input string specifies.
	private Node parse_name(String input)
	{		
		// Handle String constants.
		if(input.charAt(0) == '"')
		{
			return graph.NEW_CONSTANT_NODE(input); 
		}
		
		boolean shadow = false;
		if(StringOps.hasPrefix(input, "var."))
		{
			input = input.substring(4);
			shadow = true;
		}
		
		// Handle Integer Constants.
		Integer int_val = toInteger(input);
		if(int_val != null)
		{
			return graph.NEW_CONSTANT_NODE(int_val);
		}
		
		
		
		String[] tokens = tokenize(input, name_operators);

		Node node;
		
		if(!shadow)
		{
			node = graph.findVariable(tokens[0]);
		}
		else// Shadow any variable with the same name.
		{
			node = graph.newVariable(tokens[0]);
		}
		
		int len = tokens.length;
						
		for(int i = 1; i < len; i += 1)
		{			
			String operation = tokens[i];
			
			Node node_next;
			
			// []
			// Parsing dynamic array indexing.
			if(operation.equals("["))
			{
				// We require the next token.
				i += 1;
				String name = "";
				
				// Parse the entire name String.
				while(!tokens[i].equals("]"))
				{
					name = name + tokens[i];
					i++;
				}
				
				Node var = parse_name(name);
				name = graph.dereference(var).data.toString();
				node_next = graph.dereference(node, name);
				
				if(node_next == null)
				{
					node_next = graph.NEW_NODE(name);
					graph.addDirectedEdge(node, node_next, name);
				}
			}
			else if(operation.equals("*"))
			{
				node_next = graph.dereference(node);
				
				if(node_next == null)
				{
					node_next = graph.NEW_NODE("[Default]");
					graph.addDirectedEdge(node, node_next, node);
				}
			}	
			else// '.'
			{
				// We require the next token.
				i += 1;
				String name = tokens[i];
				
				node_next = graph.dereference(node, name);
				
				if(node_next == null)
				{
					node_next = graph.NEW_NODE(name);
					graph.addDirectedEdge(node, node_next, name);
				}
			}
			
			node = node_next;
		}
		
		return node;
	}
	
	
	// Performs the splitting of code lines.
	private void split(String instruction, Box<String[]> left, Box<String[]> right, Box<String> op_box)
	{
		
		String[] parts = null;
		String the_op = null;
		for(String op : assignment_operators)
		{
			if(instruction.contains(op))
			{
				// Parse the syntax for assignment operations.
				parts = instruction.split(op);
				the_op = op;
				break;
			}
		}
		console("Op:  " + the_op);

		// Handle instruction made up of only 1 postfix expression.
		// FIXME : This is not a very general casing.
		// a + 1 + b++; will fail, but is nonsensical.
		if(the_op == null)
		{
			String name = instruction.substring(0, instruction.length() - 2);
			String postfix = instruction.substring(instruction.length() - 2);
			
			console("Interpreting Postfix:" + instruction + " " + name);
			
			left.val  = tokenize(name, logical_operators);
			right.val = tokenize("(" + name + ")+1", logical_operators);
			op_box.val = "=";
			return;
		}
		
		op_box.val = the_op;
		
		// Only split for the leftmost assignment_operator.
		parts[1] = instruction.substring(parts[0].length() + the_op.length());
		
		// FIXME : Deal with singular statements.
		
		left.val  = tokenize(parts[0], logical_operators);
		right.val = tokenize(parts[1], logical_operators);
	}

	// Returns false iff the given expression evaluates to 0.
	public boolean execute_IF(String string)
	{		
		Node result_node = parse(tokenize(string, logical_operators));
		
		return !(new Integer(0).equals(graph.dereference(result_node).data));
	}
	
	
	
	// Returns the index of the line of code containing the function that has been called.
	// Returns NO_FUNCTION_CALL otherwise.
	// IN/OUT instruction_box contains the input instruction, instruction
	private int handle_functions(Box<String> instruction_box)
	{
		String instruction = instruction_box.val;
		console(instruction);

		String[] tokens = tokenize(instruction, logical_operators);
		
		int len = tokens.length;
		for(int i = 1; i < len; i++)
		{
			String str = tokens[i];
			if(str.equals("("))
			{
				// We know that we are calling a function if the previous token
				// is not a logical syntactic symbol. 
				String potential_function_name = tokens[i - 1];
				char c = potential_function_name.charAt(0);
				if(!logical_operators.contains(c))
				{
					Node function = parse(potential_function_name);
					Integer line = (Integer) graph.dereference(function).data;
					
					// Build the argument array.
					StringBuilder args = new StringBuilder();
					
					int count = 1;
						
					while(count != 0)
					{
						i++;
						String token = tokens[i];
						if(token.equals("("))
						{
							count++;
						}
						
						if(token.equals(")"))
						{
							count--;
							
							// Don't add the last parenthesis.
							if(count == 0)
							{
								break;
							}
						}
						
						args.append(token);
						
					}
					
					// Push the comma delimited array of arguments.
					console(args.toString());
					String args_string = args.toString();
					
					// Handle the case where the function has already been evaluated and the 
					// result stored in the return variable.
					Node RETURN = graph.findVariable(Processor.RETURN_NAME, false);
					
					Node return_val = graph.dereference(RETURN);
					
					// The return has been set if it is defined and it does not point to itself.
					if(RETURN != null && return_val != RETURN)
					{
						String replace = potential_function_name + "(" + args_string + ")";
						//replace = java.util.regex.Matcher.quoteReplacement(replace);
						//StringUtils.replaceOnce("aba", "a", "")    = "ba"
						//instruction_box.val = instruction.replaceFirst(replace, Processor.RETURN_NAME);
						
						instruction_box.val = replaceString(instruction, replace, Processor.RETURN_NAME);
						
						
						console("");
						console("Original_Instruction : " + instruction);
						console("Replace : " + replace);
						console("New Statement:  " + instruction_box.val);
						
						return FUNCTION_RETURN;
					}
					
					
					String[] args_separated = args_string.split(",");
					
					for(String str1 : args_separated)
					{
						console("Arg : " + str1);
					}
					
					graph.pushArguments(args_separated);
					
					return line;
				}
			}
		}
		
		return NO_FUNCTION_CALL;
	}
	
	private String replaceString(String input, String find, String replace)
	{
		int index = input.indexOf(find);
		String first = input.substring(0, index);
		String last = input.substring(index + find.length());
		
		return first + replace + last;
	}
	
	
}
