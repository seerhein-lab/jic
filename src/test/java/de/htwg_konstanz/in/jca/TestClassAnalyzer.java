package de.htwg_konstanz.in.jca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.junit.Test;

public class TestClassAnalyzer {

	// @Test
	// public void testProperlyConstructedDefaultCtorNoInitializers() {
	// JavaClass clazz = null;
	// try {
	// clazz = Repository
	// .lookupClass("de.htwg_konstanz.in.jca.testclasses.DefaultCtorNoInitializers");
	// } catch (ClassNotFoundException e) {
	// fail("class not found");
	// }
	// assertEquals(new ClassAnalyzer(clazz).properlyConstructed(),
	// ThreeValueBoolean.yes);
	// }

	// @Test
	// public void testProperlyConstructedDefaultCtorInitializersByte() {
	// JavaClass clazz = null;
	// try {
	// clazz = Repository
	// .lookupClass("de.htwg_konstanz.in.jca.testclasses.DefaultCtorInitializersByte");
	// } catch (ClassNotFoundException e) {
	// fail("class not found");
	// }
	// assertEquals(new ClassAnalyzer(clazz).properlyConstructed(),
	// ThreeValueBoolean.yes);
	// }

	// @Test
	// public void testProperlyConstructedDefaultCtorInitializersChar() {
	// JavaClass clazz = null;
	// try {
	// clazz = Repository
	// .lookupClass("de.htwg_konstanz.in.jca.testclasses.DefaultCtorInitializersChar");
	// } catch (ClassNotFoundException e) {
	// fail("class not found");
	// }
	// assertEquals(new ClassAnalyzer(clazz).properlyConstructed(),
	// ThreeValueBoolean.yes);
	// }

	// @Test
	// public void testProperlyConstructedDefaultCtorInitializersLong() {
	// JavaClass clazz = null;
	// try {
	// clazz = Repository
	// .lookupClass("de.htwg_konstanz.in.jca.testclasses.DefaultCtorInitializersLong");
	// } catch (ClassNotFoundException e) {
	// fail("class not found");
	// }
	// assertEquals(new ClassAnalyzer(clazz).properlyConstructed(),
	// ThreeValueBoolean.yes);
	// }

	@Test
	public void testProperlyConstructedDefaultCtorInitializersObject() {
		JavaClass clazz = null;
		try {
			clazz = Repository
					.lookupClass("de.htwg_konstanz.in.jca.testclasses.DefaultCtorInitializersObject");
		} catch (ClassNotFoundException e) {
			fail("class not found");
		}
		assertEquals(new ClassAnalyzer(clazz).properlyConstructed(),
				ThreeValueBoolean.yes);
	}

	// ClassPath cp = new
	// ClassPath("/Users/haase/Documents/workspace/utils/target/classes");
	// SyntheticRepository repo = SyntheticRepository.getInstance(cp);
	// Repository.setRepository(repo);
	// JavaClass clazz =
	// Repository.lookupClass("de.htwg_konstanz.util.GraphicIO");

}
