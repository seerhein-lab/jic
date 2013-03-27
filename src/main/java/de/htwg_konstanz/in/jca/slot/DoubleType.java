package de.htwg_konstanz.in.jca.slot;

public class DoubleType extends Slot {
	private static final DoubleType defaultInstance = new DoubleType();

	public static DoubleType getDefaultInstance() {
		return defaultInstance;
	}

	@Override
	public int getNumSlots() {
		return 2;
	}

	@Override
	public Slot getCopy() {
		return defaultInstance;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof DoubleType;
	}

	@Override
	public String toString() {
		return "halfDouble";
	}
}
