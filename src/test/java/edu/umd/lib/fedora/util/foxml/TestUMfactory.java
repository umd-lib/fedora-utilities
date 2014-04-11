package edu.umd.lib.fedora.util.foxml;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


import junit.framework.TestCase;


public class TestUMfactory extends TestCase {

	private Properties configFile = new Properties();
	private boolean bOnLine = true;
	private boolean bPrintXML = false;
	
	public void testUMDM() {
		try {
			//configFile.load(new FileInputStream("Load.properties"));
			
			UMfactory umf = new UMfactory();
			
			UMDMxml thisUMDM = umf.getUMDM();
			
			assertTrue(thisUMDM != null);
			
			XMLWriter writer = null;
			
			if (bPrintXML) {
				// Print out the XML
				OutputFormat format = OutputFormat.createPrettyPrint();
				writer = new XMLWriter(System.out, format);
				writer.write(thisUMDM.getXML());
			}
			
			thisUMDM = umf.getUMDM(System.getProperty("user.dir")+File.separator+
					"src"+File.separator+"test"+File.separator+"resources"+
					File.separator+"edu"+File.separator+"umd"+File.separator+"lib"+
					File.separator+"fedora"+File.separator+"util"+File.separator+"foxml"+
					File.separator+"UMDMsample.xml", "File" );
			
			assertTrue(thisUMDM != null);
			
			if (bPrintXML) {
				// Print out the XML
				writer.write(thisUMDM.getXML());
			}
			
			thisUMDM.removeElements("/descMeta/subject");

			System.out.println("--------------- After Removal!");
			if (bPrintXML) {
				// Print out the XML
				writer.write(thisUMDM.getXML());
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace(System.out);
		}
	}
	
	public void testUMDMpid() {

		try {

			//configFile.load(new FileInputStream("Load.properties"));

			UMfactory umf = new UMfactory();

			if (bOnLine) {
				UMDMxml thisUMDM = umf.getUMDM( "umd:51", "Pid" );

				assertTrue(thisUMDM != null);
				if (bPrintXML) {
					// Print out the XML
					OutputFormat format = OutputFormat.createPrettyPrint();
					XMLWriter writer = new XMLWriter(System.out, format);
					writer.write(thisUMDM.getXML());
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace(System.out);
		}
	}
	
	public void testUMAM() {
		try {
			//configFile.load(new FileInputStream("Load.properties"));
			
			UMfactory umf = new UMfactory();
			
			UMAMxml thisUMAM = umf.getUMAM();
			
			assertTrue(thisUMAM != null);
			
			XMLWriter writer = null;
			
			if (bPrintXML) {
				// Print out the XML
				OutputFormat format = OutputFormat.createPrettyPrint();
				writer = new XMLWriter( System.out, format );
				writer.write(thisUMAM.getXML());
			}
			thisUMAM = umf.getUMAM(System.getProperty("user.dir")+File.separator+
					"src"+File.separator+"test"+File.separator+"resources"+
					File.separator+"edu"+File.separator+"umd"+File.separator+"lib"+
					File.separator+"fedora"+File.separator+"util"+File.separator+"foxml"+
					File.separator+"UMAMsample.xml", "File" );
			
			assertTrue(thisUMAM != null);
			
			if (bPrintXML) {
				// Print out the XML
				writer.write(thisUMAM.getXML());
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace(System.out);
		}
	}
	
	public void testUMAMpid() {

		try {

			//configFile.load(new FileInputStream("Load.properties"));

			UMfactory umf = new UMfactory();

			if (bOnLine) {
				UMAMxml thisUMAM = umf.getUMAM( "umd:52", "Pid" );

				assertTrue(thisUMAM != null);
				if (bPrintXML) {
					// Print out the XML
					OutputFormat format = OutputFormat.createPrettyPrint();
					XMLWriter writer = new XMLWriter(System.out, format);
					writer.write(thisUMAM.getXML());
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace(System.out);
		}
	}
}

