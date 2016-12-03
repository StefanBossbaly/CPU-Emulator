package org.binghamton.comparch.systems;

public class Instruction {
	private InstructionType opCode;
	private String rdest;
	private String rsrc1;
	private String rsrc2;
	private int literal;

	public Instruction(InstructionType opCode, String rdest, String rsrc1, String rsrc2, int literal) {
		this.opCode = opCode;
		this.rdest = rdest;
		this.rsrc1 = rsrc1;
		this.rsrc2 = rsrc2;
		this.literal = literal;
	}

	public InstructionType getOpCode() {
		return opCode;
	}

	public String getRdest() {
		return rdest;
	}

	public void setRsrc1(String rsrc1) {
		this.rsrc1 = rsrc1;
	}

	public String getRsrc1() {
		return rsrc1;
	}

	public String getRsrc2() {
		return rsrc2;
	}

	public int getLiteral() {
		return literal;
	}

	/**
	 * Returns true if the instruction unconditionally changes the program
	 * counter
	 * 
	 * @return true the instruction unconditionally changes the program counter;
	 *         false otherwise
	 */
	public boolean isUnconditionalBranch() {
		return (this.opCode == InstructionType.JUMP) || (this.opCode == InstructionType.BAL);
	}

	/**
	 * Returns true if the instruction changes the program counter if a certain
	 * condition is meet. These instructions have a dependency on the status
	 * bits.
	 * 
	 * @return true the instruction changes the program counter if a certain
	 *         condition is meet; false otherwise
	 */
	public boolean isConditionalBranch() {
		return (this.opCode == InstructionType.BZ) || (this.opCode == InstructionType.BNZ);
	}

	/**
	 * Returns true if the instruction has the potential to change the program
	 * counter. These instruction will need to be issued to the branch FU.
	 * 
	 * @return true if the instruction has the potential to change the program
	 *         counter; false otherwise
	 */
	public boolean isBranch() {
		return this.isUnconditionalBranch() || this.isConditionalBranch();
	}

	/**
	 * Returns true if the instruction is an arithmetic operation and will need
	 * to be issued to the ALU FU.
	 * 
	 * @return true if the instruction is an arithmetic operation; false
	 *         otherwise
	 */
	public boolean isALU() {
		return (this.opCode == InstructionType.ADD) || (this.opCode == InstructionType.SUB)
				|| (this.opCode == InstructionType.MUL) || (this.opCode == InstructionType.AND)
				|| (this.opCode == InstructionType.OR) || (this.opCode == InstructionType.XOR)
				|| (this.opCode == InstructionType.MOVC) || (this.opCode == InstructionType.HALT)
				|| (this.opCode == InstructionType.LOAD) || (this.opCode == InstructionType.STORE);
	}

	public boolean resultInALUStage2() {
		return (this.opCode == InstructionType.ADD) || (this.opCode == InstructionType.SUB)
				|| (this.opCode == InstructionType.MUL) || (this.opCode == InstructionType.AND)
				|| (this.opCode == InstructionType.OR) || (this.opCode == InstructionType.XOR)
				|| (this.opCode == InstructionType.MOVC);
	}

	public boolean resultInDelayStage() {
		return this.opCode == InstructionType.BAL;
	}

	public boolean resultInBranchStage() {
		return this.opCode == InstructionType.BAL;
	}

	public boolean resultInAluStage2() {
		return (this.opCode == InstructionType.ADD) || (this.opCode == InstructionType.SUB)
				|| (this.opCode == InstructionType.MUL) || (this.opCode == InstructionType.AND)
				|| (this.opCode == InstructionType.OR) || (this.opCode == InstructionType.XOR)
				|| (this.opCode == InstructionType.MOVC);
	}

	public boolean shouldSetStatus() {
		return (this.opCode == InstructionType.ADD) || (this.opCode == InstructionType.SUB)
				|| (this.opCode == InstructionType.MUL) || (this.opCode == InstructionType.AND)
				|| (this.opCode == InstructionType.OR) || (this.opCode == InstructionType.XOR);
	}

	public boolean hasDestination() {
		return this.rdest != null;
	}

	public boolean isRsrc1FlowDependant(Instruction other) {
		return (rsrc1 != null && other.hasDestination() && rsrc1.equals(other.getRdest()));
	}

	public boolean isRsrc2FlowDependant(Instruction other) {
		return (rsrc2 != null && other.hasDestination() && rsrc2.equals(other.getRdest()));
	}

	public boolean isFlowDependant(Instruction other) {
		return isRsrc1FlowDependant(other) || isRsrc2FlowDependant(other);
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
			return String.format("%s %s, %s, %s", this.opCode.getOpCode(), this.rdest, this.rsrc1, this.rsrc2);
		case MOVC:
			return String.format("%s %s, #%d", this.opCode.getOpCode(), this.rdest, this.literal);
		case LOAD:
			return String.format("%s %s, %s, #%d", this.opCode.getOpCode(), this.rdest, this.rsrc1, this.literal);
		case STORE:
			return String.format("%s %s, %s, #%d", this.opCode.getOpCode(), this.rsrc1, this.rsrc2, this.literal);
		case BZ:
		case BNZ:
			return String.format("%s #%d", this.opCode.getOpCode(), this.literal);
		case JUMP:
			return String.format("%s %s, #%d", this.opCode.getOpCode(), this.rsrc1, this.literal);
		case BAL:
			return String.format("%s %s, #%d", this.opCode.getOpCode(), this.rsrc1, this.literal);
		case HALT:
			return this.opCode.getOpCode();
		default:
			return this.opCode.getOpCode();
		}
	}
}