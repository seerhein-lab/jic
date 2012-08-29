package de.htwg_konstanz.in.jca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.junit.Test;

import de.htwg_konstanz.in.jca.testclasses.DefaultCtorNoInitializers;

public class TestClassAnalyzer {

	@Test
	public void testProperlyConstructedDefaultCtorNoInitializers() {
		JavaClass clazz = null;
		try {
			clazz = Repository.lookupClass(DefaultCtorNoInitializers.class
					.getCanonicalName());
		} catch (ClassNotFoundException e) {
			fail("class not found");
		}
		assertEquals(ClassAnalyzer.bugCollection2successfull(new ClassAnalyzer(
				clazz).properlyConstructed()), ThreeValueBoolean.yes);
	}

	@Test
	public void testProperlyConstructedDefaultCtorInitializersByte() {
		JavaClass clazz = null;
		try {
			clazz = Repository.lookupClass(DefaultCtorInitializersByte.class
					.getCanonicalName());
		} catch (ClassNotFoundException e) {
			fail("class not found");
		}
		assertEquals(ClassAnalyzer.bugCollection2successfull(new ClassAnalyzer(
				clazz).properlyConstructed()), ThreeValueBoolean.yes);
	}

	@Test
	public void testProperlyConstructedDefaultCtorInitializersChar() {
		JavaClass clazz = null;
		try {
			clazz = Repository.lookupClass(DefaultCtorInitializersChar.class
					.getCanonicalName());
		} catch (ClassNotFoundException e) {
			fail("class not found");
		}
		assertEquals(ClassAnalyzer.bugCollection2successfull(new ClassAnalyzer(
				clazz).properlyConstructed()), ThreeValueBoolean.yes);
	}

	@Test
	public void testProperlyConstructedDefaultCtorInitializersLong() {
		JavaClass clazz = null;
		try {
			clazz = Repository.lookupClass(DefaultCtorInitializersLong.class
					.getCanonicalName());
		} catch (ClassNotFoundException e) {
			fail("class not found");
		}
		assertEquals(ClassAnalyzer.bugCollection2successfull(new ClassAnalyzer(
				clazz).properlyConstructed()), ThreeValueBoolean.yes);
	}

	@Test
	public void testProperlyConstructedDefaultCtorInitializersObject() {
		JavaClass clazz = null;
		try {
			clazz = Repository.lookupClass(DefaultCtorInitializersObject.class
					.getCanonicalName());
		} catch (ClassNotFoundException e) {
			fail("class not found");
		}
		assertEquals(ClassAnalyzer.bugCollection2successfull(new ClassAnalyzer(
				clazz).properlyConstructed()), ThreeValueBoolean.yes);
	}

	@Test
	public void testProperlyConstructedDefaultCtorInitializersLongObject() {
		JavaClass clazz = null;
		try {
			clazz = Repository
					.lookupClass(DefaultCtorInitializersLongObject.class
							.getCanonicalName());
		} catch (ClassNotFoundException e) {
			fail("class not found");
		}
		assertEquals(ClassAnalyzer.bugCollection2successfull(new ClassAnalyzer(
				clazz).properlyConstructed()), ThreeValueBoolean.yes);
	}

	// ClassPath cp = new
	// ClassPath("/Users/haase/Documents/workspace/utils/target/classes");
	// SyntheticRepository repo = SyntheticRepository.getInstance(cp);
	// Repository.setRepository(repo);
	// JavaClass clazz =
	// Repository.lookupClass("de.htwg_konstanz.util.GraphicIO");

}
