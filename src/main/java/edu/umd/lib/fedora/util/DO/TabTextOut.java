package edu.umd.lib.fedora.util.DO;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TabTextOut {

  private List<String> lKeys;
  private OutputStreamWriter oListWriter;
  
  public TabTextOut( String strFileName, List<String> lKeyList, List<String> lLabelList ) throws IOException {
    
    lKeys = new ArrayList<String>();
    
    if( ( lKeyList != null ) && 
        ( lLabelList != null ) && 
        ( lKeyList.size() == lLabelList.size() ) ) {
      
      oListWriter = new OutputStreamWriter( new FileOutputStream( strFileName ), "UTF-8" );
      
      for (int i = 0; i < lKeyList.size(); i++) {
        
        lKeys.add(lKeyList.get(i));
        
        if( i > 0 ) {
          oListWriter.write("\t");
        }
        oListWriter.write(lLabelList.get(i));
      }
      
      oListWriter.write("\n");
      
    }
  }
  
  public HashMap<String, String> getBlankRecord() {
    HashMap<String, String> hRecord = new HashMap<String, String>();
    
    for (int i = 0; i < lKeys.size(); i++) {
      
      hRecord.put(lKeys.get(i), "");
      
    }
    
    return hRecord;
  }
  
  public void printRecord( HashMap<String, String> thisRecord ) throws IOException {
    
    if (thisRecord != null) {
      String sCurrentValue = null;
      for (int i = 0; i < lKeys.size(); i++) {
        if (i > 0) {
          oListWriter.write("\t");
        }
        sCurrentValue = thisRecord.get(lKeys.get(i));
        if (sCurrentValue != null) {
          oListWriter.write(sCurrentValue);
        } else {
          oListWriter.write("");
        }
      }
      oListWriter.write("\n");
    }
  }
  
  public void closeWriter() throws IOException {
    oListWriter.close();
  }
}
