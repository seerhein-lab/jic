package de.htwg_konstanz.in.jca.slot;

public class FloatType extends Slot {
	private static final FloatType defaultInstance = new FloatType();

	public static FloatType getDefaultInstance() {
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
		return object instanceof FloatType;
	}

	@Override
	public String toString() {
		return "float";
	}

}
