package edu.umd.lib.fedora.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.dom4j.DocumentFactory;

import edu.umd.lib.fedora.sweep.SubSheet;
import edu.umd.lib.fedora.util.DO.LIMSlookup;
import edu.umd.lib.fedora.util.foxml.LIMSimage;
import edu.umd.lib.fedora.util.foxml.UMfactory;
import edu.umd.lib.util.excelReader.ExcelReader;

public class LoadImages {

  private final Properties configFile = new Properties();
  private OutputStreamWriter oFileStatWriter;

  private String strGroup = "416-001";
  private String strSuperGroup = "416";
  private String strSourcePath = null;
  private String strDestPath = null;
  private String strTitle = null;

  private int iPidCount = 0;
  private int iErrorCount = 0;

  private boolean bWritePids = false;

  public LoadImages(String propFile, String fileName) {
    try {
      configFile.load(new FileInputStream(propFile));
      new UMfactory(configFile.getProperty("host"));
      new DocumentFactory();

      // If a file name was passed in, make it the batch file
      if (fileName != null && fileName.length() > 0) {
        configFile.put("batchFile", fileName);
      }

      if (configFile.getProperty("writePids").equalsIgnoreCase("Y")) {
        bWritePids = true;
      }

      // Setup the UMAM list output file
      oFileStatWriter = new OutputStreamWriter(new FileOutputStream(
          "fileStats.txt"), "UTF-8");
      oFileStatWriter.write("fileName\twidth\theight\tsize\n");

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
    String strID = "999-998";
    String strModel = "Book";
    // Set up the TabText object from the properties File
    /*
     * batchFile is the file containing the records to be ingested.
     */

    HashMap<String, String> hRecord;
    Iterator<HashMap<String, String>> iSourceFile;
    ExcelReader xSourceFile;

    try {
      xSourceFile = new ExcelReader(configFile.getProperty("baseLoc") + "/"
          + configFile.getProperty("dataLoc") + "/"
          + configFile.getProperty("batchFile"));

      System.out.println(configFile.getProperty("baseLoc") + "/"
          + configFile.getProperty("dataLoc") + "/"
          + configFile.getProperty("batchFile"));

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

      // The input batch file
      // TabText oSourceFile = new
      // TabText(configFile.getProperty("baseLoc") + "/" +
      // configFile.getProperty("refLoc") + "/" +
      // configFile.getProperty("batchFile"));

      // Iterate through the TabText File
      // for (HashMap<String, String> hRecord : oSourceFile) {

      // System.out.println("---");
      // System.out.println("id: " + hRecord.get("id"));
      // System.out.println("pid: " + hRecord.get("pid"));
      // System.out.println("type: " + hRecord.get("type"));
      // System.out.println("title: " + hRecord.get("title-jp"));
      // System.out.println("date: " + hRecord.get("date"));
      // System.out.println("description: " + hRecord.get("description"));
      // System.out.println("subjTopical: " + hRecord.get("subjTopical"));
      // System.out.println("repository: " + hRecord.get("repository"));
      // System.out.println("label: " + hRecord.get("label"));
      // System.out.println("fileName: " + hRecord.get("fileName"));
      // System.out.println("---");

      // We are starting a new object

      // Set the collection
      if ((hRecord.get("collection") != null)
          && (hRecord.get("collection").length() > 0)) {
        strCollection = hRecord.get("collection");
      } else {
        hRecord.put("collection", strCollection);
      }

      // Set the Content Model which is under the type column in the
      // spreadsheet
      // This should not be null in any record
      if ((hRecord.get("type") != null) && (hRecord.get("type").length() > 0)) {
        strModel = LIMSlookup.getContentModel(hRecord.get("type"));
      } else {
        hRecord.put("type", strModel);
      }

      // The id column is supposed to be blank in all UMAM records
      if ((hRecord.get("id") != null) && (hRecord.get("id").length() > 0)) {

        // ... so this is an object record (UMDM)

        // Collect the data for the new UMDM object
        strID = hRecord.get("id");

        strGroup = strID;
        String[] aGroupParts = strGroup.split("-");
        if (aGroupParts.length == 2) {
          strSuperGroup = aGroupParts[0];
        }

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

        bSuccess = true;

        iPidCount++;

      } else if (hRecord.get("fileName") != null) {

        // This is a file/administrative record (UMAM)

        // Put the stored ID value into the hRecord
        hRecord.put("id", strID);

        // OK Now process the UMAM for each file name
        String strFileName = hRecord.get("fileName");

        // First lets find the file

        // Normalize the filename on jpg
        if (strFileName.matches("\\.tif")) {
          strFileName = strFileName.replace(".tif", ".jpg");
        } else if (strFileName.matches("\\.jpeg")) {
          strFileName = strFileName.replace(".jpeg", ".jpg");
        } else if (!strFileName.matches("\\.jpg")) {
          strFileName += ".jpg";
        }

        if (strCollection.equalsIgnoreCase("Prange")) {
          int iFileCounter = 0;

          String strTempPath = null;
          String strFullPath = null;
          boolean bFound = false;

          // Has a path already been found?
          if (strSourcePath != null && strSourcePath.length() > 0) {
            strTempPath = strSourcePath + "/" + strFileName;

            if (new File(strTempPath).exists()) {
              iFileCounter++;
              strFullPath = strTempPath;
            } else {
              bFound = false;
            }
          }

          if (!bFound) {

            // List<String> lCollBases = LIMSlookup.getPrangeBases();
            //
            // for (String strBase : lCollBases) {
            //
            // strTempPath = strBase + "/" + strGroup + "/" + strFileName;
            //
            // System.out
            // .println("Looking in " + strBase + " at " + strTempPath);
            //
            // if (new File(strTempPath).exists()) {
            // iFileCounter++;
            // strSourcePath = strBase;
            // strFullPath = strTempPath;
            // bFound = true;
            // // This will set the path to the last one found
            // // which is hopefully the only one found
            // }
            // }

            // Get the name of the directory listing

            String sDirlist = configFile.getProperty("baseLoc") + "/"
                + configFile.getProperty("refLoc") + "/"
                + configFile.getProperty("dirList");

            SubSheet oSubSheet = new SubSheet(sDirlist, "File Name",
                strFileName);

            if (oSubSheet != null) {

              List<HashMap<String, String>> lMatches = oSubSheet.getMembers();

              if (lMatches.size() > 0) {

                String strTempDate = "";

                // Get the Path from the record with the most recent Date
                for (HashMap<String, String> hMatch : lMatches) {

                  if (strTempDate == ""
                      || strTempDate.compareTo(hMatch.get("Date")) < 0) {
                    strTempDate = hMatch.get("Date");
                    strTempPath = hMatch.get("Path");
                  }
                }

                // Check to see if the file exists

                if (new File(strTempPath + "/" + strFileName).exists()) {
                  iFileCounter = lMatches.size();
                  strSourcePath = strTempPath;
                  strFullPath = strTempPath + "/" + strFileName;
                  bFound = true;
                  // This will set the path to the last one found
                  // which is hopefully the only one found
                }
              }
            }
          }

          if (bFound) {
            System.out.println("Found " + strFileName + " at " + strSourcePath);
          } else {
            System.out.println("Error: " + strFileName + " not Found!!!");
            iErrorCount++;
          }

          if (iFileCounter > 0) {
            if (iFileCounter > 1) {
              System.out.println("Found more than one, boss.");
            }

            /*
             * For Prange, as a space savings measure, we link the file to its
             * actual location rather than copy or move. Create the link if it
             * does not exist already
             */
            String strPathBase = LIMSlookup.getCollectionBase(strCollection);
            Process process;
            strDestPath = strPathBase + "/" + strSuperGroup + "/" + strGroup;
            try {
              if (!new File(strDestPath).exists()) {
                System.out.println(strDestPath + " does not exist.");
                new File(strDestPath).mkdirs();
              } else {
                System.out.println(strDestPath + " does exist!");
              }

              if (!new File(strDestPath + "/" + strFileName).exists()) {
                process = Runtime.getRuntime().exec(
                    new String[] { "ln", "-s", strFullPath,
                        (strDestPath + "/" + strFileName) });
                process.waitFor();
                process.destroy();
              }

              // OK, now that we have found the graphic and linked
              // it,
              // we need to create and place the derivatives: 110,
              // 250, Zoom

              LIMSimage thisImage = new LIMSimage(strDestPath, strFileName);

              if (thisImage != null) {

                thisImage.makeThumbs(110);
                thisImage.makeThumbs(250);

                // and now for the Zoom
                String sZoomPath = LIMSlookup.getZoomFileBase(strCollection)
                    + "/" + strSuperGroup + "/" + strGroup;

                if (!testZoom(sZoomPath, strFileName)) {

                  thisImage.makeZoom(sZoomPath);
                }

                oFileStatWriter.write(thisImage.getFileName() + "\t"
                    + thisImage.getWidth() + "\t" + thisImage.getHeight()
                    + "\t" + thisImage.getFileSize() + "\n");

              }

            } catch (IOException e) {
              e.printStackTrace();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }

            iPidCount++;

          }
        } else {
          // This is where the code for non-Prange images goes
        }
      }
    }
    try {
      oFileStatWriter.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    if (iErrorCount < 1 && bWritePids) {
      // If nothing bad happened, then get the pids
      String pidCmd = "/apps/fedora/bin/fedora-getpid -n umd -c " + iPidCount;

      System.out.println("Pids to be Generated - " + pidCmd);

      try {
        Process p = Runtime.getRuntime().exec(pidCmd);
        p.waitFor();

        // Write out the list of pids
        BufferedReader b = new BufferedReader(new InputStreamReader(
            p.getInputStream()));
        String line = "";

        OutputStreamWriter oPidWriter = new OutputStreamWriter(
            new FileOutputStream("newPids.txt"), "UTF-8");

        if (oPidWriter != null) {

          while ((line = b.readLine()) != null) {
            oPidWriter.write(line + " \n");
          }

          oPidWriter.close();
        }

        b = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        line = "";

        while ((line = b.readLine()) != null) {
          System.out.println("err: " + line);
        }

      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    } else {
      System.out.println("No Pids to be Generated - check the log.");
    }

    return bSuccess;
  }

  private boolean testZoom(String sZoomPath, String sFileName) {

    boolean bResult = false;
    String sFileBase;

    // Test for the existence of the zoom parent directory and that it is a
    // directory
    if (new File(sZoomPath).exists()) {
      sFileBase = sFileName.substring(0, sFileName.indexOf('.'));

      // Check for the existence of the zoom directory
      if (new File(sZoomPath + "/" + sFileBase).exists()) {

        // Check for the existence of the last file created in processing zooms
        // If this file exists, then we should be OK
        if (new File(sZoomPath + "/" + sFileBase + "/ImageProperties.xml")
            .exists()) {
          bResult = true;
        }
      }
    }

    return bResult;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    if (args.length == 2) {
      LoadImages thisBatch = new LoadImages(args[0], args[1]);
      if (thisBatch.process()) {
        System.out.println("Done!");
      } else {
        System.out.println("Fail!");
      }
    }
  }

}
