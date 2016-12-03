package org.binghamton.comparch.systems;

public enum InstructionType {
	ADD   ("ADD",   1, 2, 0),
	SUB   ("SUB",   1, 2, 0),
	MOVC  ("MOVC",  1, 0, 1),
	MUL   ("MUL",   1, 2, 0),
	AND   ("AND",   1, 2, 0),
	OR    ("OR",    1, 2, 0),
	XOR   ("EX-OR", 1, 2, 0),
	LOAD  ("LOAD",  1, 1, 1),
	STORE ("STORE", 0, 2, 1),
	BZ    ("BZ",    1, 0, 1),
	BNZ   ("BNZ",   1, 0, 1),
	JUMP  ("JUMP",  0, 1, 1),
	BAL   ("BAL",   1, 1, 1),
	HALT  ("HALT",  0, 0, 0);
	
	private final String opCode;
	private final int destinations;
	private final int sources;
	private final int literals;

	InstructionType(String opCode, int destinations, int sources, int literals) {
		this.opCode = opCode;
		this.destinations = destinations;
		this.sources = sources;
		this.literals = literals;
	}

	/* Accessors */
	public String getOpCode() { return this.opCode; }
	public int getDestinationCount() { return this.destinations; }
	public int getSourceCount() { return this.sources; }
	public int getLiteralCount() { return this.literals; }
}