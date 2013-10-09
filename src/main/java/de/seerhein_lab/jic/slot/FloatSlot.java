package de.seerhein_lab.jic.slot;

public class FloatSlot extends Slot {
	private static final FloatSlot instance = new FloatSlot();

	private FloatSlot() {
	}

	public static FloatSlot getInstance() {
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
		return "float";
	}

}
