package org.binghamton.comparch.systems;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements an inorder, pipelined, two FU (one for branch and the other for
 * ALU) processor. This class provides the necessary methods to interface with
 * the processor to successfully simulate a list of instructions
 * 
 * @author Stefan Bossbaly
 * @author Gerald Brennan
 *
 */
public class Processor {
	/* Default Constants */
	public static final int NUM_OF_ARC_REGISTERS = 16;
	public static final int NUM_OF_PHY_REGISTERS = 32;
	public static final int SIZE_OF_DATA_MEMORY = 4000;
	public static final int CAPACITY_OF_IQ = 12;
	public static final int CAPACITY_OF_ROB = 40;

	/* Register Regex Pattern */
	private static final Pattern GP_REG_PATTERN = Pattern.compile("R(\\d+)");
	private static final Pattern SPEC_REG_PATTERN = Pattern.compile("X");

	/* List of instructions to be executed */
	private List<Instruction> instructions;
	private int pc;

	/* List of entries for each stage */
	private Entry fetchEntry;
	private Entry drf1Entry;
	private Entry drf2Entry;
	private int archRsrc1 = -1;
	private int archRsrc2 = -1;
	private int archRdest = -1;
	private Register phyRsrc1 = null;
	private Register phyRsrc2 = null;
	private Register phyRdest = null;

	/* ALU FU */
	private IQEntry alu1Entry;
	private IQEntry alu2Entry;
	private IQEntry aluWBEntry;
	private int alu1Result;
	private int alu2Result;
	private int aluWBResult;

	/* MULT FU */
	private int multCycle;
	private IQEntry multEntry;
	private IQEntry multWBEntry;

	/* Branch FU */
	private IQEntry branchEntry;
	private IQEntry branchMEMEntry;

	/* LOAD/STORE FU */
	private IQEntry ls1Entry;
	private IQEntry ls2Entry;
	private IQEntry lsMEMEntry;
	private IQEntry lsWBEntry;
	private int ls1Result;
	private int ls2Result;
	private int lsMEMResult;
	private int lsWBResult;

	/* Instruction Queue */
	private IQ iq;

	/* Reorder Buffer */
	private ROB rob;

	/* Unified register file */
	private URF urf;

	/* Data Memory */
	private final Memory memory;

	/* Halt Status */
	private boolean isHalted;

	/* The last destination register of a register to register command */
	private String regToRegDest;

	public Processor() {
		/* Setup memory object */
		this.memory = new Memory(SIZE_OF_DATA_MEMORY);

		/* Setup the instruction queue */
		this.iq = new IQ(CAPACITY_OF_IQ);

		/* Setup the reorder buffer */
		this.rob = new ROB(CAPACITY_OF_ROB);

		/* Setup the unified register file */
		this.urf = new URF(NUM_OF_ARC_REGISTERS, NUM_OF_PHY_REGISTERS);
	}

	/**
	 * Loads the list of instructions into program memory, sets the program
	 * counter to the beginning of program memory, clears out the pipeline, sets
	 * all general purpose registers and special register X to 0, and clears out
	 * data memory. This method should be called right after constructing an
	 * instance of a processor before any clockCycles are simulated. This method
	 * can also be used to rest the processor to the beginning state.
	 * 
	 * @param instructions
	 *            a list of instructions, in program order, that will be loaded
	 *            into instruction memory and simulated on the processor
	 */
	public void initialize(List<Instruction> instructions) {
		/* Copy the list of instructions */
		this.instructions = new ArrayList<Instruction>(instructions);
		this.pc = 4000;

		/* Clear out the pipeline */
		this.fetchEntry = null;
		this.drf1Entry = null;
		this.drf2Entry = null;

		/* ALU FU */
		this.alu1Entry = null;
		this.alu2Entry = null;
		this.aluWBEntry = null;

		/* MULT FU */
		this.multCycle = 0;
		this.multEntry = null;
		this.multWBEntry = null;

		/* Branch FU */
		this.branchEntry = null;
		this.branchEntry = null;
		this.branchMEMEntry = null;

		/* LOAD/STORE FU */
		this.ls1Entry = null;
		this.ls2Entry = null;
		this.lsMEMEntry = null;
		this.lsWBEntry = null;

		/* Clear memory */
		this.memory.clear();
	}

	/**
	 * Simulates the processor for the specified amount of clock cycles. If a
	 * HALT instruction is encountered, the simulation end when it executes the
	 * writeback stage and this method will return true. If a HALT instruction
	 * is not encountered, the method will return false once all the cycles have
	 * been executed.
	 * 
	 * @param cycles
	 *            the amount of clock cycles the simulation will run for
	 * @return true if the processor was halted during the simulation; false
	 *         otherwise
	 */
	public boolean cycle(int cycles) {
		isHalted = false;

		for (int i = 0; (i < cycles) && !isHalted; i += 1) {
			this.clockCyle();
		}

		return isHalted;
	}

	/**
	 * Takes a string representation of a register and converts that into the
	 * correct register reference.
	 * 
	 * @param str
	 *            the string representation of a register
	 * @return the register object that matches the string representation
	 * @throws IllegalArgumentException
	 *             if the string representation is invalid
	 */
	private int decodeRegister(String str) {
		Matcher gpMatcher = GP_REG_PATTERN.matcher(str);
		Matcher specMatcher = SPEC_REG_PATTERN.matcher(str);

		if (gpMatcher.matches()) {
			int index = Integer.valueOf(gpMatcher.group(1));
			return index;
		} else if (specMatcher.matches()) {
			throw new IllegalArgumentException("Needs to be implemented");
		} else {
			throw new IllegalArgumentException("Register can not be decoded");
		}
	}

	/**
	 * Simulates one clock cycle of the processor
	 */
	public void clockCyle() {
		/* DR/F COPY */
		this.drf2Entry = this.drf1Entry;
		this.drf1Entry = this.fetchEntry;

		/* ARTH FU Copy */
		this.aluWBEntry = this.alu2Entry;
		this.alu2Entry = this.alu1Entry;
		this.aluWBResult = this.alu2Result;
		this.alu2Result = this.alu1Result;
		
		/* LOAD/STORE FU Copy */
		this.lsWBEntry = this.lsMEMEntry;
		this.lsMEMEntry = this.ls2Entry;
		this.ls2Entry = this.ls1Entry;
		this.lsWBResult = this.lsMEMResult;
		this.lsMEMResult = this.ls2Result;
		this.ls2Result = this.ls1Result;

		/* Retire an rob entry if you can */
		if (rob.canRetire()) {
			ROBEntry entry = rob.retire();
			
			if (entry.getDestRegister() != null) {
				urf.commitRegister(entry.getArchRegister(), entry.getDestRegister());
			}
		}

		/* ISSUE */
		issue();

		/* Execute ALU FU */
		aluWBStage();
		alu2Stage();
		alu1Stage();
		
		/* Execute LOAD/STORE FU */
		lsWBStage();
		lsMEMStage();
		ls2Stage();
		ls1Stage();

		drf2Stage();
		drf1Stage();
		fetchStage();
	}

	private void fetchStage() {
		/* Make sure PC is divisible by four */
		if ((this.pc % 4) != 0) {
			throw new RuntimeException("PC is not divisible by 4");
		}

		/*
		 * Make sure we aren't trying to access the data section for
		 * instructions
		 */
		if (this.pc < SIZE_OF_DATA_MEMORY) {
			throw new RuntimeException("Tried to access data memory for execution");
		}

		/* Get the index of the next instruction base of the PC */
		int index = ((this.pc - SIZE_OF_DATA_MEMORY) / 4);

		/* Ensure that we do not go outside the list of instructions */
		if (index < this.instructions.size()) {
			Instruction next = this.instructions.get(index);
			this.fetchEntry = new Entry(next);
			this.fetchEntry.setPcValue(this.pc);
			this.pc += 4;
		} else {
			this.fetchEntry = null;
		}
	}

