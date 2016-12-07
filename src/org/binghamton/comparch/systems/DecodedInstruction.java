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
	
	@Override
	public String toString() {
		switch (this.opCode)
		{
		case ADD:
		case SUB:
		case MUL:
		case AND:
		case OR:
		case XOR:
			return String.format("%s %s, %s, %s", this.opCode.getOpCode(), this.rdest.getName(), this.rsrc1.getName(), this.rsrc2.getName());
		case MOVC:
			return String.format("%s %s, #%d", this.opCode.getOpCode(), this.rdest.getName(), this.literal);
		case LOAD:
			return String.format("%s %s, %s, #%d", this.opCode.getOpCode(), this.rdest.getName(), this.rsrc1.getName(), this.literal);
		case STORE:
			return String.format("%s %s, %s, #%d", this.opCode.getOpCode(), this.rsrc1.getName(), this.rsrc2.getName(), this.literal);
		case BZ:
		case BNZ:
			return String.format("%s #%d", this.opCode.getOpCode(), this.literal);
		case JUMP:
			return String.format("%s %s, #%d", this.opCode.getOpCode(), this.rsrc1.getName(), this.literal);
		case BAL:
			return String.format("%s %s, #%d", this.opCode.getOpCode(), this.rsrc1.getName(), this.literal);
		case HALT:
			return this.opCode.getOpCode();
		default:
			return this.opCode.getOpCode();
		}
	}
}
