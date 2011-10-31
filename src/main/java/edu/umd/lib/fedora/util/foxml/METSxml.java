package edu.umd.lib.fedora.util.foxml;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.InvalidXPathException;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import edu.umd.lib.fedora.util.DO.DoUtils;

public class METSxml {

	DocumentFactory docFactory = DocumentFactory.getInstance();

	private String strCreationDate = "";
	
	// collection[0] is the primary collection
	private ArrayList<String> aTypes;
	private ArrayList<String> collections;
	
	/*
	 * members is the array of pids that are associated with this object.
	 * The position of the pid in the array will be the basis for the fptr FILEID.
	 * arrays are zero based and the FILEID is one based soooooooo
	 * index + 1 = FILEID for the particular member.
	 */
	private ArrayList<String> members;
	private ArrayList<HashMap<String, String>> hasImages;
  private ArrayList<HashMap<String, String>> hasVideos;
  private ArrayList<String> hasRelations;
	
	/*
	 * hasPart kinda looks like this: {["LABEL"->"Page 1"
	 * "DISPLAY"->"umd:3030" "MASTER"->"umd:3031" "MASTER-RETOUCHED"->"umd:3032"],
	 * [{"LABEL"->"Page 2" "DISPLAY"->"5" "MASTER"->"6"
	 * "MASTER-RETOUCHED"->"7"] Order will be the key.
	 */
  /*
   * hasRelations is an array of pids that indicate related objects
   * The position of the pid in the array will be the basis for the ORDER.
   * arrays are zero based and the ORDER is one based soooooooo
   * index + 1 = ORDER for the particular member.
   */

	private String memberType;

	private static Logger log = Logger.getLogger(METSxml.class);

	public METSxml(String newType) {
		
	  BasicConfigurator.configure();
	  
		aTypes = new ArrayList<String>();
		collections = new ArrayList<String>();
		members = new ArrayList<String>();
		hasImages = new ArrayList<HashMap<String, String>>();
    hasVideos = new ArrayList<HashMap<String, String>>();
    hasRelations = new ArrayList<String>();
		
		if( newType != null && !newType.equals("images") ) {
			memberType = newType.toLowerCase();
		} else {
			memberType = "images";
		}
		
		// Load up the types
		aTypes.add("DISPLAY");
		aTypes.add("MASTER");
		aTypes.add("MASTER-RETOUCHED");

	}

	public METSxml(Document METSsource) {

		this("images");
		parseDoc(METSsource);
		
	}

	public void addCollection(int iCollectionPos, String collPid) {

		int iSize = collections.size();
		
		if( iCollectionPos > 0 ) {
		  if( collPid != null && collPid.length() > 0 ) {
		    if( iCollectionPos >= iSize ) {
		      collections.add(collPid);
		    } else {
		      collections.add((iCollectionPos - 1), collPid);
		    }
		  }
		}
	}

	/**
	 * 
	 * @param collPid
	 */
  public void removeCollection(String collPid) {

    if( collPid != null && collPid.length() > 0 ) {
      int iIndex = collections.indexOf(collPid);
      if( iIndex >= 0 ) {
        collections.remove(iIndex);
      }
    }
  }

	/**
	 * 
	 * type is DISPLAY, MASTER, or MASTER-RETOUCHED
	 * 
	 * @param position
	 * @param memberPid
	 * @param type
	 * @param label
	 */
	public void addPart(int memberPos, String memberPid, String type,
			String label) {
    
    boolean bIsStillGood = true;
		
	  //addMember("1", "umd:1234", "DISPLAY", "Page 1")

		// Test the incoming values
		if( memberPos < 1 ) {
			bIsStillGood = false;
		}
		
		if( memberPid == null || type == null ) {
			bIsStillGood = false;
		} 
		
		if( type == null || ( ! aTypes.contains(type) ) ) {
		  bIsStillGood = false;
		}
		
		if (bIsStillGood) {
		  
	    int iSize;
	    HashMap<String, String> hThisOne;
			
			// Add the part, if necessary
			iSize = hasImages.size();
			if (memberPos < iSize) {
				// Get the object from the existing parts
			  hThisOne = hasImages.get((memberPos - 1));
			} else {
			  hThisOne = new HashMap<String, String>();
			  hasImages.add(hThisOne);
			}
			
			// These values will replace any existing values
			hThisOne.put("LABEL", label);
			hThisOne.put(type, memberPid);
		}
	}

