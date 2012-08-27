package de.htwg_konstanz.in.jca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Stack;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.htwg_konstanz.in.jca.testclasses.ClassWithMethodArg;

public abstract class TestLocalVars {
	/* Edit finals below for use */
	private final String USED_CLASS = ClassWithMethodArg.class.getName();
	protected final boolean HAS_METHODS = true;
	protected final Entry[] EXPECTED_ENTRIES_CTOR = { Entry.someReference };
	protected final int[] EXPECTED_INDEXES_CTOR = { 0 };
	protected final Entry[] ENTRIES_CALLER_STACK_CTOR = { Entry.thisReference };
	protected final Entry[] EXPECTED_ENTRIES_METHOD = { Entry.someReference,
			Entry.someInt };
	protected final int[] EXPECTED_INDEXES_METHOD = { 0, 1 };
	protected final Entry[] ENTRIES_CALLER_STACK_METHOD = {
			Entry.thisReference, Entry.someInt };
	/* end final section for setup */

	protected Method[] methods;
	protected Method usedMethod;
	protected LocalVars localVars;
	protected Entry[] usedEntries;
	protected int[] usedIndexes;
	protected Entry[] usedCallerStackEntries;
	protected Stack<Entry> usedStack;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		JavaClass clazz = Repository.lookupClass(USED_CLASS);
		methods = clazz.getMethods();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLocalVarsNull() {
		new LocalVars(null);
	}

	@Test
	public void testLocalVarsEmpty() {
		LocalVars emptyLocalVars = new LocalVars(new LocalVariable[0]);
		assertTrue(emptyLocalVars.getEntriesLength() == emptyLocalVars
				.getIndexesLength());
		assertEquals(0, emptyLocalVars.getEntriesLength());
	}

	@Test
	public void testLocalVars() {
		assertTrue(localVars.getEntriesLength() == localVars.getIndexesLength());
		LocalVariableTable tmp = usedMethod.getLocalVariableTable();
		assertEquals(tmp != null ? tmp.getLocalVariableTable().length
				: new LocalVariableTable[0].length,
				localVars.getEntriesLength());
	}

	@Test
	public void testInitWithArgs() {
		usedStack = new Stack<Entry>();
		for (Entry entry : ENTRIES_CALLER_STACK_CTOR) {
			usedStack.add(entry);
		}
		Entry[] entriesBefore = new Entry[localVars.getEntriesLength()];
		for (int i = 0; i < entriesBefore.length; i++) {
			entriesBefore[i] = localVars.getEntry(i);
		}
		localVars.initWithArgs(usedStack, usedStack.size());
		for (int i = 0; i < localVars.getEntriesLength(); i++) {
			assertEquals(
					"LocalVars' entries should match expected callerStack.",
					usedCallerStackEntries[i], localVars.getEntry(i));
			if (i == 0) {
				assertTrue("First entry should be changed.", !localVars
						.getEntry(i).equals(entriesBefore[i]));
			} else {
				assertEquals("All other entries should not be changed.",
						entriesBefore[i], localVars.getEntry(i));
			}
		}
	}

	@Test(expected = AssertionError.class)
	public void testGetForIndexFail() {
		localVars.getForIndex(Integer.MAX_VALUE);
	}

	@Test
	public void testGetForIndex() {
		for (int i = 0; i < usedIndexes.length; i++) {
			assertEquals(usedEntries[i], localVars.getForIndex(usedIndexes[i]));
		}
	}

	@Test(expected = AssertionError.class)
	public void testSetForIndexFail() {
		localVars.setForIndex(Integer.MAX_VALUE, Entry.someReference);
	}

	@Test
	public void testSetForIndex() {
		for (int i = 0; i < localVars.getIndexesLength(); i++) {
			int index = localVars.getIndex(i);
			Entry localSave = localVars.getEntry(index);
			Entry testItem = localSave.equals(Entry.someBoolean) ? Entry.someInt
					: Entry.someBoolean;
			assertTrue("The testItem must not be the same as the current.",
					!localSave.equals(testItem));
			localVars.setForIndex(index, testItem);
			assertEquals("The entry should be set to the test value.",
					testItem, localVars.getEntry(index));
			localVars.setForIndex(index, localSave);
			assertEquals("The entry should be set to the old value.",
					localSave, localVars.getEntry(index));
		}
	}
}
