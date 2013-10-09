package de.seerhein_lab.jic.slot;

public class CharSlot extends Slot {
	private final static CharSlot instance = new CharSlot();

	private CharSlot() {
	}

	public static CharSlot getInstance() {
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
		return "char";
	}
}
