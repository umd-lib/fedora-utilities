package edu.umd.lib.fedora.util.DO;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.InvalidXPathException;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class DoUtils {

  private static Map mXPath = new HashMap();
  private static Map namespace = new LIMSns().getNamespace();

  private static DocumentFactory df = DocumentFactory.getInstance();

  public static boolean saveDoc(Document docThisDocument, String strFile) {

    
    
    strFile = strFile.replaceAll("umd:", "umd_");
    
    try {

      // Set up the interminable number of structures for printing this
      // puppy
      if( strFile.equalsIgnoreCase("Terminal") ) {
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(System.out, format);
        writer.write(docThisDocument);
      } else {
        OutputStreamWriter outWriter = new OutputStreamWriter(
            new FileOutputStream(strFile), "UTF-8");
      
      
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(outWriter, format);
        writer.write(docThisDocument);
        writer.close();
      }

    }

    catch (Exception e) {
      e.printStackTrace();
    }

    return true;
  }

  /**
   * Many DM/AM tags are repeatable so that lists of strings are returned from
   * queries. This function concatenates them into one string with a custom
   * delimiter;
   * 
   * @param lStrings
   * @param strDelimiter
   * @return
   */
  public static String singleString(List<String> lStrings, String strDelimiter) {
    String sResult = "";

    for (String sMember : lStrings) {

      if (sResult.length() > 0) {
        sResult += strDelimiter;
      }
      sResult += sMember;
    }

    // Replace returns with spaces
    sResult = sResult.replaceAll("\n", " ");
    sResult = sResult.replaceAll("\r", " ");

    return sResult;
  }

  /**
   * Concatenates all of the strings existing inside of the Element and returns
   * them this includes Element names and attribute key/value pairs.
   * 
   * @param eParent
   * @return
   */
  @SuppressWarnings("unchecked")
  public static String elementString(Element eParent) {

    String sReturn = "";
    String sTemp = "";
    String sElementName = eParent.getName();
    List<String> lAttribStrings = new ArrayList<String>();

    Branch bElement = (Branch) eParent;
    
    List<Attribute> lAttributes = eParent.attributes();

    for (Attribute aAttribute : lAttributes) {
      lAttribStrings.add(aAttribute.getName() + "=" + aAttribute.getValue());
    }
    
    List<Node> lNodes = bElement.content();

    for (Node nNode : lNodes) {

      if (nNode.getNodeTypeName().equalsIgnoreCase("Text")
          || nNode.getNodeTypeName().equalsIgnoreCase("CDATA")) {

        sTemp += nNode.getText();

        // Replace returns with spaces
        sTemp = sTemp.replaceAll("\n", " ");
        sTemp = sTemp.replaceAll("\r", " ");

        // Trim leading and traiiling spaces
        sTemp = sTemp.trim();

      } else if (nNode.getNodeTypeName().equalsIgnoreCase("Element")) {
        sTemp += elementString((Element) nNode);
      }

      if (sTemp.length() > 0) {
        sTemp += " ";

        sReturn = sElementName;

        if (lAttribStrings.size() > 0) {
          sReturn += " (";

          for (int i = 0; i < lAttribStrings.size(); i++) {

            if (i > 0) {
              sReturn += ", ";
            }

            sReturn += lAttribStrings.get(i);

          }

          sReturn += ")";
        }

        sReturn += ": " + sTemp + " ";
      }
    }

    return sReturn;
  }

  /************************************************************* getXPath */
  /**
   * Get a compiled XPath object for the expression. Cache.
   */

  public static XPath getXPath(String strXPath) throws InvalidXPathException {

    XPath xpath = null;

    if (mXPath.containsKey(strXPath)) {
      xpath = (XPath) mXPath.get(strXPath);

    } else {
      xpath = df.createXPath(strXPath);
      xpath.setNamespaceURIs(namespace);
      mXPath.put(strXPath, xpath);
    }

    return xpath;
  }

}
