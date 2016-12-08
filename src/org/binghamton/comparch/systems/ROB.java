package org.binghamton.comparch.systems;

import java.util.LinkedList;

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
			return entry.getStatus() && (entry.getExCodes() == 0);
		} else {
			return false;
		}
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
