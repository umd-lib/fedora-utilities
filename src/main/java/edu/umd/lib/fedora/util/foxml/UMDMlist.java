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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.InvalidXPathException;
import org.dom4j.XPath;
import org.dom4j.Node;
import org.dom4j.Element;
import org.dom4j.Attribute;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;

import java.net.URL;
import edu.umd.lims.fedora.api.DigitalObject;
import edu.umd.lims.fedora.api.DigitalObjectFactory;
import edu.umd.lims.fedora.api.UMAMObject;
import edu.umd.lims.util.*;
import fedora.client.FedoraClient;
import fedora.client.Uploader;
import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;

import src.Configurator;

public class UMDMlist {

	public Map namespace = new HashMap();

	public Map mXPath = new HashMap();

	public static final String CSV_PATTERN = "\\G(?:^|\t)(?:\"((?:[^\"]|\"\")*+)\"|([^\"\t]*))";

	private static Pattern csvRE;

	private int waitTime = 5000;

	DocumentFactory df = DocumentFactory.getInstance();

	private static Logger log = Logger.getLogger(UMDMlist.class);

	FedoraAPIA APIA;

	FedoraAPIM APIM;

	Uploader uploader;

	String pid;

	XMLWriter writer;

	OutputStreamWriter outWriter;

	OutputStreamWriter outWriter2;

	OutputStreamWriter UMDMWriter;

	OutputStreamWriter UMAMWriter;

	OutputStreamWriter CustomWriter;

	static String strPrintType = "File";

	String strSaveDir = "C:/Temp/Sweep/";

	String strLogDir = "C:/Temp/Sweep/";

	String strListTitle = "";

	// strServer may have the values Dev, Test, or Live at the moment
	static final String strServer = "Live";

	String strCollection = "";

	//String strCollection = "umd:2"; //Worlds Fair - Test and Live
	//String strCollection = "umd:1158"; //Films -  Test and Live
	String strPidFile = "";

	/* These boolean variables determine which datastreams get saved
	 * and which get altered.
	 * They also determine whether the UMAMs are included in the act.
	 */
	boolean bScanUMAMs = true;

	boolean bSaveUMDM = true;

	boolean bSaveUMAM = true;

	boolean bSaveMETS = true;

	boolean bPrintScan = true;

	boolean bNewUMAM = false;

	boolean bUpdateUMAM = false;

	String strUMAMstatus = "";

	boolean bCompleteUMAM = false;

	boolean bNewUMDM = false;

	boolean bUpdateUMDM = false;

	boolean bCompleteUMDM = false;

	String strUMDMstatus = "";

	boolean bForceCompletion = false;

	boolean bNewMETS = false;

	boolean bUpdateMETS = false;

	boolean bPrintPidList = true;

	String sTitle = "";

	long iLineCounter = 0;

	int iUMDMCounter = 0;

	int iUMAMCounter = 0;

	int iVideoCounter = 0;

	int iImageCounter = 0;

	int iBookCounter = 0;

	int iPageCounter = 0;

	int iTEICounter = 0;

	HashMap hTestCounts = new HashMap();

	HashMap hTestPids = new HashMap();

	private static final int iProcessed = 0;

	private static final int iToChange = 1;

	private static final int iDontChange = 2;

	private static final int iChanged = 2;

	private static final int iCategories = 4;

	private String strProtocol;

	private String strHost;

	private String strPort;

	private String strUser;

	private String strPasswd;

	private int nPort;

