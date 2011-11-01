package edu.umd.lib.fedora.sweep;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.umd.lib.fedora.util.DO.LIMSns;
import edu.umd.lib.fedora.util.foxml.*;

/**
 * This is a UMDM report
 * It looks into the repository element for marylandia items
 * It then looks into the relationships record for various values
 * 
 * 
 * @author phammer
 *
 */
public class BibRefSweep {

	private Properties configFile = new Properties();

	private LIMSns namespace = new LIMSns();
	
	private OutputStreamWriter oListWriter;
	private OutputStreamWriter oPidWriter;
	private OutputStreamWriter oDelWriter;
	private OutputStreamWriter oReindexWriter;
	private OutputStreamWriter oUmamWriter;
	
	private int UMDMcounter = 0;
	private int UMAMcounter = 0;
	private int ImageCounter = 0;
	private int VideoCounter = 0;
	private int TEIcounter = 0;
	private int EADcounter = 0;
	private int MiscCounter = 0;
	private long CollectionSize = 0;
	
	private int CompleteCounter = 0;
	private int IncompleteCounter = 0;
	private int PendingCounter = 0;
	private int PrivateCounter = 0;
	private int QuarantinedCounter = 0;
	private int DeletedCounter = 0;
	
	private int iFoundCounter = 0;
	private int iFoundUMAM = 0;
	
	private UMfactory UmFactory;

