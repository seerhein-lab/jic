package de.htwg_konstanz.in.jca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class EntryTest {

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
		assertEquals(Entry.getInstance("O"), Entry.notThisReference);
	}

	/**
	 * Checks that if the outcome of another execution path is null
	 * that the value is unchanged.
	 */
	@Test
	public void testCombineWithOther_NullValue() {
		assertEquals(Entry.someByte, Entry.someByte.combineWithOther(null));
		assertEquals(Entry.someShort, Entry.someShort.combineWithOther(null));
		assertEquals(Entry.someInt, Entry.someInt.combineWithOther(null));
		assertEquals(Entry.someLong, Entry.someLong.combineWithOther(null));
		assertEquals(Entry.someFloat, Entry.someFloat.combineWithOther(null));
		assertEquals(Entry.someDouble, Entry.someDouble.combineWithOther(null));
		assertEquals(Entry.someChar, Entry.someChar.combineWithOther(null));
		assertEquals(Entry.someBoolean, Entry.someBoolean.combineWithOther(null));
		assertEquals(Entry.notThisReference, Entry.notThisReference.combineWithOther(null));
		assertEquals(Entry.maybeThisReference, Entry.maybeThisReference.combineWithOther(null));
		assertEquals(Entry.thisReference, Entry.thisReference.combineWithOther(null));
	}
	/**

	 * Checks that if the outcome of another execution path is the same value
	 * that the value is unchanged.
	 */
	@Test
	public void testCombineWithOther_SameValue() {
		assertEquals(Entry.someByte, Entry.someByte.combineWithOther(Entry.someByte));
		assertEquals(Entry.someShort, Entry.someShort.combineWithOther(Entry.someShort));
		assertEquals(Entry.someInt, Entry.someInt.combineWithOther(Entry.someInt));
		assertEquals(Entry.someLong, Entry.someLong.combineWithOther(Entry.someLong));
		assertEquals(Entry.someFloat, Entry.someFloat.combineWithOther(Entry.someFloat));
		assertEquals(Entry.someDouble, Entry.someDouble.combineWithOther(Entry.someDouble));
		assertEquals(Entry.someChar, Entry.someChar.combineWithOther(Entry.someChar));
		assertEquals(Entry.someBoolean, Entry.someBoolean.combineWithOther(Entry.someBoolean));
		assertEquals(Entry.notThisReference, Entry.notThisReference.combineWithOther(Entry.notThisReference));
		assertEquals(Entry.maybeThisReference, Entry.maybeThisReference.combineWithOther(Entry.maybeThisReference));
		assertEquals(Entry.thisReference, Entry.thisReference.combineWithOther(Entry.thisReference));
	}
	
	/**
	 * Checks that if the outcome of another execution path is correct if
	 * the own value value is a notThisReference.
	 */
	@Test
	public void testCombineWithOther_NotThisReference() {
		assertEquals(Entry.notThisReference, Entry.notThisReference.combineWithOther(Entry.notThisReference));
		assertEquals(Entry.maybeThisReference, Entry.notThisReference.combineWithOther(Entry.maybeThisReference));
		assertEquals(Entry.maybeThisReference, Entry.notThisReference.combineWithOther(Entry.thisReference));
		try
		{
			Entry.notThisReference.combineWithOther(Entry.someByte);
			fail("Exception expected");
		}
		catch (IllegalArgumentException e)
		{
			assertEquals(Entry.notThisReference
					+ " cannot be combined with " + Entry.someByte,
				e.getMessage());
		}
	}

	/**
	 * Checks that if the outcome of another execution path is correct if
	 * the own value value is a maybeThisReference.
	 */
	@Test
	public void testCombineWithOther_MaybeThisReference() {
		assertEquals(Entry.maybeThisReference, Entry.maybeThisReference.combineWithOther(Entry.maybeThisReference));
		assertEquals(Entry.maybeThisReference, Entry.maybeThisReference.combineWithOther(Entry.maybeThisReference));
		assertEquals(Entry.maybeThisReference, Entry.maybeThisReference.combineWithOther(Entry.thisReference));
		try
		{
			Entry.maybeThisReference.combineWithOther(Entry.someByte);
			fail("Exception expected");
		}
		catch (IllegalArgumentException e)
		{
			assertEquals(Entry.maybeThisReference
					+ " cannot be combined with " + Entry.someByte,
				e.getMessage());
		}
	}

	/**
	 * Checks that if the outcome of another execution path is correct if
	 * the own value value is a thisReference.
	 */
	@Test
	public void testCombineWithOther_thisReference() {
		assertEquals(Entry.maybeThisReference, Entry.thisReference.combineWithOther(Entry.maybeThisReference));
		assertEquals(Entry.maybeThisReference, Entry.thisReference.combineWithOther(Entry.maybeThisReference));
		assertEquals(Entry.thisReference, Entry.thisReference.combineWithOther(Entry.thisReference));
		try
		{
			Entry.thisReference.combineWithOther(Entry.someByte);
			fail("Exception expected");
		}
		catch (IllegalArgumentException e)
		{
			assertEquals(Entry.thisReference
					+ " cannot be combined with " + Entry.someByte,
				e.getMessage());
		}
	}

	/**
	 * Checks that if the outcome of another execution path is correct if
	 * the own value value is a thisReference.
	 */
	@Test
	public void testCombineWithOther_IllegalKnownTypes() {
		List<Entry> types = new ArrayList<Entry>();
		types.add(Entry.someByte);
		types.add(Entry.someShort);
		types.add(Entry.someInt);
		types.add(Entry.someLong);
		types.add(Entry.someFloat);
		types.add(Entry.someDouble);
		types.add(Entry.someChar);
		types.add(Entry.someBoolean);
		for (Entry own : types)
		{
			for (Entry other : types)
			{
				if (own == other)
				{
					continue;
				}
				try
				{
					own.combineWithOther(other);
					fail("Exception expected for combination " 
							+ own + " : " + other);
				}
				catch (IllegalArgumentException e)
				{
					assertEquals(own + " cannot be combined with " + other,
						e.getMessage());
				}
			}
			
		}
	}
	
	/**
	 * Checks sample Execution for the 
	 */
	@Test
	public void testCombineWithOthers() {
		List<Entry> others = new ArrayList<Entry>();
		others.add(Entry.someByte);
		assertEquals(Entry.someByte, Entry.someByte.combineWithOthers(others));
	}
}
