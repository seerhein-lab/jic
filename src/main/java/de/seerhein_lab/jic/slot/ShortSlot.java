package de.seerhein_lab.jic.slot;

public class ShortSlot extends Slot {
	private static final ShortSlot instance = new ShortSlot();

	private ShortSlot() {
	}

	public static ShortSlot getInstance() {
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
		return "short";
	}

}
