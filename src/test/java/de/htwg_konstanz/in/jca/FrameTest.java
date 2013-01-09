package de.htwg_konstanz.in.jca;

import static org.junit.Assert.assertEquals;

import java.util.EmptyStackException;
import java.util.Stack;

import org.junit.Test;

/**
 * JUnit test for Frame class. Does not test simple getters and setters.
 */
public class FrameTest {

	/**
	 * Tests constructor with negative maxLocals. Expects
	 * NegativeArraySizeException.
	 */
	@Test(expected = NegativeArraySizeException.class)
	public void testFrameIntStackOfSlotInt0() {
		Stack<Slot> callerStack = new Stack<Slot>();
		callerStack.push(Slot.thisReference);
		callerStack.push(Slot.notThisReference);
		callerStack.push(Slot.someBoolean);
		callerStack.push(Slot.someByte);
		callerStack.push(Slot.someChar);
		callerStack.push(Slot.someFloat);
		callerStack.push(Slot.someHalfDouble);
		callerStack.push(Slot.someHalfLong);
		callerStack.push(Slot.someInt);
		callerStack.push(Slot.someShort);
		callerStack.push(Slot.maybeThisReference);
		callerStack.push(Slot.noSlot);

		@SuppressWarnings("unused")
		Frame frame = new Frame(-1, callerStack, 12);
	}

	/**
	 * Tests constructor with callerStack = null. Expects NullPointerException.
	 */
	@Test(expected = NullPointerException.class)
	public void testFrameIntStackOfSlotInt1() {

		@SuppressWarnings("unused")
		Frame frame = new Frame(12, null, 12);
	}

	/**
	 * Tests constructor with all Slot types and complete stack popped.
	 */
	@Test
	public void testFrameIntStackOfSlotInt2() {
		Stack<Slot> callerStack = new Stack<Slot>();
		callerStack.push(Slot.thisReference);
		callerStack.push(Slot.notThisReference);
		callerStack.push(Slot.someBoolean);
		callerStack.push(Slot.someByte);
		callerStack.push(Slot.someChar);
		callerStack.push(Slot.someFloat);
		callerStack.push(Slot.someHalfDouble);
		callerStack.push(Slot.someHalfLong);
		callerStack.push(Slot.someInt);
		callerStack.push(Slot.someShort);
		callerStack.push(Slot.maybeThisReference);
		callerStack.push(Slot.noSlot);

		Frame frame = new Frame(12, callerStack, 12);

		assertEquals(12, frame.getLocalVars().length);

		assertEquals(0, callerStack.size());

		assertEquals(Slot.thisReference, frame.getLocalVars()[0]);
		assertEquals(Slot.notThisReference, frame.getLocalVars()[1]);
		assertEquals(Slot.someBoolean, frame.getLocalVars()[2]);
		assertEquals(Slot.someByte, frame.getLocalVars()[3]);
		assertEquals(Slot.someChar, frame.getLocalVars()[4]);
		assertEquals(Slot.someFloat, frame.getLocalVars()[5]);
		assertEquals(Slot.someHalfDouble, frame.getLocalVars()[6]);
		assertEquals(Slot.someHalfLong, frame.getLocalVars()[7]);
		assertEquals(Slot.someInt, frame.getLocalVars()[8]);
		assertEquals(Slot.someShort, frame.getLocalVars()[9]);
		assertEquals(Slot.maybeThisReference, frame.getLocalVars()[10]);
		assertEquals(Slot.noSlot, frame.getLocalVars()[11]);
	}

	/**
	 * Tests constructor with all Slot types and half of the callerStack popped.
	 */
	@Test
	public void testFrameIntStackOfSlotInt3() {
		Stack<Slot> callerStack = new Stack<Slot>();
		callerStack.push(Slot.thisReference);
		callerStack.push(Slot.notThisReference);
		callerStack.push(Slot.someBoolean);
		callerStack.push(Slot.someByte);
		callerStack.push(Slot.someChar);
		callerStack.push(Slot.someFloat);
		callerStack.push(Slot.someHalfDouble);
		callerStack.push(Slot.someHalfLong);
		callerStack.push(Slot.someInt);
		callerStack.push(Slot.someShort);
		callerStack.push(Slot.maybeThisReference);
		callerStack.push(Slot.noSlot);

		Frame frame = new Frame(12, callerStack, 6);

		assertEquals(12, frame.getLocalVars().length);

		assertEquals(6, callerStack.size());

		assertEquals(Slot.someHalfDouble, frame.getLocalVars()[0]);
		assertEquals(Slot.someHalfLong, frame.getLocalVars()[1]);
		assertEquals(Slot.someInt, frame.getLocalVars()[2]);
		assertEquals(Slot.someShort, frame.getLocalVars()[3]);
		assertEquals(Slot.maybeThisReference, frame.getLocalVars()[4]);
		assertEquals(Slot.noSlot, frame.getLocalVars()[5]);
		assertEquals(null, frame.getLocalVars()[6]);
		assertEquals(null, frame.getLocalVars()[7]);
		assertEquals(null, frame.getLocalVars()[8]);
		assertEquals(null, frame.getLocalVars()[9]);
		assertEquals(null, frame.getLocalVars()[10]);
		assertEquals(null, frame.getLocalVars()[11]);
	}

	/**
	 * Tests constructor with all Slot types without touching callerStack.
	 */
	@Test
	public void testFrameIntStackOfSlotInt4() {
		Stack<Slot> callerStack = new Stack<Slot>();
		callerStack.push(Slot.thisReference);
		callerStack.push(Slot.notThisReference);
		callerStack.push(Slot.someBoolean);
		callerStack.push(Slot.someByte);
		callerStack.push(Slot.someChar);
		callerStack.push(Slot.someFloat);
		callerStack.push(Slot.someHalfDouble);
		callerStack.push(Slot.someHalfLong);
		callerStack.push(Slot.someInt);
		callerStack.push(Slot.someShort);
		callerStack.push(Slot.maybeThisReference);
		callerStack.push(Slot.noSlot);

		Frame frame = new Frame(12, callerStack, 0);

		assertEquals(12, frame.getLocalVars().length);

		assertEquals(12, callerStack.size());

		assertEquals(null, frame.getLocalVars()[0]);
		assertEquals(null, frame.getLocalVars()[1]);
		assertEquals(null, frame.getLocalVars()[2]);
		assertEquals(null, frame.getLocalVars()[3]);
		assertEquals(null, frame.getLocalVars()[4]);
		assertEquals(null, frame.getLocalVars()[5]);
		assertEquals(null, frame.getLocalVars()[6]);
		assertEquals(null, frame.getLocalVars()[7]);
		assertEquals(null, frame.getLocalVars()[8]);
		assertEquals(null, frame.getLocalVars()[9]);
		assertEquals(null, frame.getLocalVars()[10]);
		assertEquals(null, frame.getLocalVars()[11]);
	}

	/**
	 * Tests constructor with all Slot types and maxLocals smaller than numArgs.
	 * IndexOutOfBoundsException expected.
	 */
	@Test(expected = IndexOutOfBoundsException.class)
	public void testFrameIntStackOfSlotInt5() {
		Stack<Slot> callerStack = new Stack<Slot>();
		callerStack.push(Slot.thisReference);
		callerStack.push(Slot.notThisReference);
		callerStack.push(Slot.someBoolean);
		callerStack.push(Slot.someByte);
		callerStack.push(Slot.someChar);
		callerStack.push(Slot.someFloat);
		callerStack.push(Slot.someHalfDouble);
		callerStack.push(Slot.someHalfLong);
		callerStack.push(Slot.someInt);
		callerStack.push(Slot.someShort);
		callerStack.push(Slot.maybeThisReference);
		callerStack.push(Slot.noSlot);

		@SuppressWarnings("unused")
		Frame frame = new Frame(5, callerStack, 12);
	}

	/**
	 * Tests copy constructor with null. NullPointerException expected.
	 */
	@Test(expected = NullPointerException.class)
	public void testFrameFrame0() {

		@SuppressWarnings("unused")
		Frame frame = new Frame(null);
	}

	/**
	 * Tests copy constructor with valid data.
	 */
	@Test
	public void testFrameFrame1() {
		Stack<Slot> callerStack = new Stack<Slot>();
		callerStack.add(Slot.someBoolean);
		callerStack.add(Slot.someFloat);
		Frame frameToCopy = new Frame(5, callerStack, 2);
		frameToCopy.getStack().push(Slot.someHalfDouble);

		Frame frame = new Frame(frameToCopy);

		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someHalfDouble, frame.getStack().pop());

		assertEquals(5, frame.getLocalVars().length);
		assertEquals(Slot.someBoolean, frame.getLocalVars()[0]);
		assertEquals(Slot.someFloat, frame.getLocalVars()[1]);
		assertEquals(null, frame.getLocalVars()[2]);
		assertEquals(null, frame.getLocalVars()[3]);
		assertEquals(null, frame.getLocalVars()[4]);
	}

	/**
	 * Tests pushStackByRequiredSlots(Slot) for all Slot types.
	 */
	@Test
	public void testPushStackByRequiredSlots() {
		Frame frame = new Frame(0, null, 0);

		frame.pushStackByRequiredSlots(Slot.maybeThisReference);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.maybeThisReference, frame.getStack().pop());

		frame.pushStackByRequiredSlots(Slot.noSlot);
		assertEquals(0, frame.getStack().size());

		frame.pushStackByRequiredSlots(Slot.notThisReference);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.notThisReference, frame.getStack().pop());

		frame.pushStackByRequiredSlots(Slot.someBoolean);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someBoolean, frame.getStack().pop());

		frame.pushStackByRequiredSlots(Slot.someByte);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someByte, frame.getStack().pop());

		frame.pushStackByRequiredSlots(Slot.someChar);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someChar, frame.getStack().pop());

		frame.pushStackByRequiredSlots(Slot.someFloat);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someFloat, frame.getStack().pop());

		frame.pushStackByRequiredSlots(Slot.someHalfDouble);
		assertEquals(2, frame.getStack().size());
		assertEquals(Slot.someHalfDouble, frame.getStack().pop());
		assertEquals(Slot.someHalfDouble, frame.getStack().pop());

		frame.pushStackByRequiredSlots(Slot.someHalfLong);
		assertEquals(2, frame.getStack().size());
		assertEquals(Slot.someHalfLong, frame.getStack().pop());
		assertEquals(Slot.someHalfLong, frame.getStack().pop());

		frame.pushStackByRequiredSlots(Slot.someInt);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someInt, frame.getStack().pop());

		frame.pushStackByRequiredSlots(Slot.someShort);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someShort, frame.getStack().pop());

		frame.pushStackByRequiredSlots(Slot.thisReference);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.thisReference, frame.getStack().pop());
	}

	/**
	 * Tests pushStackByDataType(DataType) for all DataTypes.
	 */
	@Test
	public void testPushStackByDataType() {
		Frame frame = new Frame(0, null, 0);

		frame.pushStackByDataType(DataType.referenceType);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.notThisReference, frame.getStack().pop());

		frame.pushStackByDataType(DataType.voidType);
		assertEquals(0, frame.getStack().size());

		frame.pushStackByDataType(DataType.booleanType);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someBoolean, frame.getStack().pop());

		frame.pushStackByDataType(DataType.byteType);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someByte, frame.getStack().pop());

		frame.pushStackByDataType(DataType.charType);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someChar, frame.getStack().pop());

		frame.pushStackByDataType(DataType.floatType);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someFloat, frame.getStack().pop());

		frame.pushStackByDataType(DataType.doubleType);
		assertEquals(2, frame.getStack().size());
		assertEquals(Slot.someHalfDouble, frame.getStack().pop());
		assertEquals(Slot.someHalfDouble, frame.getStack().pop());

		frame.pushStackByDataType(DataType.longType);
		assertEquals(2, frame.getStack().size());
		assertEquals(Slot.someHalfLong, frame.getStack().pop());
		assertEquals(Slot.someHalfLong, frame.getStack().pop());

		frame.pushStackByDataType(DataType.intType);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someInt, frame.getStack().pop());

		frame.pushStackByDataType(DataType.shortType);
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someShort, frame.getStack().pop());
	}

	/**
	 * Tests popStackByRequiredSlots() with single pop.
	 */
	@Test
	public void testPopStackByRequiredSlots0() {
		Frame frame = new Frame(0, null, 0);
		frame.getStack().push(Slot.someBoolean);
		frame.getStack().push(Slot.someFloat);

		assertEquals(Slot.someFloat, frame.popStackByRequiredSlots());
		assertEquals(1, frame.getStack().size());
		assertEquals(Slot.someBoolean, frame.getStack().pop());
	}

	/**
	 * Tests popStackByRequiredSlots() with double pop.
	 */
	@Test
	public void testPopStackByRequiredSlots1() {
		Frame frame = new Frame(0, null, 0);
		frame.getStack().push(Slot.someBoolean);
		frame.getStack().push(Slot.someHalfDouble);

		assertEquals(Slot.someHalfDouble, frame.popStackByRequiredSlots());
		assertEquals(0, frame.getStack().size());
	}

	/**
	 * Tests popStackByRequiredSlots() with double pop but only one element onto
	 * the stack. EmptyStackException expected.
	 */
	@Test(expected = EmptyStackException.class)
	public void testPopStackByRequiredSlots2() {
		Frame frame = new Frame(0, null, 0);
		frame.getStack().push(Slot.someHalfDouble);

		frame.popStackByRequiredSlots();
	}
}
