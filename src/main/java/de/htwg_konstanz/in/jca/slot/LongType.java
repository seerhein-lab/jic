package de.htwg_konstanz.in.jca.slot;

public class LongType extends Slot {
	private static final LongType defaultInstance = new LongType();

	public static LongType getDefaultInstance() {
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
		return object instanceof LongType;
	}

	@Override
	public String toString() {
		return "halfLong";
	}

}
