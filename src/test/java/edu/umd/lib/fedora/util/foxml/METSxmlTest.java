package edu.umd.lib.fedora.util.foxml;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import edu.umd.lib.fedora.util.DO.DoUtils;

import junit.framework.TestCase;

public class METSxmlTest extends TestCase {
	
	private String strPrintType = "Terminal";
	private boolean bOnline = true;

	public void testMETSxml() {
		METSxml thisMETS = new METSxml("images");
		assertTrue(thisMETS != null);
	}
	
	public void testMETSfromFile() {

	  UMfactory uf = new UMfactory();
		METSxml thisMETS;
		
		thisMETS = uf.getMETS(System.getProperty("user.dir")+"src/test/resources/edu/umd/lib/fedora/util/foxml/"+"mets_sample.xml", "File");
        
    assertTrue(thisMETS != null);
		
		DoUtils.saveDoc(thisMETS.getXML(), strPrintType);
				
		//METSxml secondMETS = new METSxml(thisMETS.getXML());
				
		//fPrintDoc(secondMETS.getXML(), "");
				
		List<List<String>> lParts = thisMETS.getParts();
				
		System.out.println(lParts.size());
				
		//assertEquals(77, lParts.size());
				
				for (List<String> lPart : lParts) {
					//System.out.print("->");
					
					for (String string : lPart) {
					  System.out.print(">" + string + "<");
          }
					System.out.println("");
				}
	}
	
	public void testMETSimagesFromPid() {

	  if (bOnline) {
	    
      UMfactory uf = new UMfactory();
      METSxml thisMETS;
      List<List<String>> lParts = null;
      
      thisMETS = uf.getMETS("umd:51", "Pid");
      
      assertTrue(thisMETS != null);
      
      DoUtils.saveDoc(thisMETS.getXML(), strPrintType);
      
      lParts = thisMETS.getParts();
      
      System.out.println(lParts.size());
      
      for (List<String> lPart : lParts) {
        for (String string : lPart) {
          System.out.print(">" + string + "<");
        }
        System.out.println("");
      }
    }
	}
  
  public void testMETSvideosFromPid() {

    if (bOnline) {
      
      UMfactory uf = new UMfactory();
      METSxml thisMETS;
      List<List<String>> lParts = null;
      
      thisMETS = uf.getMETS("umd:1075", "Pid");
      
      DoUtils.saveDoc(thisMETS.getXML(), strPrintType);
      
      assertTrue(thisMETS != null);
      
      lParts = thisMETS.getParts();
      
      System.out.println(lParts.size());
      
      for (List<String> lPart : lParts) {
        for (String string : lPart) {
          System.out.print(">" + string + "<");
        }
        System.out.println("");
      }
    }
  }

	public void testMETSfromBadFile() {
	
	  UMfactory uf = new UMfactory();
    METSxml thisMETS;
    List<List<String>> lParts = null;
      
    thisMETS = uf.getMETS("mets_bad_sample.xml", "File");
				
		assertTrue(thisMETS != null);
				
		lParts = thisMETS.getParts();
				
		System.out.println(lParts.size());
				
		// assertEquals(76, lParts.size());
		
		System.out.println(lParts.size());
      
    //assertEquals(77, lParts.size());
    
    for (List<String> lPart : lParts) {
      //System.out.print("->");
      
      for (String string : lPart) {
        System.out.print(">" + string + "<");
      }
      System.out.println("");
    }
		
		
	}
    
    /**************************************************************** PrintDoc */
	/**
	 * Prints a document based on the class's printing directive string
	 */
    private boolean fPrintDoc ( Document docThisDocument, String strFile ) {
    	
    	boolean bTerminalOut = false;
    	boolean bFileOut = false;
    	
    	if( strPrintType.equals("Terminal") ) {
    		bTerminalOut = true;
    	}
    	
    	if( strPrintType.equals("File") ) {
    		bFileOut = true;
    	}
    	
    	if( strPrintType.equals("Both") ) {
    		bTerminalOut = true;
    		bFileOut = true;
    	}
    	
    	strFile = strFile.replaceAll("umd:", "umd_");
    	
    	try {
    		
    		if( bFileOut ) {
    			// Create the string of the filename to be written to
    			String strFileName = strFile;
    	    	
    			// Set up the interminable number of structures for printing this puppy
        		OutputStreamWriter outWriter = new OutputStreamWriter( new FileOutputStream( strFileName ), "UTF-8" );
        		OutputFormat format = OutputFormat.createPrettyPrint();
        		XMLWriter writer = new XMLWriter( outWriter, format );
                writer.write( docThisDocument );
                writer.close();
    		}
    	
    		if( bTerminalOut ) {
    			// Set up the interminable number of structures for printing this puppy
    			System.out.println("File Name is " + strFile );
    			OutputFormat format = OutputFormat.createPrettyPrint();
        		XMLWriter writer = new XMLWriter( System.out, format );
    	        writer.write( docThisDocument );
    		}
    		
    	}
    	
    	catch( Exception e ) {
    		e.printStackTrace();
    	}
    	
    	return true;
    }
	
}
