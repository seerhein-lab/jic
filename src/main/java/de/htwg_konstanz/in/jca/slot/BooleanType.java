package de.htwg_konstanz.in.jca.slot;

public class BooleanType extends Slot {

	private final static BooleanType defaultInstance = new BooleanType();

	public static BooleanType getDefaultInstance() {
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
		return object instanceof BooleanType;
	}

	@Override
	public String toString() {
		return "boolean";
	}
}
