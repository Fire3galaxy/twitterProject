package categories;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import au.com.bytecode.opencsv.CSVReader;

public class Specialties {
	Map<String, Set<String>> specialtiesMap;
	Set<String> specKeywords;
	
	public Specialties() {
		specialtiesMap = new HashMap<String, Set<String>>();
		specKeywords = new TreeSet<String>();
		loadMappings("medicare specialties to other names.xlsx");
		loadKeywords("CHV_concepts_terms_flatfile_20110204.csv");
	}
	void loadMappings(final String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			Workbook workbook = new XSSFWorkbook(fis);
			
			int numberOfSheets = workbook.getNumberOfSheets();
			
			for (int i = 0; i < numberOfSheets; i++) {
				Sheet sheet = workbook.getSheetAt(i);
				Iterator<Row> it = sheet.iterator();
				
				while (it.hasNext()) {
					Row row = it.next();
					Cell specialty = row.getCell(0);
					String spec = specialty.getStringCellValue().toLowerCase();
					
					Cell synonym = row.getCell(1);
					String syn = synonym.getStringCellValue().toLowerCase();
					
					if ( !(specialtiesMap.containsKey(spec)) ) {
//						System.out.print("\n" + spec + " | ");
						
						specialtiesMap.put(spec, new TreeSet<String>());
						specialtiesMap.get(spec).add(spec);
					}
					
//					System.out.print(syn + " | ");
					
					specialtiesMap.get(spec).add(syn);
				}
			}
			
			fis.close();
		} catch (IOException p) {
			p.printStackTrace();
			System.exit(-1);
		}
	}
	void loadKeywords(final String filename) {
		try {
			Reader reader = new FileReader(filename);
			CSVReader csvReader = new CSVReader(reader);
			
			String[] line = csvReader.readNext();
			
			while (line != null) {
				if (line[7].equals("no")) {
					specKeywords.add(line[1].toLowerCase());
					specKeywords.add(line[2].toLowerCase());
					specKeywords.add(line[3].toLowerCase());
				}
				
				line = csvReader.readNext();
			}
			
			csvReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException ie) {
			ie.printStackTrace();
			System.exit(-1);
		}
	}
	public Set<String> getSynonyms(String specialty) {
		if (specialtiesMap.containsKey(specialty)) return specialtiesMap.get(specialty);
		
		return null;
	}
	public Set<String> getKeywords() {
		return specKeywords;
	}
}