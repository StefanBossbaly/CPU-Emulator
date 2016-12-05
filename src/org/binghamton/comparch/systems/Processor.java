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
	/* Constants */
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
	private Entry drfEntry;
	private Entry alu1Entry;
	private Entry alu2Entry;
	private Entry branchEntry;
	private Entry multEntry;
	private Entry ls1Entry;
	private Entry ls2Entry;
	private Entry memEntry;
	private Entry wbEntry;

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
		this.drfEntry = null;
		this.alu1Entry = null;
		this.alu2Entry = null;
		this.branchEntry = null;
		this.multEntry = null;
		this.ls1Entry = null;
		this.ls2Entry = null;
		this.memEntry = null;
		this.wbEntry = null;

		this.urf.clear();

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
	 * Checks to see if the instruction in the D/RF stage can be issued. This
	 * method will check for dependencies in the pipeline and will ensure that
	 * any flow dependencies that are not forwarded are blocked
	 * 
	 * @return true if the processor should block the issuing of this
	 *         Instruction; false if the instruction has all of its source
	 *         registers and can be issued
	 */
	private boolean block() {
		boolean result = false;

		if (this.drfEntry == null) {
			result = false;
		} else {
			Instruction current = this.drfEntry.getInstruction();

			if (current.getOpCode() == InstructionType.STORE) {
				Instruction aluInst = (this.alu1Entry == null) ? null : this.alu1Entry.getInstruction();

				/*
				 * See if the special LOAD -> STORE forwarding opportunity
				 * exists
				 */
				if (aluInst != null && aluInst.getOpCode() == InstructionType.LOAD
						&& aluInst.getRdest().equals(current.getRsrc1())) {
					result = false;
				} else {
					result = !this.drfEntry.isReady();
				}
			} else if (current.isALU() || current.isBranch()) {
				result = !this.drfEntry.isReady();
			} else {
				throw new RuntimeException("Unreconized instruction");
			}
		}

		return result;

	}

	/**
	 * Simulates one clock cycle of the processor
	 */
	public void clockCyle() {
		/* WB Stage */
		this.wbEntry = this.memEntry;
		wbStage();

		/* MEM Stage */
		if (this.delayEntry != null) {
			this.memEntry = this.delayEntry;
		} else {
			this.memEntry = this.alu2Entry;
		}
		memStage();

		/* DELAY Stage */
		this.delayEntry = this.branchEntry;
		delayStage();

		/* ALU2 Stage */
		this.alu2Entry = this.alu1Entry;
		alu2Stage();

		/* See if we are blocked */
		if (this.block()) {
			this.branchEntry = null;
			this.alu1Entry = null;
		} else {
			/* See if there is an entry in the drf Stage */
			if (this.drfEntry != null) {
				Instruction instr = this.drfEntry.getInstruction();

				/* Issue to the branch FU if the instruction is a branch */
				if (instr.isBranch()) {
					/* Branch Stage */
					this.branchEntry = this.drfEntry;
					this.alu1Entry = null;
					branchStage();
				} else if (instr.isALU()) { /* Else issue to the ALU FU */
					/* ALU Stage */
					this.branchEntry = null;
					this.alu1Entry = this.drfEntry;
					alu1Stage();
				} else {
					throw new RuntimeException("Can not dispatch unknown instruction");
				}
				/* No entry in the drf */
			} else {
				this.branchEntry = null;
				this.alu1Entry = null;
			}

			/* D/RF Stage */
			this.drfEntry = this.fetchEntry;
			drfStage();

			/* Fetch Stage */
			fetchStage();
		}
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

	private void issue() {
		IQEntry entry = iq.peek();

		if (entry.isSrc1Valid() && entry.isSrc2Valid()) {
			// TODO do stuff
		}
	}

	private void dispatch(Instruction current) {
		/* Make sure that we have space on the iq */
		if (iq.isFull()) {
			/* Stall */
			return;
		// TODO not sure if this goes on dispatch or issue
		/* Make sure there is space on the ROB */
		} else if (rob.isFull()) {
			/* Stall */
			return;
		} 
		
		/* If we have a destination, make sure that there is a physical register available */
		if (current.getOpCode().getDestinationCount() > 0 && !urf.hasRegisterAvailable()) {
			/* Stall */
			return;
		}
		
		/* Physical register to decode */
		int archRsrc1 = -1, archRsrc2 = -1, archRdest = -1;
		Register phyRsrc1 = null, phyRsrc2 = null, phyRdest = null;
		
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
			archRdest = decodeRegister(current.getRdest());
			
			/* Read out the renamed registers */
			phyRsrc1 = urf.getRenamedRegister(archRsrc1);
			phyRsrc2 = urf.getRenamedRegister(archRsrc2);
			
			/* Get a new physical register */
			phyRdest = urf.allocateRegister(archRdest);
			break;
		/* Decode Rdest */
		case MOVC:
			/* Decode the architectural register */
			archRdest = decodeRegister(current.getRdest());
			
			/* Get a new physical register */
			phyRdest = urf.allocateRegister(archRdest);
			break;
		/* Decode Rsrc1 and Rdest */
		case LOAD:
			/* Decode the architectural register */
			archRsrc1 = decodeRegister(current.getRsrc1());
			archRdest = decodeRegister(current.getRdest());
			
			/* Read out the renamed registers */
			phyRsrc1 = urf.getRenamedRegister(archRsrc1);
			
			/* Get a new physical register */
			phyRdest = urf.allocateRegister(archRdest);
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
			archRdest = decodeRegister(current.getRdest());
			
			/* Read out the renamed registers */
			phyRsrc1 = urf.getRenamedRegister(archRsrc1);
			
			/* Get a new physical register */
			phyRdest = urf.allocateRegister(archRdest);
			break;
		case HALT:
			/* No operation for HALT */
			break;
		default:
			throw new RuntimeException("Can not decode unknown instruction");
		}
		
		/* Create the IQ Entry */
		IQEntry entry = new IQEntry(drfEntry.getInstruction(), drfEntry.getPcValue());
		
		// TODO if the source register is valid then mark the valid bit as true
		// TODO if the source register is an ROB entry then mark it as false

		/* Enqueue the the current instruction in the IQ */
		iq.enqueue( );
	}

	private void drf1Stage() {

	}

	private void drf2Stage() {

	}

	private void drfStage() {
		/* Make sure we have the entry for this stage */
		if (this.drfEntry != null) {
			/* Get the instruction for this stage */
			Instruction current = this.drfEntry.getInstruction();

			/* Registers to decode */
			Register rsrc1 = null;
			Register rsrc2 = null;
			Register rdest = null;

			switch (current.getOpCode()) {
			/* Decode Rsrc1, Rsrc2 and Rdest */
			case ADD:
			case SUB:
			case MUL:
			case AND:
			case OR:
			case XOR:
				rsrc1 = decodeRegister(current.getRsrc1());
				rsrc2 = decodeRegister(current.getRsrc2());
				rdest = decodeRegister(current.getRdest());
				regToRegDest = current.getRdest();
				break;
			/* Decode Rdest */
			case MOVC:
				rdest = decodeRegister(current.getRdest());
				break;
			/* Decode Rsrc1 and Rdest */
			case LOAD:
				rsrc1 = decodeRegister(current.getRsrc1());
				rdest = decodeRegister(current.getRdest());
				break;
			/* Decode Rsrc1 and Rsrc2 */
			case STORE:
				rsrc1 = decodeRegister(current.getRsrc1());
				rsrc2 = decodeRegister(current.getRsrc2());
				break;
			case BZ:
			case BNZ:
				if (regToRegDest == null) {
					throw new RuntimeException("Status bits were not set before conditionial branch");
				}

				/* Implicit status code dependency */
				current.setRsrc1(regToRegDest);
				rsrc1 = decodeRegister(current.getRsrc1());
				break;
			/* Decode Rsrc1 */
			case JUMP:
				rsrc1 = decodeRegister(current.getRsrc1());
				break;
			case BAL:
				rsrc1 = decodeRegister(current.getRsrc1());
				rdest = decodeRegister(current.getRdest());
				break;
			case HALT:
				/* No operation for HALT */
				break;
			default:
				throw new RuntimeException("Can not decode unknown instruction");
			}

			/* Ensure that there are no dependencies */
			Instruction drfInst = (this.drfEntry == null) ? null : this.drfEntry.getInstruction();
			Instruction alu1Inst = (this.alu1Entry == null) ? null : this.alu1Entry.getInstruction();
			Instruction alu2Inst = (this.alu2Entry == null) ? null : this.alu2Entry.getInstruction();
			Instruction delayInst = (this.delayEntry == null) ? null : this.delayEntry.getInstruction();
			Instruction branchInst = (this.branchEntry == null) ? null : this.branchEntry.getInstruction();
			Instruction memInst = (this.memEntry == null) ? null : this.memEntry.getInstruction();

			if (current.getOpCode().getSourceCount() == 2) {
				boolean rsrc2Dep = (alu1Inst != null && drfInst.isRsrc2FlowDependant(alu1Inst))
						|| (alu2Inst != null && drfInst.isRsrc2FlowDependant(alu2Inst))
						|| (branchInst != null && drfInst.isRsrc2FlowDependant(branchInst))
						|| (delayInst != null && drfInst.isRsrc2FlowDependant(delayInst))
						|| (memInst != null && drfInst.isRsrc2FlowDependant(memInst));

				if (rsrc2Dep) {
					this.drfEntry.setRsrc2Valid(false);
				} else {
					this.drfEntry.setRsrc2Result(rsrc2.getValue());
					this.drfEntry.setRsrc2Valid(true);
				}
			}

			if (current.getOpCode().getSourceCount() >= 1) {
				boolean rsrc1Dep = (alu1Inst != null && drfInst.isRsrc1FlowDependant(alu1Inst))
						|| (alu2Inst != null && drfInst.isRsrc1FlowDependant(alu2Inst))
						|| (branchInst != null && drfInst.isRsrc1FlowDependant(branchInst))
						|| (delayInst != null && drfInst.isRsrc1FlowDependant(delayInst))
						|| (memInst != null && drfInst.isRsrc1FlowDependant(memInst));

				if (rsrc1Dep) {
					this.drfEntry.setRsrc1Valid(false);
				} else {
					this.drfEntry.setRsrc1Result(rsrc1.getValue());
					this.drfEntry.setRsrc1Valid(true);
				}
			}

			/* Update the branch entry */;
			this.drfEntry.setRdest(rdest);
		}
	}

	private void alu1Stage() {
		/* Make sure we have the entry for this stage */
		if (this.alu1Entry != null) {
			Instruction current = this.alu1Entry.getInstruction();

			int result = 0;
			int rsrc1 = 0;
			int rsrc2 = 0;

			/* Get the source entries out */
			switch (current.getOpCode().getSourceCount()) {
			case 2:
				rsrc1 = alu1Entry.getRsrc1Result();
				rsrc2 = alu1Entry.getRsrc2Result();
				break;
			case 1:
				rsrc1 = alu1Entry.getRsrc1Result();
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
			this.alu1Entry.setExResult(result);
		}
	}

	private void alu2Stage() {
		/* No operation */
	}

	private void branchStage() {
		/* Make sure we have the entry for this stage */
		if (this.branchEntry != null) {
			Instruction current = this.branchEntry.getInstruction();

			boolean taken = false;
			int targetAddress = 0;

			switch (current.getOpCode()) {
			case BNZ:
				taken = (this.branchEntry.getRsrc1Result() != 0);
				targetAddress = branchEntry.getPcValue() + current.getLiteral();
				break;
			case BZ:
				taken = (this.branchEntry.getRsrc1Result() == 0);
				targetAddress = branchEntry.getPcValue() + current.getLiteral();
				break;
			case JUMP:
				taken = true;
				targetAddress = branchEntry.getRsrc1Result() + current.getLiteral();
				break;
			case BAL:
				taken = true;
				targetAddress = branchEntry.getRsrc1Result() + current.getLiteral();

				/* BAL instruction set PC to address of next instruction */
				this.branchEntry.setExResult(this.branchEntry.getPcValue() + 4);
				break;
			default:
				throw new RuntimeException("Unreconized branch");
			}

			/* See if the branch is taken */
			if (taken) {
				/* Zero out F and D/RF */
				this.fetchEntry = null;
				this.drfEntry = null;

				/* Transfer control to the target address */
				this.pc = targetAddress;
			}
		}
	}

	private void delayStage() {
		/* LOL */
	}

	private void memStage() {
		/* Make sure we have the entry for this stage */
		if (this.memEntry != null) {
			Instruction current = this.memEntry.getInstruction();

			int result = 0;

			/* Switch on the instruction op code */
			switch (current.getOpCode()) {
			case LOAD:
				result = this.memory.getValue(this.memEntry.getExResult());
				this.memEntry.setMemResult(result);
				break;
			case STORE:
				result = this.memEntry.getRsrc1Result();
				this.memory.setValue(this.memEntry.getExResult(), result);
				break;
			default:
				/* No OP */
				break;
			}
		}
	}

	private void wbStage() {
		/* Make sure we have the entry for this stage */
		if (this.wbEntry != null) {
			Instruction current = this.wbEntry.getInstruction();
			switch (current.getOpCode()) {
			case ADD:
			case SUB:
			case MUL:
			case AND:
			case OR:
			case XOR:
			case MOVC:
				this.wbEntry.getRdest().setValue(this.wbEntry.getExResult());
				break;
			case STORE:
				break;
			case LOAD:
				this.wbEntry.getRdest().setValue(this.wbEntry.getMemResult());
				break;
			case BAL:
				this.wbEntry.getRdest().setValue(this.wbEntry.getExResult());
				break;
			case HALT:
				this.isHalted = true;
				break;
			default:
				break;
			}
		}
	}

	@Override
	public String toString() {
		String str = "";

		str += "--- Stages\n";
		str += "FETCH: " + ((this.fetchEntry == null) ? "Empty" : this.fetchEntry.getInstruction().toString()) + "\n";
		str += "D/RF:  " + ((this.drfEntry == null) ? "Empty" : this.drfEntry.getInstruction().toString()) + "\n";
		str += "ALU1:  " + ((this.alu1Entry == null) ? "Empty" : this.alu1Entry.getInstruction().toString()) + "\n";
		str += "ALU2:  " + ((this.alu2Entry == null) ? "Empty" : this.alu2Entry.getInstruction().toString()) + "\n";
		str += "BR:    " + ((this.branchEntry == null) ? "Empty" : this.branchEntry.getInstruction().toString()) + "\n";
		str += "DELAY: " + ((this.delayEntry == null) ? "Empty" : this.delayEntry.getInstruction().toString()) + "\n";
		str += "MEM:   " + ((this.memEntry == null) ? "Empty" : this.memEntry.getInstruction().toString()) + "\n";
		str += "WB:    " + ((this.wbEntry == null) ? "Empty" : this.wbEntry.getInstruction().toString()) + "\n";

		str += "--- Registers\n";
		str += String.format("%3s: %d\n", "PC", this.pc);

		/* Print out the state of the general purpose registers */
		for (int i = 0; i < NUM_OF_GP_REGISTERS; i += 1) {
			str += this.registers[i].toString() + "\n";
		}

		/* Print out the sate of the special register */
		str += this.xReg.toString() + "\n";

		/* Print out the state of memory */
		str += "--- Memory\n";
		str += this.memory.stringMemory();

		return str;
	}
}
