package org.binghamton.comparch.systems;

/**
 * A class that represents an stage entry. A stage entry contains metadata that
 * is used when processing the instruction. This meta data is used throughout
 * the pipeline when processing the instruction. This entry holds the program
 * counter value when the instruction was fetched, the decoded destination
 * register of the instruction (if any), the value of the source registers and
 * weather or not they are valid (if any), the value of the that is computed
 * during the execution stage (if any) and the value that is computed during the
 * memory stage (if any).
 * 
 * @author Stefan Bossbaly
 * @author Gerald Brennan
 *
 */
public class Entry {
	/* A reference to the instruction */
	private Instruction instruction;

	/* The instruction's address in program memory */
	private int pcValue;

	/* Decoded registers in the D/RF stage */
	private Register rdest;

	/* Status for rsrc1 */
	private int rsrc1Result;
	private boolean rsrc1Valid;

	/* Status for rsrc2 */
	private int rsrc2Result;
	private boolean rsrc2Valid;

	/* Result from the EX stage */
	private int exResult;

	/* Result from the mem stage */
	private int memResult;

	public Entry(Instruction instruction) {
		this.instruction = instruction;
		this.pcValue = 0;
		this.exResult = 0;
		this.memResult = 0;

		this.rsrc1Result = 0;
		this.rsrc1Valid = false;

		this.rsrc2Result = 0;
		this.rsrc2Valid = false;
	}

	/**
	 * Returns if the has valid source entries, depending on the type of
	 * instruction.
	 * 
	 * @return true if the entry has all of its needed source values; false
	 *         otherwise
	 */
	public boolean isReady() {
		switch (this.instruction.getOpCode().getSourceCount()) {
		case 1:
			return this.rsrc1Valid == true;
		case 2:
			return (this.rsrc1Valid == true) && (this.rsrc2Valid == true);
		default:
			return true;
		}
	}

	/**
	 * Returns the program counter value of this entry.
	 * 
	 * @return the program counter value of this entry.
	 */
	public int getPcValue() {
		return pcValue;
	}

	/**
	 * Sets the program counter value of this entry.
	 * 
	 * @param pcValue
	 *            the new program counter value
	 */
	public void setPcValue(int pcValue) {
		this.pcValue = pcValue;
	}

	/**
	 * Sets the execution stage result of this entry
	 * 
	 * @param exResult
	 *            the result of the execution stage operation
	 */
	public void setExResult(int exResult) {
		this.exResult = exResult;
	}

	/**
	 * Returns the execution stage result of this entry
	 * 
	 * @return the value that was computed during the execution stage
	 */
	public int getExResult() {
		return this.exResult;
	}

	/**
	 * Sets the memory stage result of this entry
	 * 
	 * @param memResult
	 *            the result of the memory stage operation
	 */
	public void setMemResult(int memResult) {
		this.memResult = memResult;
	}

	/**
	 * Returns the memory stage result of this entry
	 * 
	 * @return the value that was computed during the memory stage
	 */
	public int getMemResult() {
		return this.memResult;
	}

	/**
	 * Returns the reference to the destination register
	 * 
	 * @return the reference to the destination register; false otherwise
	 */
	public Register getRdest() {
		return rdest;
	}

	/**
	 * Sets the reference to the destination register
	 * 
	 * @param rdest
	 *            the reference to the destination register
	 */
	public void setRdest(Register rdest) {
		this.rdest = rdest;
	}

	/**
	 * Returns the result of the rsrc1 value. This is the value of what should
	 * be used as rsrc1 value.
	 * 
	 * @return the result of the rsrc1 value.
	 */
	public int getRsrc1Result() {
		return rsrc1Result;
	}

	/**
	 * Sets the result of the rsrc1 value. This can be populated via the
	 * register file or a data forward.
	 * 
	 * @param rsrc1Result
	 *            the value of the rsrc1 register
	 */
	public void setRsrc1Result(int rsrc1Result) {
		this.rsrc1Result = rsrc1Result;
	}

	/**
	 * Returns the result of the rsrc2 value. This is the value of what should
	 * be used as rsrc1 value.
	 * 
	 * @return the result of the rsrc2 value.
	 */
	public int getRsrc2Result() {
		return rsrc2Result;
	}

	/**
	 * Sets the result of the rsrc2 value. This can be populated via the
	 * register file or a data forward.
	 * 
	 * @param rsrc2Result
	 *            the value of the rsrc2 register
	 */
	public void setRsrc2Result(int rsrc2Result) {
		this.rsrc2Result = rsrc2Result;
	}

	/**
	 * Returns if the value for rsrc1Result is valid.
	 * 
	 * @return true if the value of rsrc1Result is valid and can be used in a
	 *         calculation; false otherwise
	 */
	public boolean isRsrc1Valid() {
		return rsrc1Valid;
	}

	/**
	 * Sets the rsrc1 valid bit
	 * 
	 * @param rsrc1Valid
	 *            weather rsrc1 is valid or not
	 */
	public void setRsrc1Valid(boolean rsrc1Valid) {
		this.rsrc1Valid = rsrc1Valid;
	}

	/**
	 * Returns if the value for rsrc2Result is valid.
	 * 
	 * @return true if the value of rsrc2Result is valid and can be used in a
	 *         calculation; false otherwise
	 */
	public boolean isRsrc2Valid() {
		return rsrc2Valid;
	}

	/**
	 * Sets the rsrc2 valid bit
	 * 
	 * @param rsrc2Valid
	 *            weather rsrc2 is valid or not
	 */
	public void setRsrc2Valid(boolean rsrc2Valid) {
		this.rsrc2Valid = rsrc2Valid;
	}

	/**
	 * Return the instruction that is associated with this entry
	 * 
	 * @return the reference to the instruction that is associated with this
	 *         entry
	 */
	public Instruction getInstruction() {
		return this.instruction;
	}
}
