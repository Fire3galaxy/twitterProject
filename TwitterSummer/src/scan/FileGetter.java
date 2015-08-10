package scan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class FileGetter {
	// Opens file, reads all ids into arraylist and returns them
	static ArrayList<Long> getIDsFromFile(int fileNum) {
		String filename = "ids_" + Integer.toString(fileNum) + ".txt";
		File file = new File(filename);
		FileInputStream ip = null;
		
		ArrayList<Long> ids_list = new ArrayList<Long>();
		
		try {
			if (file.exists()) ip = new FileInputStream(file);
			else {
				System.out.println("Error: could not open file");
				System.exit(-1);
			}
			
			while (ip.available() != 0) {
				String id_str = "";
				char c;
				
				while ( (c = (char) ip.read()) != '\n') id_str += c;
				
				ids_list.add(Long.parseLong(id_str));
			}
		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ip.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Size of array: " + Integer.toString(ids_list.size()));
		return ids_list;
	}
}
