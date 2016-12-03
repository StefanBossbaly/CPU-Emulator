import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.binghamton.comparch.systems.Processor;
import org.binghamton.comparch.util.InstructionParser;

public class Driver {
	private static final Pattern SIMULATE_PATTERN = Pattern.compile("Simulate (\\d+)");
	private static final Pattern INITIALIZE_PATTERN = Pattern.compile("Initialize (.+)");

	public static void main(String args[]) {
		Processor p = new Processor();
		Scanner reader = new Scanner(System.in);

		while (true) {
			System.out.print("> ");
			String input = reader.nextLine();
			Matcher simMatcher = SIMULATE_PATTERN.matcher(input);
			Matcher initMatcher = INITIALIZE_PATTERN.matcher(input);

			if (simMatcher.matches()) {
				int cycles = Integer.valueOf(simMatcher.group(1));
				boolean halt = p.cycle(cycles);
				
				if (halt) {
					System.out.println("HALT instruction encountered. Simulation halted");
				} else {
					System.out.println("Simulation ran for " + cycles + " cycles");
				}
			} else if ("Display".equals(input)) {
				System.out.println(p.toString());
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
