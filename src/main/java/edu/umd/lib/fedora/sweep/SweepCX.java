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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import edu.umd.lib.fedora.util.DO.DoUtils;
import edu.umd.lib.fedora.util.DO.LIMSns;
import edu.umd.lib.fedora.util.foxml.*;

public class SweepCX {

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
  private int BookCounter = 0;
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

  private int iFoundCount = 0;

  private UMfactory UmFactory;
  private DocumentFactory DmFactory;

  private String strHost;
  private String strBaseDir;
  private String strRefDir;
  private String strFileName;
  
  private boolean bScanUMAMs = false;
  
  public SweepCX() {

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
        // oListWriter.write("Title\tHandle\tUMDM Pid\tOCM\tAleph Sys\n");
        // oListWriter.write("UMDM\tTitle\tUMAM\tidentifier\tLABEL\tOrder\tSize\n");
        oListWriter.write("UMDM\tTitle\tBibTitle\n");

        // oPidWriter lists the pids that were processed.
        oPidWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
            + "/" + (String) configFile.get("pidFile")), "UTF-8");

        // oListWriter.write("Title\tHandle\tUMDM Pid\tOCM\tAleph Sys\n");
        // oListWriter.write("UMDM\tTitle\tUMAM\tidentifier\tLABEL\tOrder\tSize\n");
        // oPidWriter.write("UMDM\tTitle\tBibTitle\n");

        // The remaining writers are special purpose writers
        // based on the function performed here.
        oDelWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
            + (String) configFile.get("delFile")), "UTF-8");

