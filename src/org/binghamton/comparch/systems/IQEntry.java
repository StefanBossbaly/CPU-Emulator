package org.binghamton.comparch.systems;

public class IQEntry {
	private Instruction instruction;
	private int address;

	public IQEntry(Instruction instruction, int address) {
		this.instruction = instruction;
		this.address = address;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public int getAddress() {
		return address;
	}
	
	@Override
	public String toString() {
		return instruction.toString();
	}
}
