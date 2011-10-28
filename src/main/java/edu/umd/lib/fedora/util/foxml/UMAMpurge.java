package src;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.InvalidXPathException;
import org.dom4j.XPath;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import edu.umd.lims.fedora.api.DigitalObject;
import edu.umd.lims.fedora.api.DigitalObjectFactory;
import edu.umd.lims.fedora.api.UMAMObject;
import fedora.client.FedoraClient;
import fedora.client.Uploader;
import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;


public class UMAMpurge {

	public Map namespace = new HashMap();
	
	public Map mXPath = new HashMap();

	DocumentFactory df = DocumentFactory.getInstance();
	
	public static final String CSV_PATTERN = "\\G(?:^|\t)(?:\"((?:[^\"]|\"\")*+)\"|([^\"\t]*))";
	
	private static Pattern csvRE;
	
	private static Logger log = Logger.getLogger(UMAMpurge.class);
    FedoraAPIA APIA;
    FedoraAPIM APIM;
    Uploader uploader;
    String pid;
    XMLWriter writer;
    OutputStreamWriter outWriter;
    static String strPrintType = "Terminal";
    static String strDir = "C:/Temp/Sweep/";
    static boolean bSaveUMDM = false;
    static boolean bSaveUMAM = false;
    static boolean bSaveMETS = false;
    static boolean bPrintScan = true;
    static boolean bUpdateUMAM = true;
    static boolean bUpdateUMDM = false;
    static boolean bUpdateMETS = false;
    int iRecordCounter = 0;
    int iUMDMcounter = 0;
    int iUMAMcounter = 0;
	
