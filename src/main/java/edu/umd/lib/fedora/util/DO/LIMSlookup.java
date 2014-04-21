package edu.umd.lib.fedora.util.DO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This a collection of values and hashes associated with Fedora. This allows a
 * central repository of values that would otherwise have to be hard coded into
 * the Fedora manipulation objects. It also allows a central location for
 * modification as well.
 * 
 * @author phammer
 * 
 */
public class LIMSlookup {

  /*
   * For consistency sake, please make all paths end without a slash
   */

  private final static HashMap<String, String> hContentModels = new HashMap<String, String>();
  static {
    hContentModels.put("Image", "UMD_IMAGE");
    hContentModels.put("Book", "UMD_BOOK");
    hContentModels.put("Video", "UMD_VIDEO");
    hContentModels.put("TEI", "UMD_TEI");
    hContentModels.put("EAD", "UMD_EAD");
  }

  private final static String[] aStatuses = new String[] { "Complete",
      "Incomplete", "Pending", "Private", "Quarantined", "Deleted" };

  private final static HashMap<String, String> hCollections = new HashMap<String, String>();
  static {
    hCollections.put("album", "umd:3391"); // alb
    hCollections.put("worldsfairs", "umd:2"); // wfc
    hCollections.put("filmsum", "umd:1158"); // flm
    hCollections.put("henson", "umd:1157"); // hen
    hCollections.put("archivesum", "umd:2258"); // aum
    hCollections.put("sterling", "umd:2256"); // sfp
    hCollections.put("baroness", "umd:2257"); // efl
    hCollections.put("earlymodernwomen", "umd:10537"); // emw
    hCollections.put("prange", "umd:11575"); // pcb
    hCollections.put("ntl", "umd:15237"); // ntl
    hCollections.put("misc", "umd:3392"); // msc
    hCollections.put("irith", "umd:10538"); // irh
    hCollections.put("versioningmachine", "umd:10539"); // vmc
    hCollections.put("mdhc", "umd:16719"); // mhc
    hCollections.put("internetarchive", "umd:47085"); // iar
    hCollections.put("intransition", "umd:50580"); // trn
    hCollections.put("mdmaps", "umd:57340"); // mmc
    hCollections.put("mdcivilwar", "umd:66501"); // mcw
    hCollections.put("laborinamerica", "umd:78013"); // lia
    hCollections.put("gis", "umd:78013"); // gis
    hCollections.put("wmuc", "umd:165259"); // muc
    hCollections.put("usposters", "umd:84798"); // usp
    hCollections.put("mdnewspapers", "umd:233169"); // nsp
  }

  private final static HashMap<String, String> hCollPrefix = new HashMap<String, String>();
  static {
    hCollections.put("alb", "album");
    hCollections.put("aum", "archivesum");
    hCollections.put("efl", "baroness");
    hCollections.put("flm", "filmsum");
    hCollections.put("hen", "henson");
    hCollections.put("msc", "misc");
    hCollections.put("sfp", "sterling");
    hCollections.put("wfc", "worldsfairs");
    hCollections.put("emw", "earlymodernwomen");
    hCollections.put("irh", "irith");
    hCollections.put("vmc", "versioningmachine");
    hCollections.put("pcb", "prange");
    hCollections.put("ntl", "ntl");
    hCollections.put("mhc", "mdhc");
    hCollections.put("iar", "internetarchive");
    hCollections.put("trn", "intransition");
    hCollections.put("mmc", "mdmaps");
    hCollections.put("mcw", "mdcivilwar");
    hCollections.put("lia", "laborinamerica");
    hCollections.put("gis", "gis");
    hCollections.put("muc", "wmuc");
    hCollections.put("usp", "usposters");
    hCollections.put("nsp", "mdnewspapers");
  }

