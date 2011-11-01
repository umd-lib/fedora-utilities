package edu.umd.lib.fedora.sweep;

import java.io.BufferedReader;
import java.io.File;
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

public class SweepDisseminators {

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
	
	private int bFoundCount = 0;
	
	private UMfactory UmFactory;

	public SweepDisseminators() {

		try {
      if( new File("Sweep.properties").exists() ) {
        System.out.println("Sweep.Props exists!");
      }
      
      FileInputStream in = new FileInputStream("Sweep.properties");
      configFile.load(in);
      in.close();

			String strBaseDir = (String) configFile.get("baseDir");
			
			UmFactory = new UMfactory((String) configFile.getProperty("host"));
			
			//System.out.println(configFile.getProperty("host"));
			//System.out.println(configFile.getProperty("processUMAMs"));

			oListWriter = new OutputStreamWriter( new FileOutputStream( strBaseDir + (String) configFile.get("outFile") ), "UTF-8" );
			// oListWriter.write("Title\tHandle\tUMDM Pid\tOCM\tAleph Sys\n");
			//oListWriter.write("UMDM\tTitle\tUMAM\tidentifier\tLABEL\tOrder\tSize\n");
			oListWriter.write("UMDM\tTitle\tBibTitle\n");
			
			oPidWriter = new OutputStreamWriter( new FileOutputStream( strBaseDir +(String) configFile.get("pidFile") ), "UTF-8" );
			// oListWriter.write("Title\tHandle\tUMDM Pid\tOCM\tAleph Sys\n");
			//oListWriter.write("UMDM\tTitle\tUMAM\tidentifier\tLABEL\tOrder\tSize\n");
			//oPidWriter.write("UMDM\tTitle\tBibTitle\n");

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
				
				System.out.println("Title: " + oStats.getProp("title") );
				
				System.out.println("Disseminators: ");
				
				// Test the bdefs for a thumbnail method
				List<String> lBdefs = oStats.getProps("bdef");
		
				for( String strBdef: lBdefs){
					System.out.println(" " + strBdef );
				}
				
				// This is where the tests go
				
				// Get and evaluate the type: only process Images and Vids
				bProcessThisOne = true;
				strType = oStats.getProp("doType");
				
				if( strType.equals("UMD_IMAGE") || 
						strType.equals("UMD_VIDEO") ||
						strType.equals("UMD_BOOK")) {
					if( oStats.getProp("doStatus").equals("Deleted")){
						bProcessThisOne = false;
					}
				} else if( strType.equals("UMD_TEI")) {
					TEIcounter++;
					bProcessThisOne = false;
				} else if( strType.equals("UMD_EAD")) {
					EADcounter++;
					bProcessThisOne = false;
				} else {
					MiscCounter++;
					bProcessThisOne = false;
				}
				
				// Test for Disseminator combos that are OK
				if( lBdefs.contains("umd-bdef:thumbnail") ) {
					bProcessThisOne = false;
				}
				
				// Comment this out if the tests are meaningful
				// bProcessThisOne = true;
				
				if( bProcessThisOne ) {
	        
	        List<String> parts;
	        String recordPrefix;
	        String thisLine;
					
					// OStats is the general Info object
					// oStats.saveDocs();
					
					//oListWriter.write("---------------------------------\n");
					//oListWriter.write("PID: " + strPid + "\n");
					///oListWriter.write("Title: " + oStats.getProp("title") + "\n");
					//oListWriter.write("Collection: " + oStats.getProp("isMemberOfCollection") + "\n");
					// Prepare the output record
					
					// METSxml will grab the METS datastream from the pid
					//oMETS = new METSxml(configFile, strPid);
					
					/*
					 * getParts of the METSxml object will
					 * return the AM objects in the DM object
					 * It is a list of lists of strings
					 * Each part returned consists of
					 * 0 - Pid
	                 * 1 - Label
	                 * 2 - Position
					 */
//					lParts = oMETS.getParts();
//					String sSize;
//					int iSize = 0;
//					
//					System.out.println( "Getting UMAMs for " + strPid + " at " + lParts.size());
//					
//					for (List<String> lPart : lParts ) {
//						if (lPart.size() == 3) {
//							iSize = 0;
//							strUMAMpid = lPart.get(0);
//							System.out.println( "UMAM: " + strUMAMpid);
//							oUMAM = new UMAMxml(configFile, strUMAMpid);
//							sSize = oUMAM.getProp("/adminMeta/technical/fileSize");
//							if( sSize.length() > 0 ) {
//								iSize = Integer.parseInt(sSize);
//								CollectionSize += iSize;
//							}
//							strIdentifier = oUMAM.getIdentifier();
//							oListWriter.write(strPid + "\t" + 
//									strTitle + "\t" + 
//									strUMAMpid + "\t" + 
//									strIdentifier + "\t" + 
//									lPart.get(1) + "\t" + 
//									lPart.get(2) + "\t" + 
//									iSize + "\t" +
//									strCreateDate + "\n");
//							UMAMcounter++;
//							
//							if( strType.equals("UMD_IMAGE") || 
//									strType.equals("UMD_BOOK")) {
//								ImageCounter++;
//							} else if( strType.equals("UMD_VIDEO")) {
//								VideoCounter++;
//							}
//						}
//					}
					
					
					//oStats.getProp("title") + "\t";
					
					//parts = oStats.listParts();
					
					/* We need the label from the METS record
					 * Soooooo - we can't use the parts from the 
					 * oStats record, we need it from the rels object
					 */
					
					// METSxml oRels = new METSxml(configFile, strPid);
					
					//oListWriter.write("Parts (" + parts.size() + "):\n");
					
//					for(String thisPart: parts ) {
//						//oListWriter.write(thisPart + "\n");
//						thisLine = recordPrefix + thisPart.replaceAll(", ", "\t");
//						//thisLine = thisLine.replaceAll(", ", "\t");
//						oListWriter.write(thisLine + "\n");
//					}
					
					//oListWriter.write("\n");
					
					// System.out.println(strPid);
					
					// Get the UMDM for the Pid
					UMDMxml oUMDM = UmFactory.getUMDM(strPid, "Pid" );
					
					if( oUMDM != null ) {

							//String strAgent = oUMDM.getProp("/descMeta/relationships/relation/bibRef/title");
							
							StringBuffer strUMAMs = new StringBuffer();
							parts = oStats.listParts();
							
							// If there are no parts, then what are we doing with it?
							if (parts.size() > 0) {
								for (String thisPart : parts) {
									if (strUMAMs.length() > 0) {
										strUMAMs.append(", ");
									}
									strUMAMs.append(thisPart);
								}
								oListWriter
										.write(strPid + "\t" + strTitle + "\t"
												+ parts.size() + "\t"
												+ strUMAMs + "\n");
								oPidWriter.write(strPid + "\n");
								bFoundCount++;
							} else {
								// If there were no parts, then this is like to be a non-object
								// Needing reindexing
								oReindexWriter.write(strPid + "\n");
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
			oListWriter.write("Items Found : " + bFoundCount + "\n" );
			
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
		SweepDisseminators thisSweep = new SweepDisseminators();
		thisSweep.Process(args[0]);
		System.out.println("Done");
	}

}
