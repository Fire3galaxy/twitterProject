package excel;

import java.io.Serializable;

// IntHolder allows ints to be passed by ref!
public class IntHolder implements Serializable {
	/**
	 * Safety in deserializing (used in excelmethods)
	 */
	private static final long serialVersionUID = 1L;
	private int n;
	public IntHolder(int i) {
		n = i; 
	}
	public void addNum(int i) {
		n += i;
	}
	public void increment() {
		n++;
	}
	public int getNum() {
		return n;
	}
}