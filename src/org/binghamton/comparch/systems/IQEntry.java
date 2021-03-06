package org.binghamton.comparch.systems;

public class IQEntry {
	private final DecodedInstruction instruction;
	private final int address;
	private int src1Value;
	private boolean src1Valid;
	private int src2Value;
	private boolean src2Valid;
	private ROBEntry robEntry;
	
	public IQEntry(DecodedInstruction instruction, int address) {
		this.instruction = instruction;
		this.address = address;
	}

	public DecodedInstruction getInstruction() {
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
	
	public void setROBEntry(ROBEntry newrobEntry) {
		this.robEntry = newrobEntry;
	}
	
	public ROBEntry getROBEntry() {
		return this.robEntry;
	}

	@Override
	public String toString() {
		return instruction.toString();
	}
}
