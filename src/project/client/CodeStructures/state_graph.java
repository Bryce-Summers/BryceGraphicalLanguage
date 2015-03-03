package project.client.CodeStructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.gargoylesoftware.htmlunit.javascript.host.Console;
import com.google.gwt.canvas.dom.client.Context2d;

import project.client.GraphStructures.Edge;
import project.client.GraphStructures.Node;
import project.client.Physics.BodyPhysics;
import project.client.interfaces.Body;
import project.client.interfaces.OBJ;

/*
 * State Graph Class.
 * 
 * Written by Bryce Summers on 2-20-2015.
 * 
 * Purpose : This class implements the representation of the state of a program's execution as a directed graph.
 * 
 * This class should provide a clean interface for manipulating the state variables such as pushing and 
 * 	poping scope and adding / removing edges, finding Nodes of a given name, transversing the graph etc.
 */

public class state_graph implements OBJ
{

	// Specifies 1 operation. Assignment, scope push, or scope pop.
	// These will be used to implement the backwards execution functionality.
	private class Operation
	{
		Edge edge;
		Object key;
		
		boolean vertice_instantiation = false;
		boolean scope_push = false;
		boolean scope_pop = false;
		boolean args = false;
		
		Scope_Type scope_type = null;
				
		// Custom made operations constructor.
		public Operation()
		{
		}
		
		public Operation(Edge e, Object k)
		{
			edge = e;
			key = k;
		}
		
		public Operation(HashMap<String, Node> scope_frame, boolean scope_push_in, Scope_Type type)
		{
			key = scope_frame;
			
			if(scope_push_in)
			{
				scope_push = true;
			}
			else
			{
				scope_pop = true;
			}
			
			this.scope_type = type;
		}
		
		public Operation(String[] arguments)
		{
			key = arguments;
			args = true;
		}
	
	}
	
	
	// Defines a mapping between String names, and in scope Nodes.
	// names --> variables.
	ArrayList<HashMap<String, Node>> scope;
	
	// Stores a history of Edges that have been modified.
	ArrayList<Operation> history;
	
	Stack<String[]> arguments = new Stack<String[]>();
	
	// The specification for the current directed edges in the graph.
	// Node --> map: Object keys --> Edges.
	HashMap<Node, HashMap<Object, Edge>> edges;
	
	// Stores the indices of the start of each batch of assignment operations.
	ArrayList<Integer> push_stack = new ArrayList<Integer>();
	
    // Graph Visualization code.
	// These are the current vertices and edges that are being drawn to the screen.
    public ArrayList<Node> current_vertices = new ArrayList<Node>();
    public ArrayList<Edge> current_edges  = new ArrayList<Edge>();
	
	    
	public state_graph()
	{
		scope = new ArrayList<HashMap<String, Node>>();
		HashMap<String, Node> global_scope = new HashMap<String, Node>();
		scope.add(global_scope);
		
		history = new ArrayList<Operation>();

		edges = new HashMap<Node, HashMap<Object, Edge>>();
	}
	

	public void draw(Context2d context)
	{
    	for(Edge e : current_edges)
    	{
    		e.draw(context);
    	}
    	
    	for(Node v : current_vertices)
    	{
    		v.draw(context);
    	}

	}

	// NBody Physics code for vertices.
	public void update()
	{		
		 // -- NBody Physics simulation.
        for(Body body : current_vertices)
        {
        	body.resetForce();
        }
        
        int separation = 15;
        
        //BodyPhysics.NBody(current_vertices, .01, false, 150 + separation);
        
        // Add a repelling force between all of the vertices.
        int len = current_vertices.size();
		for(int a = 0; a < len; a++)
		for(int b = a; b < len; b++)
		{
			Node n1 = current_vertices.get(a);
			Node n2 = current_vertices.get(b);
			
			BodyPhysics.NBody2(n1, n2, .01, false, n1.radius + n2.radius + separation);
			
		}
        
		// Add Attractive forces for the edge related vertices.
        for(Edge e : current_edges)
        {
        	BodyPhysics.NBody2(e.n1, e.n2, .03, true, e.n1.radius + e.n2.radius + separation);
        }
        
        for(Body body : current_vertices)
        {
        	BodyPhysics.Friction(body, .5);
        	BodyPhysics.Integrate(body);
        }
	}
	
	// ENSURES : Makes the current_vertices and current_edges have the correct nodes to display.
	public void updateCurrentlyDisplayedGraph()
	{
		ArrayList<Node> vertices = new ArrayList<Node>();
		HashSet<Node>   vert_set = new HashSet<Node>();
				
		updateCurrentlyDisplayedVertices(vertices, vert_set);
		updateCurrentlyDisplayedEdges(vertices, vert_set);
		
		current_vertices = vertices;
	}

