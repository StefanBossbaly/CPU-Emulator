package org.binghamton.comparch.systems;

public class ROBEntry {
	private Instruction instruction;
	private int instructionAddress;
	private Register destRegister;
	private int result;
	private int exCodes;
	private boolean status;

	public ROBEntry(Instruction instruction, int instructionAddress) {
		this.instruction = instruction;
		this.instructionAddress = instructionAddress;
		this.destRegister = null;
		this.result = 0;
		this.exCodes = 0;
		this.status = false;
	}
	
	public Instruction getInstruction() {
		return instruction;
	}
	
	public int getInstructionAddress() {
		return instructionAddress;
	}
	
	public Register getDestRegister() {
		return destRegister;
	}

	public void setDestRegister(Register destRegister) {
		this.destRegister = destRegister;
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

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean getStatus() {
		return status;
	}
}
