package categories;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import au.com.bytecode.opencsv.CSVReader;

public class Places {
	ArrayList<String> states;
	ArrayList<String> statesAbbr;
	
	Map<String, Set<String>> cities; // (state, corresponding cities)

	public Map<String, String> alternateCityNames; // Acceptable cities (alternate name + state abbr, normal city name + state abbr)
	Map<String, Set<String>> citiesWStNms;  // With State Names (an EXCEPTION) (city, corresponding state)
	
	public Places() {
		states = new ArrayList<String>();
		statesAbbr = new ArrayList<String>();
		
		cities = new HashMap<String, Set<String>>();
		
		citiesWStNms = new HashMap<String, Set<String>>(); // An EXCEPTION list
		alternateCityNames = new HashMap<String,String>(); // A Synonym list
		
		inputStates("list of states.txt");
		setupCityStates();
		inputCities("zip_code_database.csv");
	}
	
	// receives filename from constructor to load states and abbreviations
	void inputStates(final String filename) {
		FileInputStream ip = null;
		
		try {
			File file = new File(filename);
			
			if (file.exists()) ip = new FileInputStream(file);
			else {
				System.out.println("Error- Places.inputStates, could not open file");
				System.exit(-1);
			}
			
			char c = 0;
			
			while (ip.available() != 0) {
				String state = new String(), stateAbbr = new String();
				String line = new String();
				
				while ( (c = (char) ip.read()) != '\n') line += c;
				
				state = line.substring(0, line.length() - 5).toLowerCase();
				stateAbbr = line.substring(line.length() - 3, line.length() - 1).toLowerCase();
				
				states.add(state);
				statesAbbr.add(stateAbbr);
			}
			
			ip.close();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	void setupCityStates() {
		for (String abbr : statesAbbr) cities.put(abbr, new TreeSet<String>());
	}
	void inputCities(final String filename) {
		try {
			Reader reader = new FileReader(filename);
			
			CSVReader csvreader = new CSVReader(reader);
			
			csvreader.readNext();
			String[] line = csvreader.readNext(); // first line is ignored
			
			while (line != null) {
				cities.get( line[5].toLowerCase() ).add( line[2].toLowerCase() );
				
				// Some cities have state names, which will need to be recognized for location grading
				for (String state : states)
					if (state.equals(line[2].toLowerCase())) {
						// city, corresponding state
						if (!citiesWStNms.containsKey(line[2].toLowerCase())) citiesWStNms.put(line[2].toLowerCase(), new TreeSet<String>());
						citiesWStNms.get(line[2].toLowerCase()).add(line[5].toLowerCase());
					}
				
				// Other "Acceptable Cities" for a particular zip code
				// 	some are actually other cities in the region while others...
				// 	are simply synonyms for those cities (this is because of data from usps)
				//  Also, adding to list of cities for recognition as city.
				if (!line[3].equals("")) {
					int start = 0, end = 0;
					
					while (start < line[3].length()) {
						for (; end < line[3].length() && line[3].charAt(end) != ','; end++);
						String city = line[3].substring(start, end).toLowerCase();
						start = end + 2;
						end = start;
						
						cities.get( line[5].toLowerCase() ).add( city.toLowerCase() ); // state, city
						
						// Some cities have state names, which will need to be recognized for location grading
						if (states.contains(city.toLowerCase())) {
							// city, corresponding state
							if (!citiesWStNms.containsKey(city.toLowerCase())) citiesWStNms.put(city.toLowerCase(), new TreeSet<String>());
							citiesWStNms.get(city.toLowerCase()).add(line[5].toLowerCase());
						}
						
						// City + state abbr
						String normalName = line[2].toLowerCase() + "," + line[5].toLowerCase();
						String alternateName = city + "," + line[5].toLowerCase();
						
//						if (alternateName.contains(",pr")) System.out.println(alternateName);
						
						// Acceptable cities in the csv typically represent nearby cities or synonyms of the city. Either way,  
						// this ensures that doctors in the vicinity are still recorded.
						
						// alternate names + state abbr, normal city name + state abbr (NOTE line[2] and [5], then [3])
						if (!alternateCityNames.containsKey(alternateName))
							alternateCityNames.put(alternateName, normalName);
					}
				}
				
				line = csvreader.readNext();
			}
			
			csvreader.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch(IOException ie) {
			ie.printStackTrace();
			System.exit(-1);
		}
	}
	boolean searchPlaces(String city, String stateAbbr) {
		if (cities.containsKey(stateAbbr))
			if (cities.get(stateAbbr).contains(city)) return true;
		return false; 
	}
	
	// returns list of other city names matching or near given city
	public Set<String> alternateNames(String city, String stateAbbr) {
		Set<String> otherNames = new TreeSet<String>();
		String city_state = city + "," + stateAbbr, normal_name = "";
		
		// Can be both!
		// If city is an alternate name for another city, get the usual city name
		if (alternateCityNames.containsKey(city_state)) {
			normal_name = alternateCityNames.get(city_state);
			
			for (String s : alternateCityNames.keySet()) {
				if (alternateCityNames.get(s).equals(normal_name)) otherNames.add(s);
			}
		}
		
		// If the city is the usual city name, no need to change 
		if (alternateCityNames.containsValue(city_state)) {
			normal_name = city_state;
			
			for (String s : alternateCityNames.keySet()) {
				if (alternateCityNames.get(s).equals(normal_name)) otherNames.add(s);
			}
		}
		
		// The city is invalid or does not have an alternate name
		if (otherNames.isEmpty()) return null;

		// Include usual name if not included already
		otherNames.add(normal_name);
		
		return otherNames;
	}
	// Finds exact city match from state or returns null
	// Format of address comes from ExcelMethods file
	public String findCity(String address, String stateAbbr) {
		int lastLetter = address.length() - 4; // the last letter of the first word before state abbr. 
		String query1 = "", query2 = "", query3 = ""; // query has 1 word, 2 words, then 3 words total. longest matching wins
		int i = lastLetter;
		
		for (i = lastLetter; i - 1 >= 0 && address.charAt(i - 1) != ' '; i--);
		query1 = address.substring(i, lastLetter + 1);
		
		i--; // 1 before query1
		for (; i - 1 >= 0 && address.charAt(i - 1) != ' '; i--);
		query2 = address.substring(i, lastLetter + 1);
		
		i--;
		for (; i - 1 >= 0 && address.charAt(i - 1) != ' '; i--);
		query3 = address.substring(i, lastLetter + 1);
		
		if (searchPlaces(query3, stateAbbr)) return query3;
		if (searchPlaces(query2, stateAbbr)) return query2;
		if (searchPlaces(query1, stateAbbr)) return query1;
		
		return ""; // Make sure excelMethods has condition to ignore city parameter if string is null
	}
	public int getStateNum(String stateAbbr) {
		return statesAbbr.indexOf(stateAbbr);
	}
	public String getState(int stateNum) {
		return states.get(stateNum);
	}
	public String getStateAbbr(int stateNum) {
		return states.get(stateNum);
	}
	public String fullNameOfState(String stateAbbr) {
		int i = statesAbbr.indexOf(stateAbbr);
		if (i != -1) return states.get(i);
		else return null;
	}
	public ArrayList<String> stateList() {
		return states;
	}
	public ArrayList<String> statesAbbrList() {
		return statesAbbr;
	}
	public Map<String, Set<String>> cityList() {
		return cities;
	}
	public Map<String, Set<String>> exceptionList() {
		return citiesWStNms;
	}
	
	public static void main(String args[]) {
		Places places = new Places();
//		if (places.alternateNames("aguadilla", "pr") == null) System.out.println("is null");
		for (String s : places.alternateNames("roosevelt roads", "pr")) System.out.println(s);
		
//		for (String s : places.alternateCityNames.keySet()) 
//			if (s.contains("ceiba,pr")) 
//				System.out.println(s + " + " + places.alternateCityNames.get(s));
	}
}