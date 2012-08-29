package de.htwg_konstanz.in.jca;

import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFrame {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFrame() {
		JavaClass clazz = null;
		try {
			clazz = Repository
					.lookupClass("de.htwg_konstanz.in.jca.testclasses.ClassWithDiverseCtors");
		} catch (ClassNotFoundException e) {
			fail("class not found");
		}

		Constructor[] ctors = de.htwg_konstanz.in.jca.testclasses.ClassWithDiverseCtors.class
				.getConstructors();

		for (Constructor ctor : ctors) {
			// ctor.get;
		}

		// new ClassAnalyzer(clazz).getConstructor()

		fail("Not yet implemented");
	}

	@Test
	public void testPrintLocalVariables() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLocalForOffset() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetLocalForOffset() {
		fail("Not yet implemented");
	}

	@Test
	public void testPushStackLocalVariable() {
		fail("Not yet implemented");
	}

	@Test
	public void testPushStackEntry() {
		fail("Not yet implemented");
	}

	@Test
	public void testPopStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStack() {
		fail("Not yet implemented");
	}

	// TODO have equals and hashCode method generated if
	// logical equality other than reference equality exist.
	// Often the case for value objects.

	// TODO have toString method generated

}
