package edu.umd.lib.fedora.util.foxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.InvalidXPathException;
import org.dom4j.XPath;
import edu.umd.lib.fedora.util.DO.LIMSns;
import edu.umd.lib.fedora.util.DO.*;

/**
 * Sweepstats accesses the shorthand report of the object
 * It will extract and report the items back.
 * @author phammer
 *
 */
public class MethodsXml {

	private LIMSns namespace = new LIMSns();
	private Map<String, XPath> mXPath = new HashMap<String, XPath>();
	private DocumentFactory df = DocumentFactory.getInstance();
	private Document dMethods;
	private String sDoType = "UMDM";
	
	public MethodsXml(Document theseMethods ) {
		
		Element root;
		
		if (theseMethods != null) {
      root = theseMethods.getRootElement();
      root.detach();
      dMethods = df.createDocument(root);
      
      List<String> lBdefs = listBdefs();
      
      for( String sBdef : lBdefs ) {
        if( sBdef.contains("umam")) {
          sDoType = "UMAM";
        }
      }
    }
		
	}
	
	public boolean isOK() {
		boolean IsOK = true;
		
		
		
		return IsOK;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> listBdefs() {
		
		List<String> lMethods = null;
		List returns;
		Element retNode;
		String sBdef;
		
		if (isOK()) {
			lMethods = new ArrayList<String>();
			returns = DoUtils.getXPath("/objectMethods/bDef").selectNodes(dMethods);
			for (Object thisNode : returns) {
				
				retNode = (Element) thisNode;

				sBdef = retNode.attributeValue("pid");

				if (sBdef != null) {
				  if( !lMethods.contains(sBdef)) {
				    lMethods.add(sBdef);
				  }
				}

			}
		}
		return lMethods;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> listMethods() {
		
		List<String> lMethods = null;
		List returns;
		Element retNode;
		String sBdef;
		
		if (isOK()) {
			lMethods = new ArrayList<String>();
			returns = DoUtils.getXPath("/objectMethods/bDef/method").selectNodes(dMethods);
			for (Object thisNode : returns) {
				
				retNode = (Element) thisNode;

				sBdef = retNode.attributeValue("name");

				if (sBdef != null) {

					lMethods.add(sBdef);
				}

			}
		}
		return lMethods;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
