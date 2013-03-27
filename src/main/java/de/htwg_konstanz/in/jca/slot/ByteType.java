package de.htwg_konstanz.in.jca.slot;

public class ByteType extends Slot {
	private final static ByteType defaultInstance = new ByteType();

	public static ByteType getDefaultInstance() {
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
		return object instanceof ByteType;
	}

	@Override
	public String toString() {
		return "byte";
	}
}
