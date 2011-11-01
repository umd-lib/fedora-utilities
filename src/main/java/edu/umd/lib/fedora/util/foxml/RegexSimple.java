package edu.umd.lib.fedora.util.foxml;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexSimple {

	public static List<String> getRegexSimple( String sSource, String sPattern ) {
		
		List<String> lResult = new ArrayList<String>();
		Pattern csvRE;
		csvRE = Pattern.compile(sPattern);

		Matcher m = csvRE.matcher(sSource);

		if( m.matches() ) {
			m.start();
			
		}
		
		
		return lResult;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
