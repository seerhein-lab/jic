package de.seerhein_lab.jca;

import org.junit.runner.RunWith;

import de.seerhein_lab.jca.analyzer.ClassAnalyzer;
import de.seerhein_lab.jca.testutils.BugsExpected;
import de.seerhein_lab.jca.testutils.ClassAnalyzerRunner;
import de.seerhein_lab.jca.testutils.ClassAnalyzerRunner.BindAnalyzerMethod;
import de.seerhein_lab.jca.testutils.NoBugsExpected;
import edu.umd.cs.findbugs.BugCollection;

/**
 * Functional acceptance tests for the method properlyConstructed of the class
 * ClassAnalyzer.
 * 
 * @see ProperlyConstructedTestRunner
 */
@RunWith(ClassAnalyzerRunner.class)
public class CtorArgsCpoiedAcceptanceTest {

	@BindAnalyzerMethod
	public static BugCollection bindClassAnalyzerToProperlyConstructed(
			ClassAnalyzer analyzer) {
		return analyzer.ctorParamsAreCopied();
	}

	/**
	 * Simple class without fields and with the default constructor.
	 */
	@NoBugsExpected
	public static class ClassWithDefaultCtorEmpty {
	}

	@NoBugsExpected
	public static class NewObjectFieldReference {
		private final Object o = new Object();
	}

	@NoBugsExpected
	public static class NewObjectFieldInt {
		private final int i = 5;
	}

	/**
	 * Set Field to null.
	 */
	@NoBugsExpected
	public static class SetFieldToNull {
		private final Object o = null;
	}

	@BugsExpected
	public static class DetectVirtualMethodBug_ReferredByThis {
		private final Object o = new Object();

		public DetectVirtualMethodBug_ReferredByThis() {
			equals(o);
		}
	}

	@BugsExpected
	public static class DetectVirtualMethodBug_TransitivelyReferredByThis {
		private final TestClass o = new TestClass();

		public DetectVirtualMethodBug_TransitivelyReferredByThis() {
			Object object = new Object();
			this.o.tc = new TestClass();
			this.o.tc.klass = object;
			equals(object);
		}
	}

	@NoBugsExpected
	public static class DetectXAStoreBug_Int {
		private final int[] f = new int[10];

		public DetectXAStoreBug_Int(int i) {
			f[0] = i;
		}
	}

	@BugsExpected
	public static class DetectXAStoreBug_ExternalReference {
		private final Object[] f = new Object[10];

		public DetectXAStoreBug_ExternalReference(Object o) {
			f[0] = o;
		}
	}

	@NoBugsExpected
	public static class DetectXAStoreBug_InteranlReference {
		private final Object[] f = new Object[10];

		public DetectXAStoreBug_InteranlReference(Object o) {
			f[0] = new Object();
		}
	}

	@NoBugsExpected
	public static class DetectXAStoreBug_ToArrayNotReferredByThis {
		public DetectXAStoreBug_ToArrayNotReferredByThis(Object o) {
			Object[] f = new Object[10];
			f[0] = o;
		}
	}

	@BugsExpected
	public static class DetectXAStoreBug_TransitivelyReferredByThis {
		private final TestClass f = new TestClass();

		public DetectXAStoreBug_TransitivelyReferredByThis(Object o) {
			f.array = new Object[5];
			f.array[0] = o;
		}
	}

	@BugsExpected
	public static class DetectXAStoreBug_TransitivelyReferredByThis2 {
		private final Object[] f = new Object[10];

		public DetectXAStoreBug_TransitivelyReferredByThis2(Object o) {
			TestClass tc = new TestClass();
			tc.array = new Object[1];
			tc.array[0] = o;
			f[0] = tc;
		}
	}

	@NoBugsExpected
	public static class DetectPutFieldBug_Int {
		private final int f;

		public DetectPutFieldBug_Int(int o) {
			f = o;
		}
	}

	@NoBugsExpected
	public static class DetectPutFieldBug_InternalReference {
		private final Object f;

		public DetectPutFieldBug_InternalReference(Object o) {
			f = new Object();
		}
	}

	@BugsExpected
	public static class DetectPutFieldBug_ExternalReference {
		private final Object f;

		public DetectPutFieldBug_ExternalReference(Object o) {
			f = o;
		}
	}

	@BugsExpected
	public static class DetectPutFieldBug_TransitivelyReferredByThis {
		private final TestClass f;

		public DetectPutFieldBug_TransitivelyReferredByThis(Object o) {
			f = new TestClass();
			f.tc = new TestClass();
			f.tc.tc = new TestClass();
			f.tc.tc.klass = o;

		}
	}

	@BugsExpected
	public static class DetectPutFieldBug_TransitivelyReferredByThis2 {
		private final TestClass f;

		public DetectPutFieldBug_TransitivelyReferredByThis2(Object o) {
			TestClass testClass = new TestClass();
			testClass.tc = new TestClass();
			testClass.tc.klass = o;

			f = new TestClass();
			f.tc = testClass;
		}
	}

	@BugsExpected
	public static class DetectPutFieldBug_TransitivelyReferredByThis3 {
		private final TestClass f;

		public DetectPutFieldBug_TransitivelyReferredByThis3(Object o) {
			TestClass testClass = new TestClass();
			testClass.tc = new TestClass();
			testClass.tc.klass = o;

			f = testClass;
		}
	}

	@BugsExpected
	public static class DetectPutStaticBug_This {
		public DetectPutStaticBug_This(Object o) {
			TestClassStatic.klass = this;
		}
	}

	@BugsExpected
	public static class DetectPutStaticBug_ThisSameClass {
		public static Object o;

		public DetectPutStaticBug_ThisSameClass(Object o) {
			DetectPutStaticBug_ThisSameClass.o = this;
		}
	}

	@BugsExpected
	public static class DetectPutStaticBug_TransitivelyReferredByThis {
		public final TestClass o = new TestClass();

		public DetectPutStaticBug_TransitivelyReferredByThis(Object o) {
			TestClass testClass = new TestClass();
			this.o.tc = testClass;
			TestClassStatic.tc = testClass;
		}
	}

	private static class TestClass {
		public Object[] array;
		public Object klass;
		public TestClass tc;
	}

	private static class TestClassStatic {
		public static Object[] array;
		public static Object klass;
		public static TestClass tc;
	}

}