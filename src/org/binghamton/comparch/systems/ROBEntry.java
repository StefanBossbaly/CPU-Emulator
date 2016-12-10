package org.binghamton.comparch.systems;

public class ROBEntry {
	private DecodedInstruction instruction;
	private int instructionAddress;
	private int archRegister;
	private Register destRegister;
	private boolean status;
	private boolean takenBranch;
	private int takenAddress;

	public ROBEntry(DecodedInstruction instruction, int instructionAddress) {
		this.instruction = instruction;
		this.instructionAddress = instructionAddress;
		this.destRegister = null;
		this.status = false;
		this.takenBranch = false;
		this.takenAddress = 0;
	}

	public DecodedInstruction getInstruction() {
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

	public int getArchRegister() {
		return archRegister;
	}

	public void setArchRegister(int archRegister) {
		this.archRegister = archRegister;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean getStatus() {
		return status;
	}

	public boolean isTakenBranch() {
		return takenBranch;
	}

	public void setTakenBranch(boolean takenBranch) {
		this.takenBranch = takenBranch;
	}

	public int getTakenAddress() {
		return takenAddress;
	}

	public void setTakenAddress(int takenAddress) {
		this.takenAddress = takenAddress;
	}

	@Override
	public String toString() {
		return instruction.toString();
	}
}
