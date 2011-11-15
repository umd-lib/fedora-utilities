package edu.umd.lib.fedora.sweep;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;

import edu.umd.lib.fedora.util.DO.LIMSlookup;

public class CollPids implements Iterable<List<Document>>, Iterator<List<Document>> {

  private ArrayList<Document> aPids;
  private DocumentFactory df;
  private int iHitCount = 0;
  private int iFirst = 0;
  private int iLast = 0;
  private String sCollection;
  private String sHost = "fedora.lib.umd.edu";

  public CollPids(String sColl) {
    
    aPids = new ArrayList<Document>();
    df = new DocumentFactory();
    String sCollPid;
    
    if (sCollection != null && sCollection.length() > 0) {
      if (sCollection.equals("all")) {

      } else {

        sCollPid = LIMSlookup.getCollectionPid(sColl);
        
        if( sCollPid != null ) {
          sCollection = sCollPid;
          // Escape the colon in the pid
          sCollection.replaceAll(":", "%5C:");
        }
        
        
      }
    }
  }
  
  private ArrayList<Document> getResults() {
    ArrayList<Document> aLocalPids = new ArrayList<Document>();
    
    String sHitCount;
    String sFirst;
    String sLast;
    String sSearchURL = "http://" + sHost + "/search/?query=isMemberOfCollection:" + sCollection;
    
    // Build the URL
    if( iHitCount > 0 ) {
      
    }
    
    
    return aLocalPids;
  }

  @Override
  public boolean hasNext() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public List<Document> next() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void remove() {
    // TODO Auto-generated method stub

  }

  @Override
  public Iterator<List<Document>> iterator() {
    // TODO Auto-generated method stub
    return null;
  }

}
