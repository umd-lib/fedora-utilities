package edu.umd.lib.fedora.util.DO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This a collection of values and hashes associated with Fedora.
 * This allows a central repository of values that would otherwise have to
 * be hard coded into the Fedora manipulation objects.
 * It also allows a central location for modification as well.
 * 
 * @author phammer
 *
 */
public class LIMSlookup {

  /*
   * For consistency sake, please make all paths end without a slash
   */
  
	private final static HashMap<String, String> hContentModels = 
		new HashMap<String, String>();
	static {
		hContentModels.put("Image", "UMD_IMAGE");
		hContentModels.put("Book", "UMD_BOOK");
		hContentModels.put("Video", "UMD_VIDEO");
		hContentModels.put("TEI", "UMD_TEI");
		hContentModels.put("EAD", "UMD_EAD");
	}
	
	private final static String[] aStatuses = new String[]{"Complete",
		"Incomplete",
		"Pending",
		"Private",
		"Quarantined"};

	private final static HashMap<String, String> hCollections = 
		new HashMap<String, String>();
	static {
		hCollections.put("album", "umd:3391"); //alb
		hCollections.put("worldsfairs", "umd:2"); //wfc
		hCollections.put("filmsum", "umd:1158"); //flm
		hCollections.put("henson", "umd:1157"); //hen
		hCollections.put("archivesum", "umd:2258"); //aum
		hCollections.put("sterling", "umd:2256"); //sfp
		hCollections.put("baroness", "umd:2257"); //efl
		hCollections.put("earlymodernwomen", "umd:10537"); //emw
		hCollections.put("prange", "umd:11575"); //pcb
		hCollections.put("ntl", "umd:15237"); //ntl
		hCollections.put("misc", "umd:3392"); //msc
		hCollections.put("irith", "umd:10538"); //irh
		hCollections.put("versioningmachine", "umd:10539"); //vmc
		hCollections.put("mdhc", "umd:16719"); //mhc
		hCollections.put("internetarchive", "umd:47085"); //iar
		hCollections.put("intransition", "umd:50580"); //trn
		hCollections.put("mdmaps", "umd:57340"); //mmc
		hCollections.put("mdcivilwar", "umd:66501"); //mcw
		hCollections.put("laborinamerica", "umd:78013"); //lia
		hCollections.put("gis", "umd:78013"); //gis
	}
	
	private final static HashMap<String, String> hCollSWF = 
		new HashMap<String, String>();
	static {
		hCollSWF.put("album", "http://fedora.umd.edu/content/zoom/zoom.swf"); //alb
		hCollSWF.put("worldsfairs", "http://fedora.umd.edu/content/zoom/zoom.swf"); //wfc
		hCollSWF.put("filmsum", "http://fedora.umd.edu/content/zoom/zoom.swf"); //flm
		hCollSWF.put("henson", "http://fedora.umd.edu/content/zoom/zoom.swf"); //hen
		hCollSWF.put("archivesum", "http://fedora.umd.edu/content/zoom/zoom.swf"); //aum
		hCollSWF.put("sterling", "http://fedora.umd.edu/content/zoom/zoom.swf"); //sfp
		hCollSWF.put("baroness", "http://fedora.umd.edu/content/zoom/zoom.swf"); //efl
		hCollSWF.put("earlymodernwomen", "http://fedora.umd.edu/content/zoom/zoom.swf"); //emw
		hCollSWF.put("prange", "http://fedora.umd.edu/content/zoom/zoom.swf"); //pcb
		hCollSWF.put("ntl", "http://fedora.umd.edu/content/zoom/zoom.swf"); //ntl
		hCollSWF.put("misc", "http://fedora.umd.edu/content/zoom/zoom.swf"); //msc
		hCollSWF.put("irith", "http://fedora.umd.edu/content/zoom/zoom.swf"); //irh
		hCollSWF.put("versioningmachine", "http://fedora.umd.edu/content/zoom/zoom.swf"); //vmc
		hCollSWF.put("mdhc", "http://fedora.umd.edu/content/zoom/zoom.swf"); //mhc
		hCollSWF.put("internetarchive", "http://fedora.umd.edu/content/zoom/zoom.swf"); //iar
		hCollSWF.put("intransition", "http://fedora.umd.edu/content/zoom/zoom.swf"); //trn
		hCollSWF.put("mdmaps", "http://fedora.umd.edu/content/zoom/zoom.swf"); //mmc
		hCollSWF.put("mdcivilwar", "http://fedora.umd.edu/content/zoom/zoom.swf"); //mcw
		hCollSWF.put("laborinamerica", "http://fedora.umd.edu/content/zoom/zoom.swf"); //lia
		hCollSWF.put("gis", "http://fedora.umd.edu/content/zoom/zoom.swf"); //gis
	}
	
