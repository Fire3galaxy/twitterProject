package categories;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Names {
	Map<String, Set<String>> similarNames;
	
	public Names() {
		similarNames = new HashMap<String, Set<String>>();
		
		loadNames("Appendix English given names - Wiktionary.txt");
		loadSimilarNames();
	}
	void loadNames(final String filename) {
		FileInputStream ip = null;
		try {
			File file = new File(filename);
			
			if (file.exists()) ip = new FileInputStream(file);
			else {
				System.out.println("Error: Names Loading Failed");
				System.exit(-1);
			}
			
			ip.skip(3); // because of html issue with extra chars at beginning
			
			while (ip.available() != 0) {
				String name = "", similarName = "";
				
				char c = (char) ip.read();
				
				// first name
				while (Character.isAlphabetic(c)) {
					name += c;
					c = (char) ip.read();
				}
//				System.out.print(name + " | ");
				
				similarNames.put(name.toLowerCase(), new TreeSet<String>());
				
				// space between names & nicknames
				while ( !Character.isAlphabetic(c) ) c = (char) ip.read();
				
				while ( c != '\n' ) {
					while (Character.isAlphabetic(c)) {
						similarName += c;
						c = (char) ip.read();
					}
//					System.out.print(similarName + " | ");
					
					similarNames.get(name.toLowerCase()).add(similarName.toLowerCase());
					similarName = "";
					
					if ( c != '\n' ) 
						while ( !Character.isAlphabetic(c) && c != '\n') c = (char) ip.read();
				}
			}
			
			ip.close();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	void loadSimilarNames() {
		// mapping nicknames to other names in case person's name is one of the nicknames
		Set<String> similarNamesSet = new TreeSet<String>(similarNames.keySet());
		for (String name : similarNamesSet) {
			for (String s : similarNames.get(name)) {
				if (!similarNames.containsKey(s)) similarNames.put(s, new TreeSet<String>());
				
				similarNames.get(s).add(name); // adding main name
				for (String nickname : similarNames.get(name)) // adding other names in set
					if (!nickname.equals(s)) similarNames.get(s).add(nickname);
			}
		}
	}
	// returns the set of nicknames for filterResults unless it isn't there
	public Set<String> getNames(final String firstName) {
		if (similarNames.containsKey(firstName)) return similarNames.get(firstName);
		
		return null;
	}
}