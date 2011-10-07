package src;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


public class Testeroo {

	private String strPrintType = "Terminal";

	/**************************************************************** PrintDoc */
	/**
	 * Prints a document based on the class's printing directive string
	 */
	private boolean printDoc(Document docThisDocument, String strFile) {

		boolean bTerminalOut = false;
		boolean bFileOut = false;

		if (strPrintType.equals("Terminal")) {
			bTerminalOut = true;
		}

		if (strPrintType.equals("File") ) {
			bFileOut = true;
		}

		if (strPrintType.equals("Both")) {
			bTerminalOut = true;
			bFileOut = true;
		}

		strFile = strFile.replaceAll("umd:", "umd_");

		try {

			if (bFileOut) {
				// Create the string of the filename to be written to
				String strFileName = strFile;

				// Set up the interminable number of structures for printing this puppy
				OutputStreamWriter outWriter = new OutputStreamWriter(
						new FileOutputStream(strFileName), "UTF-8");
				OutputFormat format = OutputFormat.createPrettyPrint();
				XMLWriter writer = new XMLWriter(outWriter, format);
				writer.write(docThisDocument);
				writer.close();
			}

			if (bTerminalOut) {
				// Set up the interminable number of structures for printing this puppy
				//System.out.println("File Name is " + strFile);
				OutputFormat format = OutputFormat.createPrettyPrint();
				XMLWriter writer = new XMLWriter(System.out, format);
				writer.write(docThisDocument);
			}

		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
	
	public static BufferedReader webRead(String url) throws Exception {
		return new BufferedReader(new InputStreamReader(new URL(url)
				.openStream()));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String sPattern = null;
		
		try{
			
//			Testeroo thisTest = new Testeroo();
//			
//			SAXReader reader = new SAXReader();
//			
//			URL testPage = new URL("http://streamerdev.lib.umd.edu:8080/urlprocessor/URLProcessor?url=rtsp%3A%2F%2Fstreamerdev.lib.umd.edu%2Frealvideo10.rm&tokenname=token1&life=0");
//			Document doc = reader.read(testPage);
//			
//			thisTest.printDoc(doc, "Testeroo.txt");
			
			BufferedReader reader = webRead("http://streamerdev.lib.umd.edu:8080/urlprocessor/URLProcessor?url=rtsp%3A%2F%2Fstreamerdev.lib.umd.edu%2Frealvideo10.rm&tokenname=token1&life=0");
			String line = reader.readLine();
			String sLocation = "rtsp://streamerdev.lib.umd.edu/";
			String sTokenName = "token1";
			sPattern = sLocation + ".+tokenname=" + sTokenName + ".+";
			System.out.println(sPattern);
			
			while (line != null) {
				System.out.println(line);
				
				if( line.matches(sPattern) ) {
					System.out.println("Matched");
				} else {
					System.out.println("No Match");
				}
				
				line = reader.readLine();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
