package org.binghamton.comparch.systems;

/**
 * This class represents a 4 byte register. The register is identified by it's
 * name. The name must be unique from other register objects otherwise the
 * equals() function will consider them the same register,
 * 
 * @author Stefan Bossbaly
 * @author Gerald Brennan
 *
 */
public class Register {
	private String name;
	private int value;

	/**
	 * Contrast a register
	 * 
	 * @param name
	 *            the unique name of the register
	 */
	public Register(String name) {
		this.name = name;
		this.value = 0;
	}

	/**
	 * Returns the string representation of the register name
	 * 
	 * @return the register name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the current value of the register
	 * 
	 * @return the current value of the register
	 */
	public int getValue() {
		return this.value;
	}

	/**
	 * Sets the value of the register
	 * 
	 * @param newValue
	 *            the new value of the register
	 */
	public void setValue(int newValue) {
		this.value = newValue;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Register) {
			Register otherReg = (Register) other;
			return otherReg.getName().equals(this.getName());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return String.format("%3s: %d", this.name, this.value);
	}
}
