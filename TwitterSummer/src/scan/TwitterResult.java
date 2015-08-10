package scan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class TwitterResult implements Serializable {
	private static final long serialVersionUID = 1L;
	
	String handle;
	int score,
		nameScore,
		cityScore,
		addressScore,
		specialtyScore,
		titleScore;
	
	public TwitterResult(String h) {
		this.handle = h;
	}
	
	public boolean equals(Object o) {
		return (o instanceof TwitterResult) && (((TwitterResult) o).getHandle()).equals(this.getHandle());
	}
	
	public int hashCode() {
		return handle.hashCode();
	}
	
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getNameScore() {
		return nameScore;
	}
	public void setNameScore(int nameScore) {
		this.nameScore = nameScore;
	}
	public int getCityScore() {
		return cityScore;
	}
	public void setCityScore(int cityScore) {
		this.cityScore = cityScore;
	}
	public int getAddressScore() {
		return addressScore;
	}
	public void setAddressScore(int addressScore) {
		this.addressScore = addressScore;
	}
	public int getSpecialtyScore() {
		return specialtyScore;
	}
	public void setSpecialtyScore(int specialtyScore) {
		this.specialtyScore = specialtyScore;
	}
	public int getTitleScore() {
		return titleScore;
	}
	public void setTitleScore(int titleScore) {
		this.titleScore = titleScore;
	}
	public String getHandle() {
		return handle;
	}
	
	final static String FILE_NAME = "example.ser";
	
	public static void serializeResults(Map<Integer, HashSet<TwitterResult>> o) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(FILE_NAME);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) fos.close();
				if (oos != null) oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
	
	public static Map<Integer, HashSet<TwitterResult>> deserializeResults() {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(FILE_NAME);
			ois = new ObjectInputStream(fis);
			
			return (Map<Integer, HashSet<TwitterResult>>) ois.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) fis.close();
				if (ois != null) ois.close();
			} catch(IOException ie) {
				ie.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static void main(String args[]) {
		// Testing saving elements to serialized file
		Map<Integer, HashSet<TwitterResult>> example = new TreeMap<Integer, HashSet<TwitterResult>>();
		
//		example.put(9, new HashSet<TwitterResult>());
//		example.put(27, new HashSet<TwitterResult>());
//		example.put(36, new HashSet<TwitterResult>());
//		
		TwitterResult botch1 = new TwitterResult("a"), botch2 = new TwitterResult("a");
//		botch1.setScore(5000);
//		botch2.setScore(99);
//		
//		example.get(9).add(botch1);
//		example.get(9).add(botch2); // Test that it's only inserting first one!
//		example.get(27).add(new TwitterResult("b"));
//		example.get(36).add(new TwitterResult("c"));
//		
//		serializeResults(example);
		
		example = deserializeResults();
		
		int array[] = {9, 27, 36};
		for (int i = 0; i < 3; i++) {
			Iterator<TwitterResult> it = example.get(array[i]).iterator();
			
			while (it.hasNext()) {
				TwitterResult tr = (TwitterResult) it.next();
				System.out.println("int: " + Integer.toString(i) + 
						", handle: " + tr.getHandle() + 
						", score: " + tr.getScore());
			}
		}

		// Test that hashset successfully only checks equals() through handle and not hashcode, 
		// which might differ on different runs
		if (example.get(9).contains(botch1)) System.out.println("The hashset contains botch1's username");
		if (example.get(9l).contains(botch2)) System.out.println("The hashset contains botch2's username");
		
	}
}
