package src;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;

import junit.framework.TestCase;

public class TestCovPlace extends TestCase {

  public void testBasicCovPlace() {
    CovPlace oCV = new CovPlace();
    
    assertTrue(oCV != null);
  }
  
  public void testBuiltCovPlace() {
    
    CovPlace oCV = new CovPlace();
    
    oCV.addMember("continent", "Asia");
    
    assertTrue( oCV.isOK() );
    
    oCV.addMember("country", "Japan" );
    oCV.addMember("region", "Fukushima" );
    oCV.addMember("settlement", "Sendai" );
    
    assertTrue( oCV.isOK() );
    
    DocumentFactory df = new DocumentFactory();
    Document dCovPlace = df.createDocument(oCV.getCovPlaceElement() );
    
    DoUtils.saveDoc(dCovPlace, "/Temp/fCovPlace1" );
    
    System.out.println( oCV.getString("All", ", ") );
    
  }
  
}
