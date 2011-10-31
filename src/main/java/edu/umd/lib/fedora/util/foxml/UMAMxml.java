package edu.umd.lib.fedora.util.foxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.InvalidXPathException;
import org.dom4j.Node;
import org.dom4j.XPath;

import edu.umd.lib.fedora.util.DO.LIMSns;

public class UMAMxml {

	private LIMSns namespace = new LIMSns();
	private Map<String, XPath> mXPath = new HashMap<String, XPath>();
	private DocumentFactory df = DocumentFactory.getInstance();
	private Element thisMetadata;
	private Element root;
	private String strCreationDate = "";
	
	private String[] aLabels = new String[]{"identifier", 
		"digiProv", 
		"adminRights",
		"technical" };
	
	/**
	 * 
	 * @param thisPID
	 * @param model
	 */
	public UMAMxml( Element nThisNode ) {
		if( nThisNode != null ) {
			thisMetadata = nThisNode;
	        root = thisMetadata;
		}
	}
	
	public boolean isOK() {
		if( thisMetadata != null ) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getProp( String strXpath ) {
		String result = null; 
		XPath xPath;
		
		if (thisMetadata != null) {
			xPath = getXPath(strXpath);
			if( xPath != null ) {
				Node nResult = xPath.selectSingleNode(thisMetadata);
				if (nResult != null) {
					result = nResult.getText();
				}
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getProps( String strXpath ) {
		List<Node> results;
		List<String> values = new ArrayList<String>();
		
		results = getXPath(strXpath).selectNodes(thisMetadata);
		
		for (Node nResult : results) {
			values.add(nResult.getText());
		}
		
		return values;
	}
	
	public boolean addElement(Element toBeAdded) {
		
		if( toBeAdded != null ) {
			
			root.add(toBeAdded);
			
		}
		
		return true;
	}
	
	/**
	 * This gets a list of elements copied from the Document
	 * 
	 * @param eToSet
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Element> getElements ( String strSearchPath ) {
		
		List<Element> lElements = null;
		
		if( strSearchPath != null && strSearchPath.length() > 0 ) {
			
			lElements = getXPath(strSearchPath).selectNodes(thisMetadata);
			
		}
		
		return lElements;
	}
  
  public String setCreationDate( String strDate ) {
    if( strDate != null && strDate.length() > 0 ) {
      strCreationDate = strDate;
    }
    
    return strCreationDate;
  }
  
  public String getCreationDate() {
    return strCreationDate;
  }
	
	/**
	 * This returns a list of elements extracted and removed from
	 * The UMAM.
	 * 
	 * @param strSearchPath
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Element> removeElements( String strSearchPath ) {
		
		List<Element> lElements = null;
		
		if( strSearchPath != null && strSearchPath.length() > 0 ) {
			
			lElements = getXPath(strSearchPath).selectNodes(thisMetadata);
			
			for(Element eElement : lElements ) {
				eElement.detach();
			}
		}
		
		return lElements;
	}
	
	@SuppressWarnings("unchecked")
	public Document getXML() {
		
		Document result = df.createDocument();
		// Document result = df.createDocument(thisMetadata.getRootElement().createCopy());
		
		String strXPath;
		List<Node> lElements;
		Element eHolder;
		Element localRoot = df.createElement("adminMeta");
		result.add(localRoot);
		result.setRootElement(localRoot);
		
		/* This copies each set of nodes in order
		 * to the new XML for delivery to the user
		 * Internally each tag set will be in position order.
		 * Later we will need to add attribute ordering for
		 * description and subject.
		 * aLabels is the COMPLETE list of possible
		 * top level elements in the preferred order.
		 */
		for (int i = 0; i < aLabels.length; i++) {
			strXPath = "/adminMeta/" + aLabels[i];
			
			lElements = getXPath(strXPath).selectNodes(thisMetadata);
			
			for (Node nResult : lElements) {
				eHolder = (Element) nResult;
				eHolder = eHolder.createCopy();
				localRoot.add(eHolder);
			}
		}
		
		if( result == null ) {
			return null;
		} else {
			return result;
		}
	}

	/************************************************************* getXPath */
	/**
	 * Get a compiled XPath object for the expression.  Cache.
	 */

	private XPath getXPath(String strXPath) throws InvalidXPathException {

		XPath xpath = null;

		if (mXPath.containsKey(strXPath)) {
			xpath = (XPath) mXPath.get(strXPath);

		} else {
			xpath = df.createXPath(strXPath);
			xpath.setNamespaceURIs(namespace.getNamespace());
			mXPath.put(strXPath, xpath);
		}

		return xpath;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
