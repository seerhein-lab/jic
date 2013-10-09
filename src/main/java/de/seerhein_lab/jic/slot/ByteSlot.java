package de.seerhein_lab.jic.slot;

public class ByteSlot extends Slot {
	private final static ByteSlot instance = new ByteSlot();

	private ByteSlot() {
	}

	public static ByteSlot getInstance() {
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
		return "byte";
	}
}
