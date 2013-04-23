package de.htwg_konstanz.in.jca.slot;

import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.generic.Type;

public abstract class Slot {
	private final static Map<Type, Slot> map = new HashMap<Type, Slot>();
	{
		map.put(Type.INT, IntSlot.getInstance());
		map.put(Type.LONG, LongSlot.getInstance());
		map.put(Type.CHAR, CharSlot.getInstance());
		map.put(Type.BYTE, ByteSlot.getInstance());
		map.put(Type.BOOLEAN, BooleanSlot.getInstance());
		map.put(Type.SHORT, ShortSlot.getInstance());
		map.put(Type.FLOAT, FloatSlot.getInstance());
		map.put(Type.DOUBLE, DoubleSlot.getInstance());
		map.put(Type.VOID, VoidSlot.getInstance());
	}

	public abstract int getNumSlots();

	public abstract Slot copy();

	public static Slot getDefaultSlotInstance(Type type) {
		return (map.containsKey(type)) ? map.get(type) : new ReferenceSlot();
	}
}
