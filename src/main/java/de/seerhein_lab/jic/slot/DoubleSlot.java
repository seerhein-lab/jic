package de.seerhein_lab.jic.slot;

public class DoubleSlot extends Slot {
	private static final DoubleSlot instance = new DoubleSlot();

	private DoubleSlot() {
	}

	public static DoubleSlot getInstance() {
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
		return "halfDouble";
	}
}
