package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import doUtils.LIMSlookup;

public class CovPlace {

  private String sOuterTag = "covPlace";
  private String sType = "";
  HashMap<String, List<String>> hCovPlace;
  
  public CovPlace() {
    hCovPlace = new HashMap<String, List<String>>();
  }
  
  public CovPlace(String strOuterTagName ) {
    this();
    
    sOuterTag = strOuterTagName;
  }
  
  @SuppressWarnings("unchecked")
  public CovPlace(Element thisCovPlace ) {
    this();
    
    if (thisCovPlace != null) {
      
      // Parse Element into CovPlace data structure
      sOuterTag = thisCovPlace.getName();
      List<Element> lGeogNames = thisCovPlace.elements();
      String sType;
      String sText;
      List<String> lPlaces;
      
      for (Element eElement : lGeogNames) {
        sType = eElement.attributeValue("type");
        sText = eElement.getText();

        if (LIMSlookup.isCovPlaceType(sType)) {
          lPlaces = hCovPlace.get(sType);

          if (lPlaces == null) {
            lPlaces = new ArrayList<String>();
            hCovPlace.put(sType, lPlaces);
          }

          lPlaces.add(sText);
        }
      }
    }
    
  }
  

  
  public CovPlace( Element thisCovPlace, String strOuterTagName ) {
   
    this(thisCovPlace);
    
    sOuterTag = strOuterTagName;
  }
  
  public boolean addMember( String strType, String strValue ) {
    boolean bResult = false;
    
    String sType = strType.toLowerCase();
    List<String> lPlaces;
    
    if(LIMSlookup.isCovPlaceType(sType)) {
      
      lPlaces = hCovPlace.get(sType);
      
      if( lPlaces == null ) {
        lPlaces = new ArrayList<String>();
        hCovPlace.put(sType, lPlaces);
      }
      
      lPlaces.add(strValue);
      
    }
    
    return bResult;
  }
  
  public List<String> getMember( String strType, String strDelimiter ) {
    
    List<String> lResult = new ArrayList<String>();
    List<String> lPlaces;
    
    if( LIMSlookup.isCovPlaceType(strType) ) {
      
      lPlaces = hCovPlace.get(strType);
      
      if( ( lPlaces != null ) && ( lPlaces.size() > 0 ) ) {
        
        for (String sValue : lPlaces) {
          lResult.add(sValue);
        }
        
      }
      
    }
    
    return lResult;
    
  }
  
  public String setType( String thisType ) {
    
    if( thisType != null && thisType.length() > 0 ) {
      sType = thisType;
    }
    
    return sType;
  }
  
  public String getType() {
    
    return sType;
  }
  
  public Element getCovPlaceElement() {
    
    DocumentFactory df = new DocumentFactory();
    Element eCovPlace = df.createElement(sOuterTag);
    List<String> lTypes = LIMSlookup.getCovPlaceTypes();
    List<String> lPlaces;
    
    if( sType != null && sType.length() > 0 ) {
      eCovPlace.addAttribute("type", sType );
    }
    
    for (String sType : lTypes) {
      lPlaces = hCovPlace.get(sType);
      
      if( ( lPlaces != null ) && ( lPlaces.size() > 0 ) ) {
        
        for(String sPlace : lPlaces) {

          eCovPlace.addElement("geogName")
          .addAttribute("type", sType)
          .setText(sPlace);
          
        }
      }
    }
    
    return eCovPlace;
  }
  
  public String getString( String strType, String strDelimiter ) {
    
    String sResult = "";
    List<String> lTypes = LIMSlookup.getCovPlaceTypes();
    List<String> lPlaces;
    
    for (String sType : lTypes) {
      
      if ( strType.equalsIgnoreCase("all") || 
           sType.equals(strType) ) {
        
        lPlaces = hCovPlace.get(sType);
        if ((lPlaces != null) && (lPlaces.size() > 0)) {

          for (String sPlace : lPlaces) {

            if (sResult.length() > 0) {
              sResult += strDelimiter;
            }

            sResult += sPlace;
          }
        }
      }
    }
    
    return sResult;
    
  }
  
  public boolean isOK() {
    boolean bResult = false;
    
    if( hCovPlace != null && hCovPlace.size() > 0 ) {
      List<String> lPlaces = hCovPlace.get("continent");
      
      if( lPlaces != null && lPlaces.size() > 0 ) {
        bResult = true;
      }
      
    }
    
    return bResult;
  }
  
  /**
   * @param args
   */
  public static void main ( String[] args ) {
    // TODO Auto-generated method stub

  }

}
