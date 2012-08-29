package de.htwg_konstanz.in.jca;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestEntry {

	@Test
	public void testGetInstance() {
		assertEquals(Entry.getInstance("I"), Entry.someInt);
		assertEquals(Entry.getInstance("J"), Entry.someLong);
		assertEquals(Entry.getInstance("C"), Entry.someChar);
		assertEquals(Entry.getInstance("B"), Entry.someByte);
		assertEquals(Entry.getInstance("Z"), Entry.someBoolean);
		assertEquals(Entry.getInstance("S"), Entry.someShort);
		assertEquals(Entry.getInstance("F"), Entry.someFloat);
		assertEquals(Entry.getInstance("D"), Entry.someDouble);
		assertEquals(Entry.getInstance("O"), Entry.someReference);
	}

}
