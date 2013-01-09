package de.htwg_konstanz.in.jca;

import org.apache.bcel.generic.Type;

/**
 * An entry in a frame stack.
 */
public enum Slot {

	/** A byte with an arbitrary value. */
	someByte(DataType.byteType),

	/** A short with an arbitrary value. */
	someShort(DataType.shortType),

	/** An int with an arbitrary value. */
	someInt(DataType.intType),

	/** A long with an arbitrary value. */
	someHalfLong(DataType.longType),

	/** A float with an arbitrary value. */
	someFloat(DataType.floatType),

	/** A double with an arbitrary value. */
	someHalfDouble(DataType.doubleType),

	/** A character with an arbitrary value. */
	someChar(DataType.charType),

	/** A boolean with an arbitrary value. */
	someBoolean(DataType.booleanType),

	/** A reference that certainly is not the 'this' reference */
	notThisReference(DataType.referenceType),

	/** A reference that might or might not be the 'this' reference */
	maybeThisReference(DataType.referenceType),

	/** The 'this reference */
	thisReference(DataType.referenceType),

	/**
	 * special value to indicate the (absence of a) result of a void method.
	 * This value is never pushed onto the method stack.
	 */
	noSlot(DataType.voidType);

	private DataType dataType;

	private Slot(DataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * Returns the required number of slots to store all data form a given Type
	 * array as an integer value.
	 * 
	 * @param types
	 *            The Type array to estimate the number of required slots.
	 * @return The number of required slots.
	 */
	public static int numRequiredSlots(Type[] types) {
		int numRequiredSlots = 0;
		for (Type type : types) {
			numRequiredSlots += DataType.getDataType(type).getNumSlots();
		}
		return numRequiredSlots;
	}

	/**
	 * Returns the corresponding Slot-type to a given DataType.
	 * 
	 * @param dataType
	 *            The DataType to get the default Slot instance from.
	 * @return The corresponding Slot-type for dataType.
	 */
	public static Slot getDefaultInstance(DataType dataType) {
		if (dataType == null)
			throw new AssertionError("DataType is null!");
		if (dataType.equals(DataType.byteType))
			return Slot.someByte;
		if (dataType.equals(DataType.shortType))
			return Slot.someShort;
		if (dataType.equals(DataType.intType))
			return Slot.someInt;
		if (dataType.equals(DataType.longType))
			return Slot.someHalfLong;
		if (dataType.equals(DataType.floatType))
			return Slot.someFloat;
		if (dataType.equals(DataType.doubleType))
			return Slot.someHalfDouble;
		if (dataType.equals(DataType.charType))
			return Slot.someChar;
		if (dataType.equals(DataType.booleanType))
			return Slot.someBoolean;
		if (dataType.equals(DataType.voidType))
			return Slot.noSlot;

		return Slot.notThisReference;
	}

	public DataType getDataType() {
		return dataType;

	}

}
