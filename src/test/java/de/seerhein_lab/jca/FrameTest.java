package de.seerhein_lab.jca;

import static org.junit.Assert.assertEquals;

import java.util.EmptyStackException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.seerhein_lab.jca.slot.BooleanSlot;
import de.seerhein_lab.jca.slot.ByteSlot;
import de.seerhein_lab.jca.slot.CharSlot;
import de.seerhein_lab.jca.slot.DoubleSlot;
import de.seerhein_lab.jca.slot.FloatSlot;
import de.seerhein_lab.jca.slot.IntSlot;
import de.seerhein_lab.jca.slot.LongSlot;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.ShortSlot;
import de.seerhein_lab.jca.slot.Slot;
import de.seerhein_lab.jca.slot.VoidSlot;
import de.seerhein_lab.jca.vm.Frame;
import de.seerhein_lab.jca.vm.Heap;
import de.seerhein_lab.jca.vm.OpStack;

/**
 * JUnit test for Frame class. Does not test simple getters and setters.
 */
public class FrameTest {
	Slot bool = BooleanSlot.getInstance();
	Slot bytE = ByteSlot.getInstance();
	Slot chaR = CharSlot.getInstance();
	Slot doublE = DoubleSlot.getInstance();
	Slot floaT = FloatSlot.getInstance();
	Slot inT = IntSlot.getInstance();
	Slot lonG = LongSlot.getInstance();
	Slot shorT = ShortSlot.getInstance();
	Slot voiD = VoidSlot.getInstance();
	Slot thiS;
	Slot someRef;
	Slot someOtherRef;
	Heap heap;

	@BeforeClass
	public static void beforeTest() {
	}

	@Before
	public void setUp() {
		heap = new Heap();
		thiS = ReferenceSlot.createNewInstance(heap.getThisInstance());
		someRef = ReferenceSlot.createNewInstance(heap.newClassInstance());
		someOtherRef = ReferenceSlot.createNewInstance(heap.newArray());
	}

	/**
	 * Tests constructor with negative maxLocals. Expects
	 * NegativeArraySizeException.
	 */
	@Test(expected = NegativeArraySizeException.class)
	public void testFrameIntStackOfSlotInt0() {
		OpStack callerStack = new OpStack();
		callerStack.push(thiS);
		callerStack.push(someRef);
		callerStack.push(bool);
		callerStack.push(bytE);
		callerStack.push(chaR);
		callerStack.push(floaT);
		callerStack.push(doublE);
		callerStack.push(lonG);
		callerStack.push(inT);
		callerStack.push(shorT);
		callerStack.push(someOtherRef);
		callerStack.push(voiD);

		@SuppressWarnings("unused")
		Frame frame = new Frame(-1, callerStack, 12);
	}

	@Test
	public void testFrameConstructor() {
		Frame frame = new Frame(0, new OpStack(), 0);
		frame.getStack().push(thiS);
		Frame newFrame = new Frame(1, frame.getStack(), 1);
		assertEquals(newFrame.getLocalVars()[0], thiS);
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
		OpStack callerStack = new OpStack();
		callerStack.push(thiS);
		callerStack.push(someRef);
		callerStack.push(bool);
		callerStack.push(bytE);
		callerStack.push(chaR);
		callerStack.push(floaT);
		callerStack.push(doublE);
		callerStack.push(lonG);
		callerStack.push(inT);
		callerStack.push(shorT);
		callerStack.push(someOtherRef);
		callerStack.push(voiD);

		Frame frame = new Frame(12, callerStack, 12);

		assertEquals(12, frame.getLocalVars().length);

		assertEquals(0, callerStack.size());

		assertEquals(thiS, frame.getLocalVars()[0]);
		assertEquals(someRef, frame.getLocalVars()[1]);
		assertEquals(bool, frame.getLocalVars()[2]);
		assertEquals(bytE, frame.getLocalVars()[3]);
		assertEquals(chaR, frame.getLocalVars()[4]);
		assertEquals(floaT, frame.getLocalVars()[5]);
		assertEquals(doublE, frame.getLocalVars()[6]);
		assertEquals(lonG, frame.getLocalVars()[7]);
		assertEquals(inT, frame.getLocalVars()[8]);
		assertEquals(shorT, frame.getLocalVars()[9]);
		assertEquals(someOtherRef, frame.getLocalVars()[10]);
		assertEquals(voiD, frame.getLocalVars()[11]);
	}

	/**
	 * Tests constructor with all Slot types and half of the callerStack popped.
	 */
	@Test
	public void testFrameIntStackOfSlotInt3() {
		OpStack callerStack = new OpStack();
		callerStack.push(thiS);
		callerStack.push(someRef);
		callerStack.push(bool);
		callerStack.push(bytE);
		callerStack.push(chaR);
		callerStack.push(floaT);
		callerStack.push(doublE);
		callerStack.push(lonG);
		callerStack.push(inT);
		callerStack.push(shorT);
		callerStack.push(someOtherRef);
		callerStack.push(voiD);

		Frame frame = new Frame(12, callerStack, 6);

		assertEquals(12, frame.getLocalVars().length);

		assertEquals(6, callerStack.size());

		assertEquals(doublE, frame.getLocalVars()[0]);
		assertEquals(lonG, frame.getLocalVars()[1]);
		assertEquals(inT, frame.getLocalVars()[2]);
		assertEquals(shorT, frame.getLocalVars()[3]);
		assertEquals(someOtherRef, frame.getLocalVars()[4]);
		assertEquals(voiD, frame.getLocalVars()[5]);
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
		OpStack callerStack = new OpStack();
		callerStack.push(thiS);
		callerStack.push(someRef);
		callerStack.push(bool);
		callerStack.push(bytE);
		callerStack.push(chaR);
		callerStack.push(floaT);
		callerStack.push(doublE);
		callerStack.push(lonG);
		callerStack.push(inT);
		callerStack.push(shorT);
		callerStack.push(someOtherRef);
		callerStack.push(voiD);

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
		OpStack callerStack = new OpStack();
		callerStack.push(thiS);
		callerStack.push(someRef);
		callerStack.push(bool);
		callerStack.push(bytE);
		callerStack.push(chaR);
		callerStack.push(floaT);
		callerStack.push(doublE);
		callerStack.push(lonG);
		callerStack.push(inT);
		callerStack.push(shorT);
		callerStack.push(someOtherRef);
		callerStack.push(voiD);

		@SuppressWarnings("unused")
		Frame frame = new Frame(5, callerStack, 12);
	}

	/**
	 * Tests copy constructor with null. NullPointerException expected.
	 */
	@Test(expected = NullPointerException.class)
	public void testFrameFrame0() {

		@SuppressWarnings("unused")
		Frame frame = new Frame((Frame) null);
	}

	/**
	 * Tests copy constructor with valid data.
	 */
	@Test
	public void testFrameFrame1() {
		OpStack callerStack = new OpStack();
		callerStack.push(bool);
		callerStack.push(floaT);
		Frame frameToCopy = new Frame(5, callerStack, 2);
		frameToCopy.getStack().push(doublE);

		Frame frame = new Frame(frameToCopy);

		assertEquals(1, frame.getStack().size());
		assertEquals(doublE, frame.getStack().pop());

		assertEquals(5, frame.getLocalVars().length);
		assertEquals(bool, frame.getLocalVars()[0]);
		assertEquals(floaT, frame.getLocalVars()[1]);
		assertEquals(null, frame.getLocalVars()[2]);
		assertEquals(null, frame.getLocalVars()[3]);
		assertEquals(null, frame.getLocalVars()[4]);
	}

	/**
	 * Tests pushStackByRequiredSlots(Slot) for all Slot types.
	 */
	@Test
	public void testPushStackByRequiredSlots() {
		Frame frame = new Frame(0, new OpStack(), 0);

		frame.pushStackByRequiredSlots(someOtherRef);
		assertEquals(1, frame.getStack().size());
		assertEquals(someOtherRef, frame.getStack().pop());

		frame.pushStackByRequiredSlots(voiD);
		assertEquals(0, frame.getStack().size());

		frame.pushStackByRequiredSlots(someRef);
		assertEquals(1, frame.getStack().size());
		assertEquals(someRef, frame.getStack().pop());

		frame.pushStackByRequiredSlots(bool);
		assertEquals(1, frame.getStack().size());
		assertEquals(bool, frame.getStack().pop());

		frame.pushStackByRequiredSlots(bytE);
		assertEquals(1, frame.getStack().size());
		assertEquals(bytE, frame.getStack().pop());

		frame.pushStackByRequiredSlots(chaR);
		assertEquals(1, frame.getStack().size());
		assertEquals(chaR, frame.getStack().pop());

		frame.pushStackByRequiredSlots(floaT);
		assertEquals(1, frame.getStack().size());
		assertEquals(floaT, frame.getStack().pop());

		frame.pushStackByRequiredSlots(doublE);
		assertEquals(2, frame.getStack().size());
		assertEquals(doublE, frame.getStack().pop());
		assertEquals(doublE, frame.getStack().pop());

		frame.pushStackByRequiredSlots(lonG);
		assertEquals(2, frame.getStack().size());
		assertEquals(lonG, frame.getStack().pop());
		assertEquals(lonG, frame.getStack().pop());

		frame.pushStackByRequiredSlots(inT);
		assertEquals(1, frame.getStack().size());
		assertEquals(inT, frame.getStack().pop());

		frame.pushStackByRequiredSlots(shorT);
		assertEquals(1, frame.getStack().size());
		assertEquals(shorT, frame.getStack().pop());

		frame.pushStackByRequiredSlots(thiS);
		assertEquals(1, frame.getStack().size());
		assertEquals(thiS, frame.getStack().pop());
	}

	/**
	 * Tests popStackByRequiredSlots() with single pop.
	 */
	@Test
	public void testPopStackByRequiredSlots0() {
		Frame frame = new Frame(0, new OpStack(), 0);
		frame.getStack().push(bool);
		frame.getStack().push(floaT);

		assertEquals(floaT, frame.popStackByRequiredSlots());
		assertEquals(1, frame.getStack().size());
		assertEquals(bool, frame.getStack().pop());
	}

	/**
	 * Tests popStackByRequiredSlots() with double pop.
	 */
	@Test
	public void testPopStackByRequiredSlots1() {
		Frame frame = new Frame(0, new OpStack(), 0);
		frame.getStack().push(bool);
		frame.getStack().push(doublE);

		assertEquals(doublE, frame.popStackByRequiredSlots());
		assertEquals(0, frame.getStack().size());
	}

	/**
	 * Tests popStackByRequiredSlots() with double pop but only one element onto
	 * the stack. EmptyStackException expected.
	 */
	@Test(expected = EmptyStackException.class)
	public void testPopStackByRequiredSlots2() {
		Frame frame = new Frame(0, new OpStack(), 0);
		frame.getStack().push(doublE);

		frame.popStackByRequiredSlots();
	}
}
