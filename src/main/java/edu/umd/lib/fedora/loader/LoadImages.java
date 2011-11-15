package edu.umd.lib.fedora.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.dom4j.DocumentFactory;
import edu.umd.lib.fedora.util.foxml.LIMSimage;
import edu.umd.lib.fedora.util.foxml.UMfactory;
import edu.umd.lib.fedora.util.DO.LIMSlookup;
import edu.umd.lib.fedora.util.DO.TabText;


public class LoadImages {

  private Properties configFile = new Properties();
  private OutputStreamWriter oFileStatWriter;

  private String strGroup = "416-001";
  private String strSuperGroup = "416";
  private String strSourcePath = null;
  private String strDestPath = null;
  private String strTitle = null;

  public LoadImages(String propFile) {
    try {
      configFile.load(new FileInputStream(propFile));
      new UMfactory(configFile.getProperty("host"));
      new DocumentFactory();
      
      // Setup the UMAM list output file
      oFileStatWriter = new OutputStreamWriter(new FileOutputStream(
          "fileStats.txt"), "UTF-8");
      oFileStatWriter.write("fileName\twidth\theight\tsize");

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
     * batchFile is the file containing the records to be ingested. pids.txt
     * contains the list of available pids (assumed to be >= pids required
     */
    TabText oSourceFile = new TabText(configFile.getProperty("batchFile"));
    new TabText(configFile.getProperty("refLoc") + "pids.txt");
    // Iterate through the TabText File
    for (HashMap<String, String> hRecord : oSourceFile) {

      /*
       * System.out.println("---" ); System.out.println("id: " +
       * hRecord.get("id") ); System.out.println("pid: " + hRecord.get("pid") );
       * System.out.println("type: " + hRecord.get("type") );
       * System.out.println("title: " + hRecord.get("title-jp") );
       * System.out.println("date: " + hRecord.get("date") );
       * System.out.println("description: " + hRecord.get("description") );
       * System.out.println("subjTopical: " + hRecord.get("subjTopical") );
       * System.out.println("repository: " + hRecord.get("repository") );
       * System.out.println("label: " + hRecord.get("label") );
       * System.out.println("fileName: " + hRecord.get("fileName") );
       * System.out.println("---" );
       */

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
        // If there is an existing UMDM object, close it out

        bSuccess = true;

      } else {

        // This is a file/administrative record (UMAM)

        // Put the stored ID value into the hRecord
        hRecord.put("id", strID);

        // OK Now process the UMAM for each file name
        String strFileName = hRecord.get("fileName");

        // First lets find the file

        if (strCollection.equalsIgnoreCase("Prange")) {
          int iFileCounter = 0;

          String strTempPath = null;
          String strFullPath = null;
          boolean bFound = false;

          // Has a path already been found?
          if (strSourcePath != null && strSourcePath.length() > 0) {
            strTempPath = strSourcePath + "/" + strGroup + "/" + strFileName;

            if (new File(strTempPath).exists()) {
              iFileCounter++;
              strFullPath = strTempPath;
              // This will set the path to the last one found
              // which is hopefully the only one found
            }
          }

          if (!bFound) {

            List<String> lCollBases = LIMSlookup.getPrangeBases();

            for (String strBase : lCollBases) {
              strTempPath = strBase + "/" + strGroup + "/" + strFileName;

              if (new File(strTempPath).exists()) {
                iFileCounter++;
                strSourcePath = strBase;
                strFullPath = strTempPath;
                // This will set the path to the last one found
                // which is hopefully the only one found
              }
            }
          }

          System.out.println("Found " + strFileName + " at " + strSourcePath);

          if (iFileCounter > 0) {
            if (iFileCounter > 1) {
              System.out.println("Found more than one, boss.");
            }

            /*
             * For Prange, as a space savings measure, we link
             * the file to its actual location rather than copy or move
             * Create the link if it does not exist already
             */
            String strPathBase = LIMSlookup.getCollectionBase(strCollection);
            Process process;
            strDestPath = strPathBase + "/" + strSuperGroup + "/" + strGroup;
            try {
              if (!new File( strDestPath ).exists()) {
                System.out.println(strDestPath + " does not exist.");
                new File( strDestPath ).mkdirs();
              } else {
                System.out.println(strDestPath + " does exist!");
              }
              
              if( !new File( strDestPath + "/" + strFileName ).exists() ) {
                process = Runtime.getRuntime().exec(
                    new String[] { "ln", "-s", 
                        strFullPath,
                        ( strDestPath + "/" + strFileName ) });
                process.waitFor();
                process.destroy();
              }
              
              // OK, now that we have found the graphic and linked it,
              // we need to create and place the derivatives: 110, 250, Zoom
              
              LIMSimage thisImage = new LIMSimage(strDestPath, strFileName);
              
              if( thisImage != null ) {
                thisImage.makeThumbs(110);
                thisImage.makeThumbs(250);
                
                // and now for the Zooms
                String strZoomPath = LIMSlookup.getZoomFileBase(strCollection);
                thisImage.makeZoom(strZoomPath + "/" + 
                    strSuperGroup + "/" + strGroup);
                
               oFileStatWriter.write(thisImage.getFileName() + "\t" + 
                   thisImage.getWidth() + "\t" +
                   thisImage.getHeight() + "\t" +
                   thisImage.getFileSize() );
               
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
    try {
      oFileStatWriter.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return bSuccess;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    LoadImages thisBatch = new LoadImages("Load.properties");
    if (thisBatch.process()) {
      System.out.println("Done!");
    } else {
      System.out.println("Fail!");
    }
  }

}