	public BibRefSweep() {

		try {
			configFile.load(this.getClass().getClassLoader()
					.getResourceAsStream("Sweep.properties"));
			
			String strBaseDir = (String) configFile.get("baseDir") + "\\";
			
			UmFactory = new UMfactory((String) configFile.getProperty("host"));
			
			oListWriter = new OutputStreamWriter( new FileOutputStream( strBaseDir + (String) configFile.get("outFile") ), "UTF-8" );
			oListWriter.write("UMDM\t" + 
					"Title\t" + 
					"Bibref Title\t" + 
					"Count\t" + 
					"UMAM\t" + 
					"File Name" + 
					"\n");
			
			oPidWriter = new OutputStreamWriter( new FileOutputStream( strBaseDir +(String) configFile.get("pidFile") ), "UTF-8" );
			
			oDelWriter = new OutputStreamWriter( new FileOutputStream( strBaseDir + (String) configFile.get("delFile") ), "UTF-8" );
			oUmamWriter = new OutputStreamWriter( new FileOutputStream( strBaseDir + (String) configFile.get("umamFile") ), "UTF-8" );
			oReindexWriter = new OutputStreamWriter( new FileOutputStream( strBaseDir + (String) configFile.get("reindexFile") ), "UTF-8" );
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * 
	 * @param fileName - A list of fedora Pids with one on each line
	 */
	public void Process(String fileName) {

		String strPid = "";
		String strTitle;
		String strHandle;
		String strUMAMpid;
		String strIdentifier;
		String strLabel;
		String strOrder;
		String strType;
		String strCreateDate;
		List<List<String>> lParts;
		METSxml oMETS;
		UMAMxml oUMAM;
		boolean bProcessThisOne = true;
		
		try {
			BufferedReader buffRead = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName), "UTF-8"));

			SweepStats oStats;
			
			while ((strPid = buffRead.readLine()) != null) {
				
				/* This is the bdef test */
				System.out.println("Processing Pid: " + strPid );
				
				oStats = new SweepStats(namespace, configFile, strPid);
				
				if( oStats.getHitCount() < 1 ) {
					oListWriter.write("Error: " + strPid + " does not exist\n");
					continue;
				}
				
				//System.out.println("pid: " + oStats.getProp("pid") );
				
				strTitle = oStats.getProp("title");
				strHandle = oStats.getProp("handlehttp");
				strCreateDate = oStats.getProp("objcreatedate");
				
				// Get and evaluate the type: only process undeleted Images and Vids
				bProcessThisOne = false;
				strType = oStats.getProp("doType");
				
				if( strType.equals("UMD_IMAGE") || 
						strType.equals("UMD_VIDEO") ||
						strType.equals("UMD_BOOK")) {
					if( ! oStats.getProp("doStatus").equals("Deleted")){
						bProcessThisOne = true;
					}
				} else if( strType.equals("UMD_TEI")) {
					TEIcounter++;
				} else if( strType.equals("UMD_EAD")) {
					EADcounter++;
				} else {
					MiscCounter++;
				}
				
				if( bProcessThisOne ) {
					
					List<String> parts;
					int iPartCount;
					String recordPrefix;
					String thisLine;
					String strBibTitle;
					String strProvider;
					String strSummary;
					String[] UmamParts;
					String strBaseXpath = "/descMeta/relationships/relation[@label='archivalcollection' and @type='isPartOf']/bibRef/";
					String strRelTitle;
					String strRelBox;
					String strRelFolder;
					String strRelItem;
					
					UMDMxml oUMDM = UmFactory.getUMDM( strPid, "Pid" );
					
					if( oUMDM != null ) {

						//String strRepository = oUMDM.getProp("/descMeta/repository/corpName");
						//String strBibTitle = oUMDM.getProp("/descMeta/relationships/relation[@label='archivalcollection' and @type='isPartOf']/bibRef/title[@type='main']");
						strBibTitle = oUMDM.getProp("/descMeta/relationships/relation[@label='archivalcollection' and @type='isPartOf']/bibRef/title[@type='main']");
						
						//if( strRepository != null && ( strRepository.length() > 0 ) && strRepository.equals("Marylandia") ) {
						//if( strBibTitle != null && ( strBibTitle.length() > 0 ) ) {
						
						if(  strBibTitle != null ) {

							//String strAgent = oUMDM.getProp("/descMeta/relationships/relation/bibRef/title");
							
							strRelTitle = oUMDM.getProp(strBaseXpath + "title[@type='main']");
							strRelBox = oUMDM.getProp(strBaseXpath + "bibScope[@type='box']");
							strRelFolder = oUMDM.getProp(strBaseXpath + "bibScope[@type='folder']");
							strRelItem = oUMDM.getProp(strBaseXpath + "bibScope[@type='item']");
							
							parts = oStats.listParts();
							
							// If there are no parts, then what are we doing with it?
							if (parts.size() > 0) {
								
								iPartCount = 1;
								
								for (String thisPart : parts) {
									
									UmamParts = thisPart.split(", ");
									
									//System.out.println(strRepository);
									
									oListWriter.write(strPid + "\t" 
											+ strTitle + "\t"
											+ strBibTitle + "\t"
											+ iPartCount + " of "
											+ parts.size() + "\t"
											+ UmamParts[0] + "\t"
											+ UmamParts[1] + "\n");
									
									iPartCount++;
									iFoundUMAM++;
								}

								oPidWriter.write(strPid + "\n");
								iFoundCounter++;
							} else {
								// If there were no parts, then this is like to be a non-object
								// Needing reindexing
								oReindexWriter.write(strPid + "\n");
							}
						}
					}
				} else {
//					oListWriter.write(strPid + "\t" + 
//							strTitle + "\t" + 
//							strType + "\t" + 
//							oStats.getProp("doStatus") + "\t" +
//							strCreateDate + "\n");
//					
//					if( oStats.getProp("doStatus").equals("Complete")){
//						UMAMcounter++;
//					}
					
				}
				
				UMDMcounter++;
				
				if( oStats.getProp("doStatus").equals("Complete")){
					CompleteCounter++;
				} else if( oStats.getProp("doStatus").equals("Incomplete")){
					IncompleteCounter++;
				} else if( oStats.getProp("doStatus").equals("Pending")){
					PendingCounter++;
				} else if( oStats.getProp("doStatus").equals("Deleted")){
					//Print to the list of deleted objects
					oDelWriter.write(strPid + "\n");
					DeletedCounter++;
				} else if( oStats.getProp("doStatus").equals("Private")){
					PrivateCounter++;
				} else if( oStats.getProp("doStatus").equals("Quarantined")){
					QuarantinedCounter++;
				}
				
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {

			oListWriter.write("\n\n---------------------------\n" );
			oListWriter.write("UMDM Count: " + UMDMcounter + "\n" );
			oListWriter.write("UMAM Count: " + UMAMcounter + "\n" );
			oListWriter.write("Image Count: " + ImageCounter + "\n" );
			oListWriter.write("Video Count: " + VideoCounter + "\n" );
			oListWriter.write("TEI Count: " + TEIcounter + "\n" );
			oListWriter.write("EAD Count: " + EADcounter + "\n" );
			oListWriter.write("Completed Count: " + CompleteCounter + "\n" );
			oListWriter.write("Incomplete Count: " + IncompleteCounter + "\n" );
			oListWriter.write("Private Count: " + PrivateCounter + "\n" );
			oListWriter.write("Deleted Count: " + DeletedCounter + "\n" );
			oListWriter.write("Pending Count: " + PendingCounter + "\n" );
			oListWriter.write("Quarantined Count: " + QuarantinedCounter + "\n" );
			oListWriter.write("Misc Count: " + MiscCounter + "\n" );
			oListWriter.write("Collection Size : " + CollectionSize + "\n" );
			oListWriter.write("Items Found : " + iFoundCounter + "\n" );
			oListWriter.write("UMAMs Found : " + iFoundUMAM + "\n" );
			
			oListWriter.close();
			oPidWriter.close();
			oDelWriter.close();
			oUmamWriter.close();
			oReindexWriter.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BibRefSweep thisSweep = new BibRefSweep();
		thisSweep.Process(args[0]);
		System.out.println("Done!");
	}

}
