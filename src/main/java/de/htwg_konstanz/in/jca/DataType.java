package de.htwg_konstanz.in.jca;

import org.apache.bcel.generic.Type;

public enum DataType {
	byteType(1), shortType(1), intType(1), longType(2), floatType(1), doubleType(
			2), charType(1), booleanType(1), referenceType(1);

	private int numSlots;

	private DataType(int numSlots) {
		this.numSlots = numSlots;
	}

	public int getNumSlots() {
		return numSlots;
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
