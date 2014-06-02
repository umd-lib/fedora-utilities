package edu.umd.lib.fedora.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.lib.fedora.sweep.SubSheet;
import edu.umd.lib.fedora.util.DO.LIMSlookup;
import edu.umd.lib.fedora.util.foxml.LIMSimage;
import edu.umd.lib.util.excelReader.ExcelReader;

public class LoadImages {

  private static final Logger log = LoggerFactory.getLogger(ValidateData.class);

  private static boolean debug = false;
  private static File log4jConfig;

  private final Properties configFile = new Properties();

  private String strGroup = "416-001";
  private String strSuperGroup = "416";
  private String sourcePath = null;
  private String destPath = null;

  public LoadImages(String propFile, String fileName) {
    try {
      configFile.load(new FileInputStream(propFile));
      // new UMfactory(configFile.getProperty("host"));
      // new DocumentFactory();

      // Setup logging
      if (log4jConfig != null) {
        log.debug(log4jConfig.getAbsolutePath());
        PropertyConfigurator.configure(log4jConfig.getAbsolutePath());
      } else {
        InputStream inputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("edu/umd/lib/fedora/loader/log4j.conf");

        Properties pLog4j = new Properties();
        pLog4j.load(inputStream);
        PropertyConfigurator.configure(pLog4j);
      }

      if (debug) {
        org.apache.log4j.Logger.getLogger(ValidateData.class).setLevel(
            Level.DEBUG);
      } else {
        org.apache.log4j.Logger.getLogger(ValidateData.class).setLevel(
            Level.INFO);
      }

      // If a file name was passed in, make it the batch file
      if (fileName != null && fileName.length() > 0) {
        configFile.put("batchFile", fileName);
      }

    } catch (FileNotFoundException e) {
      log.error("Error: File not found", e);
    } catch (IOException e) {
      log.error("Error: IO Exception", e);
    }
  }

  public boolean process() {

    boolean bSuccess = false;
    String strCollection = "Misc";
    String strID = "999-998";
    String strModel = "Book";

    /*
     * batchFile is the file containing the records to be ingested.
     */

    HashMap<String, String> hRecord;
    Iterator<HashMap<String, String>> iSourceFile;
    ExcelReader xSourceFile;

    // Get and open the iterator in the Excel Spreadsheet
    try {
      xSourceFile = new ExcelReader(configFile.getProperty("baseLoc") + "/"
          + configFile.getProperty("dataLoc") + "/"
          + configFile.getProperty("batchFile"));

      log.debug(configFile.getProperty("baseLoc") + "/"
          + configFile.getProperty("dataLoc") + "/"
          + configFile.getProperty("batchFile"));

      iSourceFile = xSourceFile.iterator();

    } catch (IOException e1) {
      log.error("Error; Bad things happenned with the Excel File", e1);
      iSourceFile = null;
    }

    // Iterate through the File
    while (iSourceFile.hasNext()) {
      // System.out.println(it.next());
      hRecord = iSourceFile.next();

      if (debug) {
        String debugMessage = "---\n";
        debugMessage += "id: " + hRecord.get("id") + "\n";
        debugMessage += "pid: " + hRecord.get("pid") + "\n";
        debugMessage += "type: " + hRecord.get("type") + "\n";
        debugMessage += "title: " + hRecord.get("title-jp") + "\n";
        debugMessage += "date: " + hRecord.get("date") + "\n";
        debugMessage += "description: " + hRecord.get("description") + "\n";
        debugMessage += "subjTopical: " + hRecord.get("subjTopical") + "\n";
        debugMessage += "repository: " + hRecord.get("repository") + "\n";
        debugMessage += "label: " + hRecord.get("label") + "\n";
        debugMessage += "fileName: " + hRecord.get("fileName") + "\n";
        debugMessage += "---" + "\n";
        log.debug(debugMessage);
      }

      // We are starting a new object

      // Set the collection
      if ((hRecord.get("collection") != null)
          && (hRecord.get("collection").length() > 0)) {
        strCollection = hRecord.get("collection");
      } else {
        hRecord.put("collection", strCollection);
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

        log.info("Processing: " + strID);

        String strCollectionPid = LIMSlookup.getCollectionPid(strCollection);
        log.debug("Content Model: " + strModel);
        log.debug("Collection Pid: " + strCollectionPid);

        bSuccess = true;

      } else if (hRecord.get("fileName") != null) {

        // This is a file/administrative record (UMAM)

        // Put the stored ID value into the hRecord
        hRecord.put("id", strID);

        // OK Now process the UMAM for each file name
        String fileName = hRecord.get("fileName");

        // First lets find the file

        // Normalize the filename on jpg
        if (fileName.matches("\\.tif")) {
          fileName = fileName.replace(".tif", ".jpg");
        } else if (fileName.matches("\\.jpeg")) {
          fileName = fileName.replace(".jpeg", ".jpg");
        } else if (!fileName.matches("\\.jpg")) {
          fileName += ".jpg";
        }

        if (strCollection.equalsIgnoreCase("Prange")) {
          int iFileCounter = 0;

          String strTempPath = null;
          String strFullPath = null;
          boolean bFound = false;

          // Has a path already been found?
          if (sourcePath != null && sourcePath.length() > 0) {
            strTempPath = sourcePath + "/" + fileName;

            if (new File(strTempPath).exists()) {
              iFileCounter++;
              strFullPath = strTempPath;
            } else {
              bFound = false;
            }
          }

          if (!bFound) {

            // Get the name of the directory listing

            String sDirlist = configFile.getProperty("baseLoc") + "/"
                + configFile.getProperty("refLoc") + "/"
                + configFile.getProperty("dirList");

            SubSheet oSubSheet = new SubSheet(sDirlist, "File Name", fileName);

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

                if (new File(strTempPath + "/" + fileName).exists()) {
                  iFileCounter = lMatches.size();
                  sourcePath = strTempPath;
                  strFullPath = strTempPath + "/" + fileName;
                  bFound = true;
                  // This will set the path to the last one found
                  // which is hopefully the only one found
                }
              }
            }
          }

          if (bFound) {
            log.debug("Found " + fileName + " at " + sourcePath);
          } else {
            log.error(fileName + " not Found!!!");
          }

          if (iFileCounter > 0) {
            if (iFileCounter > 1) {
              log.debug("Found more than one, boss.");
            }

            /*
             * For Prange, as a space savings measure, we link the file to its
             * actual location rather than copy or move. Create the link if it
             * does not exist already
             */
            String strPathBase = LIMSlookup.getCollectionBase(strCollection);
            Process process;
            destPath = strPathBase + "/" + strSuperGroup + "/" + strGroup;
            try {
              if (!new File(destPath).exists()) {
                log.debug(destPath + " does not exist.");
                new File(destPath).mkdirs();
              } else {
                log.debug(destPath + " does exist!");
              }

              if (!new File(destPath + "/" + fileName).exists()) {
                process = Runtime.getRuntime().exec(
                    new String[] { "ln", "-s", strFullPath,
                        (destPath + "/" + fileName) });
                process.waitFor();
                process.destroy();
              }

              // OK, now that we have found the graphic and linked
              // it,
              // we need to create and place the derivatives: 110,
              // 250, Zoom

              LIMSimage thisImage = new LIMSimage(destPath, fileName);

              if (thisImage != null) {

                thisImage.makeThumbs(110);
                thisImage.makeThumbs(250);

                // and now for the Zoom
                String sZoomPath = LIMSlookup.getZoomFileBase(strCollection)
                    + "/" + strSuperGroup + "/" + strGroup;

                if (!testZoom(sZoomPath, fileName)) {
                  thisImage.makeZoom(sZoomPath);
                }

                if (!testZoom(sZoomPath, fileName)) {
                  log.error("Error: Zoomify failed for " + fileName);
                }

              }

            } catch (IOException e) {
              e.printStackTrace();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }

          }
        } else {
          // This is where the code for non-Prange images goes
        }
      }
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
