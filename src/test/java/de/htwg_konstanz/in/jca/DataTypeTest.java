package de.htwg_konstanz.in.jca;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * JUnit test for class DataType.
 */
public class DataTypeTest {

	/**
	 * Tests if getNumSlots() returns the correct integer value.
	 */
	@Test
	public void testGetNumSlots() {
		assertEquals(1, DataType.booleanType.getNumSlots());
		assertEquals(1, DataType.byteType.getNumSlots());
		assertEquals(1, DataType.charType.getNumSlots());
		assertEquals(2, DataType.doubleType.getNumSlots());
		assertEquals(1, DataType.floatType.getNumSlots());
		assertEquals(1, DataType.intType.getNumSlots());
		assertEquals(2, DataType.longType.getNumSlots());
		assertEquals(1, DataType.referenceType.getNumSlots());
		assertEquals(1, DataType.shortType.getNumSlots());
		assertEquals(0, DataType.voidType.getNumSlots());
	}

	/**
	 * Tests if getDataType(String signature) returns the correct DataTypes.
	 */
	@Test
	public void testGetDataType() {
		assertEquals(DataType.intType, DataType.getDataType("I"));
		assertEquals(DataType.longType, DataType.getDataType("J"));
		assertEquals(DataType.charType, DataType.getDataType("C"));
		assertEquals(DataType.byteType, DataType.getDataType("B"));
		assertEquals(DataType.booleanType, DataType.getDataType("Z"));
		assertEquals(DataType.shortType, DataType.getDataType("S"));
		assertEquals(DataType.floatType, DataType.getDataType("F"));
		assertEquals(DataType.doubleType, DataType.getDataType("D"));
		assertEquals(DataType.voidType, DataType.getDataType("V"));
		assertEquals(DataType.referenceType, DataType.getDataType(""));
	}

	// no need to test getDataType(Type type)

}
