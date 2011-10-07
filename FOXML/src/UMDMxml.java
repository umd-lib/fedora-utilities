package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.InvalidXPathException;
import org.dom4j.Node;
import org.dom4j.XPath;

public class UMDMxml {

	private LIMSns namespace;
	private Map<String, XPath> mXPath = new HashMap<String, XPath>();
	private DocumentFactory df = DocumentFactory.getInstance();
	private Document thisMetadata;
	private Element root;
	private String strCreationDate = "";
	private String[] aLabels = new String[]{"mediaType", 
			"title", 
			"agent",
			"covPlace", 
			"covTime",
			"culture",
			"language",
			"description",
			"subject",
			"style",
			"identifier",
			"physDesc",
			"relationships",
			"repository",
			"rights"};
	/**
	 * 
	 * @param thisPID
	 * @param model
	 */
	public UMDMxml( Document thisDoc ) {
		
		thisMetadata = thisDoc;
        root = thisMetadata.getRootElement();
        namespace = new LIMSns();
		
//		namespace = names;
//		configFile = config;
//		String host = configFile.getProperty("host");
//		String strURL = "http://"+ host + 
//			"/fedora/get/"+ thisPID + 
//			"/umd-bdef:umdm/getUMDM/";
		
//		URL thisURL;
//		try {
//			thisURL = new URL(strURL);
//			SAXReader reader = new SAXReader();
//	        thisMetadata = reader.read(thisURL);
//	        root = thisMetadata.getRootElement();
//	        // printDoc(umdmStats, "");
//	        
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (DocumentException e) {
//			e.printStackTrace();
//		}
	}
	
	public boolean isOK() {
		
		boolean bIsOK = false;
		
		if( thisMetadata != null ) {
			if( root.getName().equals("descMeta")) {
				bIsOK = true;
			}
		}
		
		return bIsOK;
	}
	
	@SuppressWarnings("unchecked")
  public String getProp( String strXpath ) {
		String result = null; 
		XPath xPath;
		
		if (thisMetadata != null) {
			xPath = getXPath(strXpath);
			if( xPath != null ) {
				Node nResult = xPath.selectSingleNode(thisMetadata);
				Element eResult = (Element) nResult;
				Node nPart;
				String sNodeType;
				if (nResult != null) {
				  
				  for (Iterator<Node> iPart = eResult.nodeIterator(); iPart.hasNext();) {
            nPart = iPart.next();
            sNodeType = nPart.getNodeTypeName();
            System.out.println( sNodeType );
          }
				  
					result = nResult.getText();
					
					//eResult.
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
		Element eResult;
		Node nPart;
		String sNodeType;
		String sTemp;
		String sPart;
		Branch bElement;
		List<Node> lNodes;
		
		for (Node nNode : results) {

		  bElement = (Branch) nNode;
	    
	    lNodes = bElement.content();
	    
	    sTemp = "";
		  
		  for (Node nResult: lNodes) {

        if( sTemp.length() > 0 ) {
          sTemp += " ";
        }
		    
		    sPart = "";
		    
        if (nResult.getNodeTypeName().equalsIgnoreCase("Element")) {
          sTemp += DoUtils.elementString((Element) nResult);
        } else {
          
          sPart += nResult.getText();

          // Replace returns with spaces
          sPart = sPart.replaceAll("\n", " ");
          sPart = sPart.replaceAll("\r", " ");
          
          // Trim leading and trailing spaces
          sPart = sPart.trim();
          
          sTemp += sPart;

        }

      }
      if (sTemp.length() > 0) {
        values.add(sTemp);
      }
		  
      //System.out.println( nResult.getText() );
		}
		
		return values;
	}
	
	public boolean addElement(Element toBeAdded) {
		
		if( toBeAdded != null ) {
			
			root.add(toBeAdded);
			
//			List lContent = root.content();
//			String sTag = toBeAdded.getName();
//			String sNextTag = "";
//			Element eThisElement;
//			int iTag = lContent.size();
//			int X;
//			
//			for(X=0; X < aLabels.length; X++ ) {
//				if( aLabels[X].equals(sTag) ) {
//					if( ( X + 1 ) < aLabels.length ) {
//						sNextTag = aLabels[(X + 1)];
//						break;
//					}
//				}
//			}
//			
//			// Find the tag after the one we want to add
//			if( sNextTag.length() > 0 ) {
//				for( X=0; X < lContent.size(); X++ ) {
//					eThisElement = (Element) lContent.get(X);
//					if( eThisElement.getName().equals(sNextTag) ) {
//						break;
//					}
//				}
//			}
//			
//			// Insert the tag before the sNextTag or
//			// at the end of the XML
//			if( iTag < lContent.size() ) {
//				lContent.add(iTag, toBeAdded);
//			} else {
//				lContent.add(toBeAdded);
//			}
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
	
	public String getSimpleString( String strSearchPath ) {
	  String sResult = "";
	  
	  return sResult;
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
	
	@SuppressWarnings("unchecked")
	public Document getXML() {
		
		Document result = df.createDocument();
		//Document result = df.createDocument(thisMetadata.getRootElement().createCopy());
		
		String strXPath;
		List<Node> lElements;
		Element eHolder;
		Element localRoot = df.createElement("descMeta");
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
			strXPath = "/descMeta/" + aLabels[i];
			
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
		

	}

}
