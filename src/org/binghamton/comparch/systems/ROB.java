package org.binghamton.comparch.systems;

import java.util.ArrayList;

public class ROB {
	private ArrayList<ROBEntry> list;
	private int head;
	private int tail;

	public ROB(int size) {
		this.list = new ArrayList<ROBEntry>(size);
		this.head = 0;
		this.tail = 0;
	}

	public void add(ROBEntry newEntry) {
		list.set(tail, newEntry);
		tail = (tail + 1) % list.size();
	}

	public boolean isFull() {
		int normalizeTail;

		if ((tail - 1) == -1) {
			normalizeTail = list.size();
		} else {
			normalizeTail = tail - 1;
		}

		return normalizeTail == head;
	}

	public boolean canRetire() {
		ROBEntry entry = list.get(head);

		if (entry != null) {
			return entry.getStatus() && (entry.getExCodes() == 0);
		} else {
			return false;
		}
	}

	public ROBEntry retire() {
		ROBEntry entry = list.get(head);
		head = (head + 1) % list.size();
		return entry;
	}
}
