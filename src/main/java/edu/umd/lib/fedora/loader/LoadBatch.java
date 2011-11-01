package edu.umd.lib.fedora.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Properties;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import edu.umd.lib.fedora.util.DO.*;
import edu.umd.lib.fedora.util.foxml.*;

public class LoadBatch {

	private Properties configFile = new Properties();
	private UMfactory umf;
	private DocumentFactory df;
  private OutputStreamWriter oPidWriter;
  private OutputStreamWriter oUmdmWriter;
  private String strBaseDir;
	
	public LoadBatch(String propFile ) {
		try {
			configFile.load(new FileInputStream(propFile));
			umf = new UMfactory(configFile.getProperty("host"));
			df = new DocumentFactory();
			
			strBaseDir = configFile.getProperty("baseLoc");
			
			// Setup the UMAM list output file
			oPidWriter = new OutputStreamWriter(new FileOutputStream(
			    strBaseDir + "/" + "pids.txt"), "UTF-8");
      
      // Setup the UMAM list output file
      oUmdmWriter = new OutputStreamWriter(new FileOutputStream(
          strBaseDir + "/" + "umdmPids.txt"), "UTF-8");

			
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
		 * batchFile is the file containing the records to be ingested.
		 * pids.txt contains the list of available pids (assumed to be >= pids required
		 */
		TabText oSourceFile = new TabText( strBaseDir + "/" + 
		    configFile.getProperty("refLoc") + "/" + 
		    configFile.getProperty("batchFile") );
		TabText oPids = new TabText( strBaseDir + "/" + 
        configFile.getProperty("refLoc") + "/" + 
        configFile.getProperty("pidFile") );
		HashMap<String, String> hPids;
		

    System.out.println("Pid File; " + strBaseDir + "/" + 
        configFile.getProperty("refLoc") + "/" + 
        configFile.getProperty("pidFile"));
		
		// Iterate through the TabText File
		for (HashMap<String, String> hRecord : oSourceFile) {
			
			/*System.out.println("---" );
			System.out.println("id: " + hRecord.get("id") );
			System.out.println("pid: " + hRecord.get("pid") );
			System.out.println("type: " + hRecord.get("type") );
			System.out.println("title: " + hRecord.get("title-jp") );
			System.out.println("date: " + hRecord.get("date") );
			System.out.println("description: " + hRecord.get("description") );
			System.out.println("subjTopical: " + hRecord.get("subjTopical") );
			System.out.println("repository: " + hRecord.get("repository") );
			System.out.println("label: " + hRecord.get("label") );
			System.out.println("fileName: " + hRecord.get("fileName") );
			System.out.println("---" );*/
			
			// We are starting a new object
			
			// If there is an existing object, close it out
			// and save the FOXML
			// then reinitialize the tracking variables
			
			strPid = hRecord.get("pid");
			
			// If the pid is blank or missing from the record
			// Get them from the pid file.
			if( ( strPid == null || strPid.length() < 1 ) && oPids != null ) {
				
			  System.out.print("Gotta get new Pid - ");
			  
				hPids = oPids.next();
				
				if( hPids != null ) {
					strPid = hPids.get("pid");
	        System.out.println(strPid);
				}
				
			}
			
			System.out.println("Pid: " + strPid );
			
			// Set the collection
			if( ( hRecord.get("collection") != null ) && ( hRecord.get("collection").length() > 0 ) ) {
				strCollection = hRecord.get("collection");
			} else {
				hRecord.put("collection", strCollection);
			}
			
			// Set the Content Model which is under the type column in the spreadsheet
			// This should not be null in any record
			if( ( hRecord.get("type") != null ) && ( hRecord.get("type").length() > 0 ) ) {
				strModel = LIMSlookup.getContentModel(hRecord.get("type"));
			} else {
				hRecord.put("type", strModel);
			}
			
			// The id column is supposed to be blank in all UMAM records
			if( ( hRecord.get("id") != null ) && ( hRecord.get("id").length() > 0 ) ) {
				
				// ... so this is an object record (UMDM)
				
				if( ! strID.equals(hRecord.get("id")) && 
				    ! strID.equals("XXX-XXX") ) {
					/*
					 *  This is a new object record. 
					 *  Before we can process the new record, 
					 *  we have to save off the old one. 
					 *  We have to write this UMDM out 
					 *  before its values are all replaced.
					 */
					
					/* The old record should be complete 
					 * except for the METS record which
					 * should be current and ready to go!
					 */
				  
					dmFOXML.setMETS(thisMETS);
					
					// Print out the FOXML for the old UMDM
					DoUtils.saveDoc(dmFOXML.getFoxml(), 
							configFile.getProperty("baseLoc") + "/" +
							configFile.getProperty("foxmlLoc") + "/" +
							dmFOXML.getPid() + ".xml" );
					
					//Save the pid to the UMDM pid file
					try {
            oUmdmWriter.write(dmFOXML.getPid() + " - " + strTitle + " - " + strID + "\n" );
            oUmdmWriter.write("Public interface: " + 
                "http://digital.lib.umd.edu/image.jsp?pid=" + 
                dmFOXML.getPid() + "\n" );
            oUmdmWriter.write("Admin Interface: " + 
                "http://fedora.lib.umd.edu/admin/results.jsp?action=search&query1=" + 
                dmFOXML.getPid() + "&index1=pid\n" );
            oUmdmWriter.write("Fedora Object: " + 
                "http://fedora.lib.umd.edu/fedora/get/" + 
                dmFOXML.getPid() + "\n\n" );
            
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
				
				if( ( hRecord.get("title") != null ) && ( hRecord.get("title").length() > 0 ) ) {
					strTitle = hRecord.get("title");
				} else if( ( hRecord.get("title-jp") != null ) && ( hRecord.get("title-jp").length() > 0 ) ) {
					strTitle = hRecord.get("title-jp");
				}
				
				System.out.println("Processing: " + strID );
				
				String strCollectionPid = LIMSlookup.getCollectionPid(strCollection);
				System.out.println("Content Model: " + strModel);
				System.out.println("Collection Pid: " + strCollectionPid);
				// If there is an existing UMDM object, close it out
				
				// Create the new UMDM Object
				thisUMDM = getUMDM( hRecord );
				
				thisDO = new DOxml();
				thisDO.setType("doInfo");
				thisDO.setContModel(strModel);
				thisDO.setStatus("Pending");
				
				thisDC = new DCxml();
				thisDC.setIdentifier(strPid);
				thisDC.setTitle(strTitle);

				dmFOXML = new FedoraXML( strPid, 
						strModel, "UMDM", 
						(strBaseDir + "/" + configFile.getProperty("refLoc")) );
				
				dmFOXML.setCollection(strCollection);
				dmFOXML.setDC(thisDC);
				dmFOXML.setDO(thisDO);
				dmFOXML.setUMDM(thisUMDM);
				
				thisMETS = new METSxml("images");
				thisMETS.addCollection(1, strCollectionPid);
				
				/*
				 * The METS record is created 
				 * but not incorporated into the foxml yet.
				 * We have to process the UMAMs to get the 
				 * METS properly filled out
				 */
				
				// reset the UMAM position counter
				iAmPos = 1;
				
				bSuccess = true;
				
			} else {
				
				// This is a file/administrative record (UMAM)
				
				// Put the stored ID value into the hRecord
				hRecord.put("id", strID);
				
				// OK Now process the UMAM for each file name
				String strFileName = hRecord.get("fileName");
				
				System.out.println(" - Having: " + strFileName );
				
				thisUMAM = getUMAM(hRecord);
				
				// Save the UMAM for reference and debugging
				DoUtils.saveDoc( thisUMAM.getXML(), 
						configFile.getProperty("baseLoc") + "/" +
						configFile.getProperty("umamLoc") + "/" +
						strPid + ".xml");
				
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
				
				strSWF = configFile.getProperty("defaultSWF", "http://fedora.umd.edu/content/zoom/zoom.swf");
				thisZoom.setSWF(strSWF);
				// Build the Image Path
				System.out.println("ID: "+ strID);
				System.out.println("FileName: "+ strFileName);
				String strFileBase = strFileName.substring(0, ( strFileName.length() - 4 ) );
				System.out.println("ID pre: "+ strSuperGroup);
				System.out.println("File Base: "+ strFileBase);
				// The ImagePath is the image path to the Prange Zooms
				// plus the Prange ID prefix
				// Prange ID
				// Base file name
				strZoomBase = LIMSlookup.getZoomBase(strCollection);
				strZoomBase += "/" + strSuperGroup + "/" + strGroup + "/" + strFileBase;
				
				System.out.println( thisZoom.setImagePath(strZoomBase) );
				
				// Create the UMAM record
				amFOXML = new FedoraXML( strPid, 
						strModel, "UMAM", 
						(strBaseDir + "/" + configFile.getProperty("refLoc")) );
				
				amFOXML.setCollection(strCollection);
				amFOXML.setDC(thisDC);
				amFOXML.setDO(thisDO);
				amFOXML.setUMAM(thisUMAM);
				amFOXML.setZoom(thisZoom);
				
				amFOXML.setExternalImage(strFileName);
				
				// Add the image, 110 and 250
//				LIMSimage iImage = new LIMSimage( strCollection, 
//						strID, 
//						strFileName);
				
				// iImage.make110();
				// iImage.make250();
				
				// Get the image information for the datastreams'.
				
				// Save the FOXML
				DoUtils.saveDoc(amFOXML.getFoxml(), 
						configFile.getProperty("baseLoc") + "/" +
						configFile.getProperty("foxmlLoc") + "/" +
						amFOXML.getPid() + ".xml");
				
				// Write this pid to the pidlist
        try {
          oPidWriter.write(amFOXML.getPid() + "\n");
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
				
				// Add this item to the current UMDM METS record
				thisMETS.addPart(iAmPos++, 
						strPid, "DISPLAY", hRecord.get("label") );
			}
		}
		
		// Print out the FOXML for the last UMDM
		
		dmFOXML.setMETS(thisMETS);
		
		DoUtils.saveDoc(dmFOXML.getFoxml(), 
				configFile.getProperty("baseLoc") + "/" +
				configFile.getProperty("foxmlLoc") + "/" +
				dmFOXML.getPid() + ".xml" );
    

    try {
      oUmdmWriter.write(dmFOXML.getPid() + " - " + strTitle + " - " + strID + "\n" );
      oUmdmWriter.write("Public interface: " + 
          "http://digital.lib.umd.edu/image.jsp?pid=" + 
          dmFOXML.getPid() + "\n" );
      oUmdmWriter.write("Admin Interface: " + 
          "http://fedora.lib.umd.edu/admin/results.jsp?action=search&query1=" + 
          dmFOXML.getPid() + "&index1=pid\n" );
      oUmdmWriter.write("Fedora Object: " + 
          "http://fedora.lib.umd.edu/fedora/get/" + 
          dmFOXML.getPid() + "\n\n" );
      oPidWriter.write(dmFOXML.getPid() + "\n");
      oUmdmWriter.close();
      oPidWriter.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
		
		return bSuccess;
	}
	
	private UMDMxml getUMDM( HashMap<String, String> hRecord ) {
		
		UMDMxml thisUMDM = null;
		Element thisElement;
		String strElement;
		String strTemplateFile = strBaseDir + "/" + configFile.get("refLoc") + "/" +
			hRecord.get("collection").toLowerCase() +
			"-UMDM.xml";
		
		System.out.println( "File: " + strTemplateFile );
		
		File fTemplate = new File( strTemplateFile );
		
		if( fTemplate.exists() ){
			thisUMDM = umf.getUMDM( strTemplateFile, "File" );
			
		} else {
			thisUMDM = umf.getUMDM();
		}
		
		// OK, the base UMDM is rendered. Now for the detail
		// title
		strElement = hRecord.get("title");
		if( ( strElement != null ) && ( strElement.length() > 0 ) ) {
			thisElement = df.createElement("title");
			thisElement.addAttribute("type", "main");
			thisElement.setText(strElement);
			thisUMDM.addElement(thisElement);
		}
		
		// title-jp
		strElement = hRecord.get("title-jp");
		if( ( strElement != null ) && ( strElement.length() > 0 ) ) {
			thisElement = df.createElement("title");
			thisElement.addAttribute("type", "main");
			thisElement.addAttribute("xml:lang", "ja");
			thisElement.setText(strElement);
			thisUMDM.addElement(thisElement);
		}
		
		// title-ro
		strElement = hRecord.get("title-ro");
		if( ( strElement != null ) && ( strElement.length() > 0 ) ) {
			thisElement = df.createElement("title");
			thisElement.addAttribute("type", "main");
			thisElement.addAttribute("xml:lang", "ja-Latn");
			thisElement.setText(strElement);
			thisUMDM.addElement(thisElement);
		}
		
		// date
		strElement = hRecord.get("date");
		if( ( strElement != null ) && ( strElement.length() > 0 ) ) {
			// There can only be 1 covTime - remove existing, if it is there
			thisUMDM.removeElements( "/descMeta/covTime");
			thisElement = df.createElement("covTime");
			thisElement.addElement("century")
				.addAttribute("era", "ad")
				.addText("1901-2000");
			thisElement.addElement("date")
				.addAttribute("era", "ad")
				.addText(strElement);
			thisUMDM.addElement(thisElement);
		}
		
		// description
		strElement = hRecord.get("description");
		if( ( strElement != null ) && ( strElement.length() > 0 ) ) {
			thisElement = df.createElement("description");
			thisElement.setText(strElement);
			thisUMDM.addElement(thisElement);
		}
		
		// subjTopical
		strElement = hRecord.get("subjTopical");
		if( ( strElement != null ) && ( strElement.length() > 0 ) ) {
			thisElement = df.createElement("subject");
			thisElement.addAttribute("type", "topical");
			thisElement.setText(strElement);
			thisUMDM.addElement(thisElement);
		}
		
		// subjBrowse
		strElement = hRecord.get("subjBrowse");
		if( ( strElement != null ) && ( strElement.length() > 0 ) ) {
			String[] strList = strElement.split(", ");
			for(String thisSubj : strList ) {
				if( thisSubj.length() > 0 ) {
					thisElement = df.createElement("subject");
					thisElement.addAttribute("type", "browse");
					thisElement.setText(strElement);
					thisUMDM.addElement(thisElement);
					
				}
			}
		}
		
		// subjLCSH
		strElement = hRecord.get("subjLCSH");
		if( ( strElement != null ) && ( strElement.length() > 0 ) ) {
			String[] strList = strElement.split(", ");
			for(String thisSubj : strList ) {
				if( thisSubj.length() > 0 ) {
					thisElement = df.createElement("subject");
					thisElement.addAttribute("type", "LCSH");
					thisElement.setText(strElement);
					thisUMDM.addElement(thisElement);
					
				}
			}
		}
		
		// style
		strElement = hRecord.get("style");
		if( ( strElement != null ) && ( strElement.length() > 0 ) ) {
			String[] strList = strElement.split(", ");
			for(String thisSubj : strList ) {
				if( thisSubj.length() > 0 ) {
					thisElement = df.createElement("style");
					thisElement.setText(strElement);
					thisUMDM.addElement(thisElement);
					
				}
			}
		}
		
		// collection identifier
		strElement = hRecord.get("id");
		if( ( strElement != null ) && ( strElement.length() > 0 ) ) {
			thisElement = df.createElement("identifier");
			thisElement.addAttribute("label", 
					hRecord.get("collection").toLowerCase() );
			thisElement.setText(strElement);
			thisUMDM.addElement(thisElement);
		}
		
		return thisUMDM;
	}
	
	private UMAMxml getUMAM( HashMap<String, String> hRecord ) {
		
		UMAMxml thisUMAM = null;
		Element thisElement;
		String strElement;
		String strTemplateFile = strBaseDir + "/" + configFile.get("refLoc") + "/" +
			hRecord.get("collection").toLowerCase() +
			"-UMAM.xml";
		
		System.out.println( "File: " + strTemplateFile );
		
		File fTemplate = new File( strTemplateFile );
		
		if( fTemplate.exists() ){
			thisUMAM = umf.getUMAM( strTemplateFile, "File" );
			
		} else {
			thisUMAM = umf.getUMAM();
		}
		
		// OK, the base UMAM is rendered. Now for the detail
		// identifier
		strElement = hRecord.get("fileName");
		if( ( strElement != null ) && ( strElement.length() > 0 ) ) {
			// There can only be 1 identifier - remove existing, if it is there
			thisUMAM.removeElements( "/adminMeta/identifier");
			thisElement = df.createElement("identifier");
			thisElement.setText(strElement);
			thisUMAM.addElement(thisElement);
		}
		
		return thisUMAM;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LoadBatch thisBatch = new LoadBatch("Load.properties");
		if( thisBatch.process() ) {
			System.out.println("Done!");
		} else {
			System.out.println("Fail!");
		}
	}

}
