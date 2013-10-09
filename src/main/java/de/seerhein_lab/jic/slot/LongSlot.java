package de.seerhein_lab.jic.slot;

public class LongSlot extends Slot {
	private static final LongSlot instance = new LongSlot();

	private LongSlot() {
	}

	public static LongSlot getInstance() {
		return instance;
	}

	@Override
	public int getNumSlots() {
		return 2;
	}

	@Override
	public Slot copy() {
		return instance;
	}

	@Override
	public String toString() {
		return "halfLong";
	}

}
