package de.htwg_konstanz.in.jca;

import static org.junit.Assert.assertEquals;

import java.util.EmptyStackException;
import java.util.Stack;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;
import org.junit.Before;
import org.junit.Test;

import de.htwg_konstanz.in.jca.testclasses.ClassWithAllTypes;

public class TestLocalVars {

	private LocalVariable[] localVariableTableExample;

	/**
	 * Creates the LocalVariable array from the test file and stores it in
	 * localVariableTableExample. Is needed because a LocalVariableTable is hard
	 * to create.
	 * 
	 * @throws ClassNotFoundException
	 */
	@Before
	public void setUp() throws ClassNotFoundException {
		String usedClass = ClassWithAllTypes.class.getName();
		Method[] methods = Repository.lookupClass(usedClass).getMethods();
		localVariableTableExample = methods[0].getLocalVariableTable()
				.getLocalVariableTable();
	}

	/**
	 * Tests the case when the LocalVariable array is null.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testLocalVarsNull() {
		new LocalVars(null);
	}

	/**
	 * Tests the case when LocalVariable array with length 0 is passed to the
	 * constructor.
	 */
	@Test
	public void testLocalVarsZeroLength() {
		LocalVars localVars = new LocalVars(new LocalVariable[0]);
		assertEquals("entries.length should be 0.", 0,
				localVars.getEntriesLength());
		assertEquals("indexes.length should be 0.", 0,
				localVars.getIndexesLength());
	}

	/**
	 * Tests the constructor with an example VariableTable array.
	 */
	@Test
	public void testLocalVars() {
		LocalVars localVars = new LocalVars(localVariableTableExample);
		assertEquals(
				"localVars should contain 10 entries (1 for thisReference, 9 for other variables).",
				10, localVars.getEntriesLength());
		assertEquals(
				"localVars should contain 10 indexes (1 for thisReference, 9 for other variables).",
				10, localVars.getIndexesLength());
		assertEquals("Each entry should have an index.",
				localVars.getEntriesLength(), localVars.getIndexesLength());
	}

	/**
	 * Tests if LocalVars sets the right entries.
	 */
	@Test
	public void testLocalVarsEntries() {
		LocalVars localVars = new LocalVars(localVariableTableExample);
		assertEquals("1st entry should be someReference.", Entry.someReference,
				localVars.getEntry(0));
		assertEquals("2nd entry should be someByte.", Entry.someByte,
				localVars.getEntry(1));
		assertEquals("3th entry should be someShort.", Entry.someShort,
				localVars.getEntry(2));
		assertEquals("4th entry should be someReference.", Entry.someReference,
				localVars.getEntry(3));
		assertEquals("5th entry should be someInt.", Entry.someInt,
				localVars.getEntry(4));
		assertEquals("6th entry should be someLong.", Entry.someLong,
				localVars.getEntry(5));
		assertEquals("7th entry should be someFloat.", Entry.someFloat,
				localVars.getEntry(6));
		assertEquals("8th entry should be someDouble.", Entry.someDouble,
				localVars.getEntry(7));
		assertEquals("9th entry should be someChar.", Entry.someChar,
				localVars.getEntry(8));
		assertEquals("10th entry should be someBoolean.", Entry.someBoolean,
				localVars.getEntry(9));
	}

	/**
	 * Tests if LocalVars sets the right indexes.
	 */
	@Test
	public void testLocalVarsIndexes() {
		LocalVars localVars = new LocalVars(localVariableTableExample);
		assertEquals("1st index should be 0.", 0, localVars.getIndex(0));
		assertEquals("2nd index should be 1.", 1, localVars.getIndex(1));
		assertEquals("3th index should be 2.", 2, localVars.getIndex(2));
		assertEquals("4th index should be 3.", 3, localVars.getIndex(3));
		assertEquals("5th index should be 4.", 4, localVars.getIndex(4));
		assertEquals("6th index should be 5.", 5, localVars.getIndex(5));
		assertEquals("7th index should be 7.", 7, localVars.getIndex(6));
		assertEquals("8th index should be 8.", 8, localVars.getIndex(7));
		assertEquals("9th index should be 10.", 10, localVars.getIndex(8));
		assertEquals("10th index should be 11.", 11, localVars.getIndex(9));
	}

