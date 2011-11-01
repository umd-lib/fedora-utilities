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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.umd.lib.fedora.util.DO.LIMSns;
import edu.umd.lib.fedora.util.foxml.*;

public class SweepDeleted {

	private UMfactory umFactory;
	
	private Properties configFile = new Properties();

	private LIMSns namespace = new LIMSns();
	
	private String strBaseDir;
	
	private OutputStreamWriter oListWriter;
	private OutputStreamWriter oPidWriter;
	private OutputStreamWriter oDelWriter;
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
	
	private List<String> lImageBdefs;

	public SweepDeleted() {

		try {
			
			if( new File("Sweep.properties").exists() ) {
				System.out.println("Sweep.Props exists!");
			}
			
			FileInputStream in = new FileInputStream("Sweep.properties");
			configFile.load(in);
			in.close();
			
//			configFile.load(this.getClass().getClassLoader()
//					.getResourceAsStream("Sweep.properties"));
			
			umFactory = new UMfactory((String) configFile.getProperty("host"));
			
			strBaseDir = (String) configFile.get("baseDir");
			
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
			
			// This is the complete list of Image bdefs
			lImageBdefs = new ArrayList<String>();
			lImageBdefs.add("umd-bdef:rels");
			lImageBdefs.add("umd-bdef:amInfo");
			lImageBdefs.add("umd-bdef:umam");
			lImageBdefs.add("umd-bdef:image");
			lImageBdefs.add("umd-bdef:zoomify");
			
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
	public void Process() {

		String fileName = configFile.getProperty("inPids");
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
					new FileInputStream(strBaseDir + fileName), "UTF-8"));

			SweepStats oStats;
			
			while ((strPid = buffRead.readLine()) != null) {
				
				/* This is the bdef test */
				// System.out.println("Processing Pid: " + strPid );
				
				oStats = new SweepStats(namespace, configFile, strPid);
				
				if( oStats.getHitCount() < 1 ) {
					oListWriter.write("Error: " + strPid + " does not exist\n");
					continue;
				}
				
				//System.out.println("pid: " + oStats.getProp("pid") );
				
				strTitle = oStats.getProp("title");
				strHandle = oStats.getProp("handlehttp");
				strCreateDate = oStats.getProp("objcreatedate");
				
				// This is where the tests go
				
				// Get and evaluate the type: only process Images/Books
				bProcessThisOne = false;
				strType = oStats.getProp("doType");
				
				if( strType.equals("UMD_IMAGE") ||
//						strType.equals("UMD_VIDEO") ||
						strType.equals("UMD_BOOK")) {
					if( oStats.getProp("doStatus").equals("Deleted")){
						bProcessThisOne = true;
					}
					ImageCounter++;
				} else if( strType.equals("UMD_TEI")) {
					TEIcounter++;
				} else if( strType.equals("UMD_EAD")) {
					EADcounter++;
				} else {
					MiscCounter++;
				}
				
//				if( !lBdefs.contains("umd-bdef:thumbnail") ) {
//					//System.out.println("Thumbnail test - Fails" );
//					bProcessThisOne = true;
//				}
				
				// Comment this out if the tests are meaningful
				// bProcessThisOne = true;
				
				List<String> parts;
				String recordPrefix;
				String thisLine;
				
				if( bProcessThisOne ) {
					
					// OStats is the general Info object
					// oStats.saveDocs();
					
					// Write the UMDM to the pid list
					System.out.println("UMDM: " + strPid + " is deleted!");
					
					oPidWriter.write(strPid + "\n");
					bFoundCount++;
					
					parts = oStats.listParts();
					//List<String> lBdefs;
					
					boolean badUMAM = false;
					//MethodsXml umamMethods;
					
					for (String part : parts) {
						String[] partComponents = part.split(", ");
						
						System.out.println("-UMAM: " + partComponents[0]);
						oPidWriter.write(partComponents[0] + "\n");
						UMAMcounter++;
						
						//badUMAM = false;
						//umamMethods = umFactory.getMethods(partComponents[0], "Pid");
						//lBdefs = umamMethods.listBdefs();
						//if( ( lBdefs != null ) && (lBdefs.size() > 0 ) ) {
							//for (String bDef : lImageBdefs) {
								//if( ! lBdefs.contains(bDef) ) {
								//	badUMAM = true;
								//}
							//}
							//if( badUMAM ) {
								//What do we do with a bad UMAM?
							//}
						//}
					}
					
					
				} else {
					
					// What we do when we do not process this one
					
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
		SweepDeleted thisSweep = new SweepDeleted();
		thisSweep.Process();
		System.out.println("Done");
	}

}
