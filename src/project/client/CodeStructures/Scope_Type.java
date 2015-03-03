package project.client.CodeStructures;

public class Scope_Type
{
	public enum Type{NORMAL, FUNCTION_CALL, IF_STATEMENT, WHILE_LOOP, FOR_LOOP};
	
	public Type type;
	
	public int return_line;
	
	public Scope_Type(Type type, int line)
	{
		this.type = type;
		this.return_line = line;
	}
}