	/* DR/RF Stages */
	private void drf1Stage() {
		if (this.drf1Entry == null) {
			return;
		}

		Instruction current = this.drf1Entry.getInstruction();

		/*
		 * If we have a destination, make sure that there is a physical register
		 * available
		 */
		if (current.getOpCode().getDestinationCount() > 0 && !urf.hasPhysicalRegisterAvailable()) {
			/* Stall */
			System.out.println("Pipelined stalled. No Physical register available.");
			return;
		}

		/* Architectural Register Indices */
		archRsrc1 = -1;
		archRsrc2 = -1;
		archRdest = -1;

		/* Physical Register References */
		phyRsrc1 = null;
		phyRsrc2 = null;
		phyRdest = null;

		switch (current.getOpCode()) {
		/* Decode Rsrc1, Rsrc2 and Rdest */
		case ADD:
		case SUB:
		case MUL:
		case AND:
		case OR:
		case XOR:
			/* Decode and get a new physical register */
			archRdest = decodeRegister(current.getRdest());
			phyRdest = urf.allocatePhysicalRegister();
			break;
		case MOVC:
			/* Decode and get a new physical register */
			archRdest = decodeRegister(current.getRdest());
			phyRdest = urf.allocatePhysicalRegister();
			break;
		case LOAD:
			/* Decode and get a new physical register */
			archRdest = decodeRegister(current.getRdest());
			phyRdest = urf.allocatePhysicalRegister();
			break;
		case STORE:
			break;
		case BZ:
		case BNZ:
			break;
		case JUMP:
			break;
		case BAL:
			archRdest = decodeRegister(current.getRdest());
			phyRdest = urf.allocatePhysicalRegister();
			break;
		case HALT:
			break;
		default:
			throw new RuntimeException("Can not decode unknown instruction");
		}

		switch (current.getOpCode()) {
		/* Decode Rsrc1, Rsrc2 and Rdest */
		case ADD:
		case SUB:
		case MUL:
		case AND:
		case OR:
		case XOR:
			/* Decode the architectural register */
			archRsrc1 = decodeRegister(current.getRsrc1());
			archRsrc2 = decodeRegister(current.getRsrc2());

			/* Read out the renamed registers */
			phyRsrc1 = urf.getRenamedRegister(archRsrc1);
			phyRsrc2 = urf.getRenamedRegister(archRsrc2);
			break;
		/* Decode Rdest */
		case MOVC:
			/* No source registers */
			break;
		/* Decode Rsrc1 and Rdest */
		case LOAD:
			/* Decode the architectural register */
			archRsrc1 = decodeRegister(current.getRsrc1());

			/* Read out the renamed registers */
			phyRsrc1 = urf.getRenamedRegister(archRsrc1);
			break;
		/* Decode Rsrc1 and Rsrc2 */
		case STORE:
			/* Decode the architectural register */
			archRsrc1 = decodeRegister(current.getRsrc1());
			archRsrc2 = decodeRegister(current.getRsrc2());

			/* Read out the renamed registers */
			phyRsrc1 = urf.getRenamedRegister(archRsrc1);
			phyRsrc2 = urf.getRenamedRegister(archRsrc2);
			break;
		case BZ:
		case BNZ:
			/* Decode the architectural register */
			archRsrc1 = decodeRegister(current.getRsrc1());

			/* Read out the renamed registers */
			phyRsrc1 = urf.getRenamedRegister(archRsrc1);
			break;
		/* Decode Rsrc1 */
		case JUMP:
			/* Decode the architectural register */
			archRsrc1 = decodeRegister(current.getRsrc1());

			/* Read out the renamed registers */
			phyRsrc1 = urf.getRenamedRegister(archRsrc1);
			break;
		case BAL:
			/* Decode the architectural register */
			archRsrc1 = decodeRegister(current.getRsrc1());

			/* Read out the renamed registers */
			phyRsrc1 = urf.getRenamedRegister(archRsrc1);
			break;
		case HALT:
			/* No operation for HALT */
			break;
		default:
			throw new RuntimeException("Can not decode unknown instruction");
		}

		/* Record Rdest as new stand in */
		if (phyRdest != null) {
			urf.updateMapping(archRdest, phyRdest);
		}
	}

