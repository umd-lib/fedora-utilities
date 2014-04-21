package edu.umd.lib.fedora.util.foxml;

import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;

import edu.umd.lib.fedora.util.DO.DoUtils;

public class ImageXML {

  // See parseElement() for examples

  private String dGroup = null;
  private String dID = null;
  private String sIDsuffix = ".0";
  private String dState = null;
  private String dVersionable = "false";

  private String vLabel = null;
  private String vLocationRef = null;
  private String vLocationType = null;
  private String vMimeType = null;

  public ImageXML() {

    dState = "A";
    dVersionable = "false";

  }

  public ImageXML(Element eDataStream) {

    // Parse the element and extract the information
    // This is assumed to be the Datastream element.
    // The datastream version element will come later hopefully
    // Pull the max datastreamversion from the datastream by default

    parseDatastream(eDataStream);

  }

  /**
   * @return the dGroup
   */
  public String getdGroup() {
    return dGroup;
  }

  /**
   * @param dGroup
   *          the dGroup to set
   */
  public void setdGroup(String dGroup) {
    if ((dGroup != null) && (dGroup.length() > 0)) {
      this.dGroup = dGroup;
    }
  }

  /**
   * @return the dID
   */
  public String getdID() {
    return dID;
  }

  /**
   * @param dID
   *          the dID to set
   */
  public void setdID(String dID) {
    if ((dID != null) && (dID.length() > 0)) {
      this.dID = dID;
    }
  }

  /**
   * @return the dState
   */
  public String getdState() {
    return dState;
  }

  /**
   * @param dState
   *          the dState to set
   */
  public void setdState(String dState) {
    if ((dState != null) && (dState.length() > 0)) {
      this.dState = dState;
    }
  }

  /**
   * @return the dVersionable
   */
  public String getdVersionable() {
    return dVersionable;
  }

  /**
   * @param dVersionable
   *          the dVersionable to set
   */
  public void setdVersionable(String dVersionable) {
    if ((dVersionable != null) && (dVersionable.length() > 0)) {
      this.dVersionable = dVersionable;
    }
  }

  /**
   * @return the vLabel
   */
  public String getvLabel() {
    return vLabel;
  }

  /**
   * @param vLabel
   *          the vLabel to set
   */
  public void setvLabel(String vLabel) {
    if ((vLabel != null) && (vLabel.length() > 0)) {
      this.vLabel = vLabel;
    }
  }

  /**
   * @return the vLocationRef
   */
  public String getvLocationRef() {
    return vLocationRef;
  }

  /**
   * @param vLocationRef
   *          the vLocationRef to set
   */
  public void setvLocationRef(String vLocationRef) {
    if ((vLocationRef != null) && (vLocationRef.length() > 0)) {
      this.vLocationRef = vLocationRef;
    }
  }

  /**
   * @return the vLoationType
   */
  public String getvLoationType() {
    return vLocationType;
  }

  /**
   * @param vLoationType
   *          the vLoationType to set
   */
  public void setvLoationType(String vLoationType) {
    if ((vLoationType != null) && (vLoationType.length() > 0)) {
      this.vLocationType = vLoationType;
    }
  }

  public boolean isOK() {
    boolean bResult = false;

    if ((dGroup != null && dGroup.length() > 0) &&
        (dID != null && dID.length() > 0) &&
        (dState != null && dState.length() > 0) &&
        (vLabel != null && vLabel.length() > 0) &&
        (vMimeType != null && vMimeType.length() > 0) &&
        (vLocationRef != null && vLocationRef.length() > 0) &&
        (vLocationType != null && vLocationType.length() > 0)) {
      bResult = true;
    }

    return bResult;
  }

