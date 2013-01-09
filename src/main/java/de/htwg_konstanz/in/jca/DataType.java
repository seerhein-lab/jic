package de.htwg_konstanz.in.jca;

import org.apache.bcel.generic.Type;

public enum DataType {
	byteType(1), shortType(1), intType(1), longType(2), floatType(1), doubleType(
			2), charType(1), booleanType(1), referenceType(1), voidType(0);

	private int numSlots;

	private DataType(int numSlots) {
		this.numSlots = numSlots;
	}

	/**
	 * Returns the corresponding number of required slots for the given DataType
	 * as an integer value.
	 * 
	 * @return the number of required slots
	 */
	public int getNumSlots() {
		return numSlots;
	}

	/**
	 * Returns the corresponding DataType for a given signature.
	 * 
	 * @param signature
	 *            The signature from which the DataType should be determined.
	 * @return The corresponding DataType for the signature.
	 */
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
		if (signature.equals("V"))
			return voidType;
		return referenceType;
	}

	/**
	 * Returns the corresponding DataType for a given type.
	 * 
	 * @param type
	 *            The type from which the DataType should be determined.
	 * @return The corresponding DataType for the type.
	 */
	public static DataType getDataType(Type type) {
		return getDataType(type.getSignature());
	}

}
