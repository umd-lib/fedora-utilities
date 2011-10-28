package src;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.InvalidXPathException;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;

import src.LIMSlookup;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FedoraXML {

	private String pid = "";
	private String type = "FedoraObject";
	private String state = "Active";
	private String objectType = "UMDM";
	private String label = " Object";
	private String contentModel = "UMD_BOOK";
	private String status = "Pending";
	private String creationDate = null;
	private String modificationDate = null;
	private String strRefLoc;
	private String strCollection  = "Misc";
	private String strGroup = "999-999";
	private String strSuperGroup = "999";
	private String strFileName = "pcb-416-002-001-tx001.jpg";
	private String strTitle = null;
	private Element foxmlRoot;
	boolean isDisseminatorSet = false;
	
	private UMDMxml thisUMDM = null;
	private UMAMxml thisUMAM = null;
	private DOxml thisDO = null;
	private DCxml thisDC = null;
	private METSxml thisMETS = null;
	private ZOOMxml thisZoom = null;
	private VIDEOxml thisVideo = null;
	
	public FedoraXML( Document thisFoxml ) {
	  parseFoxml( thisFoxml );
	}
	
	/**
	 * 
	 * @param thisPID
	 */
	public FedoraXML(String thisPID, 
			String model, 
			String thisOType,
			String refLoc) {
		
		// e.g., fXML = new FedoraXML("umd:7777", "UMD_BOOK", "UMDM",
		// "/home/paulh/Documents/Work/Sweep/ref" );
		// refLoc is where the reference files are such as the disseminator.xml
		
		// contentModel
		if( model != null && model.length() > 0 ) {
			contentModel = model;
		}
		
		// Even if it is a dummy value, the pid must be present.
		if( thisPID != null && thisPID.length() > 0 ) {
			pid = thisPID;
		}
		
		if( thisOType != null && thisOType.length() > 0 ) {
			objectType = thisOType;
		}
		
		if( refLoc != null && refLoc.length() > 0 ) {
			strRefLoc = refLoc;
		}
		
	}
	
	@SuppressWarnings("unchecked")
  private boolean parseFoxml( Document thisFoxml ) {
	  
	  HashMap<String, String> hVersions = new HashMap<String, String>();
	  String sXPath = "";
	  String sKey = "";
	  String sValue = "";
	  String sTemp = "";
	  
	  // First lets get the elements from the header
	  Node nThisNode = DoUtils.getXPath("/foxml:digitalObject").selectSingleNode(thisFoxml);
	  
	  Element eThisElement = (Element) nThisNode;
	  
	  String sThisName;
	  String sThisValue = eThisElement.attributeValue("PID");
	  pid = sThisValue;
	  
	  System.out.println("Pid: " + sThisValue);
	  
	  // Get the list of properties from the top of the object.
	  List<Node> lTheseNodes = DoUtils.getXPath("/foxml:digitalObject/foxml:objectProperties/foxml:property").selectNodes(thisFoxml);
	  
	  for (Node node : lTheseNodes) {
      eThisElement = (Element) node;
      
      sThisName = eThisElement.attributeValue("NAME");
      sThisValue = eThisElement.attributeValue("VALUE");
      
      if( sThisName.matches(".*type") ) {
        System.out.println("Type: " + sThisValue);
        type = sThisValue;
      } else if( sThisName.matches(".*state") ) {
        System.out.println("State: " + sThisValue);
        state = sThisValue;
      } else if( sThisName.matches(".*label") ) {
        System.out.println("Label: " + sThisValue);
        label = sThisValue;
      } else if( sThisName.matches(".*contentModel") ) {
        System.out.println("Content Model: " + sThisValue);
        contentModel = sThisValue;
      } else if( sThisName.matches(".*createdDate") ) {
        System.out.println("Created Date: " + sThisValue);
        creationDate = sThisValue;
      } else if( sThisName.matches(".*lastModifiedDate") ) {
        System.out.println("Last Modified Date: " + sThisValue);
        modificationDate = sThisValue;
      }
    }
	  
	  // Next, Harvest the datastreams - remember at this point we don't know if it is a collection
	  
	  // Next, lets cruise the audit records in order to get the version info
	  // It is an ordered list so we can "trust" later changes to be later changes
	  
	  int iAuditCtr = 1;
	  
	  String sBasePath = "/foxml:digitalObject";
    //sBasePath += "/foxml:datastream[@ID='AUDIT']/foxml:datastreamVersion/foxml:xmlContent";
	  sBasePath += "/foxml:datastream[@ID='AUDIT']/foxml:datastreamVersion/foxml:xmlContent";
	  
	  do {
      sXPath = sBasePath +
        "/audit:auditTrail/audit:record[@ID='AUDREC" + String.valueOf(iAuditCtr)+"']/audit:componentID";
      
      //System.out.println("XPath = " + sXPath );
      
      nThisNode = DoUtils.getXPath(sXPath).selectSingleNode(thisFoxml);
      
      if( nThisNode != null ) {
        sKey = nThisNode.getText();
        
        sXPath = sBasePath +
        "/audit:auditTrail/audit:record[@ID='AUDREC" + 
        String.valueOf(iAuditCtr)+"']/audit:date";
        
        nThisNode = DoUtils.getXPath(sXPath).selectSingleNode(thisFoxml);
        
        if( nThisNode != null ) {
          sValue = nThisNode.getText();
          
          hVersions.put(sKey, sValue);
          
          System.out.println("Audit: " + iAuditCtr + " - " + sKey + "-" + sValue );
          
          iAuditCtr++;
          
        }
      
      }
      
    } while (nThisNode != null);
	  
    // Get the amInfo
	  sBasePath = "/foxml:digitalObject/foxml:datastream[@ID = 'amInfo']";
	  
	  nThisNode = DoUtils.getXPath(sBasePath).selectSingleNode(thisFoxml);
    
	  if( nThisNode != null ) {
      
      objectType = "UMAM";
	    
	    System.out.println("This is a UMAM record. " + objectType);
	    
	    sBasePath += "/foxml:datastreamVersion";
	    
	    // Check the version hash to see if there were revisions to the amInfo and if so
	    // Get the current version.
	    sTemp = hVersions.get("amInfo");
	    
	    if( sTemp != null && sTemp.length() > 0 ) {
	      
	      sBasePath += "[@CREATED='" + sTemp + "']";
	      
	    }
	    
	    sBasePath += "/foxml:xmlContent/amInfo:amInfo";
	    
	    sXPath = sBasePath + "/amInfo:type";
      
      System.out.println( sXPath );
	    
      nThisNode = DoUtils.getXPath(sXPath).selectSingleNode(thisFoxml);
      
      if( nThisNode != null ) {
        contentModel = nThisNode.getText();
      }
      
	    sXPath = sBasePath + "/amInfo:status";
      
      nThisNode = DoUtils.getXPath(sXPath).selectSingleNode(thisFoxml);
      
      if( nThisNode != null ) {
        status = nThisNode.getText();
      }
      
      System.out.println("amInfo - " + contentModel + "-" + status );
      
      thisDO = new DOxml("amInfo", contentModel, status );
	    
	  } else {
	    
	    sBasePath = "/foxml:digitalObject/foxml:datastream[@ID='doInfo']";
	    
	    nThisNode = DoUtils.getXPath(sBasePath).selectSingleNode(thisFoxml);
	    
	    if( nThisNode != null ) {
	      
	      sBasePath += "/foxml:datastreamVersion";
	      
	      // Check the version hash to see if there were revisions to the amInfo and if so
	      // Get the current version.
	      sTemp = hVersions.get("doInfo");
	      
	      if( sTemp != null && sTemp.length() > 0 ) {
	        
	        sBasePath += "[@CREATED='" + sTemp + "']";
	        
	      }
	      
	      sBasePath += "/foxml:xmlContent/doInfo:doInfo";
	      
	      sXPath = sBasePath + "/doInfo:type";
	      
	      nThisNode = DoUtils.getXPath(sXPath).selectSingleNode(thisFoxml);
	      
	      if( nThisNode != null ) {
	        contentModel = nThisNode.getText();
	      }
	      
	      if( contentModel.equals("UMD_COLLECTION") ) {
	        objectType = "Collection";
	      } else {
	        objectType = "UMDM";
	      }
	      
	      System.out.println("This is a Digital Object record. " + objectType);
	      
	      sXPath = sBasePath + "/doInfo:status";
	      
	      nThisNode = DoUtils.getXPath(sXPath).selectSingleNode(thisFoxml);
	      
	      if( nThisNode != null ) {
	        status = nThisNode.getText();
	      }
	      
	      System.out.println("amInfo - " + contentModel + "-" + status );
	      
	      thisDO = new DOxml("doInfo", contentModel, status );
	    }
	  }
	  
	  // Get the DC record
	  sXPath = "/foxml:digitalObject/foxml:datastream[@ID='DC']";
	  
	  // Check the version hash to see if there were revisions to the amInfo and if so
    // Get the current version.
    sTemp = hVersions.get("DC");
    sXPath += "/foxml:datastreamVersion";
    
    if( sTemp != null && sTemp.length() > 0 ) {
      
      sBasePath += "[@CREATED='" + sTemp + "']";
      
    }
    
    sXPath += "/foxml:xmlContent/oai_dc:dc/dc:title";
    
    nThisNode = DoUtils.getXPath(sXPath).selectSingleNode(thisFoxml);
    
    if( nThisNode != null ) {
      
      strTitle = nThisNode.getText();
      
    }
    
    System.out.println("Title: " + strTitle );
	  
	  // For Collections and UMAM, get the UMDM record
	  if( objectType.equals("UMAM")) {
	    
	    // Get the UMAM
	    sXPath = "/foxml:digitalObject/foxml:datastream[@ID='umam']";
	    
	    // Check the version hash to see if there were revisions to the amInfo and if so
	      // Get the current version.
	      sTemp = hVersions.get("umam");
	      sXPath += "/foxml:datastreamVersion";
	      
	      if( sTemp != null && sTemp.length() > 0 ) {
	        
	        sBasePath += "[@CREATED='" + sTemp + "']";
	        
	      }
	      
	      sXPath += "/foxml:xmlContent/adminMeta";
	      
	      nThisNode = DoUtils.getXPath(sXPath).selectSingleNode(thisFoxml);
	      
	      if( nThisNode != null ) {
	        
	        nThisNode.detach();
	        
	        thisUMAM = new UMAMxml((Element)nThisNode);
	        
	        System.out.println("UMAM is OK? " + thisUMAM.isOK());
	        
	      } else {
	        System.out.println("UMAM is null!");
	      }
	    
	    // Get the image
	    
	    // Get the Thumb 110
	    
	    // Get5 the Thumb 250
	    
	    // Get the video
	    
	  } else {
	    
	    // Get the UMDM
	    
	    // Get the METS
	    
	    // Get the Collection
	    if( objectType.equals("Collection")) {
	      
	      sBasePath = "/foxml:digitalObject/foxml:datastream[@ID='collection']";
	      
	    }
	    
	  }
	  
	  // For TEIs Get the text
	  
	  // For EADs get the EAD info
	  
	  return true;
	}
	
	public String getCollection() {
		return strCollection;
	}
	
	public String setCollection( String newCollection ) {
		if( newCollection != null && LIMSlookup.isCollection(newCollection)) {
			strCollection = newCollection;
		}
		return strCollection;
	}
	
	public String getPid() {
		return pid;
	}
	
	public UMDMxml getUMDM() {
		if( thisUMDM != null ) {
			UMDMxml returnUMDM = new UMDMxml(thisUMDM.getXML());
			return returnUMDM;
		} else {
			return null;
		}

	}
	
	public void setUMDM(UMDMxml candidateUMDM) {
		if( objectType.equals("UMDM") && candidateUMDM != null ) {
			thisUMDM = candidateUMDM;
		}
	}
	
	public UMAMxml getUMAM() {
		if( thisUMAM != null ) {
			UMAMxml returnUMAM = new UMAMxml(thisUMAM.getXML().getRootElement());
			return returnUMAM;
		} else {
			return null;
		}

	}
	
	public void setUMAM(UMAMxml candidateUMAM) {
		if( objectType.equals("UMAM") && candidateUMAM != null ) {
			thisUMAM = candidateUMAM;
		}
	}
	
	public METSxml getMETS() {
		if( thisMETS != null ) {
			
			System.out.println("Getting FOXML METS.");
			METSxml returnMETS = new METSxml(thisMETS.getXML());
			return returnMETS;
		} else {
			return null;
		}

	}
	
	public void setMETS(METSxml candidateMETS) {
		if( objectType.equals("UMDM") && candidateMETS != null ) {
			thisMETS = candidateMETS;
		}
	}
	
	public DOxml getDO() {
		if( thisDO != null ) {
			DOxml returnDO = new DOxml();
			returnDO.setType(thisDO.getType());
			returnDO.setContModel(thisDO.getContModel());
			returnDO.setStatus(thisDO.getStatus());
			return returnDO;
		} else {
			return new DOxml();
		}

	}
	
	public void setDO(DOxml candidateDO) {
		if( candidateDO != null ) {
			thisDO = candidateDO;
			
		}
	}
	
	public DCxml getDC() {
		if( thisDC != null ) {
			DCxml returnDC = new DCxml();
			returnDC.setTitle(thisDC.getTitle());
			returnDC.setIdentifier(thisDC.getIdentifier());
			return returnDC;
		} else {
			return new DCxml();
		}

	}
	
	public void setDC(DCxml candidateDC) {
		if( candidateDC != null ) {
			thisDC = candidateDC;
			
		}
	}
	
	public ZOOMxml getZoom() {
		if( thisZoom != null ) {
			ZOOMxml returnZoom = new ZOOMxml();
			returnZoom.setTitle(thisZoom.getTitle());
			returnZoom.setSWF(thisZoom.getSWF());
			returnZoom.setImagePath(thisZoom.getImagePath());
			return returnZoom;
		} else {
			return null;
		}

	}
	
	public void setZoom(ZOOMxml newZoom) {
		if( objectType.equals("UMAM") && 
				contentModel.equals("UMD_IMAGE") && 
				newZoom != null ) {
			thisZoom = newZoom;
			System.out.println("Posted Zoom to UMAM");
			
		}
	}
	
	/**
	 * 
	 * @param metaRecord
	 */
	private void setDataStream(String dataStreamGroup,
			String dataStreamID,
			String dataStreamlabel,
			String dataStreamMimeType,
			Document metaRecord ) {

		// setDataStream( "X", "DC", "Dublin Core Metadata", "text/xml", thisDC.getXML() );
		// setDataStream( "X", "doInfo", "Digital Object Information", "text/xml", thisDO.getXML() );
		// setDataStream( "X", "umdm", "University of Maryland Descriptive Metadata", "text/xml", thisUMDM.getXML() );
		// setDataStream( "X", "rels-mets", "METS Relationships", "text/xml", thisMETS.getXML() );
		// setDataStream( "X", "amInfo", "Digital Object Information", "text/xml", thisDO.getXML );
		// setDataStream( "X", "umam", "University of Maryland Administrative Metadata", "text/xml", thisUMAM.getXML() );
		// setDataStream( "X", "zoom", "Zoomify Data", "text/xml", thisZOOM.getXML() );
		
		String realID;
		
		realID = dataStreamID;
		
		if( ( metaRecord != null ) &&
				( dataStreamGroup.length() > 0 ) &&
				( dataStreamID.length() > 0 ) &&
				( dataStreamlabel.length() > 0 ) &&
				( dataStreamMimeType.length() > 0 ) ) {
			
			Element dataStream = foxmlRoot.addElement("foxml:datastream")
			.addAttribute("CONTROL_GROUP", dataStreamGroup)
			.addAttribute("ID", dataStreamID)
			.addAttribute("STATE", "A")
			.addAttribute("VERSIONABLE", "false");
			
			Element dsVersion = dataStream.addElement("foxml:datastreamVersion")
			.addAttribute("ID", realID + ".0")
			.addAttribute("LABEL", dataStreamlabel)
			.addAttribute("MIMETYPE", dataStreamMimeType);
			
			dsVersion.addElement("foxml:contentDigest")
			.addAttribute("DIGEST", "none")
			.addAttribute("TYPE", "DISABLED");
			
			Element xmlContent = dsVersion.addElement("foxml:xmlContent");
			
			xmlContent.add(metaRecord.getRootElement().createCopy());
			
		}
	}
	
	public void setVideo( String fileName ) {
		
		// Set image
		setVideoDatastream("E",
				"thumbnail",
				"thumbnail",
				"http://fedoradev.umd.edu/images/video_thumbnail.jpeg",
				"jpg");
	}
	
	public void setExternalImage( String fileName ) {
		
		// setImage( "pcb-416-002-001-tx001.jpg" );
		
		if (objectType.equals("UMAM")) {
		  
	    String controlGroup = "E";
	    String baseURL = LIMSlookup.getWebBase(strCollection);
	    String fileBase;
	    String fileExtension;
	    
	    strFileName = fileName;
			
	    if( strCollection.equalsIgnoreCase("prange")) {
	      
	      // Lets extract the group and supergroup
	      // and append them to the base URL
	      String[] aFileParts = fileName.split("-");
	      
	      if( aFileParts.length > 3 ) {
	        // Remove trailing underscrores
          String strSubGroup = aFileParts[2];
          int iUnderscore = strSubGroup.lastIndexOf("_");
	        while(iUnderscore > 0 ) {
	          strSubGroup = aFileParts[2].substring(0, iUnderscore);
            iUnderscore = strSubGroup.lastIndexOf("_");
	        }
	        // remove trailing "x" characters
          iUnderscore = strSubGroup.lastIndexOf("x");
          while(iUnderscore > 0 ) {
            strSubGroup = aFileParts[2].substring(0, iUnderscore);
            iUnderscore = strSubGroup.lastIndexOf("x");
          }
	        strGroup = aFileParts[1] + "-" + strSubGroup;
	        strSuperGroup = aFileParts[1];
	      }
	    }
	    
//			System.out.println(baseURL);
//			// Set image
//			setImageDatastream("Image");
//			// Set 110 Thumbnail
//			setImageDatastream("Thumbnail 110x110");
//			// Set 250 thumbnail
//			setImageDatastream("Thumbnail 250x250");
			
		}
		
	}
	
	private void setVideoDatastream(String controlGroup,
			String id, String label, 
			String URL, String fileExtension ) {
		
		Element dsVersion;
		
		Element dataStream = foxmlRoot.addElement("foxml:datastream")
		.addAttribute("CONTROL_GROUP", controlGroup)
		.addAttribute("ID", id)
		.addAttribute("STATE", "A")
		.addAttribute("VERSIONABLE", "false");
		
		dsVersion = dataStream.addElement("foxml:datastreamVersion")
		.addAttribute("ID", id + ".0")
		.addAttribute("LABEL", label);
		
		if( fileExtension.equalsIgnoreCase("jpg") ) {
			dsVersion.addAttribute("MIMETYPE", "image/jpeg");
		} else if( fileExtension.equalsIgnoreCase("tif") ) {
			dsVersion.addAttribute("MIMETYPE", "image/tiff");
		} else {
			dsVersion.addAttribute("MIMETYPE", "image/jpeg");
		}
		
		dsVersion.addElement("foxml:contentDigest")
		.addAttribute("DIGEST", "none")
		.addAttribute("TYPE", "DISABLED");
		
		dsVersion.addElement("foxml:contentLocation")
		.addAttribute("REF", URL)
		.addAttribute("TYPE", "URL");
	}
	
	private void setImageDatastream(String label ) {
		
		/*
		 * There are 4 image types: image, 110, and 250, and thumbnail
		 * image - full resolution image
		 * 250 - thumbnail 250 pixels on a side
		 * 110 - thumbnail 110 pixels on a side
		 */
		
		String id = "image";
		String strUrlExt = "";
		String strFileExt = "";
		String strPidNo = pid.split(":")[1];
		
		String[] aFileParts = strFileName.split("\\.");
		String strFileBase = aFileParts[0];
		String strFileExtension = aFileParts[1];
		
		if( label.equals("Thumbnail 110x110")) {
			id = "thumbnail-110";
			strUrlExt = "X0110";
			strFileExt = "-X0110";
			
		} else if( label.equals("Thumbnail 250x250")) {
			id = "thumbnail-250";
			strUrlExt = "X0250";
			strFileExt = "-X0250";
			
		} else if( label.equals("thumbnail") ) {
		  id = "thumbnail";
		  label = "thumbnail";
		  
		}
	
		String strBaseURL;
		
		if( label.equals("thumbnail")) {
		  
		  strBaseURL = "http://fedora.umd.edu/images/video_thumbnail.jpeg";
		  
		} else {
		  
		  strBaseURL = LIMSlookup.getWebBase(strCollection);
		
		  strBaseURL += "/" + strSuperGroup + "/" + strGroup;
		
		  if( strUrlExt.length() > 0 ) {
		    strBaseURL += "/" + strUrlExt;
		  }
		
		
		  // int iPidNo = Integer.parseInt(pid.split(":")[1]);
		
		  //strBaseURL += pidURL(strPidNo);
		
		  //strBaseURL += strUrlExt;
		
		  strBaseURL += "/" + strFileBase + strFileExt + 
		  "." + strFileExtension;
		}
		
		Element dsVersion;
		
		Element dataStream = foxmlRoot.addElement("foxml:datastream")
		.addAttribute("CONTROL_GROUP", "E")
		.addAttribute("ID", id)
		.addAttribute("STATE", "A")
		.addAttribute("VERSIONABLE", "false");
		
		dsVersion = dataStream.addElement("foxml:datastreamVersion")
		.addAttribute("ID", id + ".0")
		.addAttribute("LABEL", label);
		
//		if( fileExtension.equalsIgnoreCase("jpg") ) {
//			dsVersion.addAttribute("MIMETYPE", "image/jpeg");
//		} else if( fileExtension.equalsIgnoreCase("tif") ) {
//			dsVersion.addAttribute("MIMETYPE", "image/tiff");
//		} else {
//			dsVersion.addAttribute("MIMETYPE", "image/jpeg");
//		}
		
		dsVersion.addAttribute("MIMETYPE", "image/jpeg");
		
		dsVersion.addElement("foxml:contentDigest")
		.addAttribute("DIGEST", "none")
		.addAttribute("TYPE", "DISABLED");
		
		dsVersion.addElement("foxml:contentLocation")
		.addAttribute("REF", strBaseURL )
		.addAttribute("TYPE", "URL");
	}
	
	/**
	 * 
	 * @param strPidNo
	 * @return
	 */
	private String pidURL( String strPidNo ) {
		String strPidURL = "";
		String strPidChunk;
		String strZeroPad = "00";
		String strNinePad = "99";
		
		int iPidLen = strPidNo.length();
		
		if ( iPidLen > 2 ) {
			
			int iPidFinger = iPidLen - 2;
			
			while (iPidFinger > 0) {

				strPidChunk = strPidNo.substring(0, iPidFinger);

				strPidURL = "umd_" + strPidChunk + strZeroPad + "-" + "umd_"
						+ strPidChunk + strNinePad + "/" + strPidURL;

				strZeroPad += "0";
				strNinePad += "9";
				iPidFinger--;
			}
		} else {
			strPidURL = "umd_00-umd_99/";

		}
		
		return strPidURL;
	}
	
	@SuppressWarnings("unchecked")
	private void setDisseminator( ) {
		// Element thisDisseminator = null;
		
		// locate and open up the XML file of disseminators
		// Get the list of elements under the contentModel ID
		// add each element to thisXML
		try{
			SAXReader rRefReader = new SAXReader();
			InputSource isRef = new InputSource(new FileInputStream(strRefLoc + "/disseminators.xml"));
		
			System.out.println("Diss Path: " + strRefLoc + "/disseminators.xml");
			
			// Read the input file
			if( ( rRefReader != null ) && ( isRef != null ) ) {
			
				Document dReference = rRefReader.read(isRef);
				
				String thisPath = "/disseminators/contentModel[@label='" + 
				contentModel + "' and @type='" + objectType + "']/foxml:disseminator";
				//String thisPath = "/disseminators/contentModel[@label='" + 
				//contentModel + "' and @type='" + objectType + "']/foxml:disseminator";
				
				List lNodes = DoUtils.getXPath(thisPath)
						.selectNodes(dReference);
				
				if( lNodes.size() < 1 ) {
					System.out.println("Hey - I got no Disseminators for: " + thisPath);
				}
				
				for( Node nDisseminator : (List<Node>) lNodes ) {
					nDisseminator.detach();
					foxmlRoot.add(nDisseminator);
				}
				
				isDisseminatorSet = true;
			
			}
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isOK() {
		
		boolean isItOK = true;
		
		if( objectType.equals("UMDM")) {
			if( ( thisUMDM == null ) || ( thisMETS == null ) ) {
        System.out.println("UMDM not OK - UMDM null or METS null");
				isItOK = false;
			}
		} else if( objectType.equals("UMAM")) {
			if( ( thisUMAM == null ) ) {
				isItOK = false;
				System.out.println("UMAM not OK - UMAM null");
			}
			if( contentModel.equals("UMD_IMAGE") ) {
				if( thisZoom == null ) {
					isItOK = false;
					System.out.println("UAMM not OK - Zoom null");
				}
			}
		}
		
		if( ( thisDC == null ) || ( thisDO == null ) ) {
		  System.out.println("FOXML not OK DC or DO is null");
			isItOK = false;
		}

		return isItOK;
	}
	
	public Document getFoxml() {
		if( isOK() ) {
			
			Document thisXML = DocumentHelper.createDocument();
			foxmlRoot = thisXML.addElement( "foxml:digitalObject" )
			.addAttribute("PID", pid)
			.addAttribute("fedoraxsi:schemaLocation", 
					"info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-0.xsd")
			.addNamespace("audit", "info:fedora/fedora-system:def/audit#")
			.addNamespace("fedoraxsi", "http://www.w3.org/2001/XMLSchema-instance")
			.addNamespace("foxml", "info:fedora/fedora-system:def/foxml#");

			// Set up the Object Properties Section
			Element objectProperties = foxmlRoot.addElement("foxml:objectProperties");
			
			objectProperties.addElement("foxml:property")
			.addAttribute("NAME", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
			.addAttribute("VALUE", type);
			
			objectProperties.addElement("foxml:property")
			.addAttribute("NAME", "info:fedora/fedora-system:def/model#state")
			.addAttribute("VALUE", state);
			
			objectProperties.addElement("foxml:property")
			.addAttribute("NAME", "info:fedora/fedora-system:def/model#label")
			.addAttribute("VALUE", objectType + label);
			
			objectProperties.addElement("foxml:property")
			.addAttribute("NAME", "info:fedora/fedora-system:def/model#contentModel")
			.addAttribute("VALUE", contentModel);
			
			// Set the data streams
			// DC is on both object types

			setDataStream( "X", "DC", "Dublin Core Metadata", "text/xml", thisDC.getXML() );
			
			System.out.println("ObjectType:" + objectType);
			
			// next find out if this is a UMDM or UMAM record
			if( objectType.equals("UMDM") ) {
				setDataStream( "X", "doInfo", "Digital Object Information", "text/xml", thisDO.getXML() );
				setDataStream( "X", "umdm", "University of Maryland Descriptive Metadata", "text/xml", thisUMDM.getXML() );
				setDataStream( "X", "rels-mets", "METS Relationships", "text/xml", thisMETS.getXML() );
			} else if ( objectType.equals("UMAM") ) {
				System.out.println("UMAM Foxml");
				setDataStream( "X", "amInfo", "Digital Object Information", "text/xml", thisDO.getXML() );
				setDataStream( "X", "umam", "University of Maryland Administrative Metadata", "text/xml", thisUMAM.getXML() );
				
				if( contentModel.equals("UMD_IMAGE") ) {
				  setDataStream( "X", "zoom", "Zoomify Data", "text/xml", thisZoom.getXML() );
				  setImageDatastream("Image");
				  setImageDatastream("Thumbnail 110x110");
				  setImageDatastream("Thumbnail 250x250");
				} else if( contentModel.equals("UMD_VIDEO") ) {
				  setImageDatastream("thumbnail");
				}
			}
			
			if( ! isDisseminatorSet ) {
				setDisseminator();
			}
			return thisXML;
		} else {
			return null;
		}
	}
	
	public boolean saveFOXML( String strPath ) {
		
		File thisFile = new File(strPath);
		
		if( isOK() && thisFile.isDirectory() ) {
			
			System.out.println( thisFile.getPath() );
			
			return DoUtils.saveDoc(getFoxml(), thisFile.getPath() + "/" + getPid() + ".xml");
			
		}
		
		return false;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
