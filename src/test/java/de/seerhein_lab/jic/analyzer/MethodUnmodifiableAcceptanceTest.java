package de.seerhein_lab.jic.analyzer;

import java.util.Collection;

import org.junit.runner.RunWith;

import de.seerhein_lab.jic.testutils.BugsExpected;
import de.seerhein_lab.jic.testutils.ClassAnalyzerRunner;
import de.seerhein_lab.jic.testutils.ClassAnalyzerRunner.BindAnalyzerMethod;
import de.seerhein_lab.jic.testutils.NoBugsExpected;
import edu.umd.cs.findbugs.BugInstance;

/**
 * Functional acceptance tests for the method properlyConstructed of the class
 * ClassAnalyzer.
 * 
 * @see ProperlyConstructedTestRunner
 */
@RunWith(ClassAnalyzerRunner.class)
@SuppressWarnings("unused")
public class MethodUnmodifiableAcceptanceTest {
	@BindAnalyzerMethod
	public static Collection<BugInstance> bindClassAnalyzerToProperlyConstructed(
			ClassAnalyzer analyzer) {
		analyzer.ctorsUnmodifiable();
		return analyzer.methodsUnmodifiable();
	}

	@BugsExpected
	public static class DetectXAStoreBug_Int {
		private final int[] f = new int[5];

		public void modifie(int i) {
			f[0] = i;
		}
	}

	@BugsExpected
	public static class DetectXAStoreBug_Null {
		private final Object[] f = new Object[5];

		public void modifie() {
			f[0] = null;
		}
	}

	@BugsExpected
	public static class DetectXAStoreBug_ReferenceToArrayReferredByThis {
		private final Object[] f = new Object[5];

		public void modifie(Object i) {
			f[0] = i;
		}
	}

	@NoBugsExpected
	public static class DetectXAStoreBug_ReferenceToInternalArray {
		public void modifie() {
			Object[] array = new Object[1];
			array[0] = new Object();
		}
	}

	@BugsExpected
	public static class DetectXAStoreBug_ReferenceToArrayTransitivelyReferredByThis {
		private final TestClass[] f = new TestClass[5];

		public DetectXAStoreBug_ReferenceToArrayTransitivelyReferredByThis() {
			f[0] = new TestClass();
		}

		public void modifie(int i) {
			f[0].i = 5;
		}
	}

	@BugsExpected
	public static class DetectXAStoreBug_ReferenceToArrayTransitivelyReferredByThis2 {
		private final TestClass[] f = new TestClass[5];

		public DetectXAStoreBug_ReferenceToArrayTransitivelyReferredByThis2() {
			f[0] = new TestClass();
		}

		public void modifie(int i) {
			f[0].klass = new Object();
		}
	}

	@BugsExpected
	public static class DetectPutFieldBug_Null {
		private final TestClass f = new TestClass();

		public void modifie() {
			f.klass = null;
		}
	}

	@BugsExpected
	public static class DetectPutFieldBug_ReferenceToObjectReferredByThis {
		private final TestClass f = new TestClass();

		public void modifie(Object o) {
			f.klass = o;
		}
	}

	@NoBugsExpected
	public static class DetectPutFieldBug_ReferenceToInternalObject {
		public void modifie() {
			TestClass tc = new TestClass();
			tc.klass = new Object();
		}
	}

	@BugsExpected
	public static class DetectPutFieldBug_ReferenceToObjectTransitivelyReferredByThis {
		private final TestClass f = new TestClass();

		public DetectPutFieldBug_ReferenceToObjectTransitivelyReferredByThis() {
			f.tc = new TestClass();
		}

		public void modifie(int i) {
			f.tc.i = 5;
		}
	}

	@BugsExpected
	public static class DetectPutFieldBug_ReferenceToObjectTransitivelyReferredByThis2 {
		private final TestClass f = new TestClass();

		public DetectPutFieldBug_ReferenceToObjectTransitivelyReferredByThis2() {
			f.tc = new TestClass();
		}

		public void modifie(int i) {
			f.tc.klass = new Object();
		}
	}

	private static class TestClass {
		public Object[] array;
		public Object klass;
		public TestClass tc;
		public int i;
	}

	private static class TestClassStatic {
		public static Object[] array;
		public static Object klass;
		public static TestClass tc;
		public static int i;
	}
}