  private final static HashMap<String, String> hCollSWF = new HashMap<String, String>();
  static {
    hCollSWF.put("album", "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // alb
    hCollSWF.put("worldsfairs",
        "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // wfc
    hCollSWF.put("filmsum", "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // flm
    hCollSWF.put("henson", "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // hen
    hCollSWF.put("archivesum",
        "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // aum
    hCollSWF.put("sterling", "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // sfp
    hCollSWF.put("baroness", "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // efl
    hCollSWF.put("earlymodernwomen",
        "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // emw
    hCollSWF.put("prange", "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // pcb
    hCollSWF.put("ntl", "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // ntl
    hCollSWF.put("misc", "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // msc
    hCollSWF.put("irith", "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // irh
    hCollSWF.put("versioningmachine",
        "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // vmc
    hCollSWF.put("mdhc", "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // mhc
    hCollSWF.put("internetarchive",
        "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // iar
    hCollSWF.put("intransition",
        "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // trn
    hCollSWF.put("mdmaps", "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // mmc
    hCollSWF.put("mdcivilwar",
        "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // mcw
    hCollSWF.put("laborinamerica",
        "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // lia
    hCollSWF.put("gis", "http://fedora.lib.umd.edu/content/zoom/zoom.swf"); // gis
  }

  private final static HashMap<String, String> hCollImageBase = new HashMap<String, String>();
  static {
    hCollImageBase.put("album", "DBharmony"); // alb
    hCollImageBase.put("worldsfairs", "Worlds_Fairs"); // wfc
    hCollImageBase.put("filmsum", "UMDfilms"); // flm
    hCollImageBase.put("henson", "Henson"); // hen
    hCollImageBase.put("archivesum", "Archives"); // aum
    hCollImageBase.put("sterling", "Sterling"); // sfp
    hCollImageBase.put("baroness", "Baroness"); // efl
    hCollImageBase.put("earlymodernwomen", "EMW"); // emw
    hCollImageBase.put("prange", "Prange"); // pcb
    hCollImageBase.put("ntl", "NTL"); // ntl
    hCollImageBase.put("misc", "Misc"); // msc
    hCollImageBase.put("irith", "Irith"); // irh
    hCollImageBase.put("versioningmachine", "VMC"); // vmc
    hCollImageBase.put("mdhc", "MDHB"); // mhc
    hCollImageBase.put("internetarchive", "Iarch"); // iar
    hCollImageBase.put("intransition", "Baroness"); // trn
    hCollImageBase.put("mdmaps", "Maps"); // mmc
    hCollImageBase.put("mdcivilwar", "MCW"); // mcw
    hCollImageBase.put("laborinamerica", "Labor"); // lia
    hCollImageBase.put("gis", "GIS"); // gis
  }

  private final static HashMap<String, String> hCollWebBase = new HashMap<String, String>();
  static {
    hCollWebBase.put("album", "DBharmony"); // alb
    hCollWebBase.put("worldsfairs", "Worlds_Fairs"); // wfc
    hCollWebBase.put("filmsum", "UMDfilms"); // flm
    hCollWebBase.put("henson", "Henson"); // hen
    hCollWebBase.put("archivesum", "Archives"); // aum
    hCollWebBase.put("sterling", "Sterling"); // sfp
    hCollWebBase.put("baroness", "Baroness"); // efl
    hCollWebBase.put("earlymodernwomen", "EMW"); // emw
    hCollWebBase.put("prange", "Prange"); // pcb
    hCollWebBase.put("ntl", "NTL"); // ntl
    hCollWebBase.put("misc", "Misc"); // msc
    hCollWebBase.put("irith", "Irith"); // irh
    hCollWebBase.put("versioningmachine", "VMC"); // vmc
    hCollWebBase.put("mdhc", "MDHB"); // mhc
    hCollWebBase.put("internetarchive", "Iarch"); // iar
    hCollWebBase.put("intransition", "Baroness"); // trn
    hCollWebBase.put("mdmaps", "Maps"); // mmc
    hCollWebBase.put("mdcivilwar", "MCW"); // mcw
    hCollWebBase.put("laborinamerica", "Labor"); // lia
    hCollWebBase.put("gis", "GIS"); // gis
    hCollWebBase.put("wmuc", "wmuc"); // mcw
    hCollWebBase.put("usposters", "usposters"); // lia
    hCollWebBase.put("mdnewspapers", "newspapers"); // gis
  }

  private final static String[] aPrangeBases = new String[] {
      "/prangeasset/assetstore1/PCB001",
      "/prangeasset/assetstore1/PCB001/461",
      "/prangeasset/assetstore1/PCB001/466",
      "/prangeasset/assetstore1/PCB001/467",
      "/prangeasset/assetstore1/PCB001/468",
      "/prangeasset/assetstore1/PCB001/469",
      "/prangeasset/assetstore1/PCB001/470",
      "/prangeasset/assetstore1/PCB001/471",
      "/prangeasset/assetstore1/PCB001/472",
      "/prangeasset/assetstore1/PCB001/473",
      "/prangeasset/assetstore1/PCB001/474",
      "/prangeasset/assetstore1/PCB001/475",
      "/prangeasset/assetstore1/PCB001/476",
      "/prangeasset/assetstore1/PCB001/477",
      "/prangeasset/assetstore1/PCB001/478",
      "/prangeasset/assetstore1/PCB001/479",
      "/prangeasset/assetstore1/PCB002",
      "/prangeasset/assetstore1/PCB003",
      "/prangeasset/assetstore1/PCB004",
      "/prangeasset/assetstore1/PCB005",
      "/prangeasset/assetstore1/PCB006",
      "/prangeasset/assetstore1/PCB007",
      "/prangeasset/assetstore1/PCB008/Nichimy047",
      "/prangeasset/assetstore1/PCB008/Nichimy048",
      "/prangeasset/assetstore1/PCB008/Nichimy049",
      "/prangeasset/assetstore1/PCB008/Nichimy050",
      "/prangeasset/assetstore1/PCB009/Backup070",
      "/prangeasset/assetstore1/PCB009/Backup071",
      "/prangeasset/assetstore1/PCB009/Backup072",
      "/prangeasset/assetstore1/PCB009/Backup073",
      "/prangeasset/assetstore1/PCB010/Backup074",
      "/prangeasset/assetstore1/PCB010/Backup075",
      "/prangeasset/assetstore1/PCB011/Backup076",
      "/prangeasset/assetstore1/PCB011/Backup077",
      "/prangeasset/assetstore1/PCB011/Backup078",
      "/prangeasset/assetstore1/PCB011/Backup079",
      "/prangeasset/assetstore1/PCB011/Backup080",
      "/prangeasset/assetstore1/PCB011/Backup081",
      "/prangeasset/assetstore1/PCB011/Backup082",
      "/prangeasset/assetstore1/PCB012/Prange079",
      "/prangeasset/assetstore1/PCB012/Prange080",
      "/prangeasset/assetstore1/PCB012/Prange081",
      "/prangeasset/assetstore1/PCB013/Prange082",
      "/prangeasset/assetstore1/PCB013/Prange083",
      "/prangeasset/assetstore1/PCB014/Prange084",
      "/prangeasset/assetstore1/PCB014/Prange085",
      "/prangeasset/assetstore1/PCB015/Prange086",
      "/prangeasset/assetstore1/PCB015/Prange087",
      "/prangeasset/assetstore1/PCB015/Prange088",
      "/prangeasset/assetstore1/PCB016/Prange089",
      "/prangeasset/assetstore1/PCB016/Prange090",
      "/prangeasset/assetstore1/PCB017/Prange091",
      "/prangeasset/assetstore1/PCB017/Prange092",
      "/prangeasset/assetstore1/PCB018/Prange093",
      "/prangeasset/assetstore1/PCB018/Prange094",
      "/prangeasset/assetstore1/PCB019/Prange095",
      "/prangeasset/assetstore1/PCB019/Prange096",
      "/prangeasset/assetstore1/PCB020/Prange097",
      "/prangeasset/assetstore1/PCB020/Prange098",
      "/prangeasset/assetstore1/PCB021",
      "/prangeasset/assetstore1/PCB022/Prange099",
      "/prangeasset/assetstore1/PCB022/Prange100",
      "/prangeasset/assetstore1/PCB023/Prange101",
      "/prangeasset/assetstore1/PCB024/Prange102",
      "/prangeasset/assetstore1/PCB024/Prange103",
      "/prangeasset/assetstore1/PCB025/Prange104",
      "/prangeasset/assetstore1/PCB025/Prange105",
      "/prangeasset/assetstore1/PCB025/Prange106",
      "/prangeasset/assetstore1/PCB026",
      "/prangeasset/assetstore1/PCB027/Prange107",
      "/prangeasset/assetstore1/PCB027/Prange108",
      "/prangeasset/assetstore1a",
      "/prangeasset/assetstore1a/PCB028/Prange109",
      "/prangeasset/assetstore1a/PCB028/Prange110",
      "/prangeasset/assetstore1a/PCB028/Prange111",
      "/prangeasset/assetstore1a/PCB029/Prange112",
      "/prangeasset/assetstore1a/PCB029/Prange113",
      "/prangeasset/assetstore1a/PCB029/Prange114",
      "/prangeasset/assetstore1a/PCB029/Prange115",
      "/prangeasset/assetstore1a/PCB030/Prange116",
      "/prangeasset/assetstore1a/PCB030/Prange117",
      "/prangeasset/assetstore1a/PCB030/Prange118",
      "/prangeasset/assetstore1a/PCB030/Prange119",
      "/prangeasset/assetstore1a/PCB031/Prange120",
      "/prangeasset/assetstore1a/PCB031/Prange121",
      "/prangeasset/assetstore1a/PCB031/Prange122",
      "/prangeasset/assetstore1a/PCB032/Prange123",
      "/prangeasset/assetstore1a/PCB032/Prange124",
      "/prangeasset/assetstore1a/PCB032/Prange125",
      "/prangeasset/assetstore1a/PCB033/Prange126",
      "/prangeasset/assetstore1a/PCB033/Prange127",
      "/prangeasset/assetstore1a/PCB033/Prange128",
      "/prangeasset/assetstore1a/PCB033/Prange129",
      "/prangeasset/assetstore1a/PCB034/Prange130",
      "/prangeasset/assetstore1a/PCB034/Prange131",
      "/prangeasset/assetstore1a/PCB035/Prange132",
      "/prangeasset/assetstore1a/PCB035/Prange133",
      "/prangeasset/assetstore1a/PCB036/Prange134",
      "/prangeasset/assetstore1a/PCB036/Prange135",
      "/prangeasset/assetstore1a/PCB036/Prange136",
      "/prangeasset/assetstore1a/PCB037/Prange137",
      "/prangeasset/assetstore1a/PCB037/Prange139",
      "/prangeasset/assetstore1a/PCB037/Prange140",
      "/prangeasset/assetstore1a/PCB037/Prange141",
      "/prangeasset/assetstore2/PCB038/Prange142",
      "/prangeasset/assetstore2/PCB038/Prange143",
      "/prangeasset/assetstore2/PCB038/Prange144",
      "/prangeasset/assetstore2/PCB038/Prange145/00_Manga",
      "/prangeasset/assetstore2/PCB038/Prange145/01_others",
      "/prangeasset/assetstore2/PCB038/Prange145/02_Unpublished_items",
      "/prangeasset/assetstore2/PCB038/Prange145/03_Additional_items",
      "/prangeasset/assetstore2/PCB038/Prange146",
      "/prangeasset/assetstore2/PCB039/Additional",
      "/prangeasset/assetstore2/PCB039/Missing TIFFs",
      "/prangeasset/assetstore1a/Prange075b-childrens",
      "/prangeasset/assetstore2/GeneralBooks/Prange060-general/00_General Books",
      "/prangeasset/assetstore2/GeneralBooks/Prange060-general/01_Children's Books for the addition",
      "/prangeasset/assetstore2/GeneralBooks/Prange061-general",
      "/prangeasset/assetstore1/rescannedimages",
      "/prangeasset/assetstore1/rescannedimages2",
      "/prangeasset/assetstore1/rescannedimages2/469-199a_disk136" };

  private final static String[] aCovPlaceTypes = new String[] { "continent",
      "zone", "bloc", "country", "region", "settlement", "district" };

  private final static String[] aDOtypes = new String[] { "doInfo", "amInfo" };

  public static String getContentModel(String strContentWord) {
    String strResult = hContentModels.get(strContentWord);
    return strResult;
  }

  public static boolean isContentModelKey(String strContentWord) {
    String strResult = hContentModels.get(strContentWord);
    if ((strResult != null) && (strResult.length() > 0)) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isContentModelValue(String strContentWord) {
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

  public static boolean isStatus(String strTest) {
    boolean bResult = false;

    for (int i = 0; i < aStatuses.length; i++) {
      if (strTest.equals(aStatuses[i])) {
        bResult = true;
        break;
      }
    }

    return bResult;
  }

  public static boolean isCollection(String strTest) {
    if (strTest != null && hCollections.containsKey(strTest.toLowerCase())) {
      return true;
    } else {
      if (strTest.startsWith("umd")) {
        if (hCollections.containsValue(strTest)) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    }
  }

  public static String getCollectionPid(String strCollectionName) {
    String strResult = hCollections.get(strCollectionName.toLowerCase());
    if (strCollectionName.startsWith("umd")) {
      if (hCollections.containsValue(strCollectionName)) {
        return strCollectionName;
      }
    }
    return strResult;
  }

  public static String getCollectionName(String strCollectionPid) {

    Set<String> sKeys = hCollections.keySet();
    String strResult = "";
    String strTemp;

    for (String strKey : sKeys) {

      strTemp = hCollections.get(strKey);

      if (strTemp.equals(strCollectionPid)) {
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

  public static boolean isCovPlaceType(String strTest) {
    boolean bResult = false;

    for (int i = 0; i < aCovPlaceTypes.length; i++) {
      if (strTest.equals(aCovPlaceTypes[i])) {
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

  public static String getSWF(String strCollection) {
    return hCollSWF.get(strCollection.toLowerCase());
  }

  public static String getWebBase(String strCollection) {
    String strResult = getWebBase();
    strResult += "/" + hCollWebBase.get(strCollection.toLowerCase());
    return strResult;
  }

  public static String getZoomBase(String strCollection) {
    if (strCollection.equalsIgnoreCase("prange")) {
      return "http://fedora.lib.umd.edu/images/Zoom/pcb";
    } else {
      return "http://fedora.lib.umd.edu/content/zoom";
    }
  }

  public static String getZoomFileBase(String strCollection) {
    if (strCollection.equalsIgnoreCase("prange")) {
      return "/apps/fedora/images/Zoom/pcb";
    } else {
      return "/apps/fedora/content/zoom";
    }
  }

  public static String getZoomExecDir() {
    String strZoomExecDir = "/apps/fedora/lib/python/ZoomifyImage";
    return strZoomExecDir;
  }

  public static String getWebBase() {
    return "http://fedora.lib.umd.edu/images";
  }

  public static String getFileBase() {
    return "/apps/fedora/images";
  }

  public static String getCollectionBase(String strCollection) {
    String strBasePath = "";

    if (strCollection != null && isCollection(strCollection)) {
      strBasePath = getFileBase() + "/"
          + hCollImageBase.get(strCollection.toLowerCase());
    }
    return strBasePath;
  }

  public static List<String> getPrangeBases() {

    ArrayList<String> lReturn = new ArrayList<String>();

    for (String strBase : aPrangeBases) {
      lReturn.add(strBase);
    }

    return lReturn;
  }

  public static boolean isDOtype(String strTestType) {
    boolean bResult = false;

    if (strTestType != null && strTestType.length() > 0) {
      for (String strDOtype : aDOtypes) {
        if (strDOtype.equals(strTestType)) {
          bResult = true;
        }
      }
    }

    return bResult;
  }

  public static List<String> getDOtypes() {

    ArrayList<String> lReturn = new ArrayList<String>();

    for (String strDOtype : aDOtypes) {
      lReturn.add(strDOtype);
    }

    return lReturn;
  }

}
