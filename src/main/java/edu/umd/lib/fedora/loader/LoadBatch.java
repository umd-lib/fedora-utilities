package edu.umd.lib.fedora.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import edu.umd.lib.fedora.sweep.SubSheet;
import edu.umd.lib.fedora.util.DO.DoUtils;
import edu.umd.lib.fedora.util.DO.LIMSlookup;
import edu.umd.lib.fedora.util.DO.TabText;
import edu.umd.lib.fedora.util.foxml.DCxml;
import edu.umd.lib.fedora.util.foxml.DOxml;
import edu.umd.lib.fedora.util.foxml.FedoraXML;
import edu.umd.lib.fedora.util.foxml.METSxml;
import edu.umd.lib.fedora.util.foxml.UMAMxml;
import edu.umd.lib.fedora.util.foxml.UMDMxml;
import edu.umd.lib.fedora.util.foxml.UMfactory;
import edu.umd.lib.fedora.util.foxml.ZOOMxml;
import edu.umd.lib.util.excelReader.ExcelReader;

public class LoadBatch {

  private final Properties configFile = new Properties();
  private UMfactory umf;
  private DocumentFactory df;
  private OutputStreamWriter oPidWriter;
  private OutputStreamWriter oUmdmWriter;
  private OutputStreamWriter oLinkWriter;
  private String strBaseDir;
  private String strStatus = "Pending";

  private final int iBookCounter = 0;
  private final int iPageCounter = 0;

  private final String strPrintType = "File";

  public LoadBatch(String propFile) {
    try {
      configFile.load(new FileInputStream(propFile));
      umf = new UMfactory(configFile.getProperty("host"));
      df = new DocumentFactory();
      strStatus = configFile.getProperty("status");

      strBaseDir = configFile.getProperty("baseLoc");

      // Setup the pid list output file - for ingest
      oPidWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir + "/"
          + configFile.getProperty("outputLoc") + "/" + "pids.txt"), "UTF-8");

      // Setup the UMDM pid list output file - for luceneRX
      oUmdmWriter = new OutputStreamWriter(
          new FileOutputStream(strBaseDir + "/"
              + configFile.getProperty("outputLoc") + "/" + "reindexPids.txt"),
          "UTF-8");

      oLinkWriter = new OutputStreamWriter(new FileOutputStream(strBaseDir
          + "/" + configFile.getProperty("outputLoc") + "/" + "linkURLs.txt"),
          "UTF-8");

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public boolean process() {

    boolean bSuccess = false;
    String strCollection = "Misc";
    String strPid = "umd:0000";
    String strID = "XXX-XXX";
    String strGroup = "999-998";
    String strSuperGroup = "999";
    String strTitle = "Unknown";
    String strModel = "Book";
    String strSWF = "";
    String strZoomBase = "";
    String strImageBase = "";
    UMDMxml thisUMDM = null;
    UMAMxml thisUMAM = null;
    METSxml thisMETS = null;
    FedoraXML dmFOXML = null;
    FedoraXML amFOXML = null;
    DOxml thisDO = null;
    DCxml thisDC = null;
    ZOOMxml thisZoom = null;
    int iAmPos = 1;

    // Set up the TabText object from the properties File
    /*
     * batchFile is the file containing the records to be ingested. pids.txt
     * contains the list of available pids (assumed to be >= pids required
     */
    // TabText oSourceFile = new TabText( strBaseDir + "/" +
    // configFile.getProperty("refLoc") + "/" +
    // configFile.getProperty("batchFile") );

    TabText oPids = new TabText(strBaseDir + "/"
        + configFile.getProperty("refLoc") + "/"
        + configFile.getProperty("pidFile"));

    HashMap<String, String> hPids;
    HashMap<String, String> hRecord;
    Iterator<HashMap<String, String>> iSourceFile;
    ExcelReader xSourceFile;

    try {
      xSourceFile = new ExcelReader(strBaseDir + "/"
          + configFile.getProperty("refLoc") + "/"
          + configFile.getProperty("batchFile"));

      System.out.println(strBaseDir + "/" + configFile.getProperty("refLoc")
          + "/" + configFile.getProperty("batchFile"));

      iSourceFile = xSourceFile.iterator();

    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      iSourceFile = null;
    }

    // Iterate through the TabText File
    // for (HashMap<String, String> hRecord : iSourceFile) {
    while (iSourceFile.hasNext()) {
      // System.out.println(it.next());
      hRecord = iSourceFile.next();

      System.out.println("---");
      System.out.println("id: " + hRecord.get("id"));
      System.out.println("pid: " + hRecord.get("pid"));
      System.out.println("type: " + hRecord.get("type"));
      System.out.println("title: " + hRecord.get("title-jp"));
      System.out.println("date: " + hRecord.get("date"));
      System.out.println("description: " + hRecord.get("description"));
      System.out.println("subjTopical: " + hRecord.get("subjTopical"));
      System.out.println("repository: " + hRecord.get("repository"));
      System.out.println("label: " + hRecord.get("label"));
      System.out.println("fileName: " + hRecord.get("fileName"));
      System.out.println("---");

      // We are starting a new object

      // If there is an existing object, close it out
      // and save the FOXML
      // then reinitialize the tracking variables

      strPid = hRecord.get("pid");

      // If the pid is blank or missing from the record
      // Get them from the pid file.
      if ((strPid == null || strPid.length() < 1) && oPids != null) {

        // Only get a new pid if we know that this is not a blank line.
        // The record must have an id or a filename - one or the other
        // all other cases are illegal -- don't get pids for them
        if (((hRecord.get("id") != null) && (hRecord.get("id").length() > 0))
            || ((hRecord.get("fileName") != null) && (hRecord.get("fileName")
                .trim().length() > 0))) {
          System.out.print("Gotta get new Pid - ");
          hPids = oPids.next();
          if (hPids != null) {
            strPid = hPids.get("pid");
            System.out.println(strPid);
          }
        }

      }

      strPid = strPid.trim();

      System.out.println("Pid: " + strPid);

      // Set the collection - which is only in the UMDM record
      // if it is null, then this is a UMAM record
      // and we need to set it from the last UMDM record
      if ((hRecord.get("collection") != null)
          && (hRecord.get("collection").length() > 0)) {
        strCollection = hRecord.get("collection");
      } else {
        hRecord.put("collection", strCollection);
      }

      // Set the Content Model which is under the type column in the spreadsheet
      // This should not be null in any record
      if ((hRecord.get("type") != null) && (hRecord.get("type").length() > 0)) {
        strModel = LIMSlookup.getContentModel(hRecord.get("type"));
      } else {
        hRecord.put("type", strModel);
      }

      // The id column is supposed to be blank in all UMAM records
      if ((hRecord.get("id") != null) && (hRecord.get("id").length() > 0)) {

        // ... so this is an object record (UMDM)

        if (!strID.equals(hRecord.get("id")) && !strID.equals("XXX-XXX")) {
          /*
           * This is a new object record. Before we can process the new record,
           * we have to save off the old one. We have to write this UMDM out
           * before its values are all replaced.
           */

          /*
           * The old record should be complete except for the METS record which
           * should be current and ready to go!
           */

          dmFOXML.setMETS(thisMETS);

          // Print out the FOXML for the old UMDM
          DoUtils.saveDoc(
              dmFOXML.getFoxml(),
              configFile.getProperty("baseLoc") + "/"
                  + configFile.getProperty("foxmlLoc") + "/" + dmFOXML.getPid()
                  + ".xml");

          try {
            String sHost = configFile.getProperty("host");

            // Save the pid to the UMDM pid file for reindexng, if necessary
            oUmdmWriter.write(dmFOXML.getPid() + "\n");

            // Store the particulars of this object in the links file
            // to email to the client later.
            oLinkWriter.write(dmFOXML.getPid() + " - " + strTitle + " - "
                + strID + "\n");
            oLinkWriter.write("Public interface: " + "http://"
                + sHost.replaceAll("fedora", "digital") + "/image.jsp?pid="
                + dmFOXML.getPid() + "\n");
            oLinkWriter.write("Admin Interface: " + "http://" + sHost
                + "/admin/results.jsp?action=search&query1=" + dmFOXML.getPid()
                + "&index1=pid\n");
            oLinkWriter.write("Fedora Object: " + "http://" + sHost
                + "/fedora/get/" + dmFOXML.getPid() + "\n\n");

            // load this pid into the pid file of pids to be loaded
            oPidWriter.write(dmFOXML.getPid() + "\n");

          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

        }

        // Collect the data for the new UMDM object
        strID = hRecord.get("id");
        strGroup = strID;
        String[] aIDparts = strID.split("-");
        strSuperGroup = aIDparts[0];

        strTitle = "Unknown";

        if ((hRecord.get("title") != null)
            && (hRecord.get("title").length() > 0)) {
          strTitle = hRecord.get("title");
        } else if ((hRecord.get("title-jp") != null)
            && (hRecord.get("title-jp").length() > 0)) {
          strTitle = hRecord.get("title-jp");
        }

        System.out.println("Processing: " + strID);

        String strCollectionPid = LIMSlookup.getCollectionPid(strCollection);
        System.out.println("Content Model: " + strModel);
        System.out.println("Collection Pid: " + strCollectionPid);
        // If there is an existing UMDM object, close it out

        // Create the new UMDM Object
        thisUMDM = getUMDM(hRecord);

        thisDO = new DOxml();
        thisDO.setType("doInfo");
        thisDO.setContModel(strModel);
        thisDO.setStatus(strStatus);

        System.out.println("Trying to set UMDM status to - "
            + hRecord.get("status"));

        thisDC = new DCxml();
        thisDC.setIdentifier(strPid);
        thisDC.setTitle(strTitle);

        dmFOXML = new FedoraXML(strPid, strModel, "UMDM",
            (strBaseDir + "/" + configFile.getProperty("refLoc")));

        dmFOXML.setCollection(strCollection);
        dmFOXML.setDC(thisDC);
        dmFOXML.setDO(thisDO);
        dmFOXML.setUMDM(thisUMDM);

        thisMETS = new METSxml("images");
        thisMETS.addCollection(1, strCollectionPid);

        /*
         * The METS record is created but not incorporated into the foxml yet.
         * We have to process the UMAMs to get the METS properly filled out
         */

        // reset the UMAM position counter
        iAmPos = 1;

        bSuccess = true;

      } else if ((hRecord.get("fileName") != null)
          && (hRecord.get("fileName").trim().length() > 0)) {

        // This is a file/administrative record (UMAM)
        // We have to remember to skip "blank" lines though

        // Put the stored ID value into the hRecord
        hRecord.put("id", strID);

        // OK Now process the UMAM for each file name
        String strFileName = hRecord.get("fileName");

        // OK the filename must currently end in jpg
        // if it does then fine if not, force it
        String strFileBase = "";
        if (strFileName != null) {
          if (!strFileName.matches("\\.jpg")) {
            int iDotPos = strFileName.lastIndexOf('.');
            if (iDotPos < 0) {
              strFileBase = strFileName;
            } else {
              strFileBase = strFileName.substring(0, iDotPos);
            }

            strFileName = strFileBase + ".jpg";
            hRecord.put("fileName", strFileName);

          }
          System.out.println(" - Having: " + strFileName);

          // Go to the fileStats.txt file and get the height and width
          SubSheet oFileStats = new SubSheet(configFile.getProperty("baseLoc")
              + "/" + configFile.getProperty("refLoc") + "/fileStats.txt",
              "fileName", strFileName);

          if (oFileStats != null) {
            List<HashMap<String, String>> lStats = oFileStats.getMembers();

            if (lStats != null && lStats.size() > 0) {
              // There should never be more than one,
              // so we can just get the first one
              HashMap<String, String> hStat = lStats.get(0);

              System.out.println("File Stats Filename: " + strFileName);
              System.out.println("File Stats Size: " + hStat.get("size"));
              System.out.println("File Stats Height: " + hStat.get("height"));
              System.out.println("File Stats Width: " + hStat.get("width"));

              hRecord.put("size", hStat.get("size"));
              hRecord.put("height", hStat.get("height"));
              hRecord.put("width", hStat.get("width"));
            }
          }

          thisUMAM = getUMAM(hRecord);

          // Save the UMAM for reference and debugging
          DoUtils.saveDoc(thisUMAM.getXML(), configFile.getProperty("baseLoc")
              + "/" + configFile.getProperty("umamLoc") + "/" + strPid.trim()
              + ".xml");

          thisDO = new DOxml();
          thisDO.setType("amInfo");
          thisDO.setContModel(strModel);
          thisDO.setStatus("Complete");

          thisDC = new DCxml();
          thisDC.setIdentifier(strPid);
          thisDC.setTitle(strTitle);

          // Fabricate the Zoomify datastream
          thisZoom = new ZOOMxml();
          thisZoom.setTitle(strTitle);

          strSWF = configFile.getProperty("defaultSWF",
              "http://fedora.lib.umd.edu/content/zoom/zoom.swf");
          thisZoom.setSWF(strSWF);
          // Build the Image Path
          System.out.println("ID: " + strID);
          System.out.println("FileName: " + strFileName);
          System.out.println("ID pre: " + strSuperGroup);
          System.out.println("File Base: " + strFileBase);
          // The ImagePath is the image path to the Prange Zooms
          // plus the Prange ID prefix
          // Prange ID
          // Base file name
          strZoomBase = LIMSlookup.getZoomBase(strCollection);
          strZoomBase += "/" + strSuperGroup + "/" + strGroup + "/"
              + strFileBase;

          System.out.println(thisZoom.setImagePath(strZoomBase));

          // Create the UMAM record
          amFOXML = new FedoraXML(strPid, strModel, "UMAM",
              (strBaseDir + "/" + configFile.getProperty("refLoc")));

          amFOXML.setCollection(strCollection);
          amFOXML.setDC(thisDC);
          amFOXML.setDO(thisDO);
          amFOXML.setUMAM(thisUMAM);
          amFOXML.setZoom(thisZoom);

          amFOXML.setExternalImage(strFileName);

          // Add the image, 110 and 250
          // LIMSimage iImage = new LIMSimage( strCollection,
          // strID,
          // strFileName);

          // iImage.make110();
          // iImage.make250();

          // Get the image information for the datastreams'.

          // Save the FOXML
          DoUtils.saveDoc(amFOXML.getFoxml(),
              strBaseDir + "/" + configFile.getProperty("foxmlLoc") + "/"
                  + amFOXML.getPid() + ".xml");

          // Write this pid to the pidlist
          try {
            oPidWriter.write(amFOXML.getPid() + "\n");
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

          // Add this item to the current UMDM METS record
          thisMETS.addPart(iAmPos++, strPid, "DISPLAY", hRecord.get("label"));
        } else {
          System.out.println("This Record had a null filename!");
        }
      }
    }

    // -- End of processing each line of the input spreadsheet --

    // Print out the FOXML for the last UMDM

    dmFOXML.setMETS(thisMETS);

    DoUtils.saveDoc(dmFOXML.getFoxml(), configFile.getProperty("baseLoc") + "/"
        + configFile.getProperty("foxmlLoc") + "/" + dmFOXML.getPid().trim()
        + ".xml");

    try {
      // output the last UMDM
      oUmdmWriter.write(dmFOXML.getPid() + "\n");
      String sHost = configFile.getProperty("host");
      oLinkWriter.write(dmFOXML.getPid() + " - " + strTitle + " - " + strID
          + "\n");
      oLinkWriter.write("Public interface: " + "http://"
          + sHost.replaceAll("fedora", "digital") + "/image.jsp?pid="
          + dmFOXML.getPid() + "\n");
      oLinkWriter.write("Admin Interface: " + "http://" + sHost
          + "/admin/results.jsp?action=search&query1=" + dmFOXML.getPid()
          + "&index1=pid\n");
      oLinkWriter.write("Fedora Object: " + "http://" + sHost + "/fedora/get/"
          + dmFOXML.getPid() + "\n\n");
      oPidWriter.write(dmFOXML.getPid() + "\n");
      oUmdmWriter.close();
      oPidWriter.close();
      oLinkWriter.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return bSuccess;
  }

  private UMDMxml getUMDM(HashMap<String, String> hRecord) {

    UMDMxml thisUMDM = null;
    Element thisElement;
    Element subElement;
    Element sub2Element;
    Element sub3Element;
    Element sub4Element;
    List<Element> aElements;
    String strElement;
    String strTemplateFile = strBaseDir + "/" + configFile.get("refLoc") + "/"
        + hRecord.get("collection").toLowerCase() + "-UMDM.xml";

    System.out.println("File: " + strTemplateFile);

    File fTemplate = new File(strTemplateFile);

    if (fTemplate.exists()) {
      thisUMDM = umf.getUMDM(strTemplateFile, "File");

    } else {
      thisUMDM = umf.getUMDM();
    }

    // OK, the base UMDM is rendered. Now for the detail
    // title
    strElement = hRecord.get("title");
    if ((strElement != null) && (strElement.length() > 0)) {
      thisElement = df.createElement("title");
      thisElement.addAttribute("type", "main");
      thisElement.setText(strElement);
      thisUMDM.addElement(thisElement);
    }

    // title-jp
    strElement = hRecord.get("title-jp");
    if ((strElement != null) && (strElement.length() > 0)) {
      thisElement = df.createElement("title");
      thisElement.addAttribute("type", "main");
      thisElement.addAttribute("xml:lang", "ja");
      thisElement.setText(strElement);
      thisUMDM.addElement(thisElement);
    }

    // title-ro
    strElement = hRecord.get("title-ro");
    if ((strElement != null) && (strElement.length() > 0)) {
      thisElement = df.createElement("title");
      thisElement.addAttribute("type", "main");
      thisElement.addAttribute("xml:lang", "ja-Latn");
      thisElement.setText(strElement);
      thisUMDM.addElement(thisElement);
    }

    // Author
    int iAgent = 1;
    boolean bAgentExists = true;
    String sNumHolder;

    // Get the Author(s)
    while (bAgentExists) {
      sNumHolder = String.valueOf(iAgent);

      strElement = hRecord.get("author" + sNumHolder + "-jp");
      if ((strElement != null) && (strElement.length() > 0)) {
        thisElement = df.createElement("agent");
        thisElement.addAttribute("role", "author");
        thisElement.addAttribute("type", "creator");
        subElement = thisElement.addElement("persName");
        subElement.addAttribute("xml:lang", "ja");
        subElement.setText(strElement);
        thisUMDM.addElement(thisElement);
      } else {
        bAgentExists = false;
      }

      strElement = hRecord.get("author" + sNumHolder + "-ro");
      if ((strElement != null) && (strElement.length() > 0)) {
        thisElement = df.createElement("agent");
        thisElement.addAttribute("role", "author");
        thisElement.addAttribute("type", "creator");
        subElement = thisElement.addElement("persName");
        subElement.addAttribute("xml:lang", "ja-Latn");
        subElement.setText(strElement);
        thisUMDM.addElement(thisElement);
      } else {
        bAgentExists = false;
      }

      iAgent++;
    }

    // Illustrator
    bAgentExists = true;
    iAgent = 1;

    while (bAgentExists) {
      sNumHolder = String.valueOf(iAgent);

      strElement = hRecord.get("illustrator" + sNumHolder + "-jp");
      if ((strElement != null) && (strElement.length() > 0)) {
        thisElement = df.createElement("agent");
        thisElement.addAttribute("role", "illustrator");
        thisElement.addAttribute("type", "contributor");
        subElement = thisElement.addElement("persName");
        subElement.addAttribute("xml:lang", "ja");
        subElement.setText(strElement);
        thisUMDM.addElement(thisElement);
      } else {
        bAgentExists = false;
      }

      strElement = hRecord.get("illustrator" + sNumHolder + "-ro");
      if ((strElement != null) && (strElement.length() > 0)) {
        thisElement = df.createElement("agent");
        thisElement.addAttribute("role", "illustrator");
        thisElement.addAttribute("type", "contributor");
        subElement = thisElement.addElement("persName");
        subElement.addAttribute("xml:lang", "ja-Latn");
        subElement.setText(strElement);
        thisUMDM.addElement(thisElement);
      } else {
        bAgentExists = false;
      }

      iAgent++;
    }

    // Editor
    bAgentExists = true;
    iAgent = 1;

    while (bAgentExists) {
      sNumHolder = String.valueOf(iAgent);

      strElement = hRecord.get("editor" + sNumHolder + "-jp");
      if ((strElement != null) && (strElement.length() > 0)) {
        thisElement = df.createElement("agent");
        // thisElement.addAttribute("role", "editor");
        thisElement.addAttribute("type", "contributor");
        subElement = thisElement.addElement("persName");
        subElement.addAttribute("xml:lang", "ja");
        subElement.setText(strElement);
        thisUMDM.addElement(thisElement);
      } else {
        bAgentExists = false;
      }

      strElement = hRecord.get("editor" + sNumHolder + "-ro");
      if ((strElement != null) && (strElement.length() > 0)) {
        thisElement = df.createElement("agent");
        // thisElement.addAttribute("role", "editor");
        thisElement.addAttribute("type", "contributor");
        subElement = thisElement.addElement("persName");
        subElement.addAttribute("xml:lang", "ja-Latn");
        subElement.setText(strElement);
        thisUMDM.addElement(thisElement);
      } else {
        bAgentExists = false;
      }

      iAgent++;
    }

    // Publisher
    // <agent role="publisher" type="provider"><corpName
    // xml:lang="ja-Hani">清文堂文化教材社</corpName></agent>
    strElement = hRecord.get("publisher");
    if ((strElement != null) && (strElement.length() > 0)) {
      thisElement = df.createElement("agent").addAttribute("role", "publisher")
          .addAttribute("type", "provider");
      subElement = thisElement.addElement("corpName");

      if (hRecord.get("collection").equalsIgnoreCase("Prange")) {
        subElement.addAttribute("xml:lang", "ja");
      }

      subElement.addText(hRecord.get("publisher"));
      thisUMDM.addElement(thisElement);
    }

    // covPlace
    strElement = hRecord.get("continent") + hRecord.get("bloc")
        + hRecord.get("region") + hRecord.get("settlement")
        + hRecord.get("settlement-jp");

    if ((strElement != null) && (strElement.length() > 0)) {

      aElements = thisUMDM.getElements("/descMeta/covPlace");
      if (aElements.size() == 1) {
        thisElement = aElements.get(0);
        // There can only be one soooo - zap the existing one
        thisUMDM.removeElements("/descMeta/covPlace");
      } else {
        thisElement = df.createElement("covPlace");
      }
      // Continent
      // bloc
      // Country
      // Region
      // Settlement
      strElement = hRecord.get("settlement");
      if ((strElement != null) && (strElement.length() > 0)) {
        subElement = df.createElement("geogName")
            .addAttribute("type", "settlement").addText(strElement);

        thisElement.add(subElement);
      }

      // Settlement jp
      strElement = hRecord.get("settlement-jp");
      if ((strElement != null) && (strElement.length() > 0)) {
        subElement = df.createElement("geogName")
            .addAttribute("type", "settlement").addAttribute("xml:lang", "ja")
            .addText(strElement);

        thisElement.add(subElement);
      }

      thisUMDM.addElement(thisElement);
    }

    // date
    strElement = hRecord.get("date");
    if ((strElement != null) && (strElement.length() > 0)) {
      // There can only be 1 covTime - remove existing, if it is there
      thisUMDM.removeElements("/descMeta/covTime");
      thisElement = df.createElement("covTime");
      thisElement.addElement("century").addAttribute("era", "ad")
          .addText("1901-2000");
      thisElement.addElement("date").addAttribute("era", "ad")
          .addText(strElement);
      thisUMDM.addElement(thisElement);
    }

    // description
    strElement = hRecord.get("description");
    if ((strElement != null) && (strElement.length() > 0)) {
      thisElement = df.createElement("description");
      thisElement.setText(strElement);
      thisUMDM.addElement(thisElement);
    }

    // subjTopical
    strElement = hRecord.get("subjTopical");
    if ((strElement != null) && (strElement.length() > 0)) {
      thisElement = df.createElement("subject");
      thisElement.addAttribute("type", "topical");
      thisElement.setText(strElement);
      thisUMDM.addElement(thisElement);
    }

    // subjBrowse
    strElement = hRecord.get("subjBrowse");
    if ((strElement != null) && (strElement.length() > 0)) {
      String[] strList = strElement.split(", ");
      for (String thisSubj : strList) {
        if (thisSubj.length() > 0) {
          thisElement = df.createElement("subject");
          thisElement.addAttribute("type", "browse");
          thisElement.setText(strElement);
          thisUMDM.addElement(thisElement);

        }
      }
    }

    // subjLCSH
    strElement = hRecord.get("subjLCSH");
    if ((strElement != null) && (strElement.length() > 0)) {
      String[] strList = strElement.split(", ");
      for (String thisSubj : strList) {
        if (thisSubj.length() > 0) {
          thisElement = df.createElement("subject");
          thisElement.addAttribute("type", "LCSH");
          thisElement.setText(strElement);
          thisUMDM.addElement(thisElement);

        }
      }
    }

    // style
    strElement = hRecord.get("style");
    if ((strElement != null) && (strElement.length() > 0)) {
      String[] strList = strElement.split(", ");
      for (String thisSubj : strList) {
        if (thisSubj.length() > 0) {
          thisElement = df.createElement("style");
          thisElement.setText(strElement);
          thisUMDM.addElement(thisElement);

        }
      }
    }

    // collection identifier
    strElement = hRecord.get("id");
    if ((strElement != null) && (strElement.length() > 0)) {
      thisElement = df.createElement("identifier");
      thisElement
          .addAttribute("label", hRecord.get("collection").toLowerCase());
      thisElement.setText(strElement);
      thisUMDM.addElement(thisElement);
    }

    // Book Size
    // <physDesc><size units="cm">25 x 18</size></physDesc>
    strElement = hRecord.get("bookSize");
    if ((strElement != null) && (strElement.length() > 0)) {
      System.out.println("Book Size: " + strElement);
      aElements = thisUMDM.getElements("/descMeta/physDesc");
      if (aElements.size() == 1) {
        thisElement = aElements.get(0);
        // There can only be one soooo - zap the existing one
        thisUMDM.removeElements("/descMeta/physDesc");
      } else {
        thisElement = df.createElement("physDesk");
      }
      thisElement.addElement("size").addAttribute("units", "cm")
          .addText(strElement);
      thisUMDM.addElement(thisElement);
    }

    // Page Count
    // <physDesc><extent units="pages">12</extent></physDesc>
    strElement = hRecord.get("pageCount");
    if ((strElement != null) && (strElement.length() > 0)) {
      if (strElement.indexOf('.') > 0) {
        strElement = strElement.substring(0, strElement.indexOf('.'));
      }
      aElements = thisUMDM.getElements("/descMeta/physDesc");
      if (aElements.size() == 1) {
        thisElement = aElements.get(0);
        // There can only be one soooo - zap the existing one
        thisUMDM.removeElements("/descMeta/physDesc");
      } else {
        thisElement = df.createElement("physDesk");
      }
      thisElement.addElement("extent").addAttribute("units", "pages")
          .addText(strElement);
      thisUMDM.addElement(thisElement);
    }

    // Price Yen
    // <description type="bibref">
    // <bibref><imprint><availability><price units="yen">10 円</price>
    // </availability></imprint></bibref></description>
    strElement = hRecord.get("price-yen");
    String strWholeYen;
    if ((strElement != null) && (strElement.length() > 0)) {

      // Set the number as we will wish to display it
      // get the integer portion into strWholeYen
      if (strElement.indexOf('.') > 0) {
        strWholeYen = strElement.substring(0, strElement.indexOf('.'));
      } else {
        strWholeYen = strElement;
      }

      // If there is no fractional part, then integer only, otherwise leave it
      // alone. If we fail to parse it, just store what we are given
      try {
        if (Float.parseFloat(strWholeYen) == Float.parseFloat(strElement)) {
          strElement = strWholeYen;
        }
      } catch (NumberFormatException e) {
        // TODO Auto-generated catch block
        // Just do nothing as strElement has basically what we want anyway.
        // It is just going in as is with the yen symbol appended
      }

      // first get the element from the record if it already exists
      aElements = thisUMDM.getElements("/descMeta/description[@type='bibref']");
      if (aElements.size() >= 1) {
        thisElement = aElements.get(0);
        // There can only be one soooo - zap the existing one
        thisUMDM.removeElements("/descMeta/description[@type='bibref']");
      } else {
        thisElement = df.createElement("description").addAttribute("type",
            "bibRef");
      }
      subElement = thisElement.element("bibref");
      if (subElement == null) {
        subElement = thisElement.addElement("bibRef");
      }
      sub2Element = subElement.element("imprint");
      if (sub2Element == null) {
        sub2Element = subElement.addElement("imprint");
      }
      sub3Element = sub2Element.element("availability");
      if (sub3Element == null) {
        sub3Element = sub2Element.addElement("availability");
      }
      sub4Element = sub3Element.element("price");
      if (sub4Element == null) {
        sub4Element = sub3Element.addElement("price");
      }
      Attribute aUnits = sub4Element.attribute("units");
      if (aUnits != null) {
        if (aUnits.getValue().equals("yen")) {
          sub4Element.setText(strElement + " 円");
        } else {
          sub4Element.addAttribute("units", "yen").setText(strElement + " 円");
        }

      } else {
        sub4Element.addAttribute("units", "yen").setText(strElement + " 円");
      }
      thisUMDM.addElement(thisElement);
    }

    // ccdNumber
    // ---> <identifier type="pcbccd" label="number">P-6643</identifier>
    // <physDesc type="pcbccb" label="documents">1</physDesc>
    // <identifier type="pcbccd" label="classification">Lit-Hum</identifier>
    strElement = hRecord.get("ccdNumber1");
    if ((strElement != null) && (strElement.length() > 0)) {

      int nIDcounter = 1;
      String sIDlabel = "ccdNumber";
      boolean bProcessThisOne = true;

      while (bProcessThisOne) {
        // truncate any extraneous .0 that may have appeared on a plain nu8mber
        // identifier.
        strElement = hRecord.get("ccdNumber" + String.valueOf(nIDcounter++));
        if ((strElement != null) && (strElement.length() > 0)) {
          if (strElement.indexOf(".0") > 0) {
            strElement = strElement.substring(0, strElement.indexOf('.'));
          }
          thisElement = df.createElement("identifier")
              .addAttribute("type", "pcbccd").addAttribute("label", "number")
              .addText(strElement);
          thisUMDM.addElement(thisElement);
        } else {
          bProcessThisOne = false;
        }
      }
    }

    // censorshipAction
    // <subject type="censorshipAction">n</subject>
    // ----> <description label="pcbcensorship">n</description>

    strElement = hRecord.get("censorshipAction");
    if ((strElement != null) && (strElement.length() > 0)) {
      thisElement = df.createElement("description")
          .addAttribute("label", "pcbcensorship").addText(strElement);
      thisUMDM.addElement(thisElement);

      // 4 values have Japanese equivalents that are added
      if (strElement.equalsIgnoreCase("violation")) {
        thisElement = df.createElement("description")
            .addAttribute("label", "pcbcensorship")
            .addAttribute("xml:lang", "ja").addText("違反");
        thisUMDM.addElement(thisElement);
      } else if (strElement.equalsIgnoreCase("disapproval")) {
        thisElement = df.createElement("description")
            .addAttribute("label", "pcbcensorship")
            .addAttribute("xml:lang", "ja").addText("不許可");
        thisUMDM.addElement(thisElement);
      } else if (strElement.equalsIgnoreCase("deletion")) {
        thisElement = df.createElement("description")
            .addAttribute("label", "pcbcensorship")
            .addAttribute("xml:lang", "ja").addText("削除");
        thisUMDM.addElement(thisElement);
      } else if (strElement.equalsIgnoreCase("suppressed")) {
        thisElement = df.createElement("description")
            .addAttribute("label", "pcbcensorship")
            .addAttribute("xml:lang", "ja").addText("出版差止め");
        thisUMDM.addElement(thisElement);
      }
    }

    return thisUMDM;
  }

  private UMAMxml getUMAM(HashMap<String, String> hRecord) {

    UMAMxml thisUMAM = null;
    Element thisElement;
    String strElement;
    String strTemplateFile = strBaseDir + "/" + configFile.get("refLoc") + "/"
        + hRecord.get("collection").toLowerCase() + "-UMAM.xml";

    System.out.println("File: " + strTemplateFile);

    File fTemplate = new File(strTemplateFile);

    if (fTemplate.exists()) {
      thisUMAM = umf.getUMAM(strTemplateFile, "File");

    } else {
      thisUMAM = umf.getUMAM();
    }

    // OK, the base UMAM is rendered. Now for the detail
    // identifier
    strElement = hRecord.get("fileName");
    if ((strElement != null) && (strElement.length() > 0)) {
      // There can only be 1 identifier - remove existing, if it is there
      thisUMAM.removeElements("/adminMeta/identifier");
      thisElement = df.createElement("identifier");
      thisElement.setText(strElement);
      thisUMAM.addElement(thisElement);
    }

    // Then put in the file stats
    List<Element> lTechnical = thisUMAM.removeElements("/adminMeta/technical");

    if (lTechnical != null) {

      System.out.println("Get UMAM - Got the technical element list.");

      Document dTechnical = df.createDocument(lTechnical.get(0));

      if (dTechnical != null) {
        System.out.println("Get UMAM - Got the technical element.");
      }

      // size
      strElement = hRecord.get("size");
      if ((strElement == null) || (strElement.length() < 1)) {
        strElement = "";
      }
      thisElement = (Element) DoUtils.getXPath("/technical/fileSize")
          .selectSingleNode(dTechnical);
      if (thisElement != null) {
        thisElement.setText(strElement);
      } else {
        System.out.println("Get UMAM - Null size element, Bother!");
      }

      // height (length)
      strElement = hRecord.get("height");
      if ((strElement == null) || (strElement.length() < 1)) {
        strElement = "";
      }
      thisElement = (Element) DoUtils.getXPath(
          "/technical/image/spatialMetrics/imageLength").selectSingleNode(
          dTechnical);
      if (thisElement != null) {
        thisElement.setText(strElement);
      }

      // width
      strElement = hRecord.get("width");
      if ((strElement == null) || (strElement.length() < 1)) {
        strElement = "";
      }
      thisElement = (Element) DoUtils.getXPath(
          "/technical/image/spatialMetrics/imageWidth").selectSingleNode(
          dTechnical);
      if (thisElement != null) {
        thisElement.setText(strElement);
      }

      DoUtils.saveDoc(dTechnical, "Terminal");

      thisUMAM.addElement((Element) dTechnical.getRootElement().detach());

    }

    return thisUMAM;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    LoadBatch thisBatch;
    if (args.length > 0) {
      thisBatch = new LoadBatch(args[0]);
      if (thisBatch.process()) {
        System.out.println("Done!");
      } else {
        System.out.println("Fail!");
      }
    }
  }

}
