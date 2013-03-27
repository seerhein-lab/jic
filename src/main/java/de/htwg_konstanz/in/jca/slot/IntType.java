package de.htwg_konstanz.in.jca.slot;

public class IntType extends Slot {
	private static final IntType defaultInstance = new IntType();

	public static IntType getDefaultInstance() {
		return defaultInstance;
	}

	@Override
	public int getNumSlots() {
		return 1;
	}

	@Override
	public Slot getCopy() {
		return defaultInstance;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof IntType;
	}

	@Override
	public String toString() {
		return "int";
	}

}