	/**************************************************************** init */
	/**
	 * Initialize the data structures of the class
	 */
	public void init(String strConfigFile) {

		log.info("Initializing scanning\n**********\n\n");

		Configurator cConfiguration = new Configurator(strConfigFile);

		if (cConfiguration == null) {
			log.debug("Configurator failed");
		}

		csvRE = Pattern.compile(CSV_PATTERN);

		strProtocol = cConfiguration.getProperty("connection", "protocol");
		strHost = cConfiguration.getProperty("connection", "host");
		strPort = cConfiguration.getProperty("connection", "port");
		strUser = cConfiguration.getProperty("connection", "user");
		strPasswd = System.getenv("FEDORA_PASSWD");

		strSaveDir = cConfiguration.getProperty("location", "saveBase");
		strLogDir = cConfiguration.getProperty("location", "logBase");

		log.info("Protocol: " + strProtocol);
		log.info("Host: " + strHost);
		log.info("Port: " + strPort);
		log.info("User: " + strUser);

		if ((strPasswd == null) || (strPasswd.length() < 1)) {
			log.debug("FEDORA_PASSWD must be set.");
			System.out.println("FEDORA_PASSWD must be set.");
			System.exit(0);
		}

		nPort = Integer.parseInt(strPort);

		// Set the booleans
		bScanUMAMs = cConfiguration.getProperty("action", "scanUMAMs")
				.equalsIgnoreCase("Y");
		bSaveUMDM = cConfiguration.getProperty("action", "saveUMDM")
				.equalsIgnoreCase("Y");
		bSaveUMAM = cConfiguration.getProperty("action", "saveUMAM")
				.equalsIgnoreCase("Y");
		bSaveMETS = cConfiguration.getProperty("action", "saveMETS")
				.equalsIgnoreCase("Y");
		bPrintScan = cConfiguration.getProperty("action", "printScan")
				.equalsIgnoreCase("Y");
		bNewUMAM = cConfiguration.getProperty("action", "newUMAM")
				.equalsIgnoreCase("Y");
		bUpdateUMAM = cConfiguration.getProperty("action", "updateUMAM")
				.equalsIgnoreCase("Y");
		bCompleteUMAM = cConfiguration.getProperty("action", "completeUMAM")
				.equalsIgnoreCase("Y");
		bNewUMDM = cConfiguration.getProperty("action", "newUMDM")
				.equalsIgnoreCase("Y");
		bUpdateUMDM = cConfiguration.getProperty("action", "updateUMDM")
				.equalsIgnoreCase("Y");
		bForceCompletion = cConfiguration.getProperty("action",
				"forceCompletion").equalsIgnoreCase("Y");
		bNewMETS = cConfiguration.getProperty("action", "newMETS")
				.equalsIgnoreCase("Y");
		bUpdateMETS = cConfiguration.getProperty("action", "updateMETS")
				.equalsIgnoreCase("Y");
		bPrintPidList = cConfiguration.getProperty("action", "printPidList")
				.equalsIgnoreCase("Y");
		waitTime = Integer.parseInt(cConfiguration
				.getProperty("action", "wait"));

		strUMDMstatus = cConfiguration.getProperty("action", "statusUMDM");
		if (strUMDMstatus.equals("Complete")
				|| strUMDMstatus.equals("Incomplete")
				|| strUMDMstatus.equals("Pending")) {
			bCompleteUMDM = true;
		} else {
			bCompleteUMDM = false;
		}

		strUMAMstatus = cConfiguration.getProperty("action", "statusUMAM");
		if (strUMAMstatus.equals("Complete")
				|| strUMAMstatus.equals("Incomplete")
				|| strUMAMstatus.equals("Pending")) {
			bCompleteUMAM = true;
		} else {
			bCompleteUMAM = false;
		}

		strCollection = cConfiguration.getProperty("process", "collection");
		strPidFile = cConfiguration.getProperty("process", "pidFile");

		if ((strCollection.length() < 1) && (strPidFile.length() < 1)) {
			log.debug("Collection or Pid file name must exist.");
			//System.out.println("Collection or Pid file name must exist.");
			System.exit(0);
		}

		try {
			FedoraClient fc = new FedoraClient( strProtocol + "://" + strHost + ":" + strPort + "/fedora", strUser, strPasswd);

			namespace.put("oai_dc",
					"http://www.openarchives.org/OAI/2.0/oai_dc/");
			namespace.put("dc", "http://purl.org/dc/elements/1.1/");
			namespace.put("sparql",
					"http://www.w3.org/2001/sw/DataAccess/rf1/result");
			namespace.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			namespace.put("mets", "http://www.loc.gov/METS/");
			namespace.put("xlink", "http://www.w3.org/1999/xlink");
			namespace.put("marc", "http://www.loc.gov/MARC21/slim");
			namespace.put("doInfo", "http://www.itd.umd.edu/fedora/doInfo");
			namespace.put("amInfo", "http://www.itd.umd.edu/fedora/amInfo");
			namespace.put("xml", "http://www.w3.org/XML/1998/namespace");
			namespace.put("col",
					"http://www.fedora.info/definitions/1/0/types/");

			APIA=fc.getAPIA();
			APIM=fc.getAPIM();
			uploader = new Uploader(strProtocol,strHost,nPort,strUser,strPasswd);
			OutputFormat format = OutputFormat.createPrettyPrint();
			writer = new XMLWriter(System.out, format);

			outWriter = new OutputStreamWriter(new FileOutputStream(strLogDir
					+ "Sweep-out.txt"), "UTF-8");

			outWriter2 = new OutputStreamWriter(new FileOutputStream(strLogDir
					+ "Sweep-list.txt"), "UTF-8");
			outWriter2.write(strListTitle
					+ "\npid\tocm\taleph_sys\tdescMetaID\ttitle\tstatus\n");

			UMDMWriter = new OutputStreamWriter(new FileOutputStream(strLogDir
					+ "UMDMsource.txt"), "UTF-8");
			UMDMWriter.write("descMetaID\tpid\thandle\tobjectType\n");

			UMAMWriter = new OutputStreamWriter(new FileOutputStream(strLogDir
					+ "UMAMsource.txt"), "UTF-8");
			UMAMWriter
					.write("descMetaID\tsequence\tpid\tfileName\tobjectType\tUMAMfile\timageType\n");

			CustomWriter = new OutputStreamWriter(new FileOutputStream(
					strLogDir + "Custom.txt"), "UTF-8");
			CustomWriter.write("browseValue\ttype\tlabel\tpid\n");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**************************************************************** process */
	/**
	 * Initialize the data structures of the class
	 */
	public void fProcessCollection(String strCollectionPid) {

		DigitalObject dobj;
		String strPid = "";
		String strTitle = "";
		Element eElement = null;
		HashMap hRecord = new HashMap();
		String strCollRec;
		int inCounter = 0;

		SAXReader reader = new SAXReader();

		try {

			// Get the List of Pids
			// http://fedoratest.umd.edu/fedora/get/umd:2/umd-bdef:rels/hasCollectionMember/
			if (strServer.equals("Test")) {
				strCollRec = "http://fedoratest.umd.edu/fedora/get/";
			} else if (strServer.equals("Dev")) {
				strCollRec = "http://fedoradev.umd.edu/fedora/get/";
			} else {
				strCollRec = "http://fedora.umd.edu/fedora/get/";
			}

			strCollRec += strCollectionPid
					+ "/umd-bdef:rels/hasCollectionMember/";
			URL wMemberList = new URL(strCollRec);
			Document doc = reader.read(wMemberList);

			/*OutputFormat format = OutputFormat.createPrettyPrint();
			 XMLWriter writer = new XMLWriter( System.out, format );
			 writer.write( doc );
			 */

			List lPids = getXPath(
					"/col:result/col:resultList/col:rel/col:objectFields")
					.selectNodes(doc);

			// For each line...
			for (Iterator i = lPids.iterator(); i.hasNext();) {

				strPid = "";
				strTitle = "";

				eElement = (Element) i.next();
				strPid = eElement.getText();

				strPid = getXPath("col:pid").selectSingleNode(eElement)
						.getText();

				strTitle = getXPath("col:title").selectSingleNode(eElement)
						.getText();

				try {
					FedoraClient fc = new FedoraClient(strProtocol + "://"
							+ strHost + ":" + strPort + "/fedora", strUser,
							strPasswd);
					APIA = fc.getAPIA();
					APIM = fc.getAPIM();
					uploader = new Uploader(strProtocol, strHost, nPort,
							strUser, strPasswd);
					dobj = DigitalObjectFactory.getDigitalObject(APIM, APIA,
							uploader, strPid);

					iUMDMCounter++;

					if (strTitle.length() > 0 && !fProcessUMDM(dobj)) {
						log.info("Pid " + strPid + " failed processing");
					}

				}

				catch (Exception e) {
					e.printStackTrace();
					if (bPrintScan) {
						outWriter.write("-----------------\n");
						outWriter.write("PID: " + strPid + "\n");
						outWriter.write("Does not seem to exist.\n");
					}

				}

				if (bUpdateUMDM) {
					Thread.sleep(waitTime); // Pause
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		//System.out.println("Objects found " + inCounter);

	}

	/**************************************************************** process */
	/**
	 * Read the input file of PIDs and process each PID individually.
	 * The file is assumes to be a simple list of pids and each pid is
	 * assumed to be a uMDM pid.
	 * 
	 * @author Paul Hammer and Dave Kennedy.
	 * 
	 */
	public void fProcess(BufferedReader in) {

		DigitalObject dobj;
		UMAMObject amObj = null;
		String strPid = "";
		String strType = "";
		String strTitle = "";
		String strID = "";
		String strCollection = "";
		String strWhat = "None";
		String strDescMetaID = "";

		int retry_limit = 3;
		int error_limit = 5;
		int num_errors = 0;

		try {
			// For each line...
			while ((strPid = in.readLine()) != null) {

				int num_retries = 0;
				boolean success = false;
				while (!success && strPid.startsWith("umd") ) {
					success = true;
					try {
						//System.out.print(strPid);
						dobj = DigitalObjectFactory.getDigitalObject(APIM,
								APIA, uploader, strPid);
						
						if (dobj != null && dobj.hasEnoughContent()) {
							
							if( dobj.hasUMDM() ) {
								strWhat = "UMDM";
							} else {
								
								amObj = DigitalObjectFactory.getUMAMObject(APIM, APIA, uploader, strPid);
								
								if( amObj.hasUMAM() ) {
									strWhat = "UMAM";
								} else {
									strWhat = "NOT";
								}
							}
							

							if( strWhat.equals("UMDM" ) ) {
								
								strType = dobj.getType();
								strCollection = dobj.getCollections()[0];
								Document docUMDM = dobj.getUMDM();
								strTitle = getTitle(docUMDM, strPid);
								strID = getPrangeID(docUMDM, strPid);
								System.out.println(strPid + 
										"\t" + strType + "\t" +
										strCollection + "\t" +
										strTitle + "\t" +
										strID
										);
								
								if( strID != null && strID.length() > 0 ) {
									strDescMetaID = strID;
								} else {
									strDescMetaID = strPid;
								}
								
								fProcessUMDM( dobj );
								iUMDMCounter++;
								
								/*if( bSaveMETS ) {
									Document docMETS = dobj.getMets();
									String fileName = strSaveDir + "METS/mets_" + strDescMetaID + ".xml";
									fPrintDoc(docMETS, fileName);
									
								}

								if( bSaveUMDM ) {
									String fileName = strSaveDir + "UMDM/umdm_" + strDescMetaID + ".xml";
									fPrintDoc( docUMDM, fileName ) ;
								}*/
							} else if( strWhat.equals( "UMAM" ) ) {
								strType = dobj.getType();
								Document docUMAM = amObj.getUMAM();
								String strFile = getFile(docUMAM, strPid);
								System.out.println(strPid + 
										"\t" + strType + "\t" +
										strFile
										);

								if( bSaveUMAM ) {
									String fileName = strSaveDir + "UMAM/umam_" + strDescMetaID + ".xml";
									fPrintDoc( docUMAM, fileName ) ;
								}
							}
							
						} else {
							log.info("Pid " + strPid
									+ " has insufficient Content!");
							////System.out.println("Pid " + strPid
									//+ " has insufficient Content!");
						}

					} catch (Exception e) {
						success = false;
						num_retries++;
						log.debug("Attempt #" + num_retries + " failed");
						// e.printStackTrace();
						if (num_retries >= retry_limit) {
							success = true;
							//num_errors++;
							if (bPrintScan) {
//								outWriter.write("-----------------\n");
//								outWriter.write("PID: " + strPid + "\n");
//								outWriter.write("Does not seem to exist.\n");
								//System.out.println("\tUnknown\tUnknown\tUnknown\tNOT" );
							}
						}
					}
				}

				if (num_errors == error_limit) {
					log
							.error("Quitting because maximum number of errors has been reached");
					break;
				}
				
				Thread.sleep(3000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**************************************************************** PrintDoc */
	/**
	 * Prints a document based on the class's printing directive string
	 */
	private boolean fPrintDoc(Document docThisDocument, String strFile) {

		boolean bTerminalOut = false;
		boolean bFileOut = false;

		if (strPrintType.equals("Terminal")) {
			bTerminalOut = true;
		}

		if (strPrintType.equals("File") || bSaveUMDM || bSaveUMAM) {
			bFileOut = true;
		}

		if (strPrintType.equals("Both")) {
			bTerminalOut = true;
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
				//System.out.println("File Name is " + strFile);
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

	/**************************************************************** fTestUMDM */
	/**
	 * Set the UMDM for a PID and return the success code
	 */
	private boolean fProcessUMDM(DigitalObject oDObj) {

		boolean bStillGood = true;
		Document docUMDM = null;
		Document docMETS = null;
		String strObjPID = "";
		String strObjHandle = "";
		String strURL = "";
		String strTitle = "";
		String strObjType = "";
		String strCollection = "";
		String strAlephSys = "";
		String strOCMno = "";
		String strPrangeID = "";
		String[] strCollections = {};
		String strObjectType = "";

		try {

			// First lets look for the PID and handle values
			strObjPID = oDObj.getPid();
		    //log.info("Processing UMDM - Pid: " + strObjPID );
		    strObjHandle = oDObj.getHandle();
		    strCollections = oDObj.getCollections();
		    strObjectType = oDObj.getTypeText();

			//System.out.println("UMDM - Pid: " + strObjPID);

			String strTempString = strObjPID.replaceAll(":", "_");

			if (bNewUMDM) {
				String strNewUMDMfile = strSaveDir + "NewUMDM/" + strTempString
						+ ".xml";
				SAXReader rRefReader = new SAXReader();
				InputSource isRef = new InputSource(new InputStreamReader(
						new FileInputStream(strNewUMDMfile), "UTF-8"));

				// Read the input file, Set the resulting document for return.
				if ((rRefReader != null) && (isRef != null)) {
					docUMDM = rRefReader.read(isRef);
				}
			} else {
				docUMDM = oDObj.getUMDM();
			}

			strTitle = getTitle(docUMDM, strObjPID);
			strOCMno = getOCM(docUMDM, strObjPID);
			strAlephSys = getAlephSys(docUMDM, strObjPID);
			strPrangeID = getPrangeID(docUMDM, strObjPID);

			// Process Books and Images and Films
			if (oDObj.isUMDMEditable()) {

				strCollection = strCollections[0];

				log.debug("about to test umdm " + strObjPID);

				// Set the UMDM back to the repository
				if (bUpdateUMDM && (!bStillGood)) {
					log.debug("about to set umdm");
					oDObj.setUMDM(docUMDM);
					log.debug("done setting umdm");
					bStillGood = true;
				}

				if (bPrintScan) {
					outWriter.write("-----------------\n");
					outWriter.write("PID: " + strObjPID + "\n");
					outWriter.write("Handle: " + strObjHandle + "\n");
					outWriter.write("Title: " + strTitle + "\n");
					outWriter.write("Collection: " + strCollection + "\n");
					outWriter.write("Type: " + strObjType + "\n");

					if (strObjHandle.length() > 0) {
						strURL = strObjHandle.replaceAll("hdl:",
								"http://hdl.handle.net/");
						outWriter.write("URL: " + strURL + "\n");
					} else {
						outWriter.write("URL: Unknown\n");
					}

					if (strOCMno.length() > 0) {
						outWriter.write("OCM: " + strOCMno + "\n");
					}

					if (strAlephSys.length() > 0) {
						outWriter.write("Aleph System Number: " + strAlephSys
								+ "\n");
					}

					// Print the line to the list as well
					outWriter2.write(strObjPID + "\t" + strOCMno + "\t"
							+ strAlephSys + "\t" + strPrangeID + "\t"
							+ strTitle);
				}

				if (bSaveUMDM) {
					fPrintDoc(docUMDM,
							(strSaveDir + "UMDM/" + strObjPID + ".xml"));
				}

				// Save UMDM to the source list for the upload to Live
				UMDMWriter.write(strObjPID + "\tDummy\tDummyHandle" + "\t"
						+ strObjectType + "\n");

				// Get and possibly save the METS record
				if (bSaveMETS) {
					docMETS = oDObj.getMets();
					if (docMETS != null) {
						fPrintDoc(docMETS, (strSaveDir + "METS/mets_"
								+ strObjPID + ".xml"));
					}
				}

				if (bScanUMAMs) {
					fProcessUMAM(oDObj);
				}

				// Now we Test the DOinfo

				String sObjStatus = oDObj.getStatus();

				if (sObjStatus.equalsIgnoreCase("Complete")) {
					fSetAccumulator("incompleteUMDM", strObjPID, true, false);
					fSetAccumulator("pendingUMDM", strObjPID, true, false);
				} else if (sObjStatus.equalsIgnoreCase("Incomplete")) {
					fSetAccumulator("incompleteUMDM", strObjPID, false, false);
					fSetAccumulator("pendingUMDM", strObjPID, true, false);
				} else if (sObjStatus.equalsIgnoreCase("Pending")) {
					fSetAccumulator("incompleteUMDM", strObjPID, false, false);
					fSetAccumulator("pendingUMDM", strObjPID, false, false);
				}

				String sDocStatus = "";

				if (bStillGood && bCompleteUMDM) {
					sDocStatus = strUMDMstatus;
				} else {
					sDocStatus = sObjStatus;
				}

				oDObj.checkAndUpdateStatus(sDocStatus);

				sObjStatus = oDObj.getStatus();

				if (!sObjStatus.equals("Complete")) {
					List lProblems = oDObj.validateUMDM();
					outWriter.write("+++++++++++++++++++++++++++\n");
					outWriter.write("UMDM-Validation Problems:\n");
					for (Iterator p = lProblems.iterator(); p.hasNext();) {
						outWriter.write(p.next() + "\n");
					}
					outWriter.write("+++++++++++++++++++++++++++\n");
				}

				if (bForceCompletion) {
					oDObj.setStatus("Complete");
					sObjStatus = oDObj.getStatus();
				}

				if (bPrintScan) {
					outWriter.write("DoInfo status: " + sObjStatus + "\n");
					outWriter2.write("\t" + sObjStatus + "\n");
				}

				System.out.println();

			} else {
				System.out.println();
				fSetAccumulator("non-ImageVideo", strObjPID, true, true);
			}

			return bStillGood;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}
	
	private String getTitle( Document docUMDM, String strPid ) {
		
		String strTitle = "";
		
		List lTitles = getXPath("/descMeta/title[@type='main']")
		.selectNodes(docUMDM);
		
		Node oThisNode = null;
		
		for( Object oThisOne : lTitles) {
			
			oThisNode = (Node) oThisOne;
	
			if (oThisNode != null) {
				if( strTitle.length() > 0 ) {
					strTitle = strTitle.concat(", ");
				}
				strTitle = strTitle.concat(oThisNode.getText());
				if (strTitle.length() > 0) {
					fSetAccumulator("missingTitle", strPid, true, false);
				} else {
					fSetAccumulator("missingTitle", strPid, false, false);
				}
	
			} else {
				strTitle = "";
				fSetAccumulator("missingTitle", strPid, false, false);
			}
			
		}
		
		return strTitle;
	}

	private String getOCM( Document docUMDM, String strPid ) {
		
		String strOCM = "";
		Node oThisNode = getXPath("/descMeta/identifier[@type='oclc']")
				.selectSingleNode(docUMDM);
	
		if (oThisNode != null) {
			strOCM = oThisNode.getText();
		} else {
			strOCM = "";
		}
		
		return strOCM;
	}

	private String getAlephSys( Document docUMDM, String strPid ) {
		
		String strAlephSys = "";
		Node oThisNode = getXPath("/descMeta/identifier[@type='aleph']")
				.selectSingleNode(docUMDM);
	
		if (oThisNode != null) {
			strAlephSys = oThisNode.getText();
		} else {
			strAlephSys = "";
		}
	
		return strAlephSys;
	}

	private String getPrangeID( Document docUMDM, String strPid ) {
		
		String strPrangeID = "";
		Node oThisNode = getXPath("/descMeta/identifier[@label='prange']")
				.selectSingleNode(docUMDM);

		if (oThisNode != null) {
			strPrangeID = oThisNode.getText();
		} else {
			strPrangeID = "";
		}

		return strPrangeID;
	}

	private String getFile( Document docUMDM, String strPid ) {
		
		String strFile = "";
		
		List lTitles = getXPath("/adminMeta/identifier")
		.selectNodes(docUMDM);
		
		Node oThisNode = null;
		
		for( Object oThisOne : lTitles) {
			
			oThisNode = (Node) oThisOne;
	
			if (oThisNode != null) {
				if( strFile.length() > 0 ) {
					strFile = strFile.concat(", ");
				}
				strFile = strFile.concat(oThisNode.getText());
				if (strFile.length() > 0) {
					fSetAccumulator("missingFile", strPid, true, false);
				} else {
					fSetAccumulator("missingFile", strPid, false, false);
				}
	
			} else {
				strFile = "";
				fSetAccumulator("missingFile", strPid, false, false);
			}
			
		}
		
		return strFile;
	}

	/**************************************************************** fTestUMDM */
	/**
	 * Set the UMDM for a PID and return the success code
	 */
	private void fProcessUMAM(DigitalObject oDObj) {

		boolean bStillGood = true;
		boolean bUpdated = false;
		Document docUMAM = null;
		String strObjPid = "";
		String strFileName = "";
		String strUMAMtype = "";
		String strUMAMPid = "";
		Node nThisNode = null;
		int iUMDMUMAMcounter = 0;
		UMAMObject oUMAM;

		try {

			List lUMAMs = oDObj.getUMAMObjects();
			strObjPid = oDObj.getPid();

			if (lUMAMs.size() > 0) {

				for (Iterator iUMAMs = lUMAMs.iterator(); iUMAMs.hasNext();) {

					oUMAM = null;
					oUMAM = (UMAMObject) iUMAMs.next();

					strUMAMtype = oUMAM.getType();

					strUMAMPid = oUMAM.getPid();

					//System.out.println("  UMAM - Pid: " + strUMAMPid);

					bUpdated = false;

					bStillGood = true;

					if (oUMAM.hasUMAM()) {
						docUMAM = oUMAM.getUMAM();

						nThisNode = getXPath("/adminMeta/identifier")
								.selectSingleNode(docUMAM);

						if (nThisNode == null) {
							// fPrintDoc( docUMAM, ( "UMAM-" + iRecordCounter )
							// );
							strFileName = "Unknown";
						} else {
							strFileName = nThisNode.getText();
							iUMDMUMAMcounter++;
						}
					} else {
						strFileName = "None";
					}

					if (bPrintScan) {
						outWriter.write("UMAM Pid: " + strUMAMPid + ", File: "
								+ strFileName + "\n");
					}

					if (strUMAMtype.equals("UMD_VIDEO")) {
						iVideoCounter++;
					} else if (strUMAMtype.equals("UMD_IMAGE")) {
						iImageCounter++;
					} else if (strUMAMtype.equals("UMD_BOOK")) {
						iBookCounter++;
					} else if (strUMAMtype.equals("UMD_TEI")) {
						iTEICounter++;
					}

					// Do any UMAM processing here
					if (bUpdateUMAM && !bStillGood) {
						oUMAM.setUMAM(docUMAM);
						bUpdated = true;
					}
					if (bSaveUMAM) {
						fPrintDoc(docUMAM, (strSaveDir + "UMAM/" + strObjPid
								+ "-" + iUMDMUMAMcounter + ".xml"));
					}

					String strNewUMAMfile = strSaveDir + "UMAM/" + strObjPid
							+ "-" + iUMDMUMAMcounter + ".xml";

					strNewUMAMfile = strNewUMAMfile.replace(":", "_");
					String strTempPid = strObjPid.replace(':', '_');

					String strImageType = "";
					if (strFileName.contains("jpg")) {
						strImageType = "DISPLAY";
					} else if (strFileName.contains("tif")) {
						strImageType = "MASTER";
					} else {
						strImageType = "MASTER";
					}

					// print this UMAM out to the uMAM list for upload to Live
					UMAMWriter.write(strObjPid + "\t" + iUMDMUMAMcounter + "\t"
							+ "Dummy" + "\t" + strFileName + "\t" + strUMAMtype
							+ "\t" + strSaveDir + "UMAM/" + strTempPid + "-"
							+ iUMDMUMAMcounter + ".xml" + "\t" + strImageType
							+ "\n");

					// Update the UMAM status
					Document dAMInfo = oUMAM.getAMInfo();

					String sObjStatus = getXPath("/amInfo:amInfo/amInfo:status")
							.selectSingleNode(dAMInfo).getText();

					String sDocStatus = "";

					if (bStillGood && bCompleteUMAM) {
						sDocStatus = strUMAMstatus;
					} else {
						sDocStatus = sObjStatus;
					}

					Node nTemp = getXPath("/amInfo:amInfo/amInfo:status")
							.selectSingleNode(dAMInfo);

					if (nTemp != null) {
						nTemp.setText(sDocStatus);
						//oUMAM.setStatus(sDocStatus);
						oUMAM.setAMInfo(dAMInfo);
					}

					iUMAMCounter++;

					if (bUpdateUMAM) {
						Thread.sleep(waitTime); // Pause
					}
				}
				fSetAccumulator("anyUMAMs", strObjPid, true, false);

			} else {
				fSetAccumulator("anyUMAMs", strObjPid, false, false);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

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

			if (m.start(2) >= 0) {
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

	/************************************************************* getXPath */
	/**
	 * Get a compiled XPath object for the expression.  Cache.
	 */

	private void finish() {

		int[] aTestCount;

		try {

			// create a new writer for the aggregates.
			OutputStreamWriter aggregateWriter = new OutputStreamWriter(
					new FileOutputStream(strLogDir + "Sweep-aggregate.txt"),
					"UTF-8");

			aggregateWriter.write("\n-----------------------------------\n"
					+ strListTitle + "\n");

			aggregateWriter.write("Processed: " + iUMDMCounter + " UMDMs\n");

			if (bScanUMAMs) {

				aggregateWriter
						.write("Processed: " + iUMAMCounter + " UMAMs\n");

				if (iVideoCounter > 0) {
					aggregateWriter.write("Processed: " + iVideoCounter
							+ " Videos\n");
				}
				if (iImageCounter > 0) {
					aggregateWriter.write("Processed: " + iImageCounter
							+ " Images\n");
				}
				if (iBookCounter > 0) {
					aggregateWriter.write("Processed: " + iBookCounter
							+ " Books\n");
				}
				if (iPageCounter > 0) {
					aggregateWriter.write("Processed: " + iPageCounter
							+ " Book Pages\n");
				}
				if (iTEICounter > 0) {
					aggregateWriter.write("Processed: " + iTEICounter
							+ " TEIs\n");
				}

			}

			Set sTestCounts = hTestCounts.keySet();

			for (Iterator iTestCounts = sTestCounts.iterator(); iTestCounts
					.hasNext();) {

				String strKey = (String) iTestCounts.next();

				aTestCount = (int[]) hTestCounts.get(strKey);
				aggregateWriter.write("-----------------------------------\n");
				aggregateWriter.write("Examined: " + strKey + "\n");
				aggregateWriter.write("Processed: " + aTestCount[iProcessed]
						+ "\n");
				aggregateWriter.write("Don't need to change: "
						+ aTestCount[iDontChange] + "\n");
				aggregateWriter.write("Need to Change: "
						+ aTestCount[iToChange] + "\n");
				aggregateWriter
						.write("Changed: " + aTestCount[iChanged] + "\n");

				if (bPrintPidList) {
					ArrayList aPids = (ArrayList) hTestPids.get(strKey);

					if (aPids.size() > 0) {
						for (int i = 0; i < aPids.size(); i++) {
							if (i == 0) {
								aggregateWriter
										.write("Here are the Pids needing change: ");
							} else {
								aggregateWriter.write(", ");
							}

							aggregateWriter.write((String) aPids.get(i));
						}

						aggregateWriter.write("\n");
					}
				}

			}

			aggregateWriter.write("-----------------------------------\n");

			aggregateWriter.close();
			outWriter.close();
			outWriter2.close();
			UMAMWriter.close();
			UMDMWriter.close();
			CustomWriter.close();

		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}

	/************************************************************* fSetAccumulator */
	/**
	 * Adjust the totals in the global accumulators to print out later
	 */

	private void fSetAccumulator(String strItemName, String strPid,
			boolean bPassed, boolean bUpdated) {

		ArrayList aPids = null;
		int[] aItemCount = new int[iCategories];

		if (hTestCounts.get(strItemName) != null) {
			aItemCount = (int[]) hTestCounts.get(strItemName);
			aPids = (ArrayList) hTestPids.get(strItemName);
		} else {
			aItemCount[iProcessed] = 0;
			aItemCount[iToChange] = 0;
			aItemCount[iDontChange] = 0;
			aItemCount[iChanged] = 0;
			aPids = new ArrayList();
		}

		aItemCount[iProcessed]++;

		if (bPassed) {
			aItemCount[iDontChange]++;
		} else {
			if (bUpdated) {
				aItemCount[iChanged]++;
			} else {
				aItemCount[iToChange]++;
				aPids.add(strPid);
			}
		}

		hTestCounts.put(strItemName, aItemCount);
		hTestPids.put(strItemName, aPids);

	}

	/**
	 * @param Pid File
	 * @author Paul Hammer
	 * Date - 2008/04/10
	 * 
	 * This class takes a list of Fedora Pids and
	 * examines the datastreams of each one.
	 * It can be programmed to filter, save, or alter
	 * datastreams.  Currently it uses primarily the UMDM
	 * and associated UMAMs
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String strPID = null;
		String strControlNo = null;
		String strHandle = null;
		String strTitle = null;
		Document docUMDM = null;
		Document docNewUMDM = null;

		try {

			//OutputFormat format = OutputFormat.createPrettyPrint();
			//XMLWriter writer = new XMLWriter( outWriter, format );

			// writer.write( docThisDocument );
			// writer.close();

			PropertyConfigurator.configure("log4j.conf");
			log.info("logging initialized");

			UMDMlist oSweeper = new UMDMlist();
			oSweeper.init(args[0]);

			if (oSweeper.strCollection.length() > 0) {
				oSweeper.fProcessCollection(oSweeper.strCollection);
			} else {
				oSweeper.fProcess(new BufferedReader(new InputStreamReader(
						new FileInputStream(oSweeper.strPidFile), "UTF-8")));
			}

			oSweeper.finish();
			//System.out.println("\nDone.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
