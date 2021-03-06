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

import org.dom4j.Element;

import edu.umd.lib.fedora.util.DO.*;
import edu.umd.lib.fedora.util.foxml.*;

/**
 * This is a UMDM report It looks into the repository element for marylandia
 * items It then looks into the relationships record for various values
 * 
 * 
 * @author phammer
 * 
 */
public class SweepReportUmamSize {

  private Properties configFile = new Properties();

  private LIMSns namespace = new LIMSns();

  private OutputStreamWriter oLogWriter;
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

  private int iFoundCounter = 0;
  private int iFoundUMAM = 0;
  
  private long lAccumulator = 0;
  
  private String sFileName;

  private UMfactory umFactory;

  public SweepReportUmamSize() {

    try {
      if( new File("Sweep.properties").exists() ) {
        System.out.println("Sweep.Props exists!");
      }
      
      FileInputStream in = new FileInputStream("Sweep.properties");
      configFile.load(in);
      in.close();

      String strBaseDir = (String) configFile.get("baseDir");

      umFactory = new UMfactory((String) configFile.get("host"));
      
      sFileName = (String) configFile.get("baseDir") + 
        (String) configFile.get("refDir") + 
        (String) configFile.get("inPids");

      oLogWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
          + (String) configFile.get("logFile")), "UTF-8");
      
      //oListWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
      //    + (String) configFile.get("outFile")), "UTF-8");
      
      //oListWriter.write("UMDM\t" + "Title\t" + "Repository\t"
      //    + "Bibref Title\t" + "Box\t" + "Folder\t" + "Item\t" + "Count\t"
      //    + "UMAM\t" + "File Name" + "\n");

      oPidWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
          + (String) configFile.get("pidFile")), "UTF-8");

      oDelWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
          + (String) configFile.get("delFile")), "UTF-8");
      oUmamWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
          + (String) configFile.get("umamFile")), "UTF-8");
      oReindexWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
          + (String) configFile.get("reindexFile")), "UTF-8");

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * 
   * 
   * @param fileName
   *          - A list of fedora Pids with one on each line
   */
  public void Process () {
  
    String strPid = "";
    String strTitle;
    String strHandle;
    boolean bProcessThisOne = true;
    List<String> lParts;
    String[] aFile;
    String strUmamPid;
    UMAMxml oUMAM;
    String strFileName = "pids_Prange_L.txt";
  
    TabTextOut oOutFile = createReport();
  
    try {
      BufferedReader buffRead = new BufferedReader(new InputStreamReader(
          new FileInputStream(strFileName), "UTF-8"));
  
      SweepStats oStats;
  
      while ((strPid = buffRead.readLine()) != null) {
  
        HashMap<String, String> hRecord = oOutFile.getBlankRecord();
  
        /* This is the bdef test */
        System.out.println("Processing Pid: " + strPid);
  
        if (hRecord.containsKey("pid")) {
          hRecord.put("pid", strPid);
        }
  
        oStats = new SweepStats(namespace, configFile, strPid);
  
        if (oStats.getHitCount() < 1) {
          //oListWriter.write("Error: " + strPid + " does not exist\n");
          continue;
        }
  
        // System.out.println("pid: " + oStats.getProp("pid") );
  
        strTitle = oStats.getProp("title");
  
        if (hRecord.containsKey("title")) {
          hRecord.put("title", strTitle);
        }
  
        strHandle = oStats.getProp("handlehttp");
  
        if (hRecord.containsKey("handle")) {
          hRecord.put("handle", strHandle);
        }
  
        // evaluate the type ( Data Model )
        bProcessThisOne = testDataModel( oStats );
        
        // Then the Status
        bProcessThisOne = testStatus( oStats )? bProcessThisOne : false;
        
        // Then characteristics which may incidentally be contained in the oStats
        bProcessThisOne = testOStats( oStats )? bProcessThisOne : false;
        
        if (bProcessThisOne) {
  
          //oOutFile.printRecord(hRecord);
          
          lParts = oStats.listParts();
          
          for (String sPart : lParts) {

            // Set the UMAM data
            aFile = sPart.split(", ");
            
            strUmamPid = aFile[0];
            
            oUMAM = umFactory.getUMAM(strUmamPid, "Pid");
            
            processUMAM( oUMAM );
          }
            
          iFoundCounter++;
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
      
      oOutFile.closeWriter();
      
      oLogWriter.write("\n\n---------------------------\n");
      oLogWriter.write("UMDM Count: " + UMDMcounter + "\n");
      oLogWriter.write("UMAM Count: " + UMAMcounter + "\n");
      oLogWriter.write("Image Count: " + ImageCounter + "\n");
      oLogWriter.write("Book Count: " + BookCounter + "\n");
      oLogWriter.write("Video Count: " + VideoCounter + "\n");
      oLogWriter.write("TEI Count: " + TEIcounter + "\n");
      oLogWriter.write("EAD Count: " + EADcounter + "\n");
      oLogWriter.write("Completed Count: " + CompleteCounter + "\n");
      oLogWriter.write("Incomplete Count: " + IncompleteCounter + "\n");
      oLogWriter.write("Private Count: " + PrivateCounter + "\n");
      oLogWriter.write("Deleted Count: " + DeletedCounter + "\n");
      oLogWriter.write("Pending Count: " + PendingCounter + "\n");
      oLogWriter.write("Quarantined Count: " + QuarantinedCounter + "\n");
      oLogWriter.write("Misc Count: " + MiscCounter + "\n");
      oLogWriter.write("Collection Size: " + CollectionSize + "\n");
      oLogWriter.write("Items Processed: " + iFoundCounter + "\n");
      oLogWriter.write("UMAMs Processed: " + iFoundUMAM + "\n");
  
      oLogWriter.close();
      oPidWriter.close();
      oDelWriter.close();
      oUmamWriter.close();
      oReindexWriter.close();
  
      System.out.println("---");
      System.out.println("Collection File: " + strFileName);
      System.out.println("UMDMs: " + iFoundCounter);
      System.out.println("UMAMs: " + UMAMcounter);
      System.out.println("Total: " + lAccumulator);
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  
  }

  private TabTextOut createReport () {

    // Create the Key and Label objects (Associative arrays
    List<String> lKeys = new ArrayList<String>();
    List<String> lLabels = new ArrayList<String>();

    // pid
    lKeys.add("pid");
    lLabels.add("Pid");

    // title
    lKeys.add("title");
    lLabels.add("Title");

//    // Subject Topical
//    lKeys.add("subjTopical");
//    lLabels.add("Subject Topical");
//
//    // Subject Geographical
//    lKeys.add("subjGeog");
//    lLabels.add("Subject Geographical");
//
//    // Summary
//    lKeys.add("descSummary");
//    lLabels.add("Summary");
//
//    // Agent Creator
//    lKeys.add("agentCreator");
//    lLabels.add("Agent Creator");
//
//    // Agent Provider
//    lKeys.add("agentProvider");
//    lLabels.add("Agent Provider");
//
//    // Agent Contributor
//    lKeys.add("agentContributor");
//    lLabels.add("Agent Contributor");
//
//    // Date Range
//    lKeys.add("covTimeDateRange");
//    lLabels.add("Date Range");
//
//    // Place
//    lKeys.add("covPlace");
//    lLabels.add("Place");
//
//    // Printing Place
//    lKeys.add("covPlacePrint");
//    lLabels.add("Printing Place");
//
//    // Rights
//    lKeys.add("rights");
//    lLabels.add("Rights");
//
//    // Handle
//    lKeys.add("handle");
//    lLabels.add("Handle");
//
//    // Files
//    lKeys.add("files");
//    lLabels.add("Files");

    String sFullPath = configFile.getProperty("baseDir");
    sFullPath += configFile.getProperty("outFile");

    TabTextOut oOutFile = null;

    try {

      oOutFile = new TabTextOut(sFullPath, lKeys, lLabels);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return oOutFile;

  }
  
  private boolean processUMAM( UMAMxml oUMAM ) {
    boolean bResult = false;
    String sFileSize = "";
    
    sFileSize = oUMAM.getProp("/adminMeta/technical/fileSize");
    
    System.out.println("Size: " + sFileSize );
    
    if( sFileSize != null && sFileSize.length() > 0 ) {
      if( sFileSize.matches("[0-9]+")) {
        lAccumulator += Long.parseLong(sFileSize);
        System.out.println("Running Total: " + lAccumulator );
      }
    }
    
    UMAMcounter++;
    
    return bResult;
  }
  
  private boolean processUMDM( UMDMxml oUMDM, 
      HashMap<String, String> hThisRecord ) {
    
    boolean bResult = false;
    String sTemp;
    
    if( oUMDM != null && hThisRecord != null ) {
      
      //UMDMxml oUMDM = thisUMDM.getUMDM();
      
      if( oUMDM != null && oUMDM.isOK() ) {
        
        // OK now we want to extract the information from
        // the UMDM and start to stuff it into the Report Record
        
        // Subject Topical
        List<String> lValues = oUMDM.getProps("/descMeta/subject[@type='topical']");
        
        if( hThisRecord.containsKey("subjTopical")) {
          hThisRecord.put("subjTopical", DoUtils.singleString(lValues, "; "));
        }
        
        // See how easy that was? Let's continue
        // Subject Geographical
        List<Element> lElements = oUMDM.getElements("/descMeta/subject[@type='geographical']");
        lValues = new ArrayList<String>();
        CovPlace eCovPlace = null;
        
        //This yields a CovPlace list sooooooo
        
        for (Element eResult : lElements) {
          eCovPlace = new CovPlace(eResult);
          lValues.add(eCovPlace.getString("All", ", "));
        }
        
        if( hThisRecord.containsKey("subjGeog")) {
          hThisRecord.put("subjGeog", DoUtils.singleString(lValues, "; "));
        }
        
        // Summary
        lValues = oUMDM.getProps("/descMeta/description[@type='summary']");
        
        if( hThisRecord.containsKey("descSummary")) {
          hThisRecord.put("descSummary", DoUtils.singleString(lValues, "; "));
        }
        
        // Agent Creator
        sTemp = "";
        lValues = oUMDM.getProps("/descMeta/agent[@type='creator']/persName");
        
        if( lValues.size() > 0 ) {
          sTemp = "persName: " + DoUtils.singleString(lValues, "; ") + " ";
        }
        
        lValues = oUMDM.getProps("/descMeta/agent[@type='creator']/corpName");
        if( lValues.size() > 0 ) {
          sTemp += "corpName: " + DoUtils.singleString(lValues, "; ");
        }
        
        if( hThisRecord.containsKey("agentCreator")) {
          hThisRecord.put("agentCreator", sTemp);
        }
        
        // Agent Provider
        sTemp = "";
        lValues = oUMDM.getProps("/descMeta/agent[@type='provider']/persName");
        
        if( lValues.size() > 0 ) {
          sTemp = "persName: " + DoUtils.singleString(lValues, "; ") + " ";
        }
        
        lValues = oUMDM.getProps("/descMeta/agent[@type='provider']/corpName");
        if( lValues.size() > 0 ) {
          sTemp += "corpName: " + DoUtils.singleString(lValues, "- ");
        }
        
        if( hThisRecord.containsKey("agentProvider")) {
          hThisRecord.put("agentProvider", sTemp);
        }
        
        // Agent Contributor
        sTemp = "";
        lValues = oUMDM.getProps("/descMeta/agent[@type='contributor']/persName");
        
        if( lValues.size() > 0 ) {
          sTemp = "persName: " + DoUtils.singleString(lValues, "; ") + " ";
        }
        
        lValues = oUMDM.getProps("/descMeta/agent[@type='contributor']/corpName");
        if( lValues.size() > 0 ) {
          sTemp += "corpName: " + DoUtils.singleString(lValues, "; ");
        }
        
        if( hThisRecord.containsKey("agentContributor")) {
          hThisRecord.put("agentContributor", sTemp);
        }
        
        // Date Range
        lValues = oUMDM.getProps("/descMeta/covTime/dateRange");
        
        if( hThisRecord.containsKey("covTimeDateRange")) {
          hThisRecord.put("covTimeDateRange", DoUtils.singleString(lValues, "; "));
        }
        
        // Place (covPlace with no attributes
        lElements = oUMDM.getElements("/descMeta/covPlace[count(@*)=0]");
        lValues = new ArrayList<String>();
        eCovPlace = null;
        
        //This yields a CovPlace list sooooooo
        
        for (Element eResult : lElements) {
          eCovPlace = new CovPlace(eResult);
          lValues.add(eCovPlace.getString("All", ", "));
        }
        
        if( hThisRecord.containsKey("covPlace")) {
          hThisRecord.put("covPlace", DoUtils.singleString(lValues, "; "));
        }
        
        // Printing Place
        lElements = oUMDM.getElements("/descMeta/covPlace[@type='printing']");
        lValues = new ArrayList<String>();
        eCovPlace = null;
        
        //This yields a CovPlace list sooooooo
        
        for (Element eResult : lElements) {
          eCovPlace = new CovPlace(eResult);
          lValues.add(eCovPlace.getString("All", ", "));
        }
        
        if( hThisRecord.containsKey("covPlacePrint")) {
          hThisRecord.put("covPlacePrint", DoUtils.singleString(lValues, "; "));
        }

        // Rights
        lValues = oUMDM.getProps("/descMeta/rights");
        
        if( hThisRecord.containsKey("rights")) {
          hThisRecord.put("rights", DoUtils.singleString(lValues, "; "));
        }
        
        // Handle*
        // Files
      }
      
      
      
    }
    
    return bResult;
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
      bProcessThisOne = true;
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
    
    boolean bProcessThisOne = false;

    /* Statuses include:
     * Complete, 
     * Incomplete,
     * Pending,
     * Private, 
     * Quarantined, 
     * Deleted
     * Please include the ones you want to check for.
     */
    if (oStats.getProp("doStatus").equals("Complete")) {
      bProcessThisOne = true;
    }
    
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
  public static void main ( String[] args ) {
    // TODO Auto-generated method stub
    SweepReportUmamSize thisSweep = new SweepReportUmamSize();
    thisSweep.Process();
    System.out.println("Done!");
  }

}
