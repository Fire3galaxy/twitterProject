/* ReadFile is responsible for reading the information from the xlsx file of doctors and
 * storing the information into a list of ProfileDr objects so that the program can search
 * for similar profiles on twitter.
 */
package excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import categories.Places;

public class ExcelMethods {
	// The doctor data
	public List<ProfileDr> file; // Array of Doctors from excel file
	Places places;
	
	// Extra data/mappings about doctors
	Map<String, IntHolder> freqOfName; // how common (frequent) the name is
	Map<String, LinkedList<ProfileDr>> byFirstName; // find doctor by first name, matching profiles
	Map<String, LinkedList<ProfileDr>> byLastName;  // find doctor by last name, matching profiles
	
	public ExcelMethods(String filename) {
		file = new ArrayList<ProfileDr>();
		places = new Places();
		
		freqOfName = new TreeMap<String, IntHolder>();
		byFirstName = new TreeMap<String, LinkedList<ProfileDr>>();
		byLastName = new TreeMap<String, LinkedList<ProfileDr>>();
		
		readFile(filename);
	}
	
	public Map<String, LinkedList<ProfileDr>> getByFirstName() {
		return byFirstName;
	}
	public Map<String, LinkedList<ProfileDr>> getByLastName() {
		return byLastName;
	}
	public List<ProfileDr> getFile() {
		return file;
	}
	public Map<String, IntHolder> getFreqOfName() {
		return freqOfName;
	}
	public void readFile(String filename) {
		// I assume the xlsx file follows the structure set in "doctors.xlsx"
		// with first and last name in the 2nd and 4th column.
		// this could be changed to look for "First Name" and "Last Name"
		// in the first row then use a for loop to always look in those columns...
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filename);
			Workbook workbook = new XSSFWorkbook(fis);
			
			int id = 0;
			int numberOfSheets = workbook.getNumberOfSheets();
			
			for (int i = 0; i < numberOfSheets; i++) {
				Sheet sheet = workbook.getSheetAt(i);
				
				Iterator<Row> rowIterator = sheet.iterator();
				rowIterator.next(); // skips first row
				
				while (rowIterator.hasNext()) {
					// FMU (For my understanding) .next() returns the current row AND advances the iterator
					Row row = rowIterator.next();
					
					String firstName = "", 
							middleName = "", 
							lastName = "",
							title = "",
							city = "",
							location = "";
					List<String> specialty = new ArrayList<String>();
					int stateNum = -1;
					
					Cell cell = row.getCell(1, Row.RETURN_BLANK_AS_NULL); // first name
					firstName = cell.getStringCellValue().trim().toLowerCase();
					
					cell = row.getCell(2, Row.RETURN_BLANK_AS_NULL); // middle name
					if (cell != null) 
						middleName = cell.getStringCellValue().trim().toLowerCase();
					
					cell = row.getCell(3, Row.RETURN_BLANK_AS_NULL); // last name
					lastName = cell.getStringCellValue().toLowerCase().trim();
					
					cell = row.getCell(5, Row.RETURN_BLANK_AS_NULL); // title
					if (cell != null) title = cell.getStringCellValue().trim();
					
					cell = row.getCell(8, Row.RETURN_BLANK_AS_NULL); // specialty (possibly multiple)
					String s = cell.getStringCellValue().toLowerCase().trim();
					for (String sp : splitUp(s)) 
						specialty.add(sp);
					
//					int start = 0, end = 0;
//					for (end = 0; end < s.length(); end++) {
//						if (s.charAt(end) == ';' || s.charAt(end) == ' ' ) {
//							specialty.add(s.substring(start, end)); // first specialty
//							start = end + 1; 						// first letter of next word
//						}
//					}
//					specialty.add(s.substring(start, end)); 		// last specialty/only specialty
					
					cell = row.getCell(9, Row.RETURN_BLANK_AS_NULL); // Address
					if (cell != null) {
						// If location IS NULL, get program to recognize that and NOT do location search
						location = cell.getStringCellValue();
						
						int index = location.length() - 1;
						
						// later addresses have numbers, so this statement ignores it
						if ( Character.isDigit(location.charAt(index)) ) { 
							while ( location.charAt(index) != ' ' )
								index--;
							index -= 2; // backtracks 2 spaces to be at the first letter of abbr.
						}
						else index--; // basic format for earlier addresses: address, city, state abbr.

						// state
						String state = location.substring(index, index + 2).toLowerCase();
						stateNum = places.getStateNum(state); // num is used as way for main to get state abbr and full name
						
						// city
						String address = location.substring(0, index + 2).toLowerCase();
						city = places.findCity(address, state);
					}
										
					ProfileDr newProfile = new ProfileDr(firstName, middleName, lastName, title, specialty, city, stateNum);
					newProfile.setId(id);
					file.add(newProfile); // add to file
					
					// Increase frequency of first name/last name combination
					if (freqOfName.containsKey(firstName + " " + lastName)) // add to map or increment count
						freqOfName.get(firstName + " " + lastName).increment();
					else freqOfName.put(firstName + " " + lastName, new IntHolder(1));
					
					// Map profileDr's to first and last name
					if (!byFirstName.containsKey(firstName)) byFirstName.put(firstName, new LinkedList<ProfileDr>());;
					byFirstName.get(firstName).add(newProfile);
					
					if (!byLastName.containsKey(lastName)) byLastName.put(lastName, new LinkedList<ProfileDr>());
					byLastName.get(lastName).add(newProfile);
					
					id++;
				} //rowIterator end
			}
		} catch (IOException p) {
			p.printStackTrace();
			System.exit(-1);
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// FIXME: Doesn't remove parens: bla bla (bla)
	static String[] splitUp(String specialties) {
		return specialties.split("[^\\w+( \\w+)*$]");
	}
	
	public static void main(String args[]) {
		ExcelMethods example = new ExcelMethods("doctors (1).xlsx");
//		System.out.println("Amount of doctors: " + Integer.toString(example.getFile().size()));
		
//		Places places = new Places();
//		for (ProfileDr dr : example.getFile()) System.out.println(dr.getCity() + " " + places.getStateAbbr(dr.getStateNum()));
		int i = 0;
		for (String s : example.getByLastName().keySet()) {
			for (ProfileDr p : example.getByLastName().get(s)) {
				System.out.println(p.getFirstName() + " " + p.getLastName());
			}
			
			i++;
			if (i >= 500) System.exit(-1);
		}
	}
}
