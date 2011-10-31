package edu.umd.lib.fedora.util.foxml;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.InvalidXPathException;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import edu.umd.lib.fedora.util.DO.ByNumeric;

public class UMDMmets {

	DocumentFactory docFactory = DocumentFactory.getInstance();

	public HashMap<String, XPath> xPath = new HashMap<String, XPath>();
	public HashMap<String, String> namespace = new HashMap<String, String>();
	
	// collection[0] is the primary collection
	ArrayList<String> collections;
	ArrayList<String> members;
	ArrayList<HashMap<String, String>> hasPart;
	/*
	 * hasPart kinda looks like this:
	 * hasPart[0]->"LABEL"->"Page 1"
	 *             "DISPLAY"->"2"
	 *             "MASTER"->"3"
	 *             "MASTER-RETOUCHED"->"4"
	 * hasPart[1]->"LABEL"->"Page 2"
	 *             "DISPLAY"->"5"
	 *             "MASTER"->"6"
	 *             "MASTER-RETOUCHED"->"7"
	 * Order will be the string of the index + 1
	 */
	
	String memberType;
	
	private final int PID = 0;
	private final int FILE_ID = 1;
	private final int LABEL = 2;
	private final int TYPE = 3;
	private final int ORDER = 4;
	
	private static Logger log = Logger.getLogger(UMDMmets.class);
	
	public UMDMmets()  {
		collections = new ArrayList<String>();
		members = new ArrayList<String>();
		hasPart  = new ArrayList<HashMap<String, String>>();
		memberType = "images";
	}
	
	public UMDMmets(Document METSsource ) {
		
		this();
		
		String fileID;
		String sPid = "";
		String sFileID = "";
		String sPartType;
		Element eNode;
		Element eSubNode;
		List<Object> lNodes;
		List<Object> lSubNodes;
		String sLabel;
		
		// First lets load up the Pids associated with this UMDM
		lNodes = getXPath("/mets:mets/mets:fileSec/mets:fileGrp/mets:file/")
		.selectNodes(METSsource);
		
		for( Object node : lNodes ) {
			eNode = (Element) node;
			fileID = eNode.attributeValue("ID");
			eNode = (Element) getXPath("/mets:file/mets:FLocat/")
			.selectSingleNode(eNode);
			sPid = eNode.attributeValue("xlink:href");
			members.add(Integer.parseInt(fileID), sPid);
		}
		
		// Lets examine the collections
		lNodes = getXPath("/mets:mets/mets:structMap/mets:div[@ID='rels']/mets:div[@ID='isMemberOfCollection']/")
		.selectNodes(METSsource);
		int digObjPos = 0;
		
		for( Object node : lNodes ) {
			eNode = (Element) node;
			eNode = (Element) getXPath("/mets:fptr/")
			.selectSingleNode(eNode);
			sPid = eNode.getText();
			collections.add(digObjPos, sFileID);
		}
		
		// Next winkle out the memberType
		lNodes = getXPath("/mets:mets/mets:structMap/mets:div[@ID!='rels']/")
		.selectNodes(METSsource);
		digObjPos = 0;
		
		for( Object node : lNodes ) {
			eNode = (Element) node;
			memberType = eNode.attributeValue("ID");
		}
		
		// Well that was fun, lets get the parts here
		lNodes = getXPath("/mets:mets/mets:structMap/mets:div[@ID!='rels']/mets:dev/")
		.selectNodes(METSsource);
		
		for( Object node : lNodes ) {
			// Each loop will process one part
			eNode = (Element) node;
			sLabel =  eNode.attributeValue("LABEL");
			digObjPos = Integer.parseInt(eNode.attributeValue("ORDER"));
			lSubNodes = getXPath("/mets:div")
			.selectNodes(eNode);
			
			HashMap<String, String> hPart =  new HashMap<String, String>();
			hPart.put("LABEL", sLabel);
			
			// Each part can consist of up to 3 types (IDs)
			// Display, MASTER, and MASTER_RETOUCHED
			for( Object subNode : lSubNodes ) {
				eSubNode = (Element) subNode;
				sPartType = eSubNode.attributeValue("ID");
				eSubNode = (Element) getXPath("/mets:div/fptr")
				.selectSingleNode(eNode);
				sFileID = eSubNode.getText();
				hPart.put(sPartType, sFileID);
			}
			
			hasPart.add(digObjPos, hPart);
		}
	}
	
	public UMDMmets( Properties config, Map<String, String> names, String thisPID ) {
		
		this();
		
		if( thisPID.equals("UMD_IMAGE") ||
				thisPID.equals("UMD_BOOK") ) {
			memberType = "images";
		} else {
			memberType = "videos";
		}
		
		System.out.println("Created Object");
	}
	
