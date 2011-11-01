package edu.umd.lib.fedora.sweep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import edu.umd.lib.fedora.util.DO.TabText;

public class SubSheet {

  private TabText inFile;
  private String inKey;
  private String inValue;
  private List<HashMap<String, String>> lRecords;
  
  public SubSheet( String strFileName, String strKey, String strVal ) {
    if( strFileName != null && strKey != null && strKey.length() > 0 ) {
      inFile = new TabText(strFileName);
      if( inFile.containsKey(strKey) ) {
        inKey = strKey;
        inValue = strVal;
        lRecords = extractMatches();
      }
    }
  }
  
  private List<HashMap<String, String>> extractMatches() {
    
    ArrayList<HashMap<String, String>> lResult = new ArrayList<HashMap<String, String>>();
    
    for (HashMap<String, String> hFileRecord : inFile ) {
      if( hFileRecord.containsKey(inKey)) {
        if( inValue.equals(hFileRecord.get(inKey))) {
          lResult.add(hFileRecord);
        }
      }
    }
    return lResult;
  }
  
  public List<HashMap<String, String>> getMembers() {
    ArrayList<HashMap<String, String>> lResult = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> hRecord;
    Set<String> sKeys;
    
    for ( HashMap<String, String> hReference : lRecords) {
      hRecord = new HashMap<String, String>();
      sKeys = hRecord.keySet();
      for (String sKey : sKeys) {
        hRecord.put(sKey, hReference.get(sKey));
      }
      lResult.add(hRecord);
    }
    return lResult;
  }
}
