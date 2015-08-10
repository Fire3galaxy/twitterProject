package categories;

import java.util.*;
import java.io.*;

public class Credentials {
	Map<String, String> credentials;
	
	public Credentials() {
		credentials = new TreeMap<String,String>();
		loadFile("Titles.txt");
	}
	void loadFile(final String filename) {
		File file = new File(filename);
		
		try {
			if (!file.exists()) {
				System.out.println("Credentials: File not found");
				System.exit(-1);
			}
			
			FileInputStream fis = new FileInputStream(file);
			char c = ' ';
			while (!Character.isAlphabetic(c = (char) fis.read()));
			
			while (fis.available() != 0) {
				String title = "", fullTitle = "";
				
				while (c != ' ') {
					title += c;
					c = (char) fis.read();
				}
				
				c = (char) fis.read(); // ' '
				while (c != '\n' && c != 13) { // 13: carriage return, acts like /n
					fullTitle += Character.toLowerCase(c);
//					System.out.print( c );
					c = (char) fis.read();
				}
//				System.out.println();
				credentials.put(title, fullTitle);
				
				while (!Character.isAlphabetic(c = (char) fis.read()) && fis.available() != 0); // Next letter or end of file
			}
			
			fis.close();
		} catch(IOException ie) {
			System.out.println("Credentials: IOException");
			System.exit(-1);
		}
	}
	public String getTitle(String title) {
		return credentials.get(title);
	}
	public void printCred() {
		for (String title : credentials.keySet()) {
			System.out.println(title + " " + credentials.get(title));
		}
//		System.out.println("AA" + " " + credentials.get("AA"));
	}
	public static void main(String[] args) {
		Credentials c = new Credentials();
		c.printCred();
	}
}