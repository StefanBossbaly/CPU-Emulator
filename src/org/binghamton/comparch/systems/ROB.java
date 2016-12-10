package org.binghamton.comparch.systems;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ROB {
	private LinkedList<ROBEntry> list;
	private int capacity;

	public ROB(int capacity) {
		this.list = new LinkedList<ROBEntry>();
		this.capacity = capacity;
	}

	public void add(ROBEntry newEntry) {
		list.addLast(newEntry);
	}

	public boolean isFull() {
		return list.size() >= capacity;
	}

	public boolean canRetire() {
		if (list.isEmpty()) {
			return false;
		}

		ROBEntry entry = list.getFirst();

		if (entry != null) {
			return entry.getStatus();
		} else {
			return false;
		}
	}

	public List<ROBEntry> rollback() {
		LinkedList<ROBEntry> rollbacked = new LinkedList<ROBEntry>();

		for (Iterator<ROBEntry> itr = list.descendingIterator(); itr.hasNext();) {
			ROBEntry entry = itr.next();
			rollbacked.addLast(entry);
			itr.remove();
		}

		return rollbacked;
	}

	public Register getLatestDestReg(List<InstructionType> types) {
		for (Iterator<ROBEntry> itr = list.descendingIterator(); itr.hasNext();) {
			ROBEntry entry = itr.next();
			DecodedInstruction instruction = entry.getInstruction();

			if (types.contains(instruction.getOpCode()) && entry.getDestRegister() != null) {
				return entry.getDestRegister();
			}
		}

		throw new RuntimeException("No destination register!");

	}

	public void removeAll(Collection<ROBEntry> entries) {
		list.removeAll(entries);
	}

	public ROBEntry retire() {
		return list.removeFirst();
	}

	@Override
	public String toString() {
		String str = "";

		if (list.isEmpty()) {
			str += "Empty\n";
		} else {
			for (ROBEntry entry : list) {
				str += entry.toString() + "\n";
			}
		}

		return str;
	}
}
