package de.seerhein_lab.jic.slot;

import static org.junit.Assert.assertTrue;

import org.apache.bcel.generic.Type;
import org.junit.Test;

import de.seerhein_lab.jic.vm.ReferenceSlot;

public class SlotTest {

	@Test
	public void testGetDefaultSlotInstanceType() {
		assertTrue(Slot.getDefaultSlotInstance(Type.INT) instanceof IntSlot);
		assertTrue(Slot.getDefaultSlotInstance(Type.LONG) instanceof LongSlot);
		assertTrue(Slot.getDefaultSlotInstance(Type.CHAR) instanceof CharSlot);
		assertTrue(Slot.getDefaultSlotInstance(Type.BYTE) instanceof ByteSlot);
		assertTrue(Slot.getDefaultSlotInstance(Type.BOOLEAN) instanceof BooleanSlot);
		assertTrue(Slot.getDefaultSlotInstance(Type.SHORT) instanceof ShortSlot);
		assertTrue(Slot.getDefaultSlotInstance(Type.FLOAT) instanceof FloatSlot);
		assertTrue(Slot.getDefaultSlotInstance(Type.DOUBLE) instanceof DoubleSlot);
		assertTrue(Slot.getDefaultSlotInstance(Type.VOID) instanceof VoidSlot);
		assertTrue(Slot.getDefaultSlotInstance(Type.OBJECT) instanceof ReferenceSlot);
		assertTrue(Slot.getDefaultSlotInstance(Type.THROWABLE) instanceof ReferenceSlot);
		assertTrue(Slot.getDefaultSlotInstance(Type.UNKNOWN) instanceof ReferenceSlot);
	}

}