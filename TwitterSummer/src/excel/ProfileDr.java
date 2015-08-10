/* ProfileDr is an object that holds the information of individual doctors from the 
 * xlsx file. These are put into an array to be searched for on Twitter later.
 */

package excel;

import java.io.Serializable;
import java.util.List;

public class ProfileDr implements Serializable {
	/**
	 * Deserializing safetiness.
	 */
	private static final long serialVersionUID = 1L;
	private String firstName, middleName, lastName, title, city;
	private List<String> specialty;
	private int stateNum;
	private String handle;
	private int grade;
	private int id;

	public ProfileDr(String fn, String mn, String ln, String t, List<String> s, String c, int i) {
//		this.handle = h;
		this.firstName = fn;
		this.lastName = ln;
		this.middleName = mn;
		this.title = t;
		this.specialty = s;
		this.city = c;
		this.stateNum = i;
		
		this.handle = null; // Left null unless good twitter account is found.
		this.grade = -1;
	}
	
//	public String getHandle() {
//		return handle;
//	}
	public String getFirstName() {
		return firstName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public String getLastName() {
		return lastName;
	}
//	public String getDescription() {
//		return description;
//	}
	public String getTitle() {
		return title;
	}
	public List<String> getSpecialty() {
		return specialty;
	}
	public String getCity() {
		return city;
	}
	public int getStateNum() {
		return stateNum;
	}
	public String getHandle() {
		return handle;
	}
	public int getGrade() {
		return grade;
	}
	public void setHandle(String h, int g) {
		if (h == handle) {
			grade += 10; // Maybe a good idea?
		} else if (g > grade) {
			handle = h;
			grade = g;
		}
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}