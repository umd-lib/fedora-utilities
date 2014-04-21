package edu.umd.lib.fedora.util.foxml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.InvalidXPathException;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import edu.umd.lib.fedora.util.DO.LIMSns;

public class UMfactory {
	
	public Map mXPath = new HashMap();
	public Map namespace = new HashMap();
	private String sHost;
	
	DocumentFactory df = DocumentFactory.getInstance();
	
	public UMfactory() {
	  sHost = "fedora.lib.umd.edu";
		namespace = new LIMSns().getNamespace();
	}
  
  public UMfactory(String sThisHost) {
    this();
    if( sThisHost != null && sThisHost.length() > 0 ) {
      sHost = sThisHost;
    }
  }
	
	/**
	 * 
	 * @return
	 */
	public UMDMxml getUMDM() {
		
		DocumentFactory df = new DocumentFactory();
		Document thisDoc = df.createDocument();
		Element thisElement = df.createElement("descMeta");
		thisDoc.setRootElement(thisElement);
		UMDMxml thisUMDM = new UMDMxml(thisDoc);
		
		
		
		return thisUMDM;
	}
	
	public UMDMxml getUMDM( String strTarget, String strType) {
		UMDMxml thisUMDM = null;
		
		//Get a file with the UMDM xml
		if( strType.equals("File")) {
			try {
				File thisXml = new File(strTarget);
				SAXReader reader = new SAXReader();
				Document thisDoc = reader.read(thisXml);
				thisUMDM = new UMDMxml(thisDoc);
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace(System.out);
			}
			
		} else if( strType.equals("Pid") ) {
			
			try {
				String host = sHost;
				String strURL = "http://"+ host + 
					"/fedora/get/"+ strTarget + 
					"/umd-bdef:umdm/getUMDM/";
				URL thisURL = new URL(strURL);
				SAXReader reader = new SAXReader();
				Document thisDoc = reader.read(thisURL);
				// Element root = thisDoc.getRootElement();
				thisUMDM = new UMDMxml(thisDoc);
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace(System.out);
			}
		}
		
		return thisUMDM;
	}
	
	/**
	 * 
	 * @return
	 */
	public UMAMxml getUMAM() {
		
		DocumentFactory df = new DocumentFactory();
		Document thisDoc = df.createDocument();
		Element thisElement = df.createElement("adminMeta");
		thisDoc.setRootElement(thisElement);
		UMAMxml thisUMAM = new UMAMxml(thisDoc.getRootElement());
		
		return thisUMAM;
	}
	
	public UMAMxml getUMAM( String strTarget, String strType) {
		UMAMxml thisUMAM = null;
		
		//Get a file with the UMDM xml
		if( strType.equals("File")) {
			try {
				File thisXml = new File(strTarget);
				SAXReader reader = new SAXReader();
				Document thisDoc = reader.read(thisXml);
				thisUMAM = new UMAMxml(thisDoc.getRootElement());
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace(System.out);
			}
			
		} else if( strType.equals("Pid") ) {
			
			try {
				String host = sHost;
				String strURL = "http://"+ host + 
					"/fedora/get/"+ strTarget + 
					"/umd-bdef:umam/getUMAM/";
				URL thisURL = new URL(strURL);
				SAXReader reader = new SAXReader();
				Document thisDoc = reader.read(thisURL);
				thisUMAM = new UMAMxml(thisDoc.getRootElement());
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace(System.out);
			}
		}
		
		return thisUMAM;
	}
	
	/**
	 * 
	 * @return
	 */
	public DCxml getDC() {
		
		return new DCxml();
	}
	
	/**
	 * 
	 * @return
	 */
	public DCxml getDC(String title, String pid) {
		
		return new DCxml(title, pid);
	}
  
  /**
   * 
   * @return
   */
  public DOxml getDO(String pid, String type) {
    
    // Get the DC form the pid source
    if( pid != null ) {
    String strTitle;
    String strPid;
    DOxml oDO;
    
    try {
      String host = sHost;
      String strURL = "http://"+ host + 
        "/fedora/get/"+ pid;
      if( type.equals("UMAM")) {
        strURL += "/umd-bdef:amInfo/getAMInfo/";
      } else {
        strURL += "/umd-bdef:doInfo/getDOInfo/";
      }
      SAXReader reader = new SAXReader();
      Document thisDoc = reader.read(strURL);
      // Element root = thisDoc.getRootElement();
      if( thisDoc == null ) {
        oDO = new DOxml();
      } else {
        oDO = new DOxml(thisDoc);
      }
      
      return oDO;
      
    } catch (Exception e) {
      // TODO: handle exception
      //e.printStackTrace(System.out);
      return null;
    }
    }
    
    return null;
  }
	
	/**
	 * 
	 * @return
	 */
	public DOxml getDO() {
		
		return new DOxml();
	}
	
	/**
	 * 
	 * @return
	 */
	public DOxml getDO(String type, String contentModel, String status) {
		
		return new DOxml( type, contentModel, status);
	}
	
	/**
	 * 
	 * @return
	 */
	public METSxml getMETS(String strMETStype) {
		
		return new METSxml(strMETStype);
	}
	
	/**
	 * 
	 * @return
	 */
	public METSxml getMETS(String strTarget, String strType) {
		
		METSxml thisMETS = null;
		
		//Get a file with the UMDM xml
		if( strType.equals("File")) {
			try {
				File thisXml = new File(strTarget);
				SAXReader reader = new SAXReader();
				Document thisDoc = reader.read(thisXml);
				thisMETS = new METSxml(thisDoc);
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace(System.out);
			}
			
		} else if( strType.equals("Pid") ) {
			
			try {
				String host = sHost;
				String strURL = "http://"+ host + 
					"/fedora/get/"+ strTarget + 
					"/umd-bdef:rels-mets/getRels/";
				URL thisURL = new URL(strURL);
				SAXReader reader = new SAXReader();
				Document thisDoc = reader.read(thisURL);
				//Element root = thisDoc.getRootElement();
				thisMETS = new METSxml(thisDoc);
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace(System.out);
			}
		}
		
		return thisMETS;
	}
	
	public MethodsXml getMethods( String sSource, String sType ) {
		
		MethodsXml theseMethods = null;
		Document targetMethods;
		String host = sHost;
		
		
		if (sType.equalsIgnoreCase("Pid")) {
			String strURL = "http://" + host + 
				"/fedora/listMethods/" + 
				sSource + "?xml=true";
			
			// System.out.println(strURL);
			
			URL thisURL;
			
			try {
				thisURL = new URL(strURL);
				
				SAXReader reader = new SAXReader();
				
				targetMethods = reader.read(thisURL);

				if( targetMethods != null ) {
					theseMethods = new MethodsXml(targetMethods);
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		} else if(sType.equalsIgnoreCase("File")) {
			
		}
		return theseMethods;
	} 
	
}