	private void drf2Stage() {
		if (this.drf2Entry == null) {
			return;
		}

		Instruction current = this.drf2Entry.getInstruction();

		/* Replace the Instruction with the */
		DecodedInstruction renamed = new DecodedInstruction(current.getOpCode(), phyRdest, phyRsrc1, phyRsrc2,
				current.getLiteral());

		IQEntry iqEntry = new IQEntry(renamed, this.drf2Entry.getPcValue());

		/* Processing for Register src1 */
		if (phyRsrc1 != null) {
			if (phyRsrc1.isValid()) {
				iqEntry.setSrc1Value(phyRsrc1.getValue());
				iqEntry.setSrc1Valid(true);
			} else {
				iqEntry.setSrc1Valid(false);
			}
		}

		/* Processing for Register src2 */
		if (phyRsrc2 != null) {
			if (phyRsrc2.isValid()) {
				iqEntry.setSrc2Value(phyRsrc2.getValue());
				iqEntry.setSrc2Valid(true);
			} else {
				iqEntry.setSrc2Valid(false);
			}
		}

		/* Enqueue the iq entry */
		iq.enqueue(iqEntry);

		/* Create the ROB entry */
		ROBEntry robEntry = new ROBEntry(renamed, this.drf2Entry.getPcValue());
		robEntry.setDestRegister(phyRdest);
		robEntry.setArchRegister(archRdest);

		/* Add ROB entry to ROB */
		rob.add(robEntry);

		/* Yeah no */
		iqEntry.setROBEntry(robEntry);
	}

	private void issue() {
		this.alu1Entry = null;
		this.multEntry = null;
		this.branchEntry = null;
		this.ls1Entry = null;

		if (iq.isEmpty()) {
			return;
		}

		IQEntry iqEntry = iq.peek();
		DecodedInstruction current = iqEntry.getInstruction();

		/* Check for updated physical registers */
		if (current.getOpCode().getSourceCount() > 1) {
			if (!iqEntry.isSrc2Valid()) {
				if (current.getRsrc2().isValid()) {
					iqEntry.setSrc2Value(current.getRsrc2().getValue());
					iqEntry.setSrc2Valid(true);
				} else {
					return;
				}
			}
		}

		/* Check for updated physical registers */
		if (current.getOpCode().getSourceCount() >= 1) {
			if (!iqEntry.isSrc1Valid()) {
				if (current.getRsrc1().isValid()) {
					iqEntry.setSrc1Value(current.getRsrc1().getValue());
					iqEntry.setSrc1Valid(true);
				} else {
					return;
				}
			}
		}

		switch (current.getOpCode()) {
		/* Decode Rsrc1, Rsrc2 and Rdest */
		case ADD:
		case SUB:
		case MOVC:
		case AND:
		case OR:
		case XOR:
		case HALT:
			this.alu1Entry = iq.dequeue();
			break;
		case MUL:
			// TODO implement
			// this.alu1Entry = iq.dequeue();
			break;
		case LOAD:
		case STORE:
			this.ls1Entry = iq.dequeue();
			break;
		case BZ:
		case BNZ:
		case JUMP:
		case BAL:
			// TODO implement
			break;
		default:
			throw new RuntimeException("Can not decode unknown instruction");
		}

	}

	/* ALU Stage */
	private void alu1Stage() {
		/* Make sure we have the entry for this stage */
		if (this.alu1Entry != null) {
			DecodedInstruction current = this.alu1Entry.getInstruction();

			int result = 0;
			int rsrc1 = 0;
			int rsrc2 = 0;

			/* Get the source entries out */
			switch (current.getOpCode().getSourceCount()) {
			case 2:
				rsrc1 = alu1Entry.getSrc1Value();
				rsrc2 = alu1Entry.getSrc2Value();
				break;
			case 1:
				rsrc1 = alu1Entry.getSrc1Value();
				break;
			default:
				break;
			}

			/* Switch on the instruction op code */
			switch (current.getOpCode()) {
			case ADD:
				result = rsrc1 + rsrc2;
				break;
			case SUB:
				result = rsrc1 - rsrc2;
				break;
			case MUL:
				result = rsrc1 * rsrc2;
				break;
			case AND:
				result = rsrc1 & rsrc2;
				break;
			case OR:
				result = rsrc1 | rsrc2;
				break;
			case XOR:
				result = rsrc1 ^ rsrc2;
				break;
			case LOAD:
				result = rsrc1 + current.getLiteral();
				break;
			case STORE:
				result = rsrc2 + current.getLiteral();
				break;
			case MOVC:
				result = current.getLiteral();
				break;
			case HALT:
				/* Don't do anything */
				break;
			default:
				throw new RuntimeException("ALU Stage can not execute unknown instruction");
			}

			/* Update the ALU entry */
			alu1Result = result;
		}
	}

