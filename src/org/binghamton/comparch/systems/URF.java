package org.binghamton.comparch.systems;

public class URF {
	/* Mapping from architectural register to physical register */
	private int renameArray[];

	/* Mapping of committed registers */
	private int retirementArray[];

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
		renameArray = new int[architecturalSize + 1];
		for (int i = 0; i < renameArray.length; i += 1) {
			renameArray[i] = -1;
		}

		/* Allocate and initialize the retirement array */
		retirementArray = new int[architecturalSize + 1];
		for (int i = 0; i < retirementArray.length; i += 1) {
			retirementArray[i] = -1;
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

		/* Initialize the retirement array */
		for (int i = 0; i < retirementArray.length; i += 1) {
			retirementArray[i] = -1;
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

	private int findPhysicalRegister(Register phyRegister) {
		for (int i = 0; i < physicalRegisters.length; i += 1) {
			if (physicalRegisters[i].equals(phyRegister)) {
				return i;
			}
		}

		return -1;
	}

	private int getRATMapping(Register phyRegister) {
		for (int i = 0; i < renameArray.length; i += 1) {
			int mapping = renameArray[i];

			if (mapping != -1 && phyRegister.equals(physicalRegisters[mapping])) {
				return i;
			}
		}

		return -1;
	}

	private int getRRATMapping(Register phyRegister) {
		for (int i = 0; i < retirementArray.length; i += 1) {
			int mapping = retirementArray[i];

			if (mapping != -1 && phyRegister.equals(physicalRegisters[mapping])) {
				return i;
			}
		}

		return -1;
	}

	public boolean hasPhysicalRegisterAvailable() {
		return (getFreePhysicalRegister() != -1);
	}

	public Register allocatePhysicalRegister() {
		int instance = getFreePhysicalRegister();

		if (instance == -1) {
			throw new RuntimeException("No physical register aviable");
		}

		/* Return the instance of the physical register */
		return physicalRegisters[instance];
	}

	public void deallocatePhysicalRegister(Register physicalRegister) {
		int instance = findPhysicalRegister(physicalRegister);

		if (instance == -1) {
			throw new RuntimeException("Invalid physical register");
		}

		allocationList[instance] = false;
		physicalRegister.setValid(false);
	}

	public void updateMapping(int architecturalRegister, Register physicalRegister) {
		int instance = findPhysicalRegister(physicalRegister);

		if (instance == -1) {
			throw new RuntimeException("Invalid physical register");
		}

		allocationList[instance] = true;
		renameArray[architecturalRegister] = instance;
	}

	public void commitRegister(int architecturalRegister, Register physicalRegister) {
		int phyInstance = findPhysicalRegister(physicalRegister);

		if (phyInstance == -1) {
			throw new RuntimeException("Invalid physical register");
		}

		/* Check to see if we deallocate old register */
		if (retirementArray[architecturalRegister] != -1) {
			allocationList[retirementArray[architecturalRegister]] = false;
			physicalRegisters[retirementArray[architecturalRegister]].setValid(false);
		}

		/* Commit the new entry */
		retirementArray[architecturalRegister] = phyInstance;
		physicalRegister.setValid(true);
	}

	public void rollback() {
		renameArray = retirementArray.clone();
	}

	public Register getRenamedRegister(int architecturalRegister) {
		return physicalRegisters[renameArray[architecturalRegister]];
	}

	public String stringRAT() {
		String str = "";

		for (int i = 0; i < renameArray.length - 1; i += 1) {
			str += String.format("%2d: %2d\n", i, renameArray[i]);
		}
		str += String.format(" X: %2d\n", renameArray[renameArray.length - 1]);

		return str;
	}

	public String stringRRAT() {
		String str = "";

		for (int i = 0; i < retirementArray.length - 1; i += 1) {
			str += String.format("%2d: %2d\n", i, retirementArray[i]);
		}
		str += String.format(" X: %2d\n", retirementArray[retirementArray.length - 1]);

		return str;
	}

	public String stringRegisters() {
		String str = "";

		for (int i = 0; i < physicalRegisters.length; i += 1) {
			String status;

			if (allocationList[i] == true) {
				if (getRRATMapping(physicalRegisters[i]) != -1) {
					status = "commited";
				} else {
					status = "allocated";
				}
			} else {
				status = "free";
			}

			str += String.format("%4s: %4d %s\n", physicalRegisters[i].getName(), physicalRegisters[i].getValue(),
					status);
		}
		return str;
	}

}
