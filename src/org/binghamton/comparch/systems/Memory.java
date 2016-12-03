package org.binghamton.comparch.systems;

import java.util.LinkedList;

/**
 * Simulates a memory module with a given size. The memory can only addressable
 * only on 4 byte boundaries.
 * 
 * @author Stefan Bossbaly
 * @author Gerald Brennan
 *
 */
public class Memory {
	private int[] contents;
	private int byteSize;
	private LinkedList<Integer> accessLog;

	/**
	 * Constructs a memory object
	 * 
	 * @param byteSize
	 *            the size, in bytes, of the memory container
	 */
	public Memory(int byteSize) {
		this.byteSize = byteSize;
		int size = addressToIndex(byteSize);
		this.contents = new int[size];
		this.accessLog = new LinkedList<Integer>();
	}

	/**
	 * Clears the contents of the memory. All values are set to zero.
	 */
	public void clear() {
		for (int i = 0; i < this.byteSize; i += 4) {
			this.setValue(i, 0);
		}
		
		this.accessLog.clear();
	}

	/**
	 * Sets the value at the address to the inputed value
	 * 
	 * @param address
	 *            the address of the value
	 * @param value
	 *            the new value
	 * @throws IllegalArgumentException
	 *             if address is not divisible by four
	 * @throws IllegalArgumentException
	 *             if the address is not in the valid memory range
	 */
	public void setValue(int address, int value) {
		if (address < 0) {
			throw new IllegalArgumentException("Memory address must be positive");
		} else if (address >= this.byteSize) {
			throw new IllegalArgumentException("Memory address is greater than the size");
		}

		int index = addressToIndex(address);
		this.contents[index] = value;

		if (this.accessLog.contains(address)) {
			this.accessLog.remove((Integer) address);
			this.accessLog.addLast(address);
		} else {
			if (this.accessLog.size() >= 100) {
				this.accessLog.removeFirst();
			}
			this.accessLog.addLast(address);
		}
	}

	/**
	 * Gets the value at the provided address
	 * 
	 * @param address
	 *            the memory address of the value
	 * @return the value
	 * @throws IllegalArgumentException
	 *             if address is not divisible by four
	 * @throws IllegalArgumentException
	 *             if the address is not in the valid memory range
	 */
	public int getValue(int address) {
		if (address < 0) {
			throw new IllegalArgumentException("Memory address must be positive");
		} else if (address >= this.byteSize) {
			throw new IllegalArgumentException("Memory address is greater than the size");
		}

		int index = addressToIndex(address);
		return this.contents[index];
	}

	private int addressToIndex(int address) {
		if ((address % 4) != 0) {
			throw new IllegalArgumentException("Memory address must be divisible by 4");
		}

		return (address / 4);
	}

	/**
	 * Returns string representation of provided memory range. The range is
	 * provided as [start, end).
	 * 
	 * @param start
	 *            the start value of the memory range (inclusive)
	 * @param end
	 *            the end value of the memory range (exclusive)
	 * @return the string representation of the memory range
	 */
	public String stringMemory() {
		String s = "";

		for (int i = 0; i <= this.byteSize; i += 4) {
			if (this.accessLog.contains(i)) {
				int value = this.getValue(i);
				s += String.format("%4d: %d\n", i, value);
			}
		}

		return s;
	}
}
