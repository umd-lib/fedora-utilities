package src;

import java.util.HashMap;

public class LIMSns {

	public HashMap<String, String> namespace = 
		new HashMap<String, String>();
	
	/**
	 * Constructor
	 */
	public LIMSns() {
		namespace.put("oai_dc", 
				"http://www.openarchives.org/OAI/2.0/oai_dc/");
		namespace.put("sparql",
				"http://www.w3.org/2001/sw/DataAccess/rf1/result");
		namespace.put("rdf", 
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		namespace.put("mets", 
				"http://www.loc.gov/METS/");
		namespace.put("xlink", 
				"http://www.w3.org/1999/xlink");
		namespace.put("marc", 
				"http://www.loc.gov/MARC21/slim");
		namespace.put("doInfo", 
				"http://www.itd.umd.edu/fedora/doInfo");
		namespace.put("amInfo", 
				"http://www.itd.umd.edu/fedora/amInfo");
		namespace.put("dc", 
				"http://purl.org/dc/elements/1.1/");
		namespace.put("col", 
				"http://www.fedora.info/definitions/1/0/types/");
		namespace.put("foxml", 
				"info:fedora/fedora-system:def/foxml#");
		namespace.put("audit", 
		    "info:fedora/fedora-system:def/audit#");
	}
	
	public void setNamespace( String key, String val ) {
		if( key != null && val != null && 
				key.length() > 0 && val.length() > 0 ) {
			namespace.put(key, val);
		}
	}
	
	public String getName( String key ) {
		if( key != null && key.length() > 0 ) {
			return namespace.get(key);
		}
		return null;
	}
	
	public HashMap<String, String> getNamespace() {
		HashMap<String, String> copyNS = 
			new HashMap<String, String>();
		
		for( String thisKey: namespace.keySet() ) {
			copyNS.put(thisKey, namespace.get(thisKey));
		}
		
		return copyNS;
	}
	
	/**
	 * 
	 * This class encapsulates the structure and list of 
	 * common LIMS XML NameSpace entries.  Create this
	 * object and extract a copy of the standard namespaces 
	 * for your own use.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
