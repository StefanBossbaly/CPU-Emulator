package org.binghamton.comparch.systems;

public class URF {
	/* Mapping from architectural register to physical register */
	private int renameArray[];

	/* List of physical registers */
	private Register physicalRegisters[];

	/* Weather a physical register is allocated or not */
	private boolean allocationList[];

	public URF(int architecturalSize, int physicalSize) {
		/* Allocate the physical registers */
		physicalRegisters = new Register[physicalSize];
		for (int i = 0; i < physicalRegisters.length; i += 1) {
			physicalRegisters[i] = new Register("P" + String.valueOf(i));
		}

		/* Allocate and initialize the rename array */
		renameArray = new int[architecturalSize];
		for (int i = 0; i < renameArray.length; i += 1) {
			renameArray[i] = -1;
		}

		/* Initialize the allocation list */
		allocationList = new boolean[physicalSize];
		for (int i = 0; i < allocationList.length; i += 1) {
			allocationList[i] = false;
		}
	}

	public void clear() {
		/* Clear all of the physical registers */
		for (int i = 0; i < physicalRegisters.length; i += 1) {
			physicalRegisters[i].setValue(0);
		}

		/* Initialize the rename array */
		for (int i = 0; i < renameArray.length; i += 1) {
			renameArray[i] = -1;
		}

		/* Initialize the allocation list */
		for (int i = 0; i < allocationList.length; i += 1) {
			allocationList[i] = false;
		}
	}
	
	public void setPhysicalRegisterSize(int newSize) {
		/* Allocate the physical registers */
		physicalRegisters = new Register[newSize];
		for (int i = 0; i < physicalRegisters.length; i += 1) {
			physicalRegisters[i] = new Register("P" + String.valueOf(i));
		}
		
		/* Initialize the allocation list */
		allocationList = new boolean[newSize];
		for (int i = 0; i < allocationList.length; i += 1) {
			allocationList[i] = false;
		}
		
		/* Clear */
		clear();
	}

	private int getFreePhysicalRegister() {
		for (int i = 0; i < allocationList.length; i += 1) {
			if (allocationList[i] == false) {
				return i;
			}
		}

		return -1;
	}

	public boolean hasPhysicalRegisterAvailable() {
		return (getFreePhysicalRegister() != -1);
	}

	public Register allocatePhysicalRegister(int architecturalRegister) {
		int instance = getFreePhysicalRegister();

		if (instance == -1) {
			throw new RuntimeException("No physical register aviable");
		}

		/*
		 * Mark the physical register as allocated and update the rename table
		 */
		allocationList[instance] = true;
		renameArray[architecturalRegister] = instance;

		/* Return the instance of the physical register */
		return physicalRegisters[instance];
	}

	public Register getRenamedRegister(int architecturalRegister) {
		return physicalRegisters[renameArray[architecturalRegister]];
	}

}