	/**
	 * Tests initWithArgs() on LocalVars with zero length entries array and
	 * filled callerStack.
	 */
	@Test
	public void testInitWithArgsOnZeroLengthEntries() {
		LocalVars localVars = new LocalVars(new LocalVariable[0]);
		Stack<Entry> callerStack = new Stack<Entry>();
		callerStack.add(Entry.someReference);
		assertEquals("callerStack should not be empty.", true,
				!callerStack.isEmpty());
		localVars.initWithArgs(callerStack, 1);
		assertEquals("callerStack should be emptied.", true,
				callerStack.isEmpty());
	}

	/**
	 * Tests case when more items should be popped than the stack contains.
	 */
	@Test(expected = EmptyStackException.class)
	public void testInitWithArgsInvalidArgs() {
		// creates example LocalVars with 10 entries
		LocalVars localVars = new LocalVars(localVariableTableExample);
		Stack<Entry> callerStack = new Stack<Entry>();
		callerStack.add(Entry.someFloat);
		// only 1 entry on the stack but trying to pop 2
		localVars.initWithArgs(callerStack, 2);
	}

	/**
	 * Tests if LocalVars does not change when argsNum is 0.
	 */
	@Test
	public void testInitWithArgsNoChange() {
		// creates example LocalVars with 10 entries
		LocalVars localVars = new LocalVars(localVariableTableExample);
		Stack<Entry> callerStack = new Stack<Entry>();
		callerStack.add(Entry.someFloat);
		localVars.initWithArgs(callerStack, 0);
		assertEquals("1st entry should be someReference.", Entry.someReference,
				localVars.getEntry(0));
		assertEquals("2nd entry should be someByte.", Entry.someByte,
				localVars.getEntry(1));
		assertEquals("3th entry should be someShort.", Entry.someShort,
				localVars.getEntry(2));
		assertEquals("4th entry should be someReference.", Entry.someReference,
				localVars.getEntry(3));
		assertEquals("5th entry should be someInt.", Entry.someInt,
				localVars.getEntry(4));
		assertEquals("6th entry should be someLong.", Entry.someLong,
				localVars.getEntry(5));
		assertEquals("7th entry should be someFloat.", Entry.someFloat,
				localVars.getEntry(6));
		assertEquals("8th entry should be someDouble.", Entry.someDouble,
				localVars.getEntry(7));
		assertEquals("9th entry should be someChar.", Entry.someChar,
				localVars.getEntry(8));
		assertEquals("10th entry should be someBoolean.", Entry.someBoolean,
				localVars.getEntry(9));
	}

	/**
	 * Tests if half of the array can be overwritten.
	 */
	@Test
	public void testInitWhithArgsHalfStack() {
		// creates example LocalVars with 10 entries
		LocalVars localVars = new LocalVars(localVariableTableExample);
		Stack<Entry> callerStack = new Stack<Entry>();
		for (int i = 0; i < 5; i++) {
			callerStack.add(Entry.thisReference);
		}
		localVars.initWithArgs(callerStack, 5);
		for (int i = 0; i < 5; i++) {
			assertEquals("Entry " + (i + 1) + " should be thisReference.",
					Entry.thisReference, localVars.getEntry(i));
		}
		assertEquals("Entry 6 should be someLong.", Entry.someLong,
				localVars.getEntry(5));
		assertEquals("Entry 7 should be someFloat.", Entry.someFloat,
				localVars.getEntry(6));
		assertEquals("Entry 8 should be someDouble.", Entry.someDouble,
				localVars.getEntry(7));
		assertEquals("Entry 9 should be someChar.", Entry.someChar,
				localVars.getEntry(8));
		assertEquals("Entry 10 should be someBoolean.", Entry.someBoolean,
				localVars.getEntry(9));
	}

	/**
	 * Tests if complete entries can be overwritten.
	 */
	@Test
	public void testInitWhithArgsCompleteStack() {
		// creates example LocalVars with 10 entries
		LocalVars localVars = new LocalVars(localVariableTableExample);
		Stack<Entry> callerStack = new Stack<Entry>();
		for (int i = 0; i < localVars.getEntriesLength(); i++) {
			callerStack.add(Entry.thisReference);
		}
		localVars.initWithArgs(callerStack, callerStack.size());
		for (int i = 0; i < localVars.getEntriesLength(); i++) {
			assertEquals("Entry" + (i + 1) + " should be thisReference.",
					Entry.thisReference, localVars.getEntry(i));
		}
	}

