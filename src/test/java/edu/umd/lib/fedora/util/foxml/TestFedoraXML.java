package edu.umd.lib.fedora.util.foxml;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import junit.framework.TestCase;

public class TestFedoraXML extends TestCase {

  public void testFOXMLfile() {
    
    try {
      
      File thisXml = new File(System.getProperty("user.dir")+File.separator+
    			"src"+File.separator+"test"+File.separator+"resources"+
      			File.separator+"edu"+File.separator+"umd"+File.separator+"lib"+
      			File.separator+"fedora"+File.separator+"util"+File.separator+"foxml"+
      			File.separator+"foxml-sample-umam.xml");
      SAXReader reader = new SAXReader();
      Document thisDoc;
      thisDoc = reader.read(thisXml);
      FedoraXML thisObject = new FedoraXML(thisDoc);
      
      assertTrue( thisObject != null );
      
    } catch (DocumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
