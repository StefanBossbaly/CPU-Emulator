package org.binghamton.comparch.register;

public class ArchitecturalRegister extends Register {
	public ArchitecturalRegister(String name) {
		super(name);
	}

	@Override
	public boolean isValid() {
		return true;
	}
}