	// Input : The output buffer for the vertices currently in scope.
	private void updateCurrentlyDisplayedVertices(ArrayList<Node> vertices, HashSet<Node> vert_set)
	{
		
		// We will keep track of every name that we have accounted for.
		HashSet<String> names = new HashSet<String>();

		for(int i = scope.size() - 1; i >= 0; i--)
		{
			HashMap<String, Node> map = scope.get(i);
			
			Set<String> keys = map.keySet();
			
			for(String name : keys)
			{
				// Add all names that have not been shadowed yet.
				if(!names.contains(name))
				{
					Node n = map.get(name);
					vertices.add(n);
					vert_set.add(n);
					names.add(name);
				}
			}
			
		}

	}
	
	// FIXME : Make sure this is the correct and desiered behavior.
	private void updateCurrentlyDisplayedEdges(ArrayList<Node> vertices, HashSet<Node> vert_set)
	{
		// The Edges used by Nodes in the current scope.
		ArrayList<Edge> edges_output = new ArrayList<Edge>();
		
		
		// Dynamically performs breadth first search through all of 
		// the vertices to reach all of their edges.
		for(int i = 0; i < vertices.size(); i++)
		{
			Node node = vertices.get(i);

			// Find and add this node's outgoing edge.
			HashMap<Object, Edge> name_edge_mapping = edges.get(node);
			
			// Some nodes have no outgoing edges.
			if(name_edge_mapping == null)
			{
				continue;
			}
			
			Collection<Edge> edges_set = name_edge_mapping.values();
			
			for(Edge edge : edges_set)
			{
			
				if(edge == null || edge.isReflexive())
				{
					continue;
				}
				
				edges_output.add(edge);
				
				Node other = edge.n2;
				
				// Expand the search if we have found a new node that was not previously in scope.
				if(!vert_set.contains(other))
				{
					vertices.add(other);
					vert_set.add(other);
				}
			}
			
		}
		
		current_edges = edges_output;
		
	}
	
	public void push()
	{
		push_stack.add(history.size());
	}
	
	// Undoes all changes since the last push().
	public void undo()
	{
		int val = push_stack.remove(push_stack.size() - 1);
		
		while(history.size() > val)
		{
			undoOperation();
		}
	}
	
	// Makes Node n1 now point at Node n2. Think of this as adding a directed edge to the graph.
	// The key specifies the name of this edge.
	// if key == n1, then the edge is the singular edge for a pointer.
	// Use key == n2 for a non singular pointer.
	// Use key = "variable name" to assign an edge for a given property name.
	public void addDirectedEdge(Node n1, Node n2, Object edge_key)
	{
		Edge e = new Edge(n1, n2);
			
		HashMap<Object, Edge> current_edges = edges.get(n1);
		
		if(current_edges == null)
		{
			current_edges = new HashMap<Object, Edge>();
			edges.put(n1, current_edges);
		}
		
		Edge previous_edge = current_edges.get(edge_key);
		
		// Insert an edge with a null assignment to represent that the edge was previously unassigned.
		if(previous_edge == null)
		{
			previous_edge = new Edge(n1, null);
		}
		
		Operation previous_assignment = new Operation(previous_edge, edge_key);
		history.add(previous_assignment);
		
		// Singular
		current_edges.put(edge_key, e);
								
	}


	
	// Undoes one operation.
	private void undoOperation()
	{
					
		// Pop the last assignment from the history.
		Operation previous_assignment = history.remove(history.size() - 1);
		
		// Undo an arguments push.
		if(previous_assignment.args)
		{
			arguments.pop();
			return;
		}
		
		// - handle scope oeprations.
		if(previous_assignment.scope_push)
		{
			scope.remove(scope.size()-1);
			scope_type_stack.pop();
			return;
		}
		
		if(previous_assignment.scope_pop)
		{
			scope.add((HashMap<String, Node>) previous_assignment.key);
			scope_type_stack.push(previous_assignment.scope_type);
			return;
		}
		
		
		// Now handle regular edge assignments.
		
		
		Edge previous_edge = previous_assignment.edge;
		
		Node n1 = previous_edge.n1;
		Object edge_key = previous_assignment.key;
		
		
		// Handle Vertice instantiations.
		if(previous_assignment.vertice_instantiation)
		{
			String name = (String)edge_key;
			
			int scope_index = getScope(name);
			scope.get(scope_index).remove(name);
			return;
		}
				
		
		// -- Handle true edge instantiations.
		
		HashMap<Object, Edge> current_edges = edges.get(n1);
		
		current_edges.remove(edge_key);
		
		// Re assign the node to the previous assignment, if it had one.
		if(previous_edge.n2 != null)
		{
			current_edges.put(edge_key, previous_edge);
		}
				
		// Garbage Collect trivial hashset arrays.
		if(current_edges.size() == 0)
		{
			edges.remove(n1);
						
		}
				
		
	}
	
	// Finds the variable in the most confining scope that
	// corresponds to the given name.
	// ENSURES : Returns the Node in the most confining scope that corresponds to the given name.
	// 			 Creates a new node in the current local scope if none is found.
	public Node findVariable(String name)
	{
		return findVariable(name, scope.size() - 1);
	}
	
