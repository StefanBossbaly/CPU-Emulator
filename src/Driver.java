import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.binghamton.comparch.systems.Processor;
import org.binghamton.comparch.util.InstructionParser;

public class Driver {
	private static final Pattern SIMULATE_PATTERN = Pattern.compile("Simulate (\\d+)");
	private static final Pattern INITIALIZE_PATTERN = Pattern.compile("Initialize (.+)");
	private static final Pattern MEMORY_PATTERN = Pattern.compile("Print_Memory (?<start>\\d+) (?<end>\\d+)");
	private static final Pattern URF_SIZE_PATTERN = Pattern.compile("Set_URF_size (?<size>\\d+)");

	public static void main(String args[]) {
		Processor p = new Processor();
		Scanner reader = new Scanner(System.in);

		while (true) {
			System.out.print("> ");
			String input = reader.nextLine();
			Matcher simMatcher = SIMULATE_PATTERN.matcher(input);
			Matcher initMatcher = INITIALIZE_PATTERN.matcher(input);
			Matcher memoryMatcher = MEMORY_PATTERN.matcher(input);
			Matcher urfSizeMatcher = URF_SIZE_PATTERN.matcher(input);

			if (simMatcher.matches()) {
				int cycles = Integer.valueOf(simMatcher.group(1));
				boolean halt = p.cycle(cycles);
				
				if (halt) {
					System.out.println("HALT instruction encountered. Simulation halted");
				} else {
					System.out.println("Simulation ran for " + cycles + " cycles");
				}
			} else if (urfSizeMatcher.matches()) {
				int size = Integer.valueOf(urfSizeMatcher.group("size"));
				p.setURFSize(size);
				System.out.println("URF size updated to " + size + ". Please reintialize processor!");
			} else if ("Display".equals(input)) {
				System.out.println(p.toString());
			} else if ("Print_map_tables".equals(input)) {
				System.out.println("- RAT");
				System.out.println(p.getURF().stringRAT());
				System.out.println("- RRAT");
				System.out.println(p.getURF().stringRRAT());
			} else if ("Print_IQ".equals(input)) {
				System.out.println(p.getIQ().toString());
			} else if ("Print_ROB".equals(input)) {
				System.out.println(p.getROB().toString());
			} else if ("Print_URF".equals(input)) {
				System.out.println(p.getURF().stringRegisters());
			} else if (memoryMatcher.matches()) {
				int start = Integer.valueOf(memoryMatcher.group("start"));
				int end = Integer.valueOf(memoryMatcher.group("end"));
				
				System.out.println(p.getMemory().stringMemory(start, end));
			} else if ("Print_Stats".equals(input)) {
				double ipc = ((double) p.getInstructionsCommited() / (double) p.getTotalCycles());
				System.out.println("IPC: " + String.valueOf(ipc));
				System.out.println("Dispatched Stalled Cycles: " + p.getDispatchedStalledCycles());
				System.out.println("No Issue Cycles: " + p.getNoIssuesCycles());
				System.out.println("LOADs Committed: " + p.getLoadsCommitted());
				System.out.println("STOREs Committed: " + p.getStoresCommitted());
			} else if (initMatcher.matches()) {
				InstructionParser parser = new InstructionParser(initMatcher.group(1));
				p.initialize(parser.parserFile());
				System.out.println("Processor Initialized");
			} else {
				System.out.println("Unrecognized Command");
			}
		}
	}
}
