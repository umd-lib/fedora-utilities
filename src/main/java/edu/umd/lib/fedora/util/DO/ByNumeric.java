package edu.umd.lib.fedora.util.DO;

public class ByNumeric implements java.util.Comparator<String> {
	public int compare(String nLeft, String nRight ) {
		try{
		return Integer.parseInt(nLeft) - Integer.parseInt(nRight);
		}
		catch(Exception e){}
		return 0;
	}
}