  public Element getXML() {

    Element eImage = null;

    if (isOK()) {

      // Build the xml and return it as an element
      Namespace nFoxml = Namespace.get("foxml",
          "info:fedora/fedora-system:def/foxml#");
      QName qFoxml = DocumentHelper.createQName("datastream", nFoxml);

      eImage = DocumentHelper.createElement(qFoxml);
      Element eVersion;
      Element eSubElement;

      eImage.addAttribute("CONTROL_GROUP", dGroup);
      eImage.addAttribute("ID", dID);
      eImage.addAttribute("STATE", dState);
      eImage.addAttribute("VERSIONABLE", dVersionable);

      qFoxml = DocumentHelper.createQName("datastreamVersion", nFoxml);
      eVersion = eImage.addElement(qFoxml);

      eVersion.addAttribute("ID", (dID + sIDsuffix));
      eVersion.addAttribute("LABEL", vLabel);
      eVersion.addAttribute("MIMETYPE", vMimeType);

      qFoxml = DocumentHelper.createQName("contentDigest", nFoxml);
      eSubElement = eVersion.addElement(qFoxml);

      eSubElement.addAttribute("DIGEST", "none");
      eSubElement.addAttribute("TYPE", "DISABLED");

      qFoxml = DocumentHelper.createQName("contentLocation", nFoxml);
      eSubElement = eVersion.addElement(qFoxml);

      eSubElement.addAttribute("REF", vLocationRef);
      eSubElement.addAttribute("TYPE", vLocationType);
    }
    return eImage;
  }

  @SuppressWarnings("unchecked")
  private void parseDatastream(Element eImage) {

    /*
     * Examples <foxml:datastream CONTROL_GROUP="E" ID="thumbnail-250" STATE="A"
     * VERSIONABLE="false"> <foxml:datastreamVersion
     * CREATED="2008-03-12T20:11:17.801Z" ID="thumbnail-250.0"
     * LABEL="Thumbnail 250x250" MIMETYPE="image/jpeg" SIZE="0">
     * <foxml:contentDigest DIGEST="none" TYPE="DISABLED"/>
     * <foxml:contentLocation REF=
     * "http://fedora.umd.edu/images/Prange/517-018/X0250/pcb-517-018_-000-fc000-X0250.jpg"
     * TYPE="URL"/> </foxml:datastreamVersion> </foxml:datastream>
     * 
     * dGroup=E dID=thumbnail-250 dState=A dVersionable=false vLabel=Thumbnail
     * 250x250 vMimeType=image/jpeg
     * vLocationRef=http://fedora.umd.edu/images/Prange/517-018/X0250/pcb
     * -517-018_-000-fc000-X0250.jpg vLocationType=URL
     * 
     * <foxml:datastream CONTROL_GROUP="M" ID="thumbnail-250" STATE="A"
     * VERSIONABLE="false"> <foxml:datastreamVersion
     * CREATED="2012-03-12T17:19:43.710Z" ID="thumbnail-250.0"
     * LABEL="Thumbnail 250" MIMETYPE="image/jpeg" SIZE="0">
     * <foxml:contentDigest DIGEST="none" TYPE="DISABLED"/>
     * <foxml:contentLocation REF="umd:97561+thumbnail-250+thumbnail-250.0"
     * TYPE="INTERNAL_ID"/> </foxml:datastreamVersion> </foxml:datastream>
     * 
     * dGroup=M dID=thumbnail-250 dState=A dVersionable=false vLabel=Thumbnail
     * vMimeType=image/jpeg
     * 250 vLocationRef=umd:97561+thumbnail-250+thumbnail-250.0
     * vLocationType=INTERNAL_ID
     */

    if (eImage != null) {
      Element eTemp;
      Node nTemp;
      List<Node> lNodes = null;
      dGroup = eImage.attributeValue("CONTROL_GROUP");
      dID = eImage.attributeValue("ID");
      dState = eImage.attributeValue("STATE");
      dVersionable = eImage.attributeValue("VERSIONABLE");
      lNodes = DoUtils.getXPath("/foxml:datastream/foxml:datastreamVersion")
          .selectNodes(eImage);
      if (lNodes != null && lNodes.size() > 0) {
        for (Node nVersion : lNodes) {
          eTemp = (Element) nVersion;
          vLabel = eTemp.attributeValue("LABEL");
          vMimeType = eTemp.attributeValue("MIMETYPE");
          nTemp = DoUtils.getXPath(
              "/foxml:datastreamVersion/foxml:contentLocation")
              .selectSingleNode(eTemp);
          eTemp = (Element) nTemp;
          if (eTemp != null) {
            vLocationRef = eTemp.attributeValue("REF");
            vLocationType = eTemp.attributeValue("TYPE");
          }
        }
      }

    }
  }

}