	private final static HashMap<String, String> hCollImageBase = new HashMap<String, String>();
	static {
	  hCollImageBase.put("album", "DBharmony"); //alb
	  hCollImageBase.put("worldsfairs", "Worlds_Fairs"); //wfc
	  hCollImageBase.put("filmsum", "UMDfilms"); //flm
	  hCollImageBase.put("henson", "Henson"); //hen
	  hCollImageBase.put("archivesum", "Archives"); //aum
	  hCollImageBase.put("sterling", "Sterling"); //sfp
	  hCollImageBase.put("baroness", "Baroness"); //efl
	  hCollImageBase.put("earlymodernwomen", "EMW"); //emw
	  hCollImageBase.put("prange", "Prange"); //pcb
	  hCollImageBase.put("ntl", "NTL"); //ntl
	  hCollImageBase.put("misc", "Misc"); //msc
	  hCollImageBase.put("irith", "Irith"); //irh
	  hCollImageBase.put("versioningmachine", "VMC"); //vmc
	  hCollImageBase.put("mdhc", "MDHB"); //mhc
	  hCollImageBase.put("internetarchive", "Iarch"); //iar
	  hCollImageBase.put("intransition", "Baroness"); //trn
	  hCollImageBase.put("mdmaps", "Maps"); //mmc
	  hCollImageBase.put("mdcivilwar", "MCW"); //mcw
	  hCollImageBase.put("laborinamerica", "Labor"); //lia
	  hCollImageBase.put("gis", "GIS"); //gis
	}

	private final static HashMap<String, String> hCollWebBase = 
		new HashMap<String, String>();
	static {
		hCollWebBase.put("album", "DBharmony"); //alb
		hCollWebBase.put("worldsfairs", "Worlds_Fairs"); //wfc
		hCollWebBase.put("filmsum", "UMDfilms"); //flm
		hCollWebBase.put("henson", "Henson"); //hen
		hCollWebBase.put("archivesum", "Archives"); //aum
		hCollWebBase.put("sterling", "Sterling"); //sfp
		hCollWebBase.put("baroness", "Baroness"); //efl
		hCollWebBase.put("earlymodernwomen", "EMW"); //emw
		hCollWebBase.put("prange", "Prange"); //pcb
		hCollWebBase.put("ntl", "NTL"); //ntl
		hCollWebBase.put("misc", "Misc"); //msc
		hCollWebBase.put("irith", "Irith"); //irh
		hCollWebBase.put("versioningmachine", "VMC"); //vmc
		hCollWebBase.put("mdhc", "MDHB"); //mhc
		hCollWebBase.put("internetarchive", "Iarch"); //iar
		hCollWebBase.put("intransition", "Baroness"); //trn
		hCollWebBase.put("mdmaps", "Maps"); //mmc
		hCollWebBase.put("mdcivilwar", "MCW"); //mcw
		hCollWebBase.put("laborinamerica", "Labor"); //lia
		hCollWebBase.put("gis", "GIS"); //gis
	}

	private final static String[] aPrangeBases = new String[] {
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB1",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB2",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB3",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB4",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB5",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB6",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB7",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB8/Nichimy047",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB8/Nichimy048",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB8/Nichimy049",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB8/Nichimy050",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB9/Backup070",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB9/Backup071",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB9/Backup072",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB9/Backup073",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB10/Backup074",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB10/Backup075",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB11/Backup076",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB11/Backup077",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB11/Backup078",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB11/Backup079",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB11/Backup080",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB11/Backup081",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB11/Backup082",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB12/Prange079",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB12/Prange080",
			"/pcbweb1/upload/Full Jpegs/Full  JPEG PCB12/Prange081",
			"/pcbweb1/upload/Full Jpegs/Full JPEG PCB13/Prange082",
			"/pcbweb1/upload/Full Jpegs/Full JPEG PCB13/Prange083",
			"/pcbweb1/upload/Full Jpegs/Full JPEG PCB14/Prange084",
			"/pcbweb1/upload/Full Jpegs/Full JPEG PCB14/Prange085",
			"/pcbweb2/upload/Full JPEG PCB15/Prange086",
			"/pcbweb2/upload/Full JPEG PCB15/Prange087",
			"/pcbweb2/upload/Full JPEG PCB15/Prange088",
			"/pcbweb2/upload/Full JPEG PCB16/Prange089",
			"/pcbweb2/upload/Full JPEG PCB16/Prange090",
			"/pcbweb2/upload/Full JPEG PCB17/Prange091",
			"/pcbweb2/upload/Full JPEG PCB17/Prange092",
			"/pcbweb2/upload/Full JPEG PCB18/Prange093",
			"/pcbweb2/upload/Full JPEG PCB18/Prange094",
			"/pcbweb2/upload/Full JPEG PCB19/Prange095",
			"/pcbweb2/upload/Full JPEG PCB19/Prange096",
			"/pcbweb2/upload/Full JPEG PCB20/Prange097",
			"/pcbweb2/upload/Full JPEG PCB20/Prange098",
			"/pcbweb2/upload/Full JPEG PCB21",
			"/pcbweb2/upload/Full JPEG PCB35/Prange132"};
	
  private final static String[] aCovPlaceTypes = new String[]{"continent",
    "zone",
    "bloc",
    "country",
    "region",
    "settlement",
    "district"};
  
  private final static String[] aDOtypes = new String[]{"doInfo", "amInfo"};

	public static String getContentModel( String strContentWord ) {
		String strResult = hContentModels.get(strContentWord);
		return strResult;
	}
	
