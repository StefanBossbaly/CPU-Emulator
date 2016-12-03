package org.binghamton.comparch.systems;

public class ROBEntry {
	private int instructionAddress;
	private Instruction instruction;
	private Register destRegister;
	private int result;
	private int exCodes;
	private boolean status;

	public ROBEntry(int instructionAddress, Instruction instruction) {
		this.instructionAddress = instructionAddress;
		this.instruction = instruction;
		this.result = 0;
		this.exCodes = 0;
		this.status = false;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public void setExCodes(int exCodes) {
		this.exCodes = exCodes;
	}

	public int getExCodes() {
		return exCodes;
	}

	public int getInstructionAddress() {
		return instructionAddress;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean getStatus() {
		return status;
	}

}
