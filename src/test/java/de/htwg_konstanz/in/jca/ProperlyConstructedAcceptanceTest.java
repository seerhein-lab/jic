package de.htwg_konstanz.in.jca;

import net.jcip.annotations.Immutable;

import org.junit.runner.RunWith;

/**
 * Functional acceptance tests for the method isImmutable of the class
 * ClassAnalyzer.
 * 
 * <ul>
 * <li>The class which should be analyzed as immutable by the ClassAnalyzer are
 * marked with the annotation {@link Immutable}. Classes which are not marked by
 * this annotation are not immutable.
 * 
 * <li>The test logic and binding is implemented in a project specific JUnit
 * runner {@link AbstractTestRunner}.
 * 
 * <li>The definition of a immutable class see JavaDoc of annotation
 * {@link Immutable}.
 * </ul>
 * 
 * @see Immutable
 * @see AbstractTestRunner
 */
@RunWith(ProperlyConstructedTestRunner.class)
public class ProperlyConstructedAcceptanceTest {

	/**
	 * Simple class without fields and with the default constructor.
	 */
	@Yes
	public static class Story000_ClassWithDefaultCtorEmpty {
	}

	/**
	 * Simple class without fields and with an empty constructor.
	 */
	@Yes
	public static class Story001_ClassWithNoArgsCtorEmpty {
		public Story001_ClassWithNoArgsCtorEmpty() {
		}
	}

	/**
	 * Class without fields and with a constructor which initializes a byte.
	 */
	@Yes
	public static class Story002_ClassWithNoArgsCtorByte {
		public Story002_ClassWithNoArgsCtorByte() {
			byte b = 5;
		}
	}

	/**
	 * Class without fields and with a constructor which initializes an integer.
	 */
	@Yes
	public static class Story003_ClassWithNoArgsCtorInt {
		public Story003_ClassWithNoArgsCtorInt() {
			int i = 20;
		}
	}

	/**
	 * Class without fields and with a constructor which initializes a
	 * character.
	 */
	@Yes
	public static class Story004_ClassWithNoArgsCtorChar {
		public Story004_ClassWithNoArgsCtorChar() {
			char c = 'c';
		}
	}

	/**
	 * Class without fields and with a constructor which initializes a long.
	 */
	@Yes
	public static class Story005_ClassWithNoArgsCtorLong {
		public Story005_ClassWithNoArgsCtorLong() {
			long l = 25l;
		}
	}

	/**
	 * Class without fields and with a constructor which initializes an object.
	 */
	@Yes
	public static class Story006_ClassWithNoArgsCtorObject {
		public Story006_ClassWithNoArgsCtorObject() {
			Object o = new Object();
		}
	}

	/**
	 * Class without fields and with a constructor which initializes a long and
	 * an object.
	 */
	@Yes
	public static class Story007_ClassWithNoArgsLongObject {
		public Story007_ClassWithNoArgsLongObject() {
			long l = 1l;
			Object o = new Object();
		}
	}

	/**
	 * Class without fields and with a constructor which initializes all 8
	 * primitive types and an object.
	 */
	@Yes
	public static class Story008_ClassWithNoArgsCtorAllTypes {
		public Story008_ClassWithNoArgsCtorAllTypes() {
			byte b = 7;
			short s = 14;
			int i = 815;
			long l = 4711L;
			float f = 3.14f;
			double d = 2.18;
			char c = 'x';
			boolean bool = true;
			Object o = new Object();
		}
	}

	/**
	 * Simple class with default constructor and with a final byte initializer.
	 */
	@Yes
	public static class Story009_ClassWithDefaultCtorInizialyzerByte {
		final byte b = 7;
	}

	/**
	 * Simple class with default constructor and with a final char initializer.
	 */
	@Yes
	public static class Story010_ClassWithDefaultCtorInitialyzerChar {
		final char c = 'x';
	}

	/**
	 * Simple class with default constructor and with a final long initializer.
	 */
	@Yes
	public static class Story011_ClassWithDefaultCtorInitialyzerLong {
		final long l = 4711L;
	}

	/**
	 * Simple class with default constructor and with a final object
	 * initializer.
	 */
	@Yes
	public static class Story012_ClassWithDefaultCtorInitialyzerObject {
		final Object o = new Object();
	}

	/**
	 * Simple class with default constructor and with a final long and an object
	 * initializer.
	 */
	@Yes
	public static class Story013_ClassWithDefaultCtorInitialyzerLongObject {
		final long l = 4711L;
		final Object o = new Object();
	}

	/**
	 * Simple class with default constructor and with a final initializer for
	 * all 8 primitive types and an object.
	 */
	@Yes
	public static class Story014_ClassWithDefaultCtorInitialyzerAllTypes {
		final byte b = 7;
		final short s = 14;
		final int i = 815;
		final long l = 4711L;
		final float f = 3.14f;
		final double d = 2.18;
		final char c = 'x';
		final boolean bool = true;
		final Object o = new Object();
	}

	/**
	 * Class with a constructor with a single byte argument.
	 */
	@Yes
	public static class Story015_ClassWithOneArgCtorByte {
		final Class item = new Class((byte) 15);

		public static final class Class {

			private byte b;

			public Class(byte b) {
				this.b = b;
			}
		}
	}

}