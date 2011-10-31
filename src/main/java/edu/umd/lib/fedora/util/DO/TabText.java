package edu.umd.lib.fedora.util.DO;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class TabText 
	implements Iterable<HashMap<String, String>>, Iterator<HashMap<String, String>> {

	HashMap<String, String> nextRecord;
	ArrayList<String> theseKeys;
	
	public static final String CSV_PATTERN = "\\G(?:^[^\"]*?|\t)(?:\"((?:[^\"]|\"\")*+)\"|([^\"\t]*))";
	
	private static Pattern csvRegex;
	
	private BufferedReader in;
	
	private int recordCounter = 0;
	private int iFieldCounter = 0;
	
	public TabText(String fileName) {
		csvRegex = Pattern.compile(CSV_PATTERN);
		String line;
		List<String> lineRecord = new ArrayList<String>();
		nextRecord = new HashMap<String,String>();
		
		theseKeys = new ArrayList<String>();
		
		try{
			
			in = new BufferedReader(new InputStreamReader(new FileInputStream( fileName ), "UTF-8"));
			
			if( (line = in.readLine() ) != null ) {
				lineRecord = parse(line);
				iFieldCounter = lineRecord.size();
				for (int i = 0; i < iFieldCounter; i++) {
					
					//Add the current entry to the list of Keys
					theseKeys.add(i, lineRecord.get(i));
						
				}
			}
			
			if( (line = in.readLine() ) != null ) {
				lineRecord = parse(line);
				nextRecord.clear();
				
				for (int i = 0; i < iFieldCounter; i++) {
					
					//Stuff the Hashmap:hRecord with the key value pair
					nextRecord.put(theseKeys.get(i), lineRecord.get(i));
					
				}
			}
			
			recordCounter = 1;
			
		}	
		catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public boolean containsKey(String tryKey ) {
		return nextRecord.containsKey(tryKey);
	}
	
	public boolean hasNext() {
		return ! nextRecord.isEmpty();
	}
	
	public HashMap<String,String> next() {
		if (hasNext()) {
			
			String line;
			String thisValue;
			List<String> lineRecord = new ArrayList<String>();
			HashMap<String, String> thisRecord = new HashMap<String, String>();
			// Make a copy of the stored record for the client
			for (String thisKey : nextRecord.keySet()) {
				thisValue = nextRecord.get(thisKey);
				thisRecord.put(thisKey, thisValue);
			}
			// Get the next record and store it.
			try {

				nextRecord.clear();

				if ((line = in.readLine()) != null) {
					lineRecord = parse(line);

					for (int i = 0; i < iFieldCounter; i++) {

						//Stuff the Hashmap:nextRecord with the key value pair
						nextRecord.put(theseKeys.get(i), lineRecord.get(i));

					}
					recordCounter++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return thisRecord;
			
		} else {
			
			return null;
			
		}
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	/* -------------------------------------------------------------- parse */
	/** Parse one line.
	 * @return List of Strings, minus their double quotes
	 * 
	 */
	private List<String> parse(String line) {
		List<String> itemList = new ArrayList<String>();
		Matcher mainMatcher = csvRegex.matcher(line);
		Matcher quoteMatcher = Pattern.compile("\"\"").matcher("");
		
		/*
		 * The Regex above: \\G(?:^|\t)(?:\"((?:[^\"]*+|\"\")*+)\"|([^\"\t]*))
		 * has only 2 groups: one for quoted and one for unquoted fields.
		 * Group 1: Quoted Fields (with and without embedded double quotes)
		 * Group 2: Unquoted fields (numeric and null fields).
		 * If the Regex is changed, the clauses below will need to be altered
		 */
		
		// For each field found in the CSV file:
		while (mainMatcher.find()) {
			String match;
			
			if( mainMatcher.start(2) >= 0 )
				
				//It is unquoted
				match = mainMatcher.group(2);
			else
				
				//It is quoted and may contain double quotes 
				//that must be changed to single quotes
				match = quoteMatcher.reset(mainMatcher.group(1)).replaceAll("\"");
			
			//Stuff the string into the list (now that it has been purtified).
			itemList.add(match);
			
		}
		
		//Send the list back to the user.
		return itemList;
	}

	public Iterator<HashMap<String, String>> iterator() {
		return this;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
