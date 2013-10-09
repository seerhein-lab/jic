package de.seerhein_lab.jic.slot;

public class IntSlot extends Slot {
	private static final IntSlot instance = new IntSlot();

	private IntSlot() {
	}

	public static IntSlot getInstance() {
		return instance;
	}

	@Override
	public int getNumSlots() {
		return 1;
	}

	@Override
	public Slot copy() {
		return instance;
	}

	@Override
	public String toString() {
		return "int";
	}

}
