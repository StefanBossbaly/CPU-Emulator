package org.binghamton.comparch.register;

public class PhysicalRegister extends Register {

	private boolean valid;

	public PhysicalRegister(String name) {
		super(name);
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean newValid) {
		valid = newValid;
	}
}
