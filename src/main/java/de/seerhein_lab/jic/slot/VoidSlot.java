package de.seerhein_lab.jic.slot;

public class VoidSlot extends Slot {
	private static final VoidSlot instance = new VoidSlot();

	private VoidSlot() {
	}

	public static VoidSlot getInstance() {
		return instance;
	}

	@Override
	public int getNumSlots() {
		return 0;
	}

	@Override
	public Slot copy() {
		return instance;
	}

	@Override
	public String toString() {
		return "void";
	}

}