	public void addCollection(String collPid ) {
		
		if( ! members.contains(collPid)) {
			
			// add the collection to the general members of the object
			members.add(collPid);
			
			// Get the position of the member in the members array and put it in the collections arraylist
			String memberPos = String.valueOf(members.indexOf(collPid) + 1);
			
			if( ! collections.contains(memberPos)) {
				collections.add(memberPos);
			}
		}
		
		System.out.println("Added Collection");
	}
	
	public boolean removeCollection(String collPid ) {
		
		return removePid(collPid);
	}
	
	public void addMember(int position, String memberPid, 
			String type, String label ) {
		boolean thisIsNew = true;
		int memberPos = position > 0 ? position - 1 : 0;
		
		HashMap<String, String> hThisOne;
		
		// Check to see if a <type> already exists for this <position>
		// There may only be one - thats it
		try{
			if( hasPart.get(memberPos) == null ) {
				hasPart.add(memberPos, new HashMap<String, String>());
			}
		}
		catch( Exception e) {
			hasPart.add(memberPos, new HashMap<String, String>());
		}
		
		// add this entry to the list of members
		members.add(memberPid);
		
		hThisOne = hasPart.get(memberPos);
		if( hThisOne.get(type) != null ) {
			thisIsNew = false;
		}
		
		if( ! thisIsNew ) {
			log.info("Updating existing Member: " +
					memberPid + ", " +
					type + ", " +
					String.valueOf(position));
		}
		
		hThisOne.put("type", type);
		hThisOne.put("Order", String.valueOf(position));
		hThisOne.put("LABEL", label);
		hThisOne.put("memberPos", String.valueOf(members.indexOf(memberPid) + 1));
		
		System.out.println("Added Member");
	}
	
	public boolean removeMember(String collPid) {
		return removePid(collPid);
	}
	
	public boolean isOK() {
		if( collections.size() > 0 && members.size() > 0 ) {
			return true;
		} else {
			return false;
		}
	}
	
	public Document getXML() {
		
		Document thisMETS = DocumentHelper.createDocument();
	
		if( isOK() ) {
			
			// Start building the document 
			Namespace nsMets = Namespace.get("http://www.loc.gov/METS/");
			Namespace nsXlink = Namespace.get("xlink", "http://www.w3.org/1999/xlink");
			//Namespace nsNone = Namespace.get("");
			
			Element root = thisMETS.addElement("mets")
			.addAttribute("schemaLocation", 
					"http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd");
			root.add(nsMets);
			root.addNamespace("xlink", "http://www.w3.org/1999/xlink");
			root.addNamespace("", "http://www.loc.gov/METS/");
			
			thisMETS.setRootElement(root);
			
			// Build the file section
			if( fileSection( root.addElement("fileSec", "http://www.loc.gov/METS/") ) ) {
				// and then the structmaps - woo hoo
				setRels(root.addElement("structMap", "http://www.loc.gov/METS/") );
				setParts(root.addElement("structMap", "http://www.loc.gov/METS/") );
			}
			
		} else {
			System.out.println("Not OK");
		}
		return thisMETS;
	}
	
	private boolean fileSection(Element fileSec ) {
		int fileCount = 0;
		
		Element fileGrp = fileSec.addElement("fileGrp");
		
		fileGrp.addAttribute("ID", "fedora");
		
		// Process the members
		for (String sThisOne : members) {
			fileCount++;
			setFLocat(fileGrp, sThisOne, ( members.indexOf(sThisOne)+1 ) );
		}
		
		if( fileCount > 0 ) {
			return true;
		} else {
			return false;
		}
	}
	
	private void setFLocat(Element fileGrp, 
			String thisOne, 
			int fileCount) {
		
		// Create the entry for this pid
		Element file = fileGrp.addElement("file")
		.addAttribute("ID", String.valueOf(fileCount));
		
		file.addElement("FLocat")
		.addAttribute("LOCTYPE", "OTHER")
		.addAttribute("OTHERLOCTYPE", "PID")
		.addAttribute("xlink:href", thisOne)
		.addAttribute("xlink:type", "simple");
		
	}
	
	private void setRels(Element relsSec ) {
		
		ArrayList<String> aParts = new ArrayList<String>();
		
		relsSec.addAttribute("TYPE", "LOGICAL");
		
		Element rels = relsSec.addElement("div")
		.addAttribute("ID", "rels");
		
		// Process the collections
		Element div = rels.addElement("div")
		.addAttribute("ID", "isMemberOfCollection");
		
		for (String thisOne : collections) {
			div.addElement("fptr")
			.addAttribute("FILEID", thisOne);
		}
		
		/*
		 * Process the members.  Remember
		 * members has Pids and this list needs file IDs
		 * 10 use the Pids to check against which members are collections
		 * 2) use the index of the pid in members to gen the file ID.
		 */
		div = rels.addElement("div")
		.addAttribute("ID", "hasPart");
		
		
		for (HashMap<String, String> thisPart : hasPart) {
			//Set<String> sKeys = thisPart.keySet();
			aParts.add(thisPart.get("memberPos"));
			/*
			for(String sKey : sKeys ) {
				if( ! sKey.equals("LABEL") ) {
					aParts.add(thisPart.get(sKey));
				}
			}
			*/
		}
		
		Collections.sort(aParts, new ByNumeric() );
		
		for( String sFileID : aParts ) {
			div.addElement("fptr")
			.addAttribute("FILEID", sFileID);
		}
		
	}
	
	private void setParts( Element partsSec ) {

		Element part; // the div for the page or part
		Element item; // the div for the derivative of the part
		boolean bFinished = false;
		int position = 1;
		
		partsSec.addAttribute("TYPE", "LOGICAL");
		
		if( hasPart.size() > 0 ) {
			
			// Process the members
			Element div = partsSec.addElement("div")
			.addAttribute("ID", memberType);
			
			while (! bFinished) {
				
				bFinished = true;
				part = null;

				for (HashMap<String, String> thisPart : hasPart) {

					
					if( thisPart.get("Order").equals(String.valueOf(position))) {
						
						bFinished = false;

						if( part == null ) {
							part = div.addElement("div").addAttribute("LABEL",
								thisPart.get("LABEL")).addAttribute("ORDER",
								String.valueOf(position));
						}
						
						if( memberType.equals("images")) {
							part.addElement("div")
							.addAttribute("ID", thisPart.get("type"))
							.addElement("fptr").addAttribute("FILEID",
									thisPart.get("memberPos"));
						} else {
							part.addElement("fptr")
							.addAttribute("FILEID",
									thisPart.get("memberPos"));
						}
						
					}
					
				}
				
				position++;
			}
		}
	}
	
	private boolean removePid(String memberPid) {

		boolean bDeleted = false;
		
		if (members.contains(memberPid)) {

			String memberPos = String.valueOf(members.indexOf(memberPid) - 1);

			members.remove(memberPid);

			if (collections.contains(memberPos)) {
				collections.remove(memberPos);
				bDeleted = true;
			} else {
				// search the hasPart
				ArrayList<HashMap<String, String>> hParts = 
					new ArrayList<HashMap<String, String>>();

				for (HashMap<String, String> hThisOne : hasPart) {
					if (hThisOne.containsValue(memberPos)) {
						Set<String> sKeys = hThisOne.keySet();
						for (String sKey : sKeys) {
							if (memberPos.equals(hThisOne.get(sKey))) {
								hThisOne.remove(sKey);
							}
						}
					}

					if (hThisOne.size() > 1) {
						hParts.add(hThisOne);
					}
					
				}
				
				if( hParts.size() > 0 ) {
					hasPart = hParts;
				}

			}
		}
		return bDeleted;
	}

	/************************************************************* getXPath */
	/**
	 * Get a compiled XPath object for the expression. Cache.
	 */

	private XPath getXPath(String XPath) throws InvalidXPathException {

		XPath xpath = null;

		if (xPath.containsKey(XPath)) {
			xpath = xPath.get(XPath);

		} else {
			xpath = docFactory.createXPath(XPath);
			xpath.setNamespaceURIs(namespace);
			xPath.put(XPath, xpath);
		}

		return xpath;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UMDMmets hereTis = new UMDMmets();
		
		hereTis.addCollection("umd:1175");
		hereTis.addMember(1, "umd:52", "DISPLAY", "Page 1");
		hereTis.addMember(2, "umd:53", "DISPLAY", "Page 2");
		hereTis.addMember(3, "umd:54", "DISPLAY", "Page 3");
		hereTis.addMember(4, "umd:55", "DISPLAY", "Page 4");
		hereTis.addMember(5, "umd:56", "DISPLAY", "Page 5");
		
		Document thisMETS = hereTis.getXML();
		
		try {
			OutputStreamWriter oBigOutWriter;
			OutputFormat oBigFormat;
			XMLWriter oBigWriter;
			
			//oBigOutWriter = new OutputStreamWriter( new FileOutputStream( "METS.xml" ), "UTF-8" );
			oBigFormat = OutputFormat.createPrettyPrint();
			//oBigWriter = new XMLWriter( oBigOutWriter, oBigFormat );
			oBigWriter = new XMLWriter( System.out, oBigFormat );
			oBigWriter.write(thisMETS);
			oBigWriter.close();
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
		
		
	}

}