    /**************************************************************** init */
	/**
	 * Initialize the data structures of the class
	 */
    public void init() {
    	
    	log.info("Initializing scanning\n**********\n\n");

    	try {
    		
    		csvRE = Pattern.compile(CSV_PATTERN);
    		
    	    FedoraClient.FORCE_LOG4J_CONFIGURATION = false;
    	    FedoraClient fc = new FedoraClient("http://fedoratest.umd.edu:80/fedora", "fedoraAdmin", "0b1kn0b");
    	    
    	    namespace.put("oai_dc",
				"http://www.openarchives.org/OAI/2.0/oai_dc/");
    	    namespace.put("dc", "http://purl.org/dc/elements/1.1/");
    	    namespace.put("sparql",
				"http://www.w3.org/2001/sw/DataAccess/rf1/result");
    	    namespace.put("rdf",
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    	    namespace.put("mets", "http://www.loc.gov/METS/");
    	    namespace.put("xlink", "http://www.w3.org/1999/xlink");
    	    namespace.put("marc", "http://www.loc.gov/MARC21/slim");
    	    namespace.put("doInfo", "http://www.itd.umd.edu/fedora/doInfo");
    	    namespace.put("amInfo", "http://www.itd.umd.edu/fedora/amInfo");
    	    namespace.put("xml", "http://www.w3.org/XML/1998/namespace");
    	   
    	    APIA=fc.getAPIA();
    	    APIM=fc.getAPIM();
    	    uploader = new Uploader("http","fedoratest.umd.edu",80,"fedoraAdmin","0b1kn0b");
    	    OutputFormat format = OutputFormat.createPrettyPrint();
    	    writer = new XMLWriter(System.out, format);
    	    
    	    outWriter = new OutputStreamWriter( new FileOutputStream( strDir + "Purge-out.txt" ), "UTF-8" );
    	   
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}

    }
    

	
    /**************************************************************** process */
	/**
	 * Initialize the data structures of the class
	 */
    public void fProcess(BufferedReader in) {
		
		String line;
		List lRecord = new ArrayList();
		List lKeys = new ArrayList();
		HashMap hRecord = new HashMap();
		boolean bStillGood = true;
    	DigitalObject dobj;

		try{ 
		
			// For each line...
			while ( ( (line = in.readLine()) != null ) && bStillGood ) {
			
				//Use the regex to extract a list of items from the input line
				lRecord = parse(line);
				hRecord.clear();
			
				//Foreach item returned by the parser:
				for (int i = 0; i < lRecord.size(); i++) {
				
					if( iRecordCounter == 0 ) {
					
						//Add the current entry to the list of Keys
						lKeys.add(i, lRecord.get(i));
					
					} else {
					
						//Stuff the Hashmap:hRecord with the key value pair
						hRecord.put(lKeys.get(i), lRecord.get(i));
					
					}
				}
			
				if( iRecordCounter > 0 ) { 
				
					System.out.println("\nProcessing: " + hRecord.get("pid") );
				
	    		
					// get the digital object by pid
					dobj = DigitalObjectFactory.getDigitalObject(APIM,APIA,uploader,(String) hRecord.get("pid"));

					if( fTestUMDM( dobj ) ) {
						iUMDMcounter++;
					} else {
						System.out.println("Pid " + hRecord.get("pid") + " failed processing" );
					}
				
				}
			
				iRecordCounter++;
			}
		}
		
		catch(Exception e ) {
			 System.err.println("fProcess failed: " + e.getMessage() );
			 e.printStackTrace();
		}

    }
    
    /**************************************************************** fTestUMDM */
	/**
	 * Set the UMDM for a PID and return the success code
	 */
    private boolean fTestUMDM( DigitalObject oDObj ) {
    	
    	boolean bStillGood = true;
    	Document docUMDM = null;
    	Document docUMAM = null;
    	String strDocPID = "";
    	String strObjPID = "";
    	String strDocHandle = "";
    	String strObjHandle = "";
    	String strObjType = "";
    	String strFileName = "";
    	String strTitle = "";
    	String[] strCollections = {};
    	Node nThisNode = null;
    	
    	try{
    		
    		System.out.println("Getting the UMDM!!!");
    		
    		docUMDM = oDObj.getUMDM();
    		
    		// First lets look for the PID and handle values
    		strObjPID = oDObj.getPid();
    		strObjHandle = oDObj.getHandle();
    		strCollections = oDObj.getCollections();
    		
    		strTitle = getXPath("/descMeta/title[@type='main']")
			.selectSingleNode(docUMDM)
			.getText();
    		
    		System.out.println("Called: " + strTitle );
    		
    		strDocPID = getXPath("/descMeta/pid")
			.selectSingleNode(docUMDM)
			.getText();
    		
    		// strDocHandle 
    		
    		nThisNode = getXPath("/descMeta/identifier[@type='handle']")
			.selectSingleNode(docUMDM);
    		
    		if( nThisNode != null ) {
    			strDocHandle = nThisNode.getText();
    		} else {
    			System.out.println( "No Handle for " + strObjPID );
    			strDocHandle = "";
    		}
    		
    		strObjType = oDObj.getType();
    		
    		if( bPrintScan ) {
    			outWriter.write( "-----------------\n" );
    			outWriter.write( "PID: " + strObjPID + "\n" );
    			outWriter.write( "Handle: " + strObjHandle + "\n" );
    			outWriter.write( "Title: " + strTitle + "\n" );
    			
    			if( strObjType.equals("UMD_VIDEO") ) {
    				outWriter.write( "URL: http://fedoratest.umd.edu/public/video_standalone.jsp?pid=" + 
    					strObjPID + "\n" );
    			} else if( strObjType.equals("UMD_IMAGE") ) {
    				outWriter.write( "URL: http://fedoratest.umd.edu/public/image.jsp?pid=" + 
        					strObjPID + "\n" );
    			} else if( strObjType.equals("UMD_BOOK") ) {
    				outWriter.write( "URL: http://fedoratest.umd.edu/public//book.jsp?pid=" + 
        					strObjPID + "\n" );
    			} else {
    				outWriter.write( "URL: Unknown\n" );
    			}
    		}
    		
    		List lUMAMs = oDObj.getUMAMObjects();
    			
    		if( lUMAMs.size() > 0 ) {
    			
    			System.out.println( "UMAMS for " + strObjPID + ": " + lUMAMs.size() );
    			
    			for( Iterator iUMAMs = lUMAMs.iterator(); iUMAMs.hasNext();) {
					
					UMAMObject oUMAM = (UMAMObject) iUMAMs.next();
					
					docUMAM = oUMAM.getUMAM();
					
					String sUMAMpid = oUMAM.getPid();
					
					nThisNode = getXPath("/adminMeta/identifier")
	    			.selectSingleNode(docUMAM);
					
					
					if( nThisNode == null ) {
						// fPrintDoc( docUMAM, ( "UMAM-" + iRecordCounter ) );
						strFileName = "Unknown";
					} else {
						strFileName = nThisNode.getText();
						iUMAMcounter++;
					}
					
					System.out.println("UMAM Pid: " + sUMAMpid + ", File: " + strFileName );
					
					if( bPrintScan ) {
						outWriter.write("UMAM Pid: " + sUMAMpid + ", File: " + strFileName );
					}
					
					// Do any UMAM processing here
					if( bUpdateUMAM ) {
						oUMAM.purge();
						if( bPrintScan ) {
							outWriter.write(" - Deleted\n");
						}
					} else {
						if( bPrintScan ) {
							outWriter.write(" - To be Deleted\n");
						}
					}
    			}
    			
    		} else {
    			System.out.println("No UMAMs for " + strObjPID );
    		}
    		
    		
    		return bStillGood;
    	
    	}
    	catch( Exception e ) {
    		e.printStackTrace();
    	}
    	
    	return false;
    	
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
			xpath.setNamespaceURIs(namespace);
			mXPath.put(strXPath, xpath);
		}

		return xpath;
	}
	
