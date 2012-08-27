package de.htwg_konstanz.in.jca;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;
import org.junit.Before;
import org.junit.Test;

public class TestLocalVarsCtor extends TestLocalVars {

	@Before
	public void setUp() throws Exception {
		super.setUp();
		for (Method method : super.methods) {
			if (method.getName().equals("<init>")) {
				super.usedMethod = method;
				break;
			}
		}
		LocalVariable[] localVariables = (super.usedMethod
				.getLocalVariableTable() != null) ? super.usedMethod
				.getLocalVariableTable().getLocalVariableTable()
				: new LocalVariable[0];
		super.localVars = new LocalVars(localVariables);
	}

	@Test
	public void testInitWithArgs() {
		super.usedCallerStackEntries = super.ENTRIES_CALLER_STACK_CTOR;
		super.testInitWithArgs();
	}

	@Test
	public void testGetForIndex() {
		super.usedEntries = super.EXPECTED_ENTRIES_CTOR;
		super.usedIndexes = super.EXPECTED_INDEXES_CTOR;
		super.testGetForIndex();
	}

	@Test
	public void testSetForIndex() {
		super.usedEntries = super.EXPECTED_ENTRIES_CTOR;
		super.usedIndexes = super.EXPECTED_INDEXES_CTOR;
		super.testSetForIndex();
	}
}