	private void alu2Stage() {
		/* No operation */
	}

	private void aluWBStage() {
		if (this.aluWBEntry == null) {
			return;
		}

		ROBEntry robEntry = this.aluWBEntry.getROBEntry();

		robEntry.setStatus(true);
		robEntry.getDestRegister().setValue(aluWBResult);
	}
	//
	// /* MULT FU */
	// private void multStage() {
	// // TODO implement
	// }
	// private void multWBStage() {
	// // TODO implement
	// }

	// /* Branch FU */
	// private void branchStage() {
	// /* Make sure we have the entry for this stage */
	// if (this.branchEntry != null) {
	// Instruction current = this.branchEntry.getInstruction();
	//
	// boolean taken = false;
	// int targetAddress = 0;
	//
	// switch (current.getOpCode()) {
	// case BNZ:
	// taken = (this.branchEntry.getSrc1Value() != 0);
	// targetAddress = branchEntry.getAddress() + current.getLiteral();
	// break;
	// case BZ:
	// taken = (this.branchEntry.getSrc1Value() == 0);
	// targetAddress = branchEntry.getAddress() + current.getLiteral();
	// break;
	// case JUMP:
	// taken = true;
	// targetAddress = branchEntry.getSrc1Value() + current.getLiteral();
	// break;
	// case BAL:
	// taken = true;
	// targetAddress = branchEntry.getSrc1Value() + current.getLiteral();
	//
	// /* BAL instruction set PC to address of next instruction */
	// // TODO implement this
	// break;
	// default:
	// throw new RuntimeException("Unreconized branch");
	// }
	//
	// /* See if the branch is taken */
	// if (taken) {
	// // TODO implement this
	// }
	// }
	// }
	// private void branchMEMStage() {
	// /* LOL */
	// }
	//
	/* LOAD/STORE FU */
	private void ls1Stage() {
		if (this.ls1Entry == null) {
			return;
		}

		DecodedInstruction current = this.ls1Entry.getInstruction();

		int result;

		switch (current.getOpCode()) {
		case LOAD:
			result = ls1Entry.getSrc1Value() + current.getLiteral();
			break;
		case STORE:
			result = ls1Entry.getSrc2Value() + current.getLiteral();
			break;
		default:
			throw new RuntimeException("Programming Error: This should never happen");
		}
		
		ls1Result = result;
	}

	private void ls2Stage() {
		/* LOL */
	}

	private void lsMEMStage() {
		if (this.lsMEMEntry == null) {
			return;
		}

		DecodedInstruction current = this.lsMEMEntry.getInstruction();

		int result = -1;

		switch (current.getOpCode()) {
		case LOAD:
			result = this.memory.getValue(ls2Result);
			break;
		case STORE:
			this.memory.setValue(ls2Result, this.lsMEMEntry.getSrc1Value());
			break;
		default:
			throw new RuntimeException("Programming Error: This should never happen");
		}
		
		lsMEMResult = result;
	}

