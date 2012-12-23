package de.htwg_konstanz.in.jca;

import java.io.IOException;

import org.junit.runner.RunWith;

import de.htwg_konstanz.in.jca.testutils.ClassAnalyzerRunner;
import de.htwg_konstanz.in.jca.testutils.ClassAnalyzerRunner.BindAnalyzerMethod;
import de.htwg_konstanz.in.jca.testutils.No;
import de.htwg_konstanz.in.jca.testutils.Yes;
import edu.umd.cs.findbugs.BugCollection;

/**
 * Functional acceptance tests for the method properlyConstructed of the class
 * ClassAnalyzer.
 * 
 * TODO: JavaDoc
 * 
 * @see ProperlyConstructedTestRunner
 */
@RunWith(ClassAnalyzerRunner.class)
public class ProperlyConstructedAcceptanceTest {

	@BindAnalyzerMethod
	public static BugCollection bindClassAnalyzerToProperlyConstructed(
			PropConClassAnalyzer analyzer) {
		return analyzer.properlyConstructed();
	}

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
			@SuppressWarnings("unused")
			byte b = 5;
		}
	}

	/**
	 * Class without fields and with a constructor which initializes an integer.
	 */
	@Yes
	public static class Story003_ClassWithNoArgsCtorInt {
		public Story003_ClassWithNoArgsCtorInt() {
			@SuppressWarnings("unused")
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
			@SuppressWarnings("unused")
			char c = 'c';
		}
	}

	/**
	 * Class without fields and with a constructor which initializes a long.
	 */
	@Yes
	public static class Story005_ClassWithNoArgsCtorLong {
		public Story005_ClassWithNoArgsCtorLong() {
			@SuppressWarnings("unused")
			long l = 25l;
		}
	}

	/**
	 * Class without fields and with a constructor which initializes an object.
	 */
	@Yes
	public static class Story006_ClassWithNoArgsCtorObject {
		public Story006_ClassWithNoArgsCtorObject() {
			@SuppressWarnings("unused")
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
			@SuppressWarnings("unused")
			long l = 1l;
			@SuppressWarnings("unused")
			Object o = new Object();
		}
	}

	/**
	 * Class without fields and with a constructor which initializes all 8
	 * primitive types and an object.
	 */
	@Yes
	public static class Story008_ClassWithNoArgsCtorAllTypes {
		@SuppressWarnings("unused")
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
	public static class Story009_ClassWithDefaultCtorInitialyzerByte {
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
		@SuppressWarnings("unused")
		private byte b;

		public Story015_ClassWithOneArgCtorByte(byte b) {
			this.b = b;
		}

	}

	// TODO: other param types, more than one param

	/**
	 * Class with a constructor with a single byte argument.
	 */
	@Yes
	public static class Story016_WhileLoop {
		public Story016_WhileLoop() {
			int i = 15;
			while (i != 0) {
				i++;
			}
			i--;
		}
	}

	@Yes
	public static class Story016_NestedLoop {
		public Story016_NestedLoop() {
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < i; j++) {
					int k = i + j;
				}
			}
		}
	}

	@Yes
	public static class JsrTest {
		public JsrTest() {
			int i = 0;
			try {
				i++;
			} finally {
				i--;
			}
		}
	}

	/**
	 * Class with a constructor with a single byte argument.
	 */
	@Yes
	public static class Story017_ClassWithOneArgCtorByte {
		public Story017_ClassWithOneArgCtorByte() {
			double d = 15;
			if (d == 0.0) {
				d = 12;
			}
			d = d + 1.1;
		}
	}

	@Yes
	public static class Story018_SimpleClassWithThrows {
		public Story018_SimpleClassWithThrows() throws IOException {
			throw new IOException();
		}
	}

	@Yes
	public static class Story019_SimpleClassWithTryCatch {
		public Story019_SimpleClassWithTryCatch() {
			try {
				throw new IOException();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Yes
	public static class Story020_SimpleClassWithTryCatchOneParm {
		public Story020_SimpleClassWithTryCatchOneParm(Object obj) {
			try {
				throw new IOException();
			} catch (IOException e) {
				obj = this;
			}
		}
	}

	@No
	public static class Story021_SimpleClassWithTryCatchNoParm {
		@SuppressWarnings("unused")
		private Object obj;

		public Story021_SimpleClassWithTryCatchNoParm() {
			try {
				obj = this;
				throw new IOException();
			} catch (IOException e) {
				obj = new Object();
			}
		}
	}

	@Yes
	public static class Story022_ClassWithSingleArgumentExceptionHandling {
		public Story022_ClassWithSingleArgumentExceptionHandling(int j) {
			int i = 0;
			try {
				i = i / j;
				if (i > 3)
					throw new IOException();
				i++;
			} catch (IOException e) {
				j++;
			}
		}
	}

	@Yes
	public static class Story023_ClassWithSingleArgumentSimpleExceptionHandling {
		public Story023_ClassWithSingleArgumentSimpleExceptionHandling(int i) {
			try {
				if (i > 2)
					throw new IOException();
				i++;
			} catch (IOException e) {
				i--;
			}
		}
	}

	@Yes
	public static class Story024_ClassWithSingleArgumentSimpleTryCatchFinally {
		public Story024_ClassWithSingleArgumentSimpleTryCatchFinally(int i) {
			try {
				if (i > 2)
					throw new IOException();
				i++;
			} catch (IOException e) {
				i--;
			} finally {
				i += 2;
			}
		}
	}

	@Yes
	public static class Story025_ClassWithSingleArgumentTryDoubleCatchFinally {
		public Story025_ClassWithSingleArgumentTryDoubleCatchFinally(int i) {
			try {
				if (i > 2)
					throw new IOException();
				i++;
			} catch (IOException e) {
				i--;
			} catch (Exception e) {
				i += 5;
			} finally {
				i += 2;
			}
		}
	}

	@Yes
	public static class Story026_ClassWithSingleArgumentTryDoubleCatchFinallyMultipleExceptions {
		public Story026_ClassWithSingleArgumentTryDoubleCatchFinallyMultipleExceptions(
				int i) throws Exception {
			try {
				if (i > 2)
					throw new IOException();
				i++;
			} catch (IOException e) {
				i--;
			} catch (Exception e) {
				i += 5;
				throw new Exception();
			} finally {
				i += 2;
			}
		}
	}

	@Yes
	public static class Story027_ClassWithSimpleExceptionThrowingMethod {

		private void thrower() throws IOException {
			throw new IOException();
		}

		public Story027_ClassWithSimpleExceptionThrowingMethod(int i)
				throws Exception {
			try {
				thrower();
			} catch (IOException e) {
				i--;
			}
		}
	}

	@Yes
	public static class Story028_ClassWithNestedExceptionhandling {

		private void thrower(int i) throws IOException {
			try {
				throw new IOException();
			} catch (Exception e) {
				i *= 15;
			}
		}

		public Story028_ClassWithNestedExceptionhandling(int i)
				throws Exception {
			try {
				thrower(i);
			} catch (IOException e) {
				i--;
			}
			i /= 5;
		}
	}
}
