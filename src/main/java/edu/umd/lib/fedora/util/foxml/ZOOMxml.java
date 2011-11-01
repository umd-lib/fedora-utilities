package edu.umd.lib.fedora.util.foxml;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class ZOOMxml {

	private String strTitle;
	private String strSWF;
	private String strImagePath;
	
	public ZOOMxml() {
		strSWF = "Unknown";
		strImagePath = "Unknown";
	}
	
	public ZOOMxml( String newTitle, String newSWF, String newURL ) {
		this();
		if( newTitle != null && newSWF != null && newURL != null ) {
			strTitle = newTitle;
			strSWF = newSWF;
			strImagePath = newURL;
		}
	}
	
	public String setTitle(String newTitle ) {
		if( newTitle != null ) {
			strTitle = newTitle;
		}
		return strTitle;
	}
	
	public String getTitle() {
		return strTitle;
	}
	
	public String setSWF(String newSWF ) {
		if( newSWF != null ) {
			strSWF = newSWF;
		}
		return strSWF;
	}
	
	public String getSWF() {
		return strSWF;
	}
	
	public String setImagePath(String newImagePath ) {
		if( newImagePath != null ) {
			strImagePath = newImagePath;
		}
		return strImagePath;
	}
	
	public String getImagePath() {
		return strImagePath;
	}
	
	public boolean isOK() {
		if( strSWF != null && !strSWF.equals("Unknown") ) {
			if( strImagePath != null && !strImagePath.equals("Unknown") ) {
				return true;
			}	
		}
		return false;
			
	}
	
	public Document getXML() {
		
		Document thisZoom = DocumentHelper.createDocument();
		
		if( isOK() ) {
			
			// Start building the document 
			
			Element root = thisZoom.addElement("zoomify");
			
			thisZoom.setRootElement(root);
			
			root.addElement("title").addText(strTitle);
			root.addElement("srcSWF").addText(strSWF);
			root.addElement("imagePath").addText(strImagePath);
			
		}
		
		return thisZoom;
		
	}
}
