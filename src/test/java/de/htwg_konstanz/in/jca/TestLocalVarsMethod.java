package de.htwg_konstanz.in.jca;

import static org.junit.Assert.fail;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;
import org.junit.Before;
import org.junit.Test;

public class TestLocalVarsMethod extends TestLocalVars {

	@Before
	public void setUp() throws Exception {
		if (super.HAS_METHODS) {
			super.setUp();
			for (Method method : super.methods) {
				if (!method.getName().equals("<init>")) {
					super.usedMethod = method;
					break;
				}
			}
			LocalVariable[] localVariables = (super.usedMethod
					.getLocalVariableTable() != null) ? super.usedMethod
					.getLocalVariableTable().getLocalVariableTable()
					: new LocalVariable[0];
			super.localVars = new LocalVars(localVariables);
		} else {
			fail("Class does not have a method.");
		}
	}

	@Test
	public void testInitWithArgs() {
		if (super.HAS_METHODS) {
			super.usedCallerStackEntries = super.ENTRIES_CALLER_STACK_METHOD;
			super.testInitWithArgs();
		} else {
			fail("Class does not have a method.");
		}
	}

	@Test
	public void testGetForIndex() {
		if (super.HAS_METHODS) {
			super.usedEntries = super.EXPECTED_ENTRIES_METHOD;
			super.usedIndexes = super.EXPECTED_INDEXES_METHOD;
			super.testGetForIndex();
		} else {
			fail("Class does not have a method.");
		}
	}

	@Test
	public void testSetForIndex() {
		if (super.HAS_METHODS) {
			super.usedEntries = super.EXPECTED_ENTRIES_METHOD;
			super.usedIndexes = super.EXPECTED_INDEXES_METHOD;
			super.testSetForIndex();
		} else {
			fail("Class does not have a method.");
		}
	}

	@Test
	public void testLocalVars() {
		if (super.HAS_METHODS) {
			super.testLocalVars();
		} else {
			fail("Class does not have a method.");
		}
	}

	@Test
	public void testGetForIndexFail() {
		if (super.HAS_METHODS) {
			super.testGetForIndexFail();
		} else {
			fail("Class does not have a method.");
		}
	}

	@Test
	public void testSetForIndexFail() {
		if (super.HAS_METHODS) {
			super.testSetForIndexFail();
		} else {
			fail("Class does not have a method.");
		}
	}
}
