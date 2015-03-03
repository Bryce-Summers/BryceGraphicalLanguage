package project.client.Operations;

public class StringOps
{

	// FIXME : Consider replacing this with string.startswith() which is built in.
	
	public static boolean hasPrefix(String str, String prefix)
	{
		int len = prefix.length();
		return str.length() > len && str.substring(0, len).equals(prefix);
	}
	
}
