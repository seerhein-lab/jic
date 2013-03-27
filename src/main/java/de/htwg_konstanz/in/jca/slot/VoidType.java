package de.htwg_konstanz.in.jca.slot;

public class VoidType extends Slot {
	private static final VoidType defaultInstance = new VoidType();

	public static VoidType getDefaultInstance() {
		return defaultInstance;
	}

	@Override
	public int getNumSlots() {
		return 0;
	}

	@Override
	public Slot getCopy() {
		return defaultInstance;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof VoidType;
	}

	@Override
	public String toString() {
		return "void";
	}

}