	//TODO: Add public void removePart( String memberPid ){}
	
	public boolean isOK() {
		if (collections.size() > 0 && ( hasImages.size() > 0 || hasVideos.size() > 0 ) ) {
			System.out.println("Everything checks out in the METS");
			return true;
		} else {

			log.error("Bad METS: " + collections.size() + "-" + hasImages.size() + "-" + hasVideos.size());
			return false;
		}
	}

	public Document getXML() {

		Document thisMETS = DocumentHelper.createDocument();

		if (isOK()) {

			// Start building the document
			Namespace nsMets = Namespace.get("http://www.loc.gov/METS/");
			Namespace nsXlink = Namespace.get("xlink",
					"http://www.w3.org/1999/xlink");

			QName thisQ = new QName("mets", nsMets );
			
			Element root = thisMETS
					.addElement(thisQ)
					.addAttribute("schemaLocation",
							"http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd");
			
			root.addNamespace("", "http://www.loc.gov/METS/");
			root.addNamespace("xlink", "http://www.w3.org/1999/xlink");
			
			thisMETS.setRootElement(root);

			// Reconstruct the members array
			rebuildMembers();
			
			// Build the file section
			if (fileSection(root.addElement("fileSec"))) {
				// and then the structmaps - woo hoo
				setRels(root
						.addElement("structMap"));
				if( memberType.equals("videos") ) {
				  setVideos(root.addElement("structMap"));
				} else {
				  setImages(root.addElement("structMap"));
				}
				if( hasRelations.size() > 0 ) {
				  setRelations(root.addElement("structMap"));
				}
			}

		}
		return thisMETS;
	}
	
	public List<List<String>> getParts() {
		
		List<List<String>> lParts = new ArrayList<List<String>>();
		List<String> lPart;
		String strPosition;
		String strPart;
		int position = 0;
		
		for (HashMap<String, String> thisPart : hasImages) {
			
			strPosition = String.valueOf(++position);
				
			// Process the members

			for(String strType: aTypes) {
			  strPart = thisPart.get(strType);
        
				if( strPart != null ) {
		      lPart = new ArrayList<String>();
					lPart.add(strPart);
					lPart.add(thisPart.get("LABEL"));
          lPart.add(strType);
					lPart.add(strPosition);
					lParts.add(lPart);
				}
			}
		}
		
		return lParts;
	}
	
	private void rebuildMembers() {
	  
	  members = new ArrayList<String>();
	  
	  // Scan collections
	  for ( String sPid : collections) {
      members.add(sPid);
    }
	  
	  // Scan hasParts
    String sPartPid;
    for (HashMap<String, String> hPart : hasImages) {
      for( String sType : aTypes ) {
        sPartPid = hPart.get(sType);
        if( sPartPid != null && ( sPartPid.length() > 0 ) ) {
          members.add(sPartPid);
        }
      }
    }
    for (HashMap<String, String> hPart : hasVideos) {
      for( String sType : aTypes ) {
        sPartPid = hPart.get(sType);
        if( sPartPid != null && ( sPartPid.length() > 0 ) ) {
          members.add(sPartPid);
        }
      }
    }
    
	  // Scan hasRelations
    for (String sPid : hasRelations) {
      members.add(sPid);
    }
	  
	}
	
	@SuppressWarnings("unchecked")
	private void parseDoc(Document METSsource ) {
		
	  HashMap<String, String> hMembers = new HashMap<String, String>();
	  HashMap<String, HashMap<String, String>> hParts = new HashMap<String, HashMap<String, String>>();
	  HashMap<String, String> hPart;
	  HashMap<String, String> hRelations = new HashMap<String, String>();
		String strFileID;
		String strPid = "";
		String sFileID = "";
		String sPartType;
		String sObjType;
		Element eNode;
		Element eSubNode;
		List<Object> lNodes;
		List<Object> lSubNodes;
		String sLabel;
		String sObjPos;
		int iMaxIndex = 0;

		if( METSsource != null && METSsource.hasContent() ) {
			System.out.println("Good METS Source");
		} else {
			System.out.println("BAD METS Source");
		}
		
		// First lets load up the Pids associated with this UMDM
		lNodes = DoUtils.getXPath("/mets:mets/mets:fileSec/mets:fileGrp/mets:file")
				.selectNodes(METSsource);

		//System.out.println("Pid Nodes: " + lNodes.size() );
		
		// Roll through the list and extract the members
		for (Object node : lNodes) {
			
			eNode = (Element) node;
			strFileID = eNode.attributeValue("ID");
			eSubNode = (Element) DoUtils.getXPath("mets:FLocat")
					.selectSingleNode(eNode);
			strPid = eSubNode.attributeValue("href");
			
			hMembers.put(strFileID, strPid);
		}
		
		// Lets examine the collections
		lNodes = DoUtils.getXPath(
				"/mets:mets/mets:structMap/mets:div/mets:div[@ID='isMemberOfCollection']/mets:fptr")
				.selectNodes(METSsource);

		for (Object node : lNodes) {
			eNode = (Element) node;
			//eSubNode = (Element) getXPath("mets:fptr").selectSingleNode(eNode);
			sFileID = eNode.attributeValue("FILEID");
			strPid = hMembers.get(sFileID);
      System.out.println("Collection FileID: " + sFileID + ": " + strPid );
			collections.add(strPid);
		}

		// Next winkle out the memberType
		lNodes = DoUtils.getXPath("/mets:mets/mets:structMap/mets:div").selectNodes(METSsource);

		System.out.println("Nodes-" + lNodes.size());
		
		//Get and set the member type: images, videos
		for (Object node : lNodes) {
			eNode = (Element) node;
			sObjType = eNode.attributeValue("ID");
      System.out.println("sObjType-" + sObjType);
			if( sObjType.equals("videos") || sObjType.equals("images") ) {
			  memberType = sObjType;
	      System.out.println("memberType-" + memberType);
			}
		}
		
		// Well that was fun, lets get the images here
		lNodes = DoUtils.getXPath(
				"/mets:mets/mets:structMap[@TYPE = 'LOGICAL']/mets:div[@ID='"
						+ "images" + "']/mets:div").selectNodes(METSsource);
		
		iMaxIndex = 0;

		for (Object node : lNodes) {
			// Each loop will process one part
			eNode = (Element) node;
			sLabel = eNode.attributeValue("LABEL");
			//System.out.println("Label: " + sLabel);
			sObjPos = eNode.attributeValue("ORDER");

			if( iMaxIndex < Integer.parseInt(sObjPos)) {
			  iMaxIndex = Integer.parseInt(sObjPos);
			}
			
			hParts.put(sObjPos, new HashMap<String, String>() );
			hPart = hParts.get(sObjPos);
			
			if( sLabel != null ) {
			  hPart.put("LABEL", sLabel);
			} else {
			  hPart.put("LABEL", "");
			}
			
			/*
			 * Each part can consist of up to 3 types (IDs)
			 * Display, MASTER, and MASTER_RETOUCHED.
			 * 
			 * Image/Book METS nodes look like this
			 * <div LABEL="front" ORDER="1">
			 *   <div ID="DISPLAY">
			 *     <fptr FILEID="2"/>
			 *   </div>
			 *   <div ID="MASTER-RETOUCHED">
			 *     <fptr FILEID="3"/>
			 *   </div>
			 * </div>
			 */

	     lSubNodes = DoUtils.getXPath("mets:div").selectNodes(eNode);
			  
       for (Object subNode : lSubNodes) {
         eSubNode = (Element) subNode;
         sPartType = eSubNode.attributeValue("ID");
         eSubNode = (Element) DoUtils.getXPath("mets:fptr").selectSingleNode(eSubNode);
         sFileID = eSubNode.attributeValue("FILEID");
         strPid = hMembers.get(sFileID);
         hPart.put(sPartType.toUpperCase(), strPid);
       }
		}
		
		//We have collected all of the parts, now lets save them in order
		for (int i = 0; i <= iMaxIndex; i++) {
      sObjPos = String.valueOf(i);
      hPart = hParts.get(sObjPos);
      if( hPart != null ) {
        hasImages.add(hPart);
      }
    }
		
		//Lets get the videos
		lNodes = DoUtils.getXPath(
        "/mets:mets/mets:structMap[@TYPE = 'LOGICAL']/mets:div[@ID='"
            + "videos" + "']/mets:div").selectNodes(METSsource);
		
		hParts = new HashMap<String, HashMap<String, String>>();
		iMaxIndex = 0;

    for (Object node : lNodes) {
      // Each loop will process one part
      eNode = (Element) node;
      sLabel = eNode.attributeValue("LABEL");
      //System.out.println("Label: " + sLabel);
      sObjPos = eNode.attributeValue("ORDER");

      if( iMaxIndex < Integer.parseInt(sObjPos)) {
        iMaxIndex = Integer.parseInt(sObjPos);
      }
      
      hParts.put(sObjPos, new HashMap<String, String>() );
      hPart = hParts.get(sObjPos);
      
      if( sLabel != null ) {
        hPart.put("LABEL", sLabel);
      } else {
        hPart.put("LABEL", "");
      }

      /*
       * Videos only come in one type which is unlabelled
       * 
       * Video METS nodes look like this
       * <div ORDER="1">
       *   <fptr FILEID="2"/>
       * </div>
       */
       eSubNode = (Element) DoUtils.getXPath("mets:fptr").selectSingleNode(eNode);
       if( eSubNode != null ) {
         sFileID = eSubNode.attributeValue("FILEID");
         System.out.println("Video File ID -" + sFileID );
         strPid = hMembers.get(sFileID);
         System.out.println("Video Pid -" + strPid );
         hPart.put("DISPLAY", strPid);
       }
    }
    
    //We have collected all of the parts, now lets save them in order
    for (int i = 0; i <= iMaxIndex; i++) {
      sObjPos = String.valueOf(i);
      hPart = hParts.get(sObjPos);
      if( hPart != null ) {
        hasVideos.add(hPart);
      }
    }
		
		//Lets get the related objects (e.g. from Henson Videos)
		lNodes = DoUtils.getXPath(
        "/mets:mets/mets:structMap[@TYPE = 'LOGICAL']/mets:div[@ID='"
            + "related" + "']/mets:div").selectNodes(METSsource);

		iMaxIndex = 0;
		
    for (Object node : lNodes) {

      // Each loop will process one related item
      eNode = (Element) node;
      sObjPos = eNode.attributeValue("ORDER");
      
      if( iMaxIndex < Integer.parseInt(sObjPos)) {
        iMaxIndex = Integer.parseInt(sObjPos);
      }
      
      eSubNode = (Element) DoUtils.getXPath("mets:fptr").selectSingleNode(eNode);
      
      if( eSubNode != null ) {
        sFileID = eSubNode.attributeValue("FILEID");
        if (sFileID != null) {
          strPid = hMembers.get(sFileID);
        }
      }
      
      if (strPid != null && strPid.length() > 0 ) {
        hRelations.put(sObjPos, strPid);
      }
    }
    
    // We have gathered all of the related objects, now save them in order
    for (int i = 0; i <= iMaxIndex; i++) {
      sObjPos = String.valueOf(i);
      strPid = hRelations.get(sObjPos);
      if( strPid != null && strPid.length() > 0 ) {
        hasRelations.add(strPid);
      }
    }
    
    /* So we have now populated
     * collections
     * hasImages
     * hasVideos
     * hasRelations
     * Now we can return you to the regularly scheduled program.
     */
	}

	private boolean fileSection(Element fileSec) {
		int fileCount = 0;

		Namespace nsMets = Namespace.get("http://www.loc.gov/METS/");
		
		QName thisQ = new QName( "fileGrp", nsMets );
		Element fileGrp = fileSec.addElement(thisQ);

		thisQ = new QName( "ID", nsMets );
		fileGrp.addAttribute("ID", "fedora");

		// Process the members
		for (String sThisOne : members) {
			if( sThisOne != null ) {
				fileCount++;
				setFLocat(fileGrp, sThisOne, fileCount);
			}
		}

		if (fileCount > 0) {
			return true;
		} else {
			return false;
		}
	}

	private void setFLocat(Element fileGrp, String thisOne, int fileCount) {

		Namespace nsMets = Namespace.get("http://www.loc.gov/METS/");
		Namespace nsXlink = Namespace.get("xlink",
				"http://www.w3.org/1999/xlink");
		Element eTemp;
		
		// Create the entry for this pid
		QName thisQ = new QName("file", nsMets );
		Element file = fileGrp.addElement(thisQ);
		thisQ = new QName("ID", nsMets );
		file.addAttribute("ID",String.valueOf(fileCount));

		thisQ = new QName("FLocat", nsMets );
		eTemp = file.addElement(thisQ);
		thisQ = new QName("LOCTYPE", nsMets );
		eTemp.addAttribute("LOCTYPE", "OTHER");
		thisQ = new QName("OTHERLOCTYPE", nsMets );
		eTemp.addAttribute("OTHERLOCTYPE", "PID");
		thisQ = new QName("href", nsXlink );
		eTemp.addAttribute("xlink:href",
						thisOne);
		eTemp.addAttribute("type", "simple");

	}

