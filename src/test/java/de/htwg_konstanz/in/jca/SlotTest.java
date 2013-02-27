package de.htwg_konstanz.in.jca;

import static org.junit.Assert.assertEquals;

import org.apache.bcel.generic.Type;
import org.junit.Test;

/**
 * JUnit test for Slot class. Does not test simple getters and setters.
 */
public class SlotTest {

	/**
	 * Tests numRequiredSlots with null. A NullPointerException is expected.
	 */
	@Test(expected = NullPointerException.class)
	public void testNumRequiredSlotsTypesNull() {
		Slot.numRequiredSlots(null);
	}

	/**
	 * Tests numRequiredSlots with a zero length type array.
	 */
	@Test
	public void testNumRequiredSlotsEmptyTypes() {
		Type[] types = new Type[0];
		assertEquals(0, Slot.numRequiredSlots(types));
	}

	/**
	 * Tests numRequiredSlots with a type array including all Types except VOID.
	 */
	@Test
	public void testNumRequiredSlotsAllTypesWOVoid() {
		Type[] types = new Type[10];
		types[0] = Type.BOOLEAN;
		types[1] = Type.BYTE;
		types[2] = Type.CHAR;
		types[3] = Type.DOUBLE;
		types[4] = Type.FLOAT;
		types[5] = Type.INT;
		types[6] = Type.LONG;
		types[7] = Type.OBJECT;
		types[8] = Type.SHORT;
		types[9] = Type.STRING;

		assertEquals(12, Slot.numRequiredSlots(types));
	}

	/**
	 * Tests numRequiredSlots with a type array including all Types.
	 */
	@Test
	public void testNumRequiredSlotsAllTypes() {
		Type[] types = new Type[11];
		types[0] = Type.BOOLEAN;
		types[1] = Type.BYTE;
		types[2] = Type.CHAR;
		types[3] = Type.DOUBLE;
		types[4] = Type.VOID;
		types[5] = Type.INT;
		types[6] = Type.LONG;
		types[7] = Type.OBJECT;
		types[8] = Type.SHORT;
		types[9] = Type.STRING;
		types[10] = Type.FLOAT;

		assertEquals(12, Slot.numRequiredSlots(types));
	}

	/**
	 * Tests getDefaultInstance with null. An AssertionError is expected.
	 */
	@Test(expected = AssertionError.class)
	public final void testGetDefaultInstanceError() {
		Slot.getDefaultInstance(null);
	}

	/**
	 * Tests getDefaultInstance for all DataTypes.
	 */
	@Test
	public final void testGetDefaultInstance() {
		assertEquals(Slot.someBoolean,
				Slot.getDefaultInstance(DataType.booleanType));
		assertEquals(Slot.someByte, Slot.getDefaultInstance(DataType.byteType));
		assertEquals(Slot.someChar, Slot.getDefaultInstance(DataType.charType));
		assertEquals(Slot.someHalfDouble,
				Slot.getDefaultInstance(DataType.doubleType));
		assertEquals(Slot.someFloat,
				Slot.getDefaultInstance(DataType.floatType));
		assertEquals(Slot.someInt, Slot.getDefaultInstance(DataType.intType));
		assertEquals(Slot.someHalfLong,
				Slot.getDefaultInstance(DataType.longType));
		assertEquals(Slot.notThisReference,
				Slot.getDefaultInstance(DataType.referenceType));
		assertEquals(Slot.someShort,
				Slot.getDefaultInstance(DataType.shortType));
		assertEquals(Slot.noSlot, Slot.getDefaultInstance(DataType.voidType));
	}

	/**
	 * Tests if getDataType returns the correct DataType for a given Slot.
	 */
	@Test
	public final void testGetDataType() {
		assertEquals(DataType.referenceType,
				Slot.maybeThisReference.getDataType());
		assertEquals(DataType.voidType, Slot.noSlot.getDataType());
		assertEquals(DataType.referenceType,
				Slot.notThisReference.getDataType());
		assertEquals(DataType.booleanType, Slot.someBoolean.getDataType());
		assertEquals(DataType.byteType, Slot.someByte.getDataType());
		assertEquals(DataType.charType, Slot.someChar.getDataType());
		assertEquals(DataType.floatType, Slot.someFloat.getDataType());
		assertEquals(DataType.doubleType, Slot.someHalfDouble.getDataType());
		assertEquals(DataType.longType, Slot.someHalfLong.getDataType());
		assertEquals(DataType.intType, Slot.someInt.getDataType());
		assertEquals(DataType.shortType, Slot.someShort.getDataType());
		assertEquals(DataType.referenceType, Slot.thisReference.getDataType());
	}

}
