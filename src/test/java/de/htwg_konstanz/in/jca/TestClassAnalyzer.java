package de.htwg_konstanz.in.jca;

import static org.junit.Assert.assertEquals;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.junit.Test;

import de.htwg_konstanz.in.jca.testclasses.DefaultCtorInitializersByte;
import de.htwg_konstanz.in.jca.testclasses.DefaultCtorInitializersChar;
import de.htwg_konstanz.in.jca.testclasses.DefaultCtorInitializersLong;
import de.htwg_konstanz.in.jca.testclasses.DefaultCtorInitializersLongObject;
import de.htwg_konstanz.in.jca.testclasses.DefaultCtorInitializersObject;
import de.htwg_konstanz.in.jca.testclasses.DefaultCtorNoInitializers;

public class TestClassAnalyzer {

	@Test
	public void testProperlyConstructedDefaultCtorNoInitializers()
			throws ClassNotFoundException {
		JavaClass clazz = Repository
				.lookupClass(DefaultCtorNoInitializers.class.getCanonicalName());

		assertEquals(ClassAnalyzer.indicatesSuccess(new ClassAnalyzer(clazz)
				.properlyConstructed()), ThreeValueBoolean.yes);
	}

	@Test
	public void testProperlyConstructedDefaultCtorInitializersByte()
			throws ClassNotFoundException {
		JavaClass clazz = Repository
				.lookupClass(DefaultCtorInitializersByte.class
						.getCanonicalName());
		assertEquals(ClassAnalyzer.indicatesSuccess(new ClassAnalyzer(clazz)
				.properlyConstructed()), ThreeValueBoolean.yes);
	}

	@Test
	public void testProperlyConstructedDefaultCtorInitializersChar()
			throws ClassNotFoundException {
		JavaClass clazz = Repository
				.lookupClass(DefaultCtorInitializersChar.class
						.getCanonicalName());
		assertEquals(ClassAnalyzer.indicatesSuccess(new ClassAnalyzer(clazz)
				.properlyConstructed()), ThreeValueBoolean.yes);
	}

	@Test
	public void testProperlyConstructedDefaultCtorInitializersLong()
			throws ClassNotFoundException {
		JavaClass clazz = Repository
				.lookupClass(DefaultCtorInitializersLong.class
						.getCanonicalName());
		assertEquals(ClassAnalyzer.indicatesSuccess(new ClassAnalyzer(clazz)
				.properlyConstructed()), ThreeValueBoolean.yes);
	}

	@Test
	public void testProperlyConstructedDefaultCtorInitializersObject()
			throws ClassNotFoundException {
		JavaClass clazz = Repository
				.lookupClass(DefaultCtorInitializersObject.class
						.getCanonicalName());
		assertEquals(ClassAnalyzer.indicatesSuccess(new ClassAnalyzer(clazz)
				.properlyConstructed()), ThreeValueBoolean.yes);
	}

	@Test
	public void testProperlyConstructedDefaultCtorInitializersLongObject()
			throws ClassNotFoundException {
		JavaClass clazz = Repository
				.lookupClass(DefaultCtorInitializersLongObject.class
						.getCanonicalName());
		assertEquals(ClassAnalyzer.indicatesSuccess(new ClassAnalyzer(clazz)
				.properlyConstructed()), ThreeValueBoolean.yes);
	}

	// ClassPath cp = new
	// ClassPath("/Users/haase/Documents/workspace/utils/target/classes");
	// SyntheticRepository repo = SyntheticRepository.getInstance(cp);
	// Repository.setRepository(repo);
	// JavaClass clazz =
	// Repository.lookupClass("de.htwg_konstanz.util.GraphicIO");

}
