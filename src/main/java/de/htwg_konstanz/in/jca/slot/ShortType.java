package de.htwg_konstanz.in.jca.slot;

public class ShortType extends Slot {
	private static final ShortType defaultInstance = new ShortType();

	public static ShortType getDefaultInstance() {
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
		return object instanceof ShortType;
	}

	@Override
	public String toString() {
		return "short";
	}

}