	private void setRels(Element relsSec) {

		Namespace nsMets = Namespace.get("http://www.loc.gov/METS/");
		QName divQ = new QName("div", nsMets);
		QName idQ = new QName("ID", nsMets);
		QName fptrQ = new QName("fptr", nsMets);
		String sTempStr;
		
		// A new element comes into this function add attributes and such
		relsSec.addAttribute("TYPE", "LOGICAL");

		// Set the wrapper div for this section
		Element rels = relsSec.addElement(divQ);
		rels.addAttribute("ID", "rels");

		// Process the collections
		Element div = rels.addElement(divQ);
		div.addAttribute("ID",
				"isMemberOfCollection");

		for (String thisOne : collections) {
		  
		  // The file ID is the index of this pid in members + 1
		  sTempStr = String.valueOf(members.indexOf(thisOne) + 1);
		  
			if( sTempStr != null && sTempStr.length() > 0 ) {
				div.addElement(fptrQ)
				  .addAttribute("FILEID", sTempStr);
			}
		}

		if ( hasImages.size() > 0 || hasVideos.size() > 0 ) {
      /*
       * Process the members. 
       */
		  String sItem;
		  
		  // Attach the wrapper for the parts
      div = rels.addElement(divQ).addAttribute(idQ, "hasPart");
      
      // Get the parts
      for (HashMap<String, String> hPart : hasImages) {

        for (String sType : aTypes) {
          
          //Get the pid for this Type, if it exists
          sItem = hPart.get(sType);
          
          if (sItem != null && sItem.length() > 0) {
            
            // Get the file ID of this pid
            sTempStr = String.valueOf(members.indexOf(sItem) + 1);
            
            if (sTempStr != null && sTempStr.length() > 0) {
              div.addElement(fptrQ).addAttribute("FILEID", sTempStr);
            }
          }
        }
      }
      
      for (HashMap<String, String> hPart : hasVideos) {

        for (String sType : aTypes) {
          
          //Get the pid for this Type, if it exists
          sItem = hPart.get(sType);
          
          if (sItem != null && sItem.length() > 0) {
            
            // Get the file ID of this pid
            sTempStr = String.valueOf(members.indexOf(sItem) + 1);
            
            if (sTempStr != null && sTempStr.length() > 0) {
              div.addElement(fptrQ).addAttribute("FILEID", sTempStr);
            }
          }
        }
      }
    }
	}

	private void setImages(Element partsSec) {

		Element part; // the div for the page or part
		Namespace nsMets = Namespace.get("http://www.loc.gov/METS/");
		QName divQ = new QName("div", nsMets);
		QName fptrQ = new QName("fptr", nsMets);
    String sTempStr;
    int iPartPos = 0;
		
		partsSec.addAttribute("TYPE", "LOGICAL");

		if (hasImages.size() > 0) {

			// Process the members
			Element div = partsSec.addElement(divQ).addAttribute("ID",
					memberType);

			for (HashMap<String, String> thisPart: hasImages) {
			  
			  iPartPos++;
			  
			  // Create the wrapper div for this part
			  part = div.addElement(divQ);
			  
			  sTempStr = thisPart.get("LABEL");
			  if( sTempStr != null && sTempStr.length() > 0 ) {
			    part.addAttribute("LABEL", sTempStr );
			  }
			  part.addAttribute("ORDER", String.valueOf(iPartPos));
			  
			  // Create the detail of this part
			  for (String sType : aTypes) {
          if (thisPart.get(sType) != null) {
              
            // This pulls the File ID from the members array which is index + 1
            sTempStr = String.valueOf(members.indexOf(thisPart.get(sType)) + 1 );
              
            if( sTempStr != null && sTempStr.length() > 0 ) {
              part.addElement(divQ)
                .addAttribute("ID", sType).addElement(
                    fptrQ).addAttribute("FILEID",
                    sTempStr);
            }
          }
			  }
			}
		}
	}

