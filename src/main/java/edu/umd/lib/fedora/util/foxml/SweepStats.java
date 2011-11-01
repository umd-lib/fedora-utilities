package edu.umd.lib.fedora.util.foxml;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.InvalidXPathException;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import edu.umd.lib.fedora.util.DO.LIMSns;

/**
 * Sweepstats accesses the shorthand report of the object
 * It will extract and report the items back.
 * @author phammer
 *
 */
public class SweepStats {

	private LIMSns namespace;
	private Map<String, XPath> mXPath = new HashMap<String, XPath>();
	private DocumentFactory df = DocumentFactory.getInstance();
	private Properties configFile;
	private final String XPATH_PREFIX = "/col:result/col:resultList/col:objectFields/col:";
	private String thisPid;
	private Document sweepStatsDoc;
	private int hitCount = 0;
	
	public SweepStats(LIMSns names, Properties config, String strPid ) {
		namespace = names;
		thisPid = strPid;
		configFile = config;
		String strPidEsc = strPid.replaceFirst(":", "%5C:");
		String strHost = config.getProperty("host");
		String strURL = "http://" + strHost + "/search/?query=pid:" + strPidEsc;
		XPath xPath;
		
		// Retrieve the document from Fedora
		URL thisURL;
		try {
			thisURL = new URL(strURL);
			SAXReader reader = new SAXReader();
	        sweepStatsDoc = reader.read(thisURL);
	        
	        
	        if (sweepStatsDoc != null) {
				xPath = getXPath("/col:result/col:hitcount");
				if( xPath != null ) {
					Node nResult = xPath.selectSingleNode(sweepStatsDoc);
					if (nResult != null) {
						hitCount = Integer.parseInt(nResult.getText());
					}
				}
			}
	        // printDoc(umdmStats, "");
	        
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getProp( String key ) {
		String result = null;
		String strXpath = XPATH_PREFIX + key; 
		XPath xPath;
		
		if (sweepStatsDoc != null) {
			xPath = getXPath(strXpath);
			if( xPath != null ) {
				Node nResult = xPath.selectSingleNode(sweepStatsDoc);
				if (nResult != null) {
					result = nResult.getText();
				}
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getProps( String key ) {
		List<Node> results;
		List<String> values = new ArrayList<String>();
		String strXpath = XPATH_PREFIX + key; 
		
		results = getXPath(strXpath).selectNodes(sweepStatsDoc);
		
		for (Node nResult : results) {
			values.add(nResult.getText());
		}
		
		return values;
	}
	
	public List<String> listParts() {
		
		List<String> parts = new ArrayList<String>();
		
		List<String> umams = getProps("hasPart");
		String strHost = configFile.getProperty("host");
		String strURL;
		URL thisURL;
		Document saveThis;
		
		try{
			
			for (String thisUMAM : umams) {
				// save the UMAMs
				strURL = "http://" + strHost + 
				"/fedora/get/" + thisUMAM + 
				"/umd-bdef:umam/getUMAM/";
				thisURL = new URL(strURL);
				SAXReader reader = new SAXReader();
				saveThis = reader.read(thisURL);
	        
				String fileName = getXPath("adminMeta/identifier")
				.selectSingleNode(saveThis)
				.getText();
				
				parts.add(thisUMAM + ", " + fileName);
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return parts;
	}
	
	public int getHitCount() {
		return hitCount;
	}
	
	public void saveDocs() {
		
		String strPidEsc = thisPid.replaceFirst(":", "%5C:");
		String savePath = configFile.getProperty("UMDMdir");
		String strHost = configFile.getProperty("host");
		String strURL = "http://" + strHost + "/search/?query=pid:" + strPidEsc;
		Document saveThis;
		URL thisURL;
		// First get the general specs
		// Make sure that it is a dm type
		// Get the doType
		try {
			
			if( sweepStatsDoc != null ) {
			
				getProp("doType");
				
				if( savePath != null && savePath.length() > 0 ) {
					// save the UMDM
					strURL = "http://" + strHost + 
						"/fedora/get/" + thisPid + 
						"/umd-bdef:sweepStatsDoc/getUMDM/";
					thisURL = new URL(strURL);
					SAXReader reader = new SAXReader();
					saveThis = reader.read(thisURL);
		        
					if( saveThis != null ) {
						printDoc( saveThis, savePath + "/umdm_" + thisPid.replaceFirst(":", "-") + ".xml" );
					}
				}
				
				savePath = configFile.getProperty("METSdir");
				
				if( savePath != null && savePath.length() > 0 ) {
					// save the UMDM
					strURL = "http://" + strHost + 
					"/fedora/get/" + thisPid + 
					"/umd-bdef:rels-mets/getRels/";
					thisURL = new URL(strURL);
					SAXReader reader = new SAXReader();
					saveThis = reader.read(thisURL);
		        
					if( saveThis != null ) {
						printDoc( saveThis, savePath + "/mets_" + thisPid.replaceFirst(":", "-") + ".xml" );
					}
				}
				
				savePath = configFile.getProperty("DOdir");
				
				if( savePath != null && savePath.length() > 0 ) {
					// save the UMDM
					strURL = "http://" + strHost + 
					"/fedora/get/" + thisPid + 
					"/umd-bdef:doInfo/getDOInfo/";
					thisURL = new URL(strURL);
					SAXReader reader = new SAXReader();
					saveThis = reader.read(thisURL);
		        
					if( saveThis != null ) {
						printDoc( saveThis, savePath + "/doinfo_" + thisPid.replaceFirst(":", "-") + ".xml" );
					}
				}
				
				savePath = configFile.getProperty("UMAMdir");
				
				if( savePath != null && savePath.length() > 0 ) {
					
					List<String> umams = getProps("hasPart");
					
					for (String thisUMAM : umams) {
						// save the UMAMs
						strURL = "http://" + strHost + 
						"/fedora/get/" + thisUMAM + 
						"/umd-bdef:umam/getUMAM/";
						thisURL = new URL(strURL);
						SAXReader reader = new SAXReader();
						saveThis = reader.read(thisURL);
			        
						if( saveThis != null ) {
							printDoc( saveThis, savePath + "/umam_" + thisUMAM.replaceFirst(":", "-") + ".xml" );
						}
					}
					
					savePath = configFile.getProperty("AMdir");
					
					if( savePath != null && savePath.length() > 0 ) {
						
						for (String thisUMAM : umams) {
							// save the UMAMs
							strURL = "http://" + strHost + 
							"/fedora/get/" + thisUMAM + 
							"/umd-bdef:amInfo/getAMInfo/";
							thisURL = new URL(strURL);
							SAXReader reader = new SAXReader();
							saveThis = reader.read(thisURL);
				        
							if( saveThis != null ) {
								printDoc( saveThis, savePath + "/aminfo_" + thisUMAM.replaceFirst(":", "-") + ".xml" );
							}
						}
						
					}
				}
			}
	        
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
		

	/**************************************************************** PrintDoc */
	/**
	 * Prints a document based on the class's printing directive string
	 */
	private boolean printDoc(Document docThisDocument, String strFile) {

		boolean bTerminalOut = false;
		boolean bFileOut = false;

		if (strFile == null || strFile.length() < 1 ) {
			bTerminalOut = true;
		} else {
			bFileOut = true;
		}

		strFile = strFile.replaceAll("umd:", "umd_");

		try {

			if (bFileOut) {
				// Create the string of the filename to be written to
				String strFileName = strFile;

				// Set up the interminable number of structures for printing this puppy
				OutputStreamWriter outWriter = new OutputStreamWriter(
						new FileOutputStream(strFileName), "UTF-8");
				OutputFormat format = OutputFormat.createPrettyPrint();
				XMLWriter writer = new XMLWriter(outWriter, format);
				writer.write(docThisDocument);
				writer.close();
			}

			if (bTerminalOut) {
				// Set up the interminable number of structures for printing this puppy
				OutputFormat format = OutputFormat.createPrettyPrint();
				XMLWriter writer = new XMLWriter(System.out, format);
				writer.write(docThisDocument);
			}

		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return true;
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