	/**
	 * Tests if an exception is thrown when argsNum is greater than
	 * entries.length.
	 */
	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void testInitWithArgsTooGreatArgNum() {
		// creates example LocalVars with 10 entries
		LocalVars localVars = new LocalVars(localVariableTableExample);
		Stack<Entry> callerStack = new Stack<Entry>();
		for (int i = 0; i < localVars.getEntriesLength() + 1; i++) {
			callerStack.add(Entry.thisReference);
		}
		localVars.initWithArgs(callerStack, callerStack.size());
	}

	/**
	 * Tests if getForIndex() throws AssertionError
	 */
	@Test(expected = AssertionError.class)
	public void testGetForIndexError() {
		// creates example LocalVars with 10 entries
		LocalVars localVars = new LocalVars(localVariableTableExample);
		localVars.getForIndex(-1);
	}

	/**
	 * Tests if getForIndex() returns the right entries.
	 */
	@Test
	public void testGetForIndex() {
		// creates example LocalVars with 10 entries
		LocalVars localVars = new LocalVars(localVariableTableExample);
		assertEquals("1st entry should be someReference.", Entry.someReference,
				localVars.getForIndex(0));
		assertEquals("2nd entry should be someByte.", Entry.someByte,
				localVars.getForIndex(1));
		assertEquals("3th entry should be someShort.", Entry.someShort,
				localVars.getForIndex(2));
		assertEquals("4th entry should be someReference.", Entry.someReference,
				localVars.getForIndex(3));
		assertEquals("5th entry should be someInt.", Entry.someInt,
				localVars.getForIndex(4));
		assertEquals("6th entry should be someLong.", Entry.someLong,
				localVars.getForIndex(5));
		assertEquals("7th entry should be someFloat.", Entry.someFloat,
				localVars.getForIndex(7));
		assertEquals("8th entry should be someDouble.", Entry.someDouble,
				localVars.getForIndex(8));
		assertEquals("9th entry should be someChar.", Entry.someChar,
				localVars.getForIndex(10));
		assertEquals("10th entry should be someBoolean.", Entry.someBoolean,
				localVars.getForIndex(11));
	}

	/**
	 * Tests if setForIndex() throws AssertionError.
	 */
	@Test(expected = AssertionError.class)
	public void testSetForIndexError() {
		// creates example LocalVars with 10 entries
		LocalVars localVars = new LocalVars(localVariableTableExample);
		localVars.setForIndex(-1, Entry.someReference);
	}

	/**
	 * Tests if setForIndex() sets the right Entries.
	 */
	@Test
	public void testSetForIndex() {
		// creates example LocalVars with 10 entries
		LocalVars localVars = new LocalVars(localVariableTableExample);
		localVars.setForIndex(0, Entry.someBoolean);
		localVars.setForIndex(1, Entry.someChar);
		localVars.setForIndex(2, Entry.someDouble);
		localVars.setForIndex(3, Entry.someFloat);
		localVars.setForIndex(4, Entry.someLong);
		localVars.setForIndex(5, Entry.someInt);
		localVars.setForIndex(7, Entry.someReference);
		localVars.setForIndex(8, Entry.someShort);
		localVars.setForIndex(10, Entry.someByte);
		localVars.setForIndex(11, Entry.thisReference);
		assertEquals("1st entry should be someBoolean.", Entry.someBoolean,
				localVars.getEntry(0));
		assertEquals("2nd entry should be someChar.", Entry.someChar,
				localVars.getEntry(1));
		assertEquals("3th entry should be someDouble.", Entry.someDouble,
				localVars.getEntry(2));
		assertEquals("4th entry should be someFloat.", Entry.someFloat,
				localVars.getEntry(3));
		assertEquals("5th entry should be someLong.", Entry.someLong,
				localVars.getEntry(4));
		assertEquals("6th entry should be someInt.", Entry.someInt,
				localVars.getEntry(5));
		assertEquals("7th entry should be someReference.", Entry.someReference,
				localVars.getEntry(6));
		assertEquals("8th entry should be someShort.", Entry.someShort,
				localVars.getEntry(7));
		assertEquals("9th entry should be someByte.", Entry.someByte,
				localVars.getEntry(8));
		assertEquals("10th entry should be thisReference.",
				Entry.thisReference, localVars.getEntry(9));
	}

}