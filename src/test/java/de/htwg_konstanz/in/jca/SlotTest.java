package de.htwg_konstanz.in.jca;

import static org.junit.Assert.assertEquals;

import org.apache.bcel.generic.Type;
import org.junit.Test;

/**
 * JUnit test for SlotOld class. Does not test simple getters and setters.
 */
public class SlotTest {

	/**
	 * Tests numRequiredSlots with null. A NullPointerException is expected.
	 */
	@Test(expected = NullPointerException.class)
	public void testNumRequiredSlotsTypesNull() {
		SlotOld.numRequiredSlots(null);
	}

	/**
	 * Tests numRequiredSlots with a zero length type array.
	 */
	@Test
	public void testNumRequiredSlotsEmptyTypes() {
		Type[] types = new Type[0];
		assertEquals(0, SlotOld.numRequiredSlots(types));
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

		assertEquals(12, SlotOld.numRequiredSlots(types));
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

		assertEquals(12, SlotOld.numRequiredSlots(types));
	}

	/**
	 * Tests getDefaultInstance with null. An AssertionError is expected.
	 */
	@Test(expected = AssertionError.class)
	public final void testGetDefaultInstanceError() {
		SlotOld.getDefaultInstance(null);
	}

	/**
	 * Tests getDefaultInstance for all DataTypes.
	 */
	@Test
	public final void testGetDefaultInstance() {
		assertEquals(SlotOld.someBoolean,
				SlotOld.getDefaultInstance(DataType.booleanType));
		assertEquals(SlotOld.someByte, SlotOld.getDefaultInstance(DataType.byteType));
		assertEquals(SlotOld.someChar, SlotOld.getDefaultInstance(DataType.charType));
		assertEquals(SlotOld.someHalfDouble,
				SlotOld.getDefaultInstance(DataType.doubleType));
		assertEquals(SlotOld.someFloat,
				SlotOld.getDefaultInstance(DataType.floatType));
		assertEquals(SlotOld.someInt, SlotOld.getDefaultInstance(DataType.intType));
		assertEquals(SlotOld.someHalfLong,
				SlotOld.getDefaultInstance(DataType.longType));
		assertEquals(SlotOld.notThisReference,
				SlotOld.getDefaultInstance(DataType.referenceType));
		assertEquals(SlotOld.someShort,
				SlotOld.getDefaultInstance(DataType.shortType));
		assertEquals(SlotOld.noSlot, SlotOld.getDefaultInstance(DataType.voidType));
	}

	/**
	 * Tests if getDataType returns the correct DataType for a given SlotOld.
	 */
	@Test
	public final void testGetDataType() {
		assertEquals(DataType.referenceType,
				SlotOld.maybeThisReference.getDataType());
		assertEquals(DataType.voidType, SlotOld.noSlot.getDataType());
		assertEquals(DataType.referenceType,
				SlotOld.notThisReference.getDataType());
		assertEquals(DataType.booleanType, SlotOld.someBoolean.getDataType());
		assertEquals(DataType.byteType, SlotOld.someByte.getDataType());
		assertEquals(DataType.charType, SlotOld.someChar.getDataType());
		assertEquals(DataType.floatType, SlotOld.someFloat.getDataType());
		assertEquals(DataType.doubleType, SlotOld.someHalfDouble.getDataType());
		assertEquals(DataType.longType, SlotOld.someHalfLong.getDataType());
		assertEquals(DataType.intType, SlotOld.someInt.getDataType());
		assertEquals(DataType.shortType, SlotOld.someShort.getDataType());
		assertEquals(DataType.referenceType, SlotOld.thisReference.getDataType());
	}

}
