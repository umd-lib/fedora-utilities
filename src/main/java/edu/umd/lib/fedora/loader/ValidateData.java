package edu.umd.lib.fedora.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.lib.fedora.sweep.SubSheet;
import edu.umd.lib.fedora.util.DO.LIMSlookup;
import edu.umd.lib.fedora.util.foxml.LIMSimage;
import edu.umd.lib.util.excelReader.ExcelReader;
import fedora.client.FedoraClient;
import fedora.client.Uploader;
import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;

public class ValidateData {

  /**
   * @param args
   */
  private static final Logger log = LoggerFactory.getLogger(ValidateData.class);

  private static boolean debug = false;
  private static File log4jConfig;

  private final Properties configFile = new Properties();
  private OutputStreamWriter oFileStatWriter;

  private String group = "416-001";
  private String superGroup = "416";
  private String sourcePath = null;
  private String destPath = null;
  private String title = null;

  private FedoraClient client = null;
  private final FedoraAPIA APIA = null;
  private final FedoraAPIM APIM = null;
  private final Uploader uploader = null;

  private int pidCount = 0;
  private int umdmCount = 0;
  private int umamCount = 0;

  private boolean bWritePids = false;
  private final int localPid = 0;
  private final String localPrefix = "local";

  private String baseFileName = "default";

  private int errorCount = 0;
  private ArrayList<String> processingErrors;

  public ValidateData(String propFile, String fileName) {
    try {

      // check that the property file exists
      if (new File(propFile).exists()) {
        configFile.load(new FileInputStream(propFile));
        // new UMfactory(configFile.getProperty("host"));
        // new DocumentFactory();

        // Initialize errors
        processingErrors = new ArrayList<String>();

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
          String[] fileparts = fileName.split("\\.(?=[^\\.]+$)");
          baseFileName = fileparts[0];
        }

        if (configFile.getProperty("writePids").equalsIgnoreCase("Y")) {
          bWritePids = true;
        }

        // Get the filename base so that we can use it for the output file
        // prefix.

        // Setup the UMAM list output file
        oFileStatWriter = new OutputStreamWriter(new FileOutputStream(
            baseFileName + "-fileStats.txt"), "UTF-8");
        oFileStatWriter.write("fileName\twidth\theight\tsize\n");

      } else {
        log.error("Error: Propfile: " + propFile + " does not exist.");
        errorCount++;
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
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

      log.debug(configFile.getProperty("baseLoc") + "/"
          + configFile.getProperty("dataLoc") + "/"
          + configFile.getProperty("batchFile"));

      iSourceFile = xSourceFile.iterator();

    } catch (IOException e1) {
      e1.printStackTrace();
      iSourceFile = null;
    }

    // Iterate through the File

    while (iSourceFile.hasNext()) {

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

      // Set the Content Model which is under the type column in the
      // spreadsheet. This should not be null in any record
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

        group = strID;
        String[] aGroupParts = group.split("-");
        if (aGroupParts.length == 2) {
          superGroup = aGroupParts[0];
        }

        if ((hRecord.get("title") != null)
            && (hRecord.get("title").length() > 0)) {
          title = hRecord.get("title");
        } else if ((hRecord.get("title-jp") != null)
            && (hRecord.get("title-jp").length() > 0)) {
          title = hRecord.get("title-jp");
        }

        log.info("Processing: " + strID);

        String strCollectionPid = LIMSlookup.getCollectionPid(strCollection);
        log.debug("Content Model: " + strModel);
        log.debug("Collection Pid: " + strCollectionPid);

        bSuccess = true;

        pidCount++;
        umdmCount++;

      } else if (hRecord.get("fileName") != null) {

        // This is a file/administrative record (UMAM)

        // Put the stored ID value into the hRecord
        hRecord.put("id", strID);

        // OK Now process the UMAM for each file name
        String fileName = hRecord.get("fileName");

        // First lets find the file

        // Normalize the filename on jpg
        // This splits the file name into an array of basename
        // and extension(if it exists).
        // We do not care what the extension was or if it was.
        // the extension will now be jpg
        String[] fileParts = fileName.split("\\.(?=[^\\.]+$)");
        fileName = fileParts[0].concat(".jpg");

        if (strCollection.equalsIgnoreCase("Prange")) {
          int iFileCounter = 0;

          String tempPath = null;
          String strFullPath = null;
          boolean bFound = false;

          // Has a path already been found?
          if (sourcePath != null && sourcePath.length() > 0) {
            tempPath = sourcePath + "/" + fileName;

            if (new File(tempPath).exists()) {
              iFileCounter++;
              strFullPath = tempPath;
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
                    tempPath = hMatch.get("Path");
                  }
                }

                // Check to see if the file exists

                if (new File(tempPath + "/" + fileName).exists()) {
                  iFileCounter = lMatches.size();
                  sourcePath = tempPath;
                  strFullPath = tempPath + "/" + fileName;
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
            processingErrors.add(fileName + " not Found!!!");
            errorCount++;
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
            destPath = strPathBase + "/" + superGroup + "/" + group;
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

              // OK, now that we have found the graphic

              LIMSimage thisImage = new LIMSimage(destPath, fileName);

              if (thisImage != null) {

                // Record the vital statistics for the file
                oFileStatWriter.write(thisImage.getFileName() + "\t"
                    + thisImage.getWidth() + "\t" + thisImage.getHeight()
                    + "\t" + thisImage.getFileSize() + "\n");

              }

            } catch (IOException e) {
              e.printStackTrace();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }

            pidCount++;
            umamCount++;
          }
        } else {
          // This is where the code for non-Prange images goes
        }
      }
    }
    try {
      oFileStatWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (errorCount < 1 && bWritePids) {

      // If nothing bad happened, then get the pids
      FedoraAPIA APIA = null;
      FedoraAPIM APIM = null;
      // Get the pids through the fedora API-M
      try {
        FedoraClient fc = new FedoraClient(configFile.getProperty("host"),
            configFile.getProperty("user"), configFile.getProperty("pw"));
        APIA = fc.getAPIA();
        APIM = fc.getAPIM();
      } catch (MalformedURLException e1) {
        log.error("MAlformed URL on pid retrieval.", e1);
      } catch (Exception e) {
        log.error("APIA or APIM failed.", e);
      }

      String pidCmd = "/apps/fedora/bin/fedora-getpid -n umd -c " + pidCount;

      log.debug("Pids to be Generated - " + pidCmd);

      try {

        String[] aPids = getPids(pidCount);

        OutputStreamWriter oPidWriter = new OutputStreamWriter(
            new FileOutputStream(baseFileName + "-newPids.txt"), "UTF-8");

        if (oPidWriter != null && aPids != null && aPids.length == pidCount) {

          for (String pidVal : aPids) {
            oPidWriter.write(pidVal + " \n");
          }

          oPidWriter.close();

        } else {
          processingErrors.add("Pid Getting error");
          errorCount++;
        }

      } catch (Exception e) {
        e.printStackTrace();
      }

    } else {
      log.warn("No Pids to be Generated - check the log.");
    }

    // Print out the errors, if necessary

    // print out the counts
    log.info("pid Count: " + pidCount);
    log.info("UMDM Count: " + umdmCount);
    log.info("UMAM Count: " + umamCount);

    return bSuccess;
  }

  public String getConnection() {

    log.debug("Connection: Fedora Host: " + configFile.getProperty("host")
        + " Fedora Port: " + configFile.getProperty("port"));
    String connection = "http://" + configFile.getProperty("host") + ":"
        + configFile.getProperty("port") + "/fedora";

    return connection;
  }

  public int getFedoraClientStatus() {

    GetMethod getMethod = new GetMethod(getConnection());
    int responseCode = 0;

    try {
      if (client != null) {
        responseCode = client.getHttpClient().executeMethod(getMethod);
      } else {
        log.error("Fedora client has not been initialized.");
      }
    } catch (IOException e) {
      log.error("Cannot get Fedora client status");
    }

    log.info("Client response code: " + responseCode);

    return responseCode;
  }

  public boolean canConnect() {

    boolean result = false;
    if (getFedoraClientStatus() == HttpStatus.SC_OK) {
      result = true;
    }

    log.info("Fedora client connection status OK: " + result);

    return result;
  }

  private void initClient() throws Exception {

    log.info("Initializing Fedora client...");

    String connection = getConnection();
    log.info("Fedora Client Connection: " + connection);

    log.info("Connecting as user: " + configFile.getProperty("user"));

    FedoraClient.FORCE_LOG4J_CONFIGURATION = false;

    try {
      client = new FedoraClient(connection, configFile.getProperty("user"),
          configFile.getProperty("password"));
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    if (!canConnect()) {
      throw new Exception("Error connecting to Fedora client: "
          + getConnection());
    }

    // TCP connection timeout 10 min (establishment of the TCP connection)
    client.getHttpClient().getHttpConnectionManager().getParams()
        .setConnectionTimeout(10 * 60 * 1000);
    // Socket timeout 10 min (generate connection timeout) if there is no
    // incomimg data flow within 10 min
    client.getHttpClient().getHttpConnectionManager().getParams()
        .setSoTimeout(10 * 60 * 1000);
    // Set max connection count
    client.getHttpClient().getHttpConnectionManager().getParams()
        .setMaxTotalConnections(10);

    log.info("Fedora client has been inialized. Fedora client endpoint url: "
        + client.getUploadURL());

  }

  private String[] getPids(Integer pidCount) throws Exception {
    String[] pids;

    try {
      String intVal = Integer.toString(pidCount);
      pids = APIM.getNextPID(new NonNegativeInteger(intVal), "umd");
    } catch (Exception e) {
      throw new Exception("Unable to get new PID", e);
    }
    return pids;
  }

  public static void main(String[] args) {
    if (args.length == 2) {
      ValidateData thisBatch = new ValidateData(args[0], args[1]);
      thisBatch.process();
      log.info("Done!");
    }
  }

}
