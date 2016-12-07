package org.binghamton.comparch.systems;

public class DecodedInstruction {
	private InstructionType opCode;
	private Register rdest;
	private Register rsrc1;
	private Register rsrc2;
	private int literal;

	public DecodedInstruction(InstructionType opCode, Register rdest, Register rsrc1, Register rsrc2, int literal) {
		this.opCode = opCode;
		this.rdest = rdest;
		this.rsrc1 = rsrc1;
		this.rsrc2 = rsrc2;
		this.literal = literal;
	}

	public InstructionType getOpCode() {
		return opCode;
	}

	public Register getRdest() {
		return rdest;
	}

	public void setRsrc1(Register rsrc1) {
		this.rsrc1 = rsrc1;
	}

	public Register getRsrc1() {
		return rsrc1;
	}

	public Register getRsrc2() {
		return rsrc2;
	}

	public int getLiteral() {
		return literal;
	}
}