	public static boolean isContentModelKey( String strContentWord ) {
		String strResult = hContentModels.get(strContentWord);
		if( ( strResult != null ) && ( strResult.length() > 0 ) ) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isContentModelValue( String strContentWord ) {
		return hContentModels.containsValue(strContentWord);
	}
	
	public static HashMap<String, String> getContentModels() {
		HashMap<String, String> hResult = new HashMap<String, String>();
		Set<String> sKeys = hContentModels.keySet();
		
		for (String strKey : sKeys) {
			hResult.put(strKey, hContentModels.get(strKey));
		}
		
		return hResult;
	}
	
	public static boolean isStatus(String strTest ) {
		boolean bResult = false;
		
		for (int i = 0; i < aStatuses.length; i++) {
			if( strTest.equals(aStatuses[i]) ) {
				bResult = true;
				break;
			}
		}
		
		return bResult;
	}
	public static boolean isCollection( String strTest ) {
		if( strTest != null && hCollections.containsKey(strTest.toLowerCase())) {
			return true;
		} else {
		  if( strTest.startsWith("umd")) {
		    if( hCollections.containsValue(strTest)) {
		      return true;
		    } else {
		      return false;
		    }
		  } else {
		    return false;
		  }
		}
	}
	
	public static String getCollectionPid( String strCollectionName ) {
		String strResult = hCollections.get(strCollectionName.toLowerCase());
		if( strCollectionName.startsWith("umd")) {
		  if( hCollections.containsValue(strCollectionName)) {
		    return strCollectionName;
		  }
		}
		return strResult;
	}
  
  public static String getCollectionName( String strCollectionPid ) {
    
    Set<String> sKeys = hCollections.keySet();
    String strResult = "";
    String strTemp;
    
    for (String strKey : sKeys) {
      
      strTemp = hCollections.get(strKey);
      
      if( strTemp.equals(strCollectionPid)) {
        strResult = strKey;
        break;
      }
    }
    
    return strResult;
  }
	
	public static HashMap<String, String> getCollections() {
		HashMap<String, String> hResult = new HashMap<String, String>();
		Set<String> sKeys = hCollections.keySet();
		
		for (String strKey : sKeys) {
			hResult.put(strKey, hCollections.get(strKey));
		}
		
		return hResult;
	}
	
	public static boolean isCovPlaceType( String strTest ) {
    boolean bResult = false;
    
    for (int i = 0; i < aCovPlaceTypes.length; i++) {
      if( strTest.equals(aCovPlaceTypes[i]) ) {
        bResult = true;
        break;
      }
    }
    
    return bResult;
	}
  
  public static List<String> getCovPlaceTypes() {
    List<String> lResult = new ArrayList<String>();
    
    for (String strType : aCovPlaceTypes) {
      lResult.add(strType);
    }
    
    return lResult;
  }
	
	public static String getSWF( String strCollection ) {
		return hCollSWF.get(strCollection.toLowerCase());
	}
	
	public static String getWebBase( String strCollection ) {
	  String strResult = getWebBase();
	  strResult += "/" + hCollWebBase.get(strCollection.toLowerCase());
		return strResult;
	}
	
	public static String getZoomBase( String strCollection ) {
		if( strCollection.equalsIgnoreCase("prange")) {
			return "http://fedora.umd.edu/images/Zoom/pcb";
		} else {
			return "http://fedora.umd.edu/content/zoom";
		}
	}
  
  public static String getZoomFileBase( String strCollection ) {
    if( strCollection.equalsIgnoreCase("prange")) {
      return "/fedora/images/Zoom/pcb";
    } else {
      return "/fedora/live/content/zoom";
    }
  }
  
  public static String getZoomExecDir() {
    String strZoomExecDir = "/fedora/live/lib/python/ZoomifyImage";
    return strZoomExecDir;
  }
	
	public static String getWebBase() {
	  return "http://fedora.umd.edu/images";
	}
	
	public static String getFileBase() {
	  return "/fedora/images";
	}
  
  public static String getCollectionBase(String strCollection) {
    String strBasePath = "";

    if( strCollection != null && isCollection(strCollection) ) {
      strBasePath = getFileBase() + "/" + 
        hCollImageBase.get(strCollection.toLowerCase());
    }
    return strBasePath;
  }
	
	public static List<String> getPrangeBases() {
		
		ArrayList<String> lReturn = new ArrayList<String>();
		
		for(String strBase: aPrangeBases) {
		   lReturn.add(strBase);
		}
		
		return lReturn;
	}
	
	public static boolean isDOtype( String strTestType ) {
	  boolean bResult = false;
	  
	  if( strTestType != null && strTestType.length() > 0 ) {
	    for (String strDOtype : aDOtypes) {
        if( strDOtype.equals(strTestType)) {
          bResult = true;
        }
      }
	  }
	  
	  return bResult;
	}
  
  public static List<String> getDOtypes() {
    
    ArrayList<String> lReturn = new ArrayList<String>();
    
    for(String strDOtype: aDOtypes) {
       lReturn.add(strDOtype);
    }
    
    return lReturn;
  }
	
}
