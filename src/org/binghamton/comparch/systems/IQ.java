package org.binghamton.comparch.systems;

import java.util.Iterator;
import java.util.LinkedList;

public class IQ {
	private LinkedList<IQEntry> entries;
	private int capacity;

	public IQ(int capacity) {
		this.entries = new LinkedList<IQEntry>();
		this.capacity = capacity;
	}

	public void enqueue(IQEntry entry) {
		if (entries.size() >= capacity) {
			throw new RuntimeException("IQ is full, can not enqueue new instruction");
		} else {
			entries.addLast(entry);
		}
	}

	public IQEntry dequeue() {
		if (this.isEmpty()) {
			throw new RuntimeException("Can not dequeue an empty IQ");
		}

		return entries.removeFirst();
	}

	public IQEntry peek() {
		return entries.peek();
	}

	public boolean isFull() {
		return entries.size() == capacity;
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public void clear() {
		entries.clear();
	}

	@Override
	public String toString() {
		String str = "";
		for (Iterator<IQEntry> itr = entries.iterator(); itr.hasNext();) {
			IQEntry entry = itr.next();
			str += entry.toString() + "\n";
		}

		return str;
	}
}
