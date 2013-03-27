package de.htwg_konstanz.in.jca.slot;

public class CharType extends Slot {
	private final static CharType defaultInstance = new CharType();

	public static CharType getDefaultInstance() {
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
		return object instanceof CharType;
	}

	@Override
	public String toString() {
		return "char";
	}
}
