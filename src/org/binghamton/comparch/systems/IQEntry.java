package org.binghamton.comparch.systems;

import org.binghamton.comparch.register.Register;

public class IQEntry {
	private final Instruction instruction;
	private final int address;
	private int src1Value;
	private boolean src1Valid;
	private int src2Value;
	private boolean src2Valid;
	
	private Register destReg;
	
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
	
	public int getSrc1Value() {
		return src1Value;
	}

	public void setSrc1Value(int src1Value) {
		this.src1Value = src1Value;
	}

	public boolean isSrc1Valid() {
		return src1Valid;
	}

	public void setSrc1Valid(boolean src1Valid) {
		this.src1Valid = src1Valid;
	}

	public int getSrc2Value() {
		return src2Value;
	}

	public void setSrc2Value(int src2Value) {
		this.src2Value = src2Value;
	}

	public boolean isSrc2Valid() {
		return src2Valid;
	}

	public void setSrc2Valid(boolean src2Valid) {
		this.src2Valid = src2Valid;
	}

	public Register getDestReg() {
		return destReg;
	}

	public void setDestReg(Register destReg) {
		this.destReg = destReg;
	}

	@Override
	public String toString() {
		return instruction.toString();
	}
}
