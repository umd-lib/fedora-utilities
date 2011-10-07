package src;

import java.util.ArrayList;

import doUtils.LIMSlookup;

public class CollectionPids {

  private int iPidCount = 0;
  private String sPid = "";
  private ArrayList<String> aPids;
  
  public CollectionPids() {
    aPids = new ArrayList<String>();
  }
  
  public CollectionPids( String strPid ) {
    
    this();
    
    if( strPid != null && strPid.length() > 0 ) {
      
      if( LIMSlookup.isCollection(strPid) ) {
        
        // Get the pids and add them to the Array
        
      }
    }
  }
}
