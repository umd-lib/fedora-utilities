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

import org.dom4j.DocumentFactory;

import edu.umd.lib.fedora.util.DO.LIMSns;
import edu.umd.lib.fedora.util.foxml.*;

public class SweepAllPids {

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
  private int BookCounter = 0;
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
	private DocumentFactory DmFactory;
	
	private String strHost;
	private String strBaseDir;
	private String strRefDir;
	private String strFileName;
	
	private boolean bScanUMAMs;

	public SweepAllPids() {

		try {

      // Verify and extract the properties from the file
      if (new File("Sweep.properties").exists()) {
        System.out.println("Sweep.Props exists!");
      }

      FileInputStream in = new FileInputStream("Sweep.properties");
      configFile.load(in);
      in.close();

      strHost = (String) configFile.get("host");
      
      String strScan = (String) configFile.get("scanUMAMs");
      
      if(strScan.equalsIgnoreCase("Y")) {
        bScanUMAMs = true;
      }
      
      File fBaseDir = new File((String) configFile.get("baseDir"));

      if (fBaseDir.isDirectory()) {

        strBaseDir = fBaseDir.getPath();

        strFileName = strBaseDir + "/"
        + (String) configFile.getProperty("inPids");
        
        System.out.println("Base Dir: " + strBaseDir);

        strRefDir = strBaseDir + "/"
            + (String) configFile.getProperty("refDir");

        UmFactory = new UMfactory((String) configFile.getProperty("host"));
        DmFactory = new DocumentFactory();

        // oListwriter is the "Report" output of the program
        // It is generally a tab delimited text file.
        oListWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
            + "/" + (String) configFile.get("outFile")), "UTF-8");

        // Put the header here
         oListWriter.write("UMDM\tTitle\tBibTitle\n");

        // oPidWriter lists the pids that were processed.
        oPidWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
            + "/" + (String) configFile.get("pidFile")), "UTF-8");

        // The remaining writers are special purpose writers
        // based on the function performed here.
        oDelWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
            + (String) configFile.get("delFile")), "UTF-8");

      }
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

		String strPid = "";
		String strTitle;
		String strHandle;
		String strUMAMpid;
		String strIdentifier;
		String strLabel;
		String strOrder;
		String strDmType;
		String strCreateDate;
		List<List<String>> lParts;
		METSxml oMETS;
		UMAMxml oUMAM;
		boolean bProcessThisOne = true;
		
		try {
		  BufferedReader buffRead = new BufferedReader(new InputStreamReader(
          new FileInputStream(strFileName), "UTF-8"));

			SweepStats oStats;
			
			while ((strPid = buffRead.readLine()) != null) {

        System.out.println("Processing Pid: " + strPid);

        oStats = new SweepStats(namespace, configFile, strPid);

        //The inital test is to see if there is an object here
        if (oStats.getHitCount() < 1) {
          System.out.println("Error: " + strPid + " does not exist");
          continue;
        }

        strTitle = oStats.getProp("title");
        strHandle = oStats.getProp("handlehttp");
        strCreateDate = oStats.getProp("objcreatedate");
        strDmType = oStats.getProp("doType");

        // evaluate the type ( Data Model )
        bProcessThisOne = testDataModel( oStats );
        
        // Then the Status
        bProcessThisOne = testStatus( oStats )? bProcessThisOne : false;
        
        // Then characteristics which may incidentally be contained in the oStats
        bProcessThisOne = testOStats( oStats )? bProcessThisOne : false;
				
				if( bProcessThisOne ) {
	        
				  // Write the UMDM pid to the pid file
				  oPidWriter.write(strPid + "\n");
					
					// METSxml will grab the METS datastream from the pid
					oMETS = UmFactory.getMETS(strPid, "Pid");
					
					/*
					 * getParts of the METSxml object will
					 * return the AM objects in the DM object
					 * It is a list of lists of strings
					 * Each part returned consists of
					 * 0 - Pid
	         * 1 - Label
	         * 2 - Position
					 */
					lParts = oMETS.getParts();
					String sSize;
					int iSize = 0;
					
					System.out.println( "Getting UMAMs for " + strPid + " at " + lParts.size());
					
					for (List<String> lPart : lParts ) {
						if (lPart.size() == 3) {
							iSize = 0;
							strUMAMpid = lPart.get(0);
							
							// Write the UMAM pid to the pid writer
							oPidWriter.write(strUMAMpid + "\n");
							
							UMAMcounter++;
							VideoCounter++;
							}
						}
					}
					
				UMDMcounter++;
				
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
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
  
  /**
   * Often the data m0odel determines when the object is part of a sweep or not.
   * This is where that is specified.
   * 
   * @param oStats
   * @return
   */
  private boolean testDataModel( SweepStats oStats ) {
    
    boolean bProcessThisOne = false;
    
    String strType = oStats.getProp("doType");

    // This is where you set the Data Model filter
    if (strType.equals("UMD_IMAGE")) {
      ImageCounter++;
      bProcessThisOne = true;
    } else if (strType.equals("UMD_VIDEO")) {
      VideoCounter++;
      bProcessThisOne = false;
    } else if (strType.equals("UMD_BOOK")) {
      BookCounter++;
      bProcessThisOne = true;
    } else if (strType.equals("UMD_TEI")) {
      TEIcounter++;
      bProcessThisOne = false;
    } else if (strType.equals("UMD_EAD")) {
      EADcounter++;
      bProcessThisOne = false;
    } else {
      MiscCounter++;
      bProcessThisOne = false;
    }
    
    return bProcessThisOne;
    
  }
  
  /**
   * Often the status of an object determines whether it is part of a sweep or not.
   * This is where that is specified.
   * 
   * @param oStats
   * @return
   */
  private boolean testStatus( SweepStats oStats ) {
    
    boolean bProcessThisOne = true;

    /* Statuses include:
     * Complete, 
     * Incomplete,
     * Pending,
     * Private, 
     * Quarantined, 
     * Deleted
     * Please include the ones you want to check for.
     */
//    if (oStats.getProp("doStatus").equals("Deleted")) {
//      bProcessThisOne = false;
//    }
    
    return bProcessThisOne;
    
  }
  
  /**
   * Often characteristics in the object profile can be used to determine 
   * if the object is part of a sweep or not
   * 
   * @param oStats
   * @return
   */
  private boolean testOStats( SweepStats oStats ) {
    
    boolean bProcessThisOne = true;

    /* The oStats record has a lot of basic info about the object
     * title
     * handle
     * url
     * thumbnails
     * 
     */
//    List<String> lDisseminators = oStats.getProps("bdef");
//    
//    for (String sBdef : lDisseminators) {
//      if( sBdef.equalsIgnoreCase("umd:thumbnail")) {
//        bProcessThisOne = false;
//      }
//    }
    
    return bProcessThisOne;
    
  }


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SweepAllPids thisSweep = new SweepAllPids();
		thisSweep.Process();
		System.out.println("Done");
	}

}