	/* -------------------------------------------------------------- parse */
	/** Parse one line.
	 * @return List of Strings, minus their double quotes
	 * 
	 */
	public List parse(String line) {
		List list = new ArrayList();
		Matcher m = csvRE.matcher(line);
		Matcher mQuote = Pattern.compile("\"\"").matcher("");
		
		/*
		 * The Regex above: \\G(?:^|\t)(?:\"((?:[^\"]*+|\"\")*+)\"|([^\"\t]*))
		 * has only 2 groups: one for quoted and one for unquoted fields.
		 * Group 1: Quoted Fields (with and without embedded double quotes)
		 * Group 2: Unquoted fields (numeric and null fields).
		 * If the Regex is changed, the clauses below will need to be altered
		 */
		
		// For each field found in the CSV file:
		while (m.find()) {
			String match;
			
			if( m.start(2) >= 0 ) {
				//It is unquoted
				match = m.group(2);
			} else {
				//It is quoted and may contain double quotes 
				//that must be changed to single quotes
				match = mQuote.reset(m.group(1)).replaceAll("\"");
			}
			
			//Stuff the string into the list (now that it has been purtified).
			list.add(match);
			
		}
		
		//Send the list back to the user.
		return list;
	}
    
	/************************************************************* getXPath */
	/**
	 * Get a compiled XPath object for the expression.  Cache.
	 */

	private void finish() {

		try {
			outWriter.close();
		}
		
    	catch( Exception e ) {
    		e.printStackTrace();
    	}
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Document docUMDM = null;
		Document docNewUMDM = null;
		
		UMAMpurge oPurger = new UMAMpurge();
		
		try {
			
			PropertyConfigurator.configure("log4j.conf");
			log.info("logging initialized");
			
			oPurger.init();
			
			if (args.length == 0) {	// read standard input
				BufferedReader is = new BufferedReader(
					new InputStreamReader(System.in));
				oPurger.fProcess(is);
			} else { //There is a file to read -- so go out and get it
				for (int i=0; i<args.length; i++) {
					
					/*
					 * The input file used is assumed to be a 
					 * tab delimited, UTF-8 text file 
					 * with 3 or more fields - in the form:
					 * 
					 * descMetaID	pid	handle
					 * 5	Dummy:	DummyHandle
					 * 10	umd:10481	hdl:1903.1.DEV/4898
					 * 
					 * The headers must appear as above
					 * There could be more but these are the ones that 
					 * are used here.
					 * 
					 * All fields are evaluated as text and enclosing
					 * quotes are stripped away.
					 * 
					 */
					
					//oPurger.process(new BufferedReader(new FileReader(args[i])));
					oPurger.fProcess(new BufferedReader(new InputStreamReader(new FileInputStream(args[i]), "UTF-8")));
					//FileInputStream
				}
			}
			
			System.out.println("\n\nUMDM Records processed: " + String.valueOf( ( oPurger.iUMDMcounter ) ) );
			System.out.println("UMAM Records Processed: " + String.valueOf( ( oPurger.iUMAMcounter) ) );
			
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
	}

}
