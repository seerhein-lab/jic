package de.seerhein_lab.jic.slot;

public class BooleanSlot extends Slot {
	private final static BooleanSlot instance = new BooleanSlot();

	private BooleanSlot() {
	}

	public static BooleanSlot getInstance() {
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
		return "boolean";
	}
}
