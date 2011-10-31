package edu.umd.lib.fedora.util.foxml;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;

public class DCxml {

	private String dcTitle;
	private String dcIdentifier;
	private String strCreationDate = "";
	
	public DCxml() {
		dcTitle = "Unknown";
		dcIdentifier = "Unknown";
	}
	
	public DCxml( String title, String pid ) {
	  
	  // new DCxml( "this amazing thing", "umd:12345" );
	  
		this();
		if( title != null && pid != null ) {
		  if( title.length() > 0 ) {
		    dcTitle = title;
		  }
			dcIdentifier = pid;
		}
	}
	
	public String setTitle(String thisTitle ) {
		if( thisTitle != null ) {
			dcTitle = thisTitle;
		}
		return dcTitle;
	}
	
	public String getTitle() {
		return dcTitle;
	}
	
	public String setIdentifier(String thisIdentifier ) {
		if( thisIdentifier != null ) {
			dcIdentifier = thisIdentifier;
		}
		return dcIdentifier;
	}
	
	public String getIdentifier() {
		return dcIdentifier;
	}
	
	public boolean isOK() {
		if( dcTitle != null && 
				dcIdentifier != null &&
				!dcTitle.equals("Unknown") && 
				!dcIdentifier.equals("Unknown") ) {
			return true;
		}
		return false;
			
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
		
		Document thisDcInfo = DocumentHelper.createDocument();
		
		if( isOK() ) {
			
			// Start building the document 
			Namespace nsDC = Namespace.get("dc", "http://purl.org/dc/elements/1.1/");
			Namespace nsOAI_DC = Namespace.get("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
			
			Element root = thisDcInfo.addElement("oai_dc:dc");
			root.add(nsDC);
			root.add(nsOAI_DC);
			
			thisDcInfo.setRootElement(root);
			
			root.addElement("dc:title").addText(dcTitle);
			root.addElement("dc:identifier").addText(dcIdentifier);
			
		}
		
		return thisDcInfo;
		
	}
}
