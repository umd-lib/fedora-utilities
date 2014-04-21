package edu.umd.lib.fedora.util.foxml;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import edu.umd.lib.fedora.util.DO.DoUtils;
import edu.umd.lib.fedora.util.DO.LIMSlookup;

public class DOxml {

	private String doContModel;
	private String doStatus;
	private String doType;
	private String strCreationDate = "";
	
	public DOxml() {
		
		doContModel = "UMD_IMAGE";
		doStatus = "Pending";
		doType = "";
	}
	
	public DOxml(String type, String contentModel, String status )  {
		this();
		
		//new DOxml( "doInfo", "UMD_VIDEO", "Pending" );
		
		if( type != null && contentModel != null && status != null ) {
			if( LIMSlookup.isContentModelValue(contentModel) && 
					LIMSlookup.isStatus(status) && 
					LIMSlookup.isDOtype( type ) ) {
				doContModel = contentModel;
				doStatus = status;
				doType = type;
			}
		}
	}
	
	public DOxml(Document thisDOxml ) {
	  this();
	  
	  parseDOxml( thisDOxml );
	}
	
	public String setContModel(String thisContModel ) {
		if( thisContModel != null && LIMSlookup.isContentModelValue(thisContModel) ) {
			doContModel = thisContModel;
		}
		return doContModel;
	}
	
	public String getContModel() {
		return doContModel;
	}
	
	public String setStatus(String thisStatus ) {
		if( thisStatus != null && LIMSlookup.isStatus(thisStatus) ) {
			doStatus = thisStatus;
		}
		return doStatus;
	}
	
	public String getStatus() {
		return doStatus;
	}
	
	public String setType(String thisType ) {
		if( thisType != null && LIMSlookup.isDOtype(thisType) ) {
			doType = thisType;
		}
		return doType;
	}
	
	public String getType() {
		return doType;
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
	
	public Document getXML() {
		
		Document thisDoInfo = DocumentHelper.createDocument();
		String sNamespace;
		
		if( isOK() ) {
			
			System.out.println("do IS ok");
			
      if( doType.equals("amInfo") ) {
        sNamespace = "http://www.itd.umd.edu/fedora/amInfo";
      } else {
        sNamespace = "http://www.itd.umd.edu/fedora/doInfo";
      }
			
			// Start building the document 
			//Namespace nsDoInfo = Namespace.get("http://www.itd.umd.edu/fedora/doInfo");
			
			Element root = thisDoInfo.addElement(doType);
			
			root.addNamespace("", sNamespace);
			
			thisDoInfo.setRootElement(root);
			
			root.addElement("type", sNamespace).addText(doContModel);
			root.addElement("status", sNamespace).addText(doStatus);
			
		}
		
		return thisDoInfo;
	}
	
	public boolean isOK() {
		
		if( LIMSlookup.isContentModelValue(doContModel) )  {
			if (LIMSlookup.isStatus(doStatus)) {
				if (LIMSlookup.isDOtype( doType ) ) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean parseDOxml(Document thisDOxml ) {
	  boolean bResult = true;
	  String sTempString;
	  String sDoString = null;
	  
	  Element eRoot = thisDOxml.getRootElement();
	  
	  sDoString = eRoot.getName();
	  
	  if( LIMSlookup.isDOtype(sDoString) ) {
	    
	    doType = sDoString;
	    
	    sTempString = "/" + sDoString + ":" + sDoString
          + "/" + sDoString + ":" + "type";
	    
	    // System.out.println("ContentModel XPATH: " + sTempString);
	    
	    Node nTempNode = DoUtils.getXPath(sTempString).selectSingleNode(thisDOxml);
	    
	    doContModel = nTempNode.getText();
	    
	    if( ! LIMSlookup.isContentModelValue(doContModel) ) {
	      doContModel = "UMD_IMAGE";
	    }
      
      sTempString = "/" + sDoString + ":" + sDoString
          + "/" + sDoString + ":" + "status";
      
      // System.out.println("Status XPATH: " + sTempString);
      
      nTempNode = DoUtils.getXPath(sTempString).selectSingleNode(thisDOxml);
      
      doStatus = nTempNode.getText();
	    
      if( ! LIMSlookup.isStatus(doStatus) ) {
        doStatus = "Pending";
      }
	  }
	  
	  return bResult;
	}
	
}
