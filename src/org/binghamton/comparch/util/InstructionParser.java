package org.binghamton.comparch.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.binghamton.comparch.systems.Instruction;
import org.binghamton.comparch.systems.InstructionType;

public class InstructionParser {
	/* Regex patterns */
	private static final Pattern REG_TO_REG_PATTERN = Pattern
			.compile("(?<opCode>ADD|SUB|MUL|AND|OR|EX-OR)\\s+(?<rdest>R\\d+),\\s+(?<rsrc1>R\\d+),\\s+(?<rsrc2>R\\d+)");
	private static final Pattern MOVC_PATTERN = Pattern.compile("MOVC\\s+(?<rdest>R\\d+),\\s+#\\s*(?<literal>(\\+|-)?\\d+)");
	private static final Pattern LOAD_PATTERN = Pattern.compile("LOAD\\s+(?<rdest>R\\d+),\\s+(?<rsrc1>R\\d+),\\s+#\\s*(?<literal>(\\+|-)?\\d+)");
	private static final Pattern STORE_PATTERN = Pattern.compile("STORE\\s+(?<rsrc1>R\\d+),\\s+(?<rsrc2>R\\d+),\\s+#\\s*(?<literal>(\\+|-)?\\d+)");
	private static final Pattern BR_PATTERN = Pattern.compile("(?<opCode>BZ|BNZ)\\s+#\\s*(?<literal>(\\+|-)?\\d+)");
	private static final Pattern JUMP_PATTERN = Pattern.compile("JUMP\\s(?<rsrc1>(X|R\\d+)),\\s+#\\s*(?<literal>(\\+|-)?\\d+)");
	private static final Pattern BAL_PATTERN = Pattern.compile("BAL\\s(?<rsrc1>R\\d+),\\s+#\\s*(?<literal>(\\+|-)?\\d+)");
	private static final Pattern HALT_PATTERN = Pattern.compile("HALT");

	private String file;

	public InstructionParser(String file) {
		this.file = file;
	}

	private InstructionType strToEnum(String opCode) {
		switch (opCode) {
		case "ADD":
			return InstructionType.ADD;
		case "SUB":
			return InstructionType.SUB;
		case "MUL":
			return InstructionType.MUL;
		case "AND":
			return InstructionType.AND;
		case "OR":
			return InstructionType.OR;
		case "EX-OR":
			return InstructionType.XOR;
		case "MOVC":
			return InstructionType.MOVC;
		case "LOAD":
			return InstructionType.LOAD;
		case "STORE":
			return InstructionType.STORE;
		case "BZ":
			return InstructionType.BZ;
		case "BNZ":
			return InstructionType.BNZ;
		case "JUMP":
			return InstructionType.JUMP;
		default:
			throw new RuntimeException("Unkown opcode");
		}
	}

	public List<Instruction> parserFile() {
		ArrayList<Instruction> list = new ArrayList<Instruction>();

		try (BufferedReader br = new BufferedReader(new FileReader(this.file))) {
			for (String line; (line = br.readLine()) != null;) {
				/* Try to match */
				Matcher regToRegMatcher = REG_TO_REG_PATTERN.matcher(line);
				Matcher movcMatcher = MOVC_PATTERN.matcher(line);
				Matcher loadMatcher = LOAD_PATTERN.matcher(line);
				Matcher storeMatcher = STORE_PATTERN.matcher(line);
				Matcher brMatcher = BR_PATTERN.matcher(line);
				Matcher jumpMatcher = JUMP_PATTERN.matcher(line);
				Matcher balMatcher = BAL_PATTERN.matcher(line);
				Matcher haltMatcher = HALT_PATTERN.matcher(line);

				if (regToRegMatcher.matches()) {
					/* Extract data from line */
					String opCode = regToRegMatcher.group("opCode");
					String rdest = regToRegMatcher.group("rdest");
					String rsrc1 = regToRegMatcher.group("rsrc1");
					String rsrc2 = regToRegMatcher.group("rsrc2");

					/* Add new instruction to the list */
					list.add(new Instruction(strToEnum(opCode), rdest, rsrc1, rsrc2, 0));
				} else if (movcMatcher.matches()) {
					String rdest = movcMatcher.group("rdest");
					String literal = movcMatcher.group("literal");

					list.add(new Instruction(InstructionType.MOVC, rdest, null, null, Integer.valueOf(literal)));
				} else if (loadMatcher.matches()) {
					String rdest = loadMatcher.group("rdest");
					String rsrc1 = loadMatcher.group("rsrc1");
					String literal = loadMatcher.group("literal");
					
					list.add(new Instruction(InstructionType.LOAD, rdest, rsrc1, null, Integer.valueOf(literal)));
				} else if (storeMatcher.matches()) {
					String rsrc1 = storeMatcher.group("rsrc1");
					String rsrc2 = storeMatcher.group("rsrc2");
					String literal = storeMatcher.group("literal");

					list.add(new Instruction(InstructionType.STORE, null, rsrc1, rsrc2, Integer.valueOf(literal)));
				} else if (brMatcher.matches()) {
					String opCode = brMatcher.group("opCode");
					String literal = brMatcher.group("literal");
					
					list.add(new Instruction(strToEnum(opCode), null, null, null, Integer.valueOf(literal)));
				} else if (jumpMatcher.matches()) {
					String rsrc1 = jumpMatcher.group("rsrc1");
					String literal = jumpMatcher.group("literal");
					
					list.add(new Instruction(InstructionType.JUMP, null, rsrc1, null, Integer.valueOf(literal)));
				} else if (balMatcher.matches()) {
					String rsrc1 = balMatcher.group("rsrc1");
					String literal = balMatcher.group("literal");
					
					list.add(new Instruction(InstructionType.BAL, "X", rsrc1, null, Integer.valueOf(literal)));
				} else if (haltMatcher.matches()) {
					list.add(new Instruction(InstructionType.HALT, null, null, null, 0));
				} else {
					throw new RuntimeException("Unreconized instruction");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}
}