//        oUmamWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
//            + (String) configFile.get("umamFile")), "UTF-8");
//
//        oReindexWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
//            + (String) configFile.get("reindexFile")), "UTF-8");

      }

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
  public void Process() {

    String strPid = "";
    String strTitle;
    String strHandle;
    String strUMAMpid;
    String strIdentifier;
    String strLabel;
    String strOrder;
    String strAmType;
    String strDmType;
    String strStatus;
    String strCreateDate;
    List<List<String>> lParts;
    METSxml oMETS;
    UMAMxml oUMAM;
    UMDMxml oUMDM;
    FedoraXML oDmFOXML;
    FedoraXML oAmFOXML;
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
        strStatus = oStats.getProp("doStatus");

        // evaluate the type ( Data Model )
        bProcessThisOne = testDataModel( strDmType );
        
        // Then the Status
        bProcessThisOne = testStatus( strStatus )? bProcessThisOne : false;
        
        // Then characteristics which may incidentally be contained in the oStats
        bProcessThisOne = testOStats( oStats )? bProcessThisOne : false;

        List<String> parts;
        String recordPrefix;
        String thisLine;

        if (bProcessThisOne) {

          // Get the UMDM for testing
          oUMDM = UmFactory.getUMDM(strPid, "Pid");

          oDmFOXML = new FedoraXML(strPid, strDmType, "UMDM", strRefDir);
          oDmFOXML.setDC(UmFactory.getDC(strTitle, strPid));
          oDmFOXML.setDO(UmFactory.getDO("doInfo", strDmType, strStatus));
          oDmFOXML.setMETS(UmFactory.getMETS(strPid, "Pid"));
          oDmFOXML.setUMDM(oUMDM);

          if (oUMDM != null && oDmFOXML != null) {

            // This is one that we want
            if(fixUMDM(oDmFOXML)) {
              iFoundCount++;

              System.out.println("--Found - " + strTitle );
              
              String strFoxPath = strBaseDir + "/"
              + (String) configFile.get("FOXMLdir");

              oDmFOXML.saveFOXML(strFoxPath);
              
              oPidWriter.write(strPid + "\n");
              
            } else {
              System.out.println("-has ThumbnailInfo");
            }
            
            if (bScanUMAMs) {
              // Now process the UMAMs, if necessary
              parts = oStats.listParts();
              for (String part : parts) {

                System.out.print("UMAM-" + part);

                strUMAMpid = part.split(", ")[0];

                // All of the UMAMs for Books are Images not books themselves
                if (strDmType.equals("UMD_BOOK")) {
                  strAmType = "UMD_IMAGE";
                } else {
                  strAmType = strDmType;
                }

                oUMAM = UmFactory.getUMAM(strUMAMpid, "Pid");

                oAmFOXML = new FedoraXML(strUMAMpid, strAmType, "UMAM",
                    strRefDir);
                oAmFOXML.setDC(UmFactory.getDC(strTitle, strPid));
                oAmFOXML
                    .setDO(UmFactory.getDO("amInfo", strAmType, "Complete"));
                oAmFOXML.setUMAM(oUMAM);

                if (testUMAM(oAmFOXML)) {

                  iFoundCount++;

                  oPidWriter.write(strUMAMpid + "\n");

                  System.out.println("-has no Thumbnail");

                } else {
                  System.out.println("-has a Thumbnail");
                }

                UMAMcounter++;
              }
            }
            
          }
        } else {
          
          
          
          // oListWriter.write(strPid + "\t" +
          // strTitle + "\t" +
          // strType + "\t" +
          // oStats.getProp("doStatus") + "\t" +
          // strCreateDate + "\n");
          //					
          // if( oStats.getProp("doStatus").equals("Complete")){
          // UMAMcounter++;
          // }

        }

        UMDMcounter++;

        if (oStats.getProp("doStatus").equals("Complete")) {
          CompleteCounter++;
        } else if (oStats.getProp("doStatus").equals("Incomplete")) {
          IncompleteCounter++;
        } else if (oStats.getProp("doStatus").equals("Pending")) {
          PendingCounter++;
        } else if (oStats.getProp("doStatus").equals("Deleted")) {
          // Print to the list of deleted objects
          oDelWriter.write(strPid + "\n");
          DeletedCounter++;
        } else if (oStats.getProp("doStatus").equals("Private")) {
          PrivateCounter++;
        } else if (oStats.getProp("doStatus").equals("Quarantined")) {
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

      oListWriter.write("\n\n---------------------------\n");
      oListWriter.write("UMDM Count: " + UMDMcounter + "\n");
      oListWriter.write("UMAM Count: " + UMAMcounter + "\n");
      oListWriter.write("Image Count: " + ImageCounter + "\n");
      oListWriter.write("Video Count: " + VideoCounter + "\n");
      oListWriter.write("TEI Count: " + TEIcounter + "\n");
      oListWriter.write("EAD Count: " + EADcounter + "\n");
      oListWriter.write("Completed Count: " + CompleteCounter + "\n");
      oListWriter.write("Incomplete Count: " + IncompleteCounter + "\n");
      oListWriter.write("Private Count: " + PrivateCounter + "\n");
      oListWriter.write("Deleted Count: " + DeletedCounter + "\n");
      oListWriter.write("Pending Count: " + PendingCounter + "\n");
      oListWriter.write("Quarantined Count: " + QuarantinedCounter + "\n");
      oListWriter.write("Misc Count: " + MiscCounter + "\n");
      oListWriter.write("Collection Size : " + CollectionSize + "\n");
      oListWriter.write("Items Found : " + iFoundCount + "\n");

      oListWriter.close();
      oPidWriter.close();
      oDelWriter.close();
      //oUmamWriter.close();
      //oReindexWriter.close();

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * This is the function for UMDM tests for the digital object It returns true
   * if the tests show that the object needs to be processed. It returns false
   * if it fails the tests.
   * 
   * @param thisFOXML
   * @return
   */
  private boolean testUMDM( FedoraXML thisFOXML ) {
    
    // Set this to the default condition to test UMDMs
    // false to run tests
    // true to skip tests
    boolean bResult = false;

    if (! bResult) {
      
      //bResult = testDmDisseminator(thisFOXML.getPid());
      
//      // This is where the UMDM is tested to see
//      // if it is one that we need to look at
//      UMDMxml thisUMDM = thisFOXML.getUMDM();
//      
//      // Get the property that we are looking for
//      List<String> lProperties = thisUMDM.getProps("/descMeta/style");
//      
//      // Test if it exists, if not, we want it!
//      if (lProperties.size() < 1) {
//        bResult = true;
//      }
    }
    return bResult;
  }
  
  private boolean testUMAM( FedoraXML thisFOXML ) {
    
    // Set this for the default test condition for UMAMs
    // false in order to run tests
    // true to skip tests
    boolean bResult = false;
    
    if( ! bResult ) {
      bResult = testAmDisseminator( thisFOXML.getPid() );
    }
    
    return bResult;
    
  }
  
  private boolean testAmDisseminator( String strUMAMpid ) {
    

    // Set this for the default test condition for Disseminators
    // false in order to run tests
    // true to skip tests
    boolean bResult = true;
    
    if (! bResult) {
      try {
        // Get the list of Disseminators
        String strURL = "http://" + strHost + "/fedora/listMethods/" + strUMAMpid
            + "?xml=true";
        
        //System.out.println();
        //System.out.println(strURL);
        
        URL thisURL = new URL(strURL);
        SAXReader reader = new SAXReader();
        Document saveThis = reader.read(thisURL);

        // Print out the document
        //DoUtils.saveDoc(saveThis, "Terminal");
        
        // Look for the thumbnail disseminator in the list
        Node nDisseminator = DoUtils
            .getXPath(
                "/objectMethods/bDef[@pid='umd-bdef:image']/method[@name='getThumbnail']")
            .selectSingleNode(saveThis);

        // If it aint there, this is our guy!
        if (nDisseminator == null) {
          bResult = true;
        }

      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (DocumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return bResult;
    
  }
  
  private boolean testDmDisseminator( String strPid ) {
    

    // Set this for the default test condition for Disseminator
    // false in order to run tests
    // true to skip tests
    boolean bResult = true;
    
    if (! bResult) {
      try {
        // Get the list of Disseminators
        String strURL = "http://" + strHost + "/fedora/listMethods/" + strPid
            + "?xml=true";
        
        //System.out.println();
        //System.out.println(strURL);
        
        URL thisURL = new URL(strURL);
        SAXReader reader = new SAXReader();
        Document saveThis = reader.read(thisURL);

        // Print out the document
        //DoUtils.saveDoc(saveThis, "Terminal");
        
        // Look for the thumbnail disseminator in the list
        Node nDisseminator = DoUtils
            .getXPath(
                "/objectMethods/bDef[@pid='umd-bdef:thumbnail']/method[@name='getThumbnailInfo']")
            .selectSingleNode(saveThis);

        // If it aint there, this is our guy!
        if (nDisseminator == null) {
          bResult = true;
        }

      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (DocumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
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
  private boolean testDataModel( String strType ) {
    
    boolean bProcessThisOne = false;

    // This is where you set the Data Model filter
    if (strType.equals("UMD_IMAGE")) {
      ImageCounter++;
      bProcessThisOne = false;
    } else if (strType.equals("UMD_VIDEO")) {
      VideoCounter++;
      bProcessThisOne = true;
    } else if (strType.equals("UMD_BOOK")) {
      BookCounter++;
      bProcessThisOne = false;
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
  private boolean testStatus( String strStatus ) {
    
    boolean bResult = false;

    /* Statuses include:
     * Complete, 
     * Incomplete,
     * Pending,
     * Private, 
     * Quarantined, 
     * Deleted
     * Please include the ones you want to check for.
     */
    

    if (strStatus.equals("Complete") ) {
      bResult = true;
    }
    
//    if (oStats.getProp("doStatus").equals("Complete")) {
//      bProcessThisOne = true;
//    }
    
    return bResult;
    
  }
  
  /**
   * Often the status of an object determines whether it is part of a sweep or not.
   * This is where that is specified.
   * 
   * @param oStats
   * @return
   */
  private boolean testOStats( SweepStats oStats ) {
    
    boolean bProcessThisOne = false;

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
    
      String sTitle = oStats.getProp("title");
      if( sTitle.matches(".*ootball.*") ) {
        bProcessThisOne = true;
      }
    
    return bProcessThisOne;
    
  }

  /**
   * This function fixes all of the "errors" in the UMDM of the object. It
   * returns true if all of the fixes have been made and the object now fails
   * the UMDM tests.
   * 
   * @param thisFOXML
   * @return
   */
  private boolean fixUMDM( FedoraXML thisFOXML ) {

    boolean bItIsFixed = false;
    UMDMxml retUMDM = thisFOXML.getUMDM();
    
    // Apply the fix
    
    // Delete the old one(s)
    retUMDM.removeElements("/descMeta/relationships");
    
    // Fashion the new one(s)
    Element thisElement = DmFactory.createElement("relationships");
    Element eElement;
    Element eSubElement;

    eElement = thisElement.addElement("relation");
    eElement.addAttribute("label", "archivalcollection");
    eElement.addAttribute("type", "isPartOf");
    
    eElement = eElement.addElement("bibRef");
    
    eSubElement = eElement.addElement("title");
    eSubElement.addAttribute("type", "main");
    eSubElement.setText("University of Maryland Football Heritage Film collection");
    
    eSubElement = eElement.addElement("bibScope");
    eSubElement.addAttribute("type", "accession");
    eSubElement.setText("2011-166");

    // Add the corrected element to the UMDM
    retUMDM.addElement(thisElement);

    if (!testUMDM(thisFOXML)) {
      thisFOXML.setUMDM(retUMDM);
      bItIsFixed = true;
    }

    return bItIsFixed;

  }
  
  private boolean fixUMAM( FedoraXML thisFOXML ) {

    boolean bItIsFixed = true;
    
    return bItIsFixed;
    
  }

  /**
   * This function writes the Report to the designated file
   * 
   * @param thisFOXML
   * @return
   */
  private String objectReport( FedoraXML thisFOXML ) {
    String sResult = "";

    return sResult;
  }

  /**
   * @param args
   */
  public static void main( String[] args ) {
    // TODO Auto-generated method stub
    SweepCX thisSweep = new SweepCX();
    thisSweep.Process();
    System.out.println("Done");
  }

}
