package edu.umd.lib.fedora.sweep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.dom4j.Element;

import edu.umd.lib.fedora.util.DO.*;
import edu.umd.lib.fedora.util.foxml.*;

/**
 * This is a UMDM level report.  It creates a list of Pids 
 * and titles for objects marked as private in the 
 * status.
 * 
 * @author phammer
 * 
 */
public class SweepReportPrivate {

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

  private UMfactory umFactory;

  public SweepReportPrivate() {

    try {
      if( new File("Sweep.properties").exists() ) {
        System.out.println("Sweep.Props exists!");
      }
      
      FileInputStream in = new FileInputStream("Sweep.properties");
      configFile.load(in);
      in.close();

      String strBaseDir = (String) configFile.get("baseDir") + "\\";

      umFactory = new UMfactory((String) configFile.getProperty("host"));

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
  public void Process ( String fileName ) {
  
    String strPid = "";
    String strTitle;
    String strType;
    List<String> lParts;
    boolean bProcessThisOne = true;
  
    TabTextOut oOutFile = createReport();
  
    try {
      BufferedReader buffRead = new BufferedReader(new InputStreamReader(
          new FileInputStream(fileName), "UTF-8"));
  
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
        
        // Get and evaluate the type: only process undeleted Images and Vids
        bProcessThisOne = false;
        //bProcessThisOne = true;
        
        strType = oStats.getProp("doType");
  
        UMDMcounter++;
        
        // Update the status counters
        if( oStats.getProp("doStatus").equals("Complete")){
           CompleteCounter++;
        } else if( oStats.getProp("doStatus").equals("Incomplete")){
           IncompleteCounter++;
        } else if( oStats.getProp("doStatus").equals("Pending")){
           PendingCounter++;
        } else if( oStats.getProp("doStatus").equals("Private")){
           PrivateCounter++;
           bProcessThisOne = true;
        } else if( oStats.getProp("doStatus").equals("Quarantined")){
           QuarantinedCounter++;
        } else if( oStats.getProp("doStatus").equals("Deleted")){
           DeletedCounter++;
        }
        
        // Update the object type counters
        if (strType.equals("UMD_IMAGE")) {
          ImageCounter++;
        } else if (strType.equals("UMD_BOOK")) {
          BookCounter++;
        } else if (strType.equals("UMD_VIDEO")) {
          VideoCounter++;
        } else if (strType.equals("UMD_TEI")) {
          TEIcounter++;
        } else if (strType.equals("UMD_EAD")) {
          EADcounter++;
        } else {
          MiscCounter++;
        }
  
        lParts = oStats.listParts();
        
        UMAMcounter += lParts.size();
        
        if (bProcessThisOne) {
  
          UMDMxml oUMDM = umFactory.getUMDM(strPid, "Pid");
  
          if (oUMDM != null) {
  
            oPidWriter.write(strPid + "\n");
            
            processUMDM( oUMDM, hRecord );
            
            oOutFile.printRecord(hRecord);
            
            iFoundCounter++;
  
          }
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
   * @param args
   */
  public static void main ( String[] args ) {
    // TODO Auto-generated method stub
    SweepReportPrivate thisSweep = new SweepReportPrivate();
    thisSweep.Process(args[0]);
    System.out.println("Done!");
  }

}