	public Node findVariable(String name, boolean create_if_not_found)
	{
		return findVariable(name, scope.size() - 1, create_if_not_found);
	}
	
	// Default will create the variable if not found.
	private Node findVariable(String name, int max)
	{
		return findVariable(name, max, true);
	}
	
	// max = most limiting location that we are allowed to modify.
	// Gurranteed to return a Node object, unless create_if_not_found == false;
	// Returns null if no node of the given name exists.
	public Node findVariable(String name, int max, boolean create_if_not_found)
	{
		for(int i = max; i >= 0; i--)
		{
			HashMap<String, Node> nodes = scope.get(i);
			Node n = nodes.get(name);
			
			if(n != null)
			{
				return n;
			}
		}
		
		
		if(false == create_if_not_found)
		{
			return null;
		}
			
		// If the node is not found, then we create it and 
		// insert it into the most local scope.
		Node node_new = NEW_NODE(name);
		scope.get(max).put(name, node_new);
		
		// Add this variable instantiation to the history of assignments.
		Operation assign = new Operation(new Edge(node_new, node_new), name);
		assign.vertice_instantiation = true;
		history.add(assign);
		
		return node_new;
	}
	
	// Create a newLocal Variable in the most local scope.
	// This variable could potentially shadow those in higher scopes. 
	public Node newVariable(String name)
	{
		// If the node is not found, then we create it and 
		// insert it into the most local scope.
		Node node_new = NEW_NODE(name);
		scope.get(scope.size() - 1).put(name, node_new);
				
		// Add this variable instantiation to the history of assignments.
		Operation assign = new Operation(new Edge(node_new, node_new), name);
		assign.vertice_instantiation = true;
		history.add(assign);
		
		return node_new;
	}
		
	// Returns the index of the highest indexed scope 
	// level(The most local) that contains the given name.
	// ENSURES: Returns the most local scope index if the variable is not found.
	public int getScope(String name)
	{
		int max = scope.size() - 1;
		for(int i = max; i >= 0; i--)
		{
			HashMap<String, Node> nodes = scope.get(i);
			Node n = nodes.get(name);
			
			if(n != null)
			{
				return i;
			}
		}
		
		return max;
	}
	
	// Used to create new nodes that are not linked to anything yet.
	// A processor should call this function to create primitive regions of memory that store some data.
	public Node NEW_NODE(Object data)
	{
		return new Node(600 + Math.random()*100 - 50, 400 + Math.random()*100 - 50, data, 50);
	}
	
	
	// Dereferences the edge_key named element from the given Node n1.
	// Returns null if it does not exist.
	// Key associative dereference.
	public Node dereference(Node n1, Object edge_key)
	{
		HashMap<Object, Edge> edge_mappings = edges.get(n1);
		
		if(edge_mappings == null)
		{
			return null;
		}
		
		Edge e = edge_mappings.get(edge_key);
		
		if(e == null)
		{
			return null;
		}
		
		return e.n2;
	}
	
	// Singular dereference.
	public Node dereference(Node n1)
	{
		return dereference(n1, n1);
	}
	
	
	/* 
	 * Create a temporary node that will serve as an address that points to the node.
	 * 
	 * We can't just return the boxed node, 
	 * because Nodes semantically represent variables and the function that 
	 * parses it will need a variable that
	 * points to the computed result rather than the computed result itself.
	 */

	// Returns a new variable pointing to a node containing the given data.
	public Node NEW_CONSTANT_NODE(Object data)
	{
		Node temp = NEW_NODE(null);
		Node val  = NEW_NODE(data);
		addDirectedEdge(temp, val, temp);
		return temp;
	}
	

		
	private Stack<Scope_Type> scope_type_stack = new Stack<Scope_Type>();
		
	// -- Proccessor Functions.
	// Updates all of the scope based structures.
	// Adds information to the structures for backtracking purposes.
	// Populates the input array with the Nodes.
	public void pushScope(Scope_Type type)
	{
		scope.add(new HashMap<String, Node>());
		Operation assignment = new Operation(null, true, type);// Type = null may be ok. ?
		history.add(assignment);
		
		scope_type_stack.push(type);
	}
	
	public Scope_Type popScope()
	{		
		HashMap<String, Node> scope_frame = scope.remove(scope.size() - 1);
		
		Scope_Type type = scope_type_stack.pop();
		
		Operation assignment = new Operation(scope_frame, false, type);
		
		history.add(assignment);
		
		return type;
	}
	
	// Push function arguments.
	public void pushArguments(String[] args)
	{
		
		Operation arg_frame = new Operation();
		arg_frame.args = true;
		
		arguments.push(args);
		
		history.add(arg_frame);
	}
	
	// Returns the latest arguments to be pushed to the argument stack.
	public String[] getArguments()
	{
		if(arguments.isEmpty())
		{
			throw new Error("Args could not be found, either arg pushing or backwards operations are not working.");
		}
		
		return arguments.peek();
	}
	
	
}
