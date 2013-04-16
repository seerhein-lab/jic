package de.htwg_konstanz.in.jca.slot;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.apache.bcel.generic.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SlotTest {

	private Type[] types;
	private int expectedSlots;

	public SlotTest(Type[] types, int expectedSlots) {
		this.types = types;
		this.expectedSlots = expectedSlots;
	}

	@Parameters
	public static Collection<Object[]> testInstanceTypes() {
		return Arrays.asList(new Object[][] {
				{ new Type[] {}, 0 },
				{ new Type[] { Type.INT }, 1 },
				{ new Type[] { Type.DOUBLE }, 2 },
				{ new Type[] { Type.INT, Type.DOUBLE }, 3 },
				{ new Type[] { Type.INT, Type.LONG, Type.CHAR, Type.BYTE,
								Type.BOOLEAN, Type.SHORT, Type.FLOAT,
								Type.DOUBLE, Type.VOID }, 10 } });
	}

	@Test
	public void testNumRequiredSlots() {
		int actual = Slot.numRequiredSlots(types);
		assertEquals(expectedSlots, actual);
	}

}