package edu.umd.lib.fedora.util.foxml;

import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author phammer
 *
 */
public class NodeStats {

	private String xPath;
	private String searchLabel;
	private String searchVal;
	private String minVal;
	private String maxVal;
	private int count;
	private HashMap< String, Integer > aggregate;
	private HashMap< String, List<String> > pidList;
	private String nodeTarget; // "Attribute" or "CDATA"
	private String searchType; // "findAll", "findEq", findGe", "findLe"
	private String aggregateType; // "Empty", "Group", "Max", "Min"
	private boolean getIdentifiers;
	
	public NodeStats( String thisXpath ) {
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
