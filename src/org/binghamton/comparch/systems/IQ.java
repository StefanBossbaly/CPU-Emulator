package org.binghamton.comparch.systems;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

	public List<IQEntry> getEntries() {
		return entries;
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

	public IQEntry issue(List<InstructionType> types) {
		for (Iterator<IQEntry> itr = entries.iterator(); itr.hasNext();) {
			IQEntry entry = itr.next();
			DecodedInstruction current = entry.getInstruction();

			if (types.contains(current.getOpCode())) {
				switch (current.getOpCode().getSourceCount()) {
				case 2:
					if (entry.isSrc2Valid() && entry.isSrc1Valid()) {
						itr.remove();
						return entry;
					}
					break;
				case 1:
					if (entry.isSrc1Valid()) {
						itr.remove();
						return entry;
					}
					break;
				case 0:
					itr.remove();
					return entry;
				}
			}
		}

		throw new RuntimeException("Could not issue instruction");
	}

	public IQEntry getFirstInstance(List<InstructionType> types) {
		for (Iterator<IQEntry> itr = entries.iterator(); itr.hasNext();) {
			IQEntry entry = itr.next();
			DecodedInstruction current = entry.getInstruction();

			if (types.contains(current.getOpCode())) {
				return entry;
			}
		}

		return null;
	}

	public IQEntry issueInOrder(List<InstructionType> types) {
		for (Iterator<IQEntry> itr = entries.iterator(); itr.hasNext();) {
			IQEntry entry = itr.next();
			DecodedInstruction current = entry.getInstruction();

			if (types.contains(current.getOpCode())) {
				itr.remove();
				return entry;
			}
		}

		throw new RuntimeException("Could not issue instruction");
	}

	public boolean canIssue(List<InstructionType> types) {
		for (IQEntry entry : entries) {
			DecodedInstruction current = entry.getInstruction();

			/* Ensure the current instruction is it */
			if (types.contains(current.getOpCode())) {
				switch (current.getOpCode().getSourceCount()) {
				case 2:
					if (entry.isSrc2Valid() && entry.isSrc1Valid()) {
						return true;
					}
					break;
				case 1:
					if (entry.isSrc1Valid()) {
						return true;
					}
					break;
				case 0:
					return true;
				}
			}
		}

		return false;
	}

	public boolean contains(List<InstructionType> types) {
		for (IQEntry entry : entries) {
			DecodedInstruction current = entry.getInstruction();

			if (types.contains(current.getOpCode())) {
				return true;
			}
		}

		return false;
	}

	public boolean canIssueInOrder(List<InstructionType> types) {
		for (IQEntry entry : entries) {
			DecodedInstruction current = entry.getInstruction();

			/* Ensure the current instruction is it */
			if (types.contains(current.getOpCode())) {
				switch (current.getOpCode().getSourceCount()) {
				case 2:
					if (entry.isSrc2Valid() && entry.isSrc1Valid()) {
						return true;
					} else {
						return false;
					}
				case 1:
					if (entry.isSrc1Valid()) {
						return true;
					} else {
						return false;
					}
				case 0:
					return true;
				}
			}
		}

		return false;
	}

	public void forwardData(DecodedInstruction inst, int value) {
		for (IQEntry iqEntry : entries) {
			DecodedInstruction entryInst = iqEntry.getInstruction();

			if (entryInst.isRsrc2FlowDependant(inst)) {
				iqEntry.setSrc2Value(value);
				iqEntry.setSrc2Valid(true);
			}

			if (entryInst.isRsrc1FlowDependant(inst)) {
				iqEntry.setSrc1Value(value);
				iqEntry.setSrc1Valid(true);
			}
		}
	}

	public void updateEntries() {
		for (IQEntry iqEntry : entries) {
			DecodedInstruction current = iqEntry.getInstruction();

			/* Check for updated physical registers */
			if (current.getOpCode().getSourceCount() > 1) {
				if (!iqEntry.isSrc2Valid()) {
					if (current.getRsrc2().isValid()) {
						iqEntry.setSrc2Value(current.getRsrc2().getValue());
						iqEntry.setSrc2Valid(true);
					}
				}
			}

			/* Check for updated physical registers */
			if (current.getOpCode().getSourceCount() >= 1) {
				if (!iqEntry.isSrc1Valid()) {
					if (current.getRsrc1().isValid()) {
						iqEntry.setSrc1Value(current.getRsrc1().getValue());
						iqEntry.setSrc1Valid(true);
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		String str = "";

		if (isEmpty()) {
			str = "Empty\n";
		} else {
			for (Iterator<IQEntry> itr = entries.iterator(); itr.hasNext();) {
				IQEntry entry = itr.next();
				str += entry.toString() + "\n";
			}

		}

		return str;
	}
}