  private void setVideos(Element partsSec) {

    Element part; // the div for the page or part
    Namespace nsMets = Namespace.get("http://www.loc.gov/METS/");
    QName divQ = new QName("div", nsMets);
    QName fptrQ = new QName("fptr", nsMets);
    String sTempStr;
    int iPartPos = 0;
    
    partsSec.addAttribute("TYPE", "LOGICAL");

    if (hasImages.size() > 0) {

      
      // Process the members
      Element div = partsSec.addElement(divQ).addAttribute("ID",
          "videos");

      for (HashMap<String, String> thisPart: hasImages) {
        
        iPartPos++;
        
        // Create the wrapper div for this part
        part = div.addElement(divQ);
        
        sTempStr = thisPart.get("LABEL");
        if( sTempStr != null && sTempStr.length() > 0 ) {
          part.addAttribute("LABEL", sTempStr );
        }
        
        part.addAttribute("ORDER", String.valueOf(iPartPos));
        
        sTempStr = String.valueOf( members.indexOf(thisPart.get("DISPLAY")) + 1 );
        
        if( sTempStr != null && sTempStr.length() > 0 ) {
          part.addElement(fptrQ).addAttribute("FILEID", sTempStr);
        }
      }
    }
  }

  private void setRelations(Element relationsSec) {

    int position = 0;
    
    Namespace nsMets = Namespace.get("http://www.loc.gov/METS/");
    QName divQ = new QName("div", nsMets);
    QName fptrQ = new QName("fptr", nsMets);
    String sTempStr;
    
    // We get a newly created section here and need to set attributes and such
    relationsSec.addAttribute("TYPE", "LOGICAL");
    
    if (hasRelations.size() > 0) {

      // Create the div for related items
      Element div = relationsSec.addElement(divQ).addAttribute("ID",
          "related");

      // go through the relations, adding them as you go
      for (String strKey: hasRelations) {
        
        // This pulls the File ID from the members array which is index + 1
        sTempStr = String.valueOf(members.indexOf(strKey) + 1 );
        
        if( sTempStr != null && sTempStr.length() > 0 ) {
          
          position++;
          
          div.addElement(divQ)
            .addAttribute("ORDER", String.valueOf(position))
              .addElement(fptrQ)
              .addAttribute("FILEID", sTempStr);
        }
      }
    }
  }
  
  public String setCreationDate( String strDate ) {
    if( strDate != null && strDate.length() > 0 ) {
      strCreationDate = strDate;
    }
    
    return strCreationDate;
  }
  
  public String getCreationDate() {
    return strCreationDate;
  }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		METSxml hereTis = new METSxml("images");

		hereTis.addCollection(1, "umd:1175");
		hereTis.addPart(1, "umd:52", "DISPLAY", "Page 1");
		hereTis.addPart(2, "umd:53", "DISPLAY", "Page 2");
		hereTis.addPart(3, "umd:54", "DISPLAY", "Page 3");
		hereTis.addPart(4, "umd:55", "DISPLAY", "Page 4");
		hereTis.addPart(5, "umd:56", "DISPLAY", "Page 5");

		Document thisMETS = hereTis.getXML();

		try {
			OutputStreamWriter oBigOutWriter;
			OutputFormat oBigFormat;
			XMLWriter oBigWriter;

			// oBigOutWriter = new OutputStreamWriter( new FileOutputStream(
			// "METS.xml" ), "UTF-8" );
			oBigFormat = OutputFormat.createPrettyPrint();
			// oBigWriter = new XMLWriter( oBigOutWriter, oBigFormat );
			oBigWriter = new XMLWriter(System.out, oBigFormat);
			oBigWriter.write(thisMETS);
			//oBigWriter.close();
			
			METSxml secondMETS = new METSxml(thisMETS);
			
			oBigWriter.write(secondMETS.getXML());
			oBigWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