	private void lsWBStage() {
		if (this.lsWBEntry == null) {
			return;
		}
		
		DecodedInstruction current = this.lsWBEntry.getInstruction();
		ROBEntry robEntry = this.lsWBEntry.getROBEntry();
		
		switch(current.getOpCode()){
		case STORE:
			break;
		case LOAD:
			robEntry.getDestRegister().setValue(lsWBResult);
			break;
		default:
			throw new RuntimeException("Programming Error: This should never happen");
		}
		
		robEntry.setStatus(true);
	}

//	private void memStage() {
//		/* Make sure we have the entry for this stage */
//		if (this.memEntry != null) {
//			Instruction current = this.memEntry.getInstruction();
//
//			int result = 0;
//
//			/* Switch on the instruction op code */
//			switch (current.getOpCode()) {
//			case LOAD:
//				result = this.memory.getValue(this.memEntry.getExResult());
//				this.memEntry.setMemResult(result);
//				break;
//			case STORE:
//				result = this.memEntry.getRsrc1Result();
//				this.memory.setValue(this.memEntry.getExResult(), result);
//				break;
//			default:
//				/* No OP */
//				break;
//			}
//		}
//	}
//
//	private void wbStage() {
//		/* Make sure we have the entry for this stage */
//		if (this.wbEntry != null) {
//			Instruction current = this.wbEntry.getInstruction();
//			switch (current.getOpCode()) {
//			case ADD:
//			case SUB:
//			case MUL:
//			case AND:
//			case OR:
//			case XOR:
//			case MOVC:
//				this.wbEntry.getRdest().setValue(this.wbEntry.getExResult());
//				break;
//			case STORE:
//				break;
//			case LOAD:
//				this.wbEntry.getRdest().setValue(this.wbEntry.getMemResult());
//				break;
//			case BAL:
//				this.wbEntry.getRdest().setValue(this.wbEntry.getExResult());
//				break;
//			case HALT:
//				this.isHalted = true;
//				break;
//			default:
//				break;
//			}
//		}
//	}

	@Override
	public String toString() {
		String str = "";

		str += "--- Stages\n";
		str += "FETCH: " + ((this.fetchEntry == null) ? "Empty" : this.fetchEntry.getInstruction().toString()) + "\n";
		str += "D/RF1: " + ((this.drf1Entry == null) ? "Empty" : this.drf1Entry.getInstruction().toString()) + "\n";
		str += "D/RF2: " + ((this.drf2Entry == null) ? "Empty" : this.drf2Entry.getInstruction().toString()) + "\n";
		str += "ALU1:  " + ((this.alu1Entry == null) ? "Empty" : this.alu1Entry.getInstruction().toString()) + "\n";
		str += "ALU2:  " + ((this.alu2Entry == null) ? "Empty" : this.alu2Entry.getInstruction().toString()) + "\n";
		str += "ALUWB: " + ((this.aluWBEntry == null) ? "Empty" : this.aluWBEntry.getInstruction().toString()) + "\n";

		str += "LS1:   " + ((this.ls1Entry == null) ? "Empty" : this.ls1Entry.getInstruction().toString()) + "\n";
		str += "LS2:   " + ((this.ls2Entry == null) ? "Empty" : this.ls2Entry.getInstruction().toString()) + "\n";
		str += "LSMEM: " + ((this.lsMEMEntry == null) ? "Empty" : this.lsMEMEntry.getInstruction().toString()) + "\n";
		str += "LSWB:  " + ((this.lsWBEntry == null) ? "Empty" : this.lsWBEntry.getInstruction().toString()) + "\n";
		// str += "BR: " + ((this.branchEntry == null) ? "Empty" :
		// this.branchEntry.getInstruction().toString()) + "\n";
		// str += "DELAY: " + ((this.delayEntry == null) ? "Empty" :
		// this.delayEntry.getInstruction().toString()) + "\n";
		// str += "MEM: " + ((this.memEntry == null) ? "Empty" :
		// this.memEntry.getInstruction().toString()) + "\n";
		// str += "WB: " + ((this.wbEntry == null) ? "Empty" :
		// this.wbEntry.getInstruction().toString()) + "\n";

		str += "--- Registers\n";
		str += String.format("%3s: %d\n", "PC", this.pc);

		// /* Print out the state of the general purpose registers */
		// for (int i = 0; i < NUM_OF_GP_REGISTERS; i += 1) {
		// str += this.registers[i].toString() + "\n";
		// }
		//
		// /* Print out the sate of the special register */
		// str += this.xReg.toString() + "\n";

		/* Print out the IQ of memory */
		str += "--- IQ\n";
		str += iq.toString();
		
		/* Print out the ROB */
		str += "--- ROB\n";
		str += rob.toString();

		/* Print out the state of memory */
		str += "--- Memory\n";
		str += this.memory.stringMemory();

		return str;
	}
}
