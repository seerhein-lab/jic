package de.seerhein_lab.jca;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.Type;
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

public class UtilsTest {

	@Test
	public void testFormatLoggingOutput0() {
		String indentation = Utils.formatLoggingOutput(0);
		assertEquals("", indentation);
	}

	@Test
	public void testFormatLoggingOutput1() {
		String indentation = Utils.formatLoggingOutput(1);
		assertEquals("\t", indentation);
	}

	@Test
	public void testFormatLoggingOutput2() {
		String indentation = Utils.formatLoggingOutput(2);
		assertEquals("\t\t", indentation);
	}

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
