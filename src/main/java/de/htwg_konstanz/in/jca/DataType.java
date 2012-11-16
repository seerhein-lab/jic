package de.htwg_konstanz.in.jca;

import org.apache.bcel.generic.Type;

public enum DataType {
	byteType(1, Slot.someByte), shortType(1, Slot.someShort), intType(1,
			Slot.someInt), longType(2, Slot.someHalfLong), floatType(1,
			Slot.someFloat), doubleType(2, Slot.someHalfDouble), charType(1,
			Slot.someChar), booleanType(1, Slot.someBoolean), referenceType(1,
			Slot.notThisReference);

	private int numSlots;
	private Slot instance;

	private DataType(int numSlots, Slot instance) {
		this.numSlots = numSlots;
		this.instance = instance;
	}

	public int getNumSlots() {
		return numSlots;
	}

	public Slot getInstance() {
		return instance;
	}

	public static DataType getDataType(String signature) {
		if (signature.equals("I"))
			return intType;
		if (signature.equals("J"))
			return DataType.longType;
		if (signature.equals("C"))
			return charType;
		if (signature.equals("B"))
			return byteType;
		if (signature.equals("Z"))
			return booleanType;
		if (signature.equals("S"))
			return shortType;
		if (signature.equals("F"))
			return floatType;
		if (signature.equals("D"))
			return doubleType;
		return referenceType;
	}

	public static DataType getDataType(Type type) {
		return getDataType(type.getSignature());
	}

}
