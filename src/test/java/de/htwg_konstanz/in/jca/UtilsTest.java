package de.htwg_konstanz.in.jca;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.Type;
import org.junit.Test;

import de.htwg_konstanz.in.jca.slot.BooleanSlot;
import de.htwg_konstanz.in.jca.slot.ByteSlot;
import de.htwg_konstanz.in.jca.slot.CharSlot;
import de.htwg_konstanz.in.jca.slot.DoubleSlot;
import de.htwg_konstanz.in.jca.slot.FloatSlot;
import de.htwg_konstanz.in.jca.slot.IntSlot;
import de.htwg_konstanz.in.jca.slot.LongSlot;
import de.htwg_konstanz.in.jca.slot.ReferenceSlot;
import de.htwg_konstanz.in.jca.slot.ShortSlot;
import de.htwg_konstanz.in.jca.slot.VoidSlot;

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
		assertTrue(Utils.getDefaultSlotInstance(Type.INT) instanceof IntSlot);
		assertTrue(Utils.getDefaultSlotInstance(Type.LONG) instanceof LongSlot);
		assertTrue(Utils.getDefaultSlotInstance(Type.CHAR) instanceof CharSlot);
		assertTrue(Utils.getDefaultSlotInstance(Type.BYTE) instanceof ByteSlot);
		assertTrue(Utils.getDefaultSlotInstance(Type.BOOLEAN) instanceof BooleanSlot);
		assertTrue(Utils.getDefaultSlotInstance(Type.SHORT) instanceof ShortSlot);
		assertTrue(Utils.getDefaultSlotInstance(Type.FLOAT) instanceof FloatSlot);
		assertTrue(Utils.getDefaultSlotInstance(Type.DOUBLE) instanceof DoubleSlot);
		assertTrue(Utils.getDefaultSlotInstance(Type.VOID) instanceof VoidSlot);
		assertTrue(Utils.getDefaultSlotInstance(Type.OBJECT) instanceof ReferenceSlot);
		assertTrue(Utils.getDefaultSlotInstance(Type.THROWABLE) instanceof ReferenceSlot);
		assertTrue(Utils.getDefaultSlotInstance(Type.UNKNOWN) instanceof ReferenceSlot);
	}



}
