package search;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ExclRetr {
	// Gets twitter handles from txt file and writes to other txt file
	public void getTwitterHandles() {
		final String filename = "twitterHandleUS.txt";
		ArrayList<String> twitterHandles = new ArrayList<String>();
		
		FileInputStream ip = null;
		try {
			File file = new File(filename);
			
			if (file.exists()) ip = new FileInputStream(file);
			else {
				System.out.println("Error: could not open file");
				System.exit(-1);
			}
			
			char c = 0;
			while (ip.available() != 0) {
				String line = new String();
				
				while ( (c = (char) ip.read()) != '\n') line += c;
				
				if (!line.isEmpty()) 
					twitterHandles.add(line.substring(1).trim());
			}
		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			try {
				ip.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Write to file
		BufferedWriter writer = null;
        try {
            File logFile = new File("handles1.txt");

            // This will output the full path where the file will be written to...
            System.out.println(logFile.getCanonicalPath());

            writer = new BufferedWriter(new FileWriter(logFile));
            for (String s: twitterHandles) 
            	writer.write(s + '\n');
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
	}
	public static void main(String args[]) {
		ExclRetr e = new ExclRetr();
		e.getTwitterHandles();
	}
}
