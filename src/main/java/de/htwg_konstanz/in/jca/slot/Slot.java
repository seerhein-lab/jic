package de.htwg_konstanz.in.jca.slot;

import org.apache.bcel.generic.Type;

import de.htwg_konstanz.in.jca.Utils;

public abstract class Slot {
	public abstract int getNumSlots();

	public abstract Slot copy();

	public static int numRequiredSlots(Type[] types) {
		int numRequiredSlots = 0;
		for (Type type : types) {
			numRequiredSlots += Utils.getDefaultSlotInstance(type).getNumSlots();
		}
		return numRequiredSlots;
	}

}
