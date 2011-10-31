package edu.umd.lib.fedora.util.foxml;

import java.util.ArrayList;

import edu.umd.lib.fedora.util.DO.LIMSlookup;

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
