package de.seerhein_lab.jca;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;

import org.junit.runner.RunWith;

import de.seerhein_lab.jca.analyzer.ClassAnalyzer;
import de.seerhein_lab.jca.testutils.ClassAnalyzerRunner;
import de.seerhein_lab.jca.testutils.ClassAnalyzerRunner.BindAnalyzerMethod;
import de.seerhein_lab.jca.testutils.BugsExpected;
import de.seerhein_lab.jca.testutils.NoBugsExpected;
import edu.umd.cs.findbugs.BugCollection;

/**
 * Functional acceptance tests for the method properlyConstructed of the class
 * ClassAnalyzer.
 * 
 * @see ProperlyConstructedTestRunner
 */
@RunWith(ClassAnalyzerRunner.class)
public class ProperlyConstructedAcceptanceTest {

	@BindAnalyzerMethod
	public static BugCollection bindClassAnalyzerToProperlyConstructed(
			ClassAnalyzer analyzer) {
		return analyzer.properlyConstructed();
	}

	/**
	 * Simple class without fields and with the default constructor.
	 */
	@NoBugsExpected
	public static class Story000_ClassWithDefaultCtorEmpty {
	}

	/**
	 * Simple class without fields and with an empty constructor.
	 */
	@NoBugsExpected
	public static class Story001_ClassWithNoArgsCtorEmpty {
		public Story001_ClassWithNoArgsCtorEmpty() {
		}
	}

	/**
	 * Class without fields and with a constructor which initializes a byte.
	 */
	@NoBugsExpected
	public static class Story002_ClassWithNoArgsCtorByte {
		public Story002_ClassWithNoArgsCtorByte() {
			@SuppressWarnings("unused")
			byte b = 5;
		}
	}

	/**
	 * Class without fields and with a constructor which initializes an integer.
	 */
	@NoBugsExpected
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
	@NoBugsExpected
	public static class Story004_ClassWithNoArgsCtorChar {
		public Story004_ClassWithNoArgsCtorChar() {
			@SuppressWarnings("unused")
			char c = 'c';
		}
	}

	/**
	 * Class without fields and with a constructor which initializes a long.
	 */
	@NoBugsExpected
	public static class Story005_ClassWithNoArgsCtorLong {
		public Story005_ClassWithNoArgsCtorLong() {
			@SuppressWarnings("unused")
			long l = 25l;
		}
	}

	/**
	 * Class without fields and with a constructor which initializes an object.
	 */
	@NoBugsExpected
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
	@NoBugsExpected
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
	@NoBugsExpected
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
	@NoBugsExpected
	public static class Story009_ClassWithDefaultCtorInitialyzerByte {
		final byte b = 7;
	}

	/**
	 * Simple class with default constructor and with a final char initializer.
	 */
	@NoBugsExpected
	public static class Story010_ClassWithDefaultCtorInitialyzerChar {
		final char c = 'x';
	}

	/**
	 * Simple class with default constructor and with a final long initializer.
	 */
	@NoBugsExpected
	public static class Story011_ClassWithDefaultCtorInitialyzerLong {
		final long l = 4711L;
	}

	/**
	 * Simple class with default constructor and with a final object
	 * initializer.
	 */
	@NoBugsExpected
	public static class Story012_ClassWithDefaultCtorInitialyzerObject {
		final Object o = new Object();
	}

	/**
	 * Simple class with default constructor and with a final long and an object
	 * initializer.
	 */
	@NoBugsExpected
	public static class Story013_ClassWithDefaultCtorInitialyzerLongObject {
		final long l = 4711L;
		final Object o = new Object();
	}

	/**
	 * Simple class with default constructor and with a final initializer for
	 * all 8 primitive types and an object.
	 */
	@NoBugsExpected
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
	@NoBugsExpected
	public static class Story015_ClassWithOneArgCtorByte {
		@SuppressWarnings("unused")
		private byte b;

		public Story015_ClassWithOneArgCtorByte(byte b) {
			this.b = b;
		}

	}

	/**
	 * Class with a constructor with a single byte argument.
	 */
	@NoBugsExpected
	public static class Story016_WhileLoop {
		public Story016_WhileLoop() {
			int i = 15;
			while (i != 0) {
				i++;
			}
			i--;
		}
	}

	/**
	 * Test class containing a nested loop.
	 */
	@NoBugsExpected
	public static class Story016_NestedLoop {
		public Story016_NestedLoop() {
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < i; j++) {
					@SuppressWarnings("unused")
					int k = i + j;
				}
			}
		}
	}

	/**
	 * Class with a constructor with a single byte argument.
	 */
	@NoBugsExpected
	public static class Story017_ClassWithOneArgCtorByte {
		public Story017_ClassWithOneArgCtorByte() {
			double d = 15;
			if (d == 0.0) {
				d = 12;
			}
			d = d + 1.1;
		}
	}

	/**
	 * Class which only throws an exception.
	 */
	@NoBugsExpected
	public static class Story018_SimpleClassWithThrows {
		public Story018_SimpleClassWithThrows() throws IOException {
			throw new IOException();
		}
	}

	/**
	 * Throws an exception and catches it immediately.
	 */
	@NoBugsExpected
	public static class Story019_SimpleClassWithTryCatch {
		public Story019_SimpleClassWithTryCatch() {
			try {
				throw new IOException();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Class which throws an exception and catches it immediately. Assigning the
	 * this-reference to an arg is not a problem and should not be detected.
	 */
	@NoBugsExpected
	public static class Story020_SimpleClassWithTryCatchOneParm {
		public Story020_SimpleClassWithTryCatchOneParm(Object obj) {
			try {
				throw new IOException();
			} catch (IOException e) {
				obj = this;
			}
		}
	}


	/**
	 * Class which throws an exception and catches it immediately. In the catch clause, 
	 * the this reference escapes.
	 */
	@BugsExpected
	public static class Story020a_SimpleClassWithTryCatchOneParm {
		static Story020a_SimpleClassWithTryCatchOneParm reference;
		
		public Story020a_SimpleClassWithTryCatchOneParm() {
			try {
				throw new IOException();
			} catch (IOException e) {
				reference = this;
			}
		}
	}

	
	
	/**
	 * Class which throws an exception and catches it immediately. Assigning the
	 * this-reference to a field could be a problem and should not be detected.
	 * Due to combination of the results "UnKnown" and not "ProperlyConstructed"
	 * must be returned.
	 */
	@NoBugsExpected
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

	/**
	 * Class which throws an exception in some cases and catches it immediately.
	 * Tests exception handling combined with a BranchInstruction.
	 */
	@NoBugsExpected
	public static class Story022_ClassWithSingleArgumentSimpleExceptionHandling {
		public Story022_ClassWithSingleArgumentSimpleExceptionHandling(int i) {
			try {
				if (i > 2)
					throw new IOException();
				i++;
			} catch (IOException e) {
				i--;
			}
		}
	}

	/**
	 * More complex class which throws an exception in some cases and catches it
	 * immediately. Tests exception handling combined with a BranchInstruction.
	 */
	@NoBugsExpected
	public static class Story023_ClassWithSingleArgumentExceptionHandling {
		public Story023_ClassWithSingleArgumentExceptionHandling(int j) {
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

	/**
	 * Class which combines TryCatchFinally with a BranchInstruction.
	 */
	@NoBugsExpected
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

	/**
	 * Class which combines TryFinally with multiple catches and a
	 * BranchInsturction. Tests if multiple catches are considered by the
	 * exception handling.
	 */
	@NoBugsExpected
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

	/**
	 * Class which combines TryFinally with multiple catches and a
	 * BranchInsturction. Tests if the exception handling allows to throw
	 * exceptions in a catch block.
	 */
	@NoBugsExpected
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

	/**
	 * Class with a method throwing an exception. Tests if exception handling
	 * can detect methods throwing an exception.
	 */
	@NoBugsExpected
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

	/**
	 * Class containing a exception throwing method and multiple tryCatch
	 * blocks. Tests if exception handling considers all possible paths.
	 */
	@NoBugsExpected
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

	/**
	 * Complex class for exception handling test. Combines methods with a
	 * BranchInstruction, TryFinally and multiple catch blocks.
	 */
	@NoBugsExpected
	public static class Story029_ClassWithMultipleTryCatchFinally {
		private void thrower(int i) throws Exception {
			try {
				if (i != 0)
					throw new IOException();
				else
					i /= 20;
			} catch (IOException e) {
				i--;
			} catch (Exception e) {
				i++;
			} finally {
				i *= 16;
			}
			i /= 5;
		}

		public Story029_ClassWithMultipleTryCatchFinally(int i)
				throws IOException {
			try {
				thrower(i);
			} catch (IOException e) {
				i--;
			} catch (Exception e) {
				i++;
			} finally {
				i *= 16;
			}
			i /= 5;
		}
	}

	/**
	 * Class with multiple constructors using this().
	 */
	@NoBugsExpected
	public static class Story030_MultipleCtorsUsingThis {
		public Story030_MultipleCtorsUsingThis(int i, String s) {
			this(i, (Object) s);
		}

		public Story030_MultipleCtorsUsingThis(int i) {
			this(i, (Object) null);
		}

		public Story030_MultipleCtorsUsingThis(int i, Object o) {
		}
	}
	
	@BugsExpected 
	public static class Story031_Listener implements ActionListener {
		public Story031_Listener(JButton button) {
			button.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {			
		}
	}
	
	@BugsExpected 
	public static class Story032_AnonymousListener  {
		public Story032_AnonymousListener(JButton button) {
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
				}
			});
		}
	}
	
	@BugsExpected 
	public static class Story033_NamedInnerInstanceListener  {
		private class Listener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			}
		}
		
		public Story033_NamedInnerInstanceListener(JButton button) {
			button.addActionListener(new Listener());
		}
	}
	

	@NoBugsExpected 
	public static class Story034_StaticInnerInstanceListener  {
		private static class Listener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			}
		}
		
		public Story034_StaticInnerInstanceListener(JButton button) {
			button.addActionListener(new Listener());
		}
	}
	
	

}
