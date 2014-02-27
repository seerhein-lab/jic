package de.seerhein_lab.jic.analyzer;

import java.util.Collection;

import net.jcip.annotations.Immutable;

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
public class FieldsNotPublishedAcceptanceTest {

	@BindAnalyzerMethod
	public static Collection<BugInstance> bindClassAnalyzerToProperlyConstructed(
			ClassAnalyzer analyzer) {
		analyzer.ctorsUnmodifiable();
		return analyzer.methodsUnmodifiable();
	}

	@NoBugsExpected
	public static class DetectVirtualMethodBug_This {
		public void publish() {
			new PublishMethod().publish(this);
		}
	}

	@NoBugsExpected
	public static class DetectVirtualMethodBug_Null {
		public void publish() {
			new PublishMethod().publish(null);
		}
	}

	@NoBugsExpected
	public static class DetectVirtualMethodBug_Reference {
		public void publish() {
			new PublishMethod().publish(new Object());
		}
	}

	@BugsExpected
	public static class DetectVirtualMethodBug_ReferenceReferredByThis {
		private final Object o = new Object();

		public void publish() {
			new PublishMethod().publish(this.o);
		}
	}

	@NoBugsExpected
	public static class DetectVirtualMethodBug_ImmutableReferenceReferredByThis {
		private final Object o = new ImmutableTestClass();

		public void publish() {
			new PublishMethod().publish(this.o);
		}
	}

	@NoBugsExpected
	public static class DetectVirtualMethodBug_IntReferredByThis {
		private final int i = 5;

		public void publish() {
			new PublishMethod().publish(i);
		}
	}

	@BugsExpected
	public static class DetectVirtualMethodBug_TransitivelyReferredByThis {
		private final TestClass o = new TestClass();

		public void publish() {
			Object object = new Object();
			this.o.tc = new TestClass();
			this.o.tc.klass = object;
			new PublishMethod().publish(object);
		}
	}

	@BugsExpected
	public static class DetectVirtualMethodBug_refersObjectThatIsReferredByThis {
		private final TestClass o = new TestClass();

		public void publish() {
			Object object = new Object();
			this.o.tc = new TestClass();
			this.o.tc.klass = object;
			TestClass testClass = new TestClass();
			testClass.klass = object;
			new PublishMethod().publish(testClass);
		}
	}

	@NoBugsExpected
	public static class DetectXAStoreBug_Int {
		private final int f = 10;

		public void publish(int[] i) {
			i[0] = f;
		}
	}

	@NoBugsExpected
	public static class DetectXAStoreBug_Null {
		public void publish(Object[] array) {
			array[0] = null;
		}
	}

	@NoBugsExpected
	public static class DetectXAStoreBug_ReferenceToExternalArray {
		public void publish(Object[] array) {
			array[0] = new Object();
		}
	}

	@NoBugsExpected
	public static class DetectXAStoreBug_ReferenceToInternalArray {
		public void publish() {
			Object[] array = new Object[1];
			array[0] = new Object();
		}
	}

	@BugsExpected
	public static class DetectXAStoreBug_ReferenceReferredByThis {
		private final Object f = new Object();

		public void publish(Object[] array) {
			array[0] = f;
		}
	}

	@NoBugsExpected
	public static class DetectXAStoreBug_ImmutableReferenceReferredByThis {
		private final Object f = new ImmutableTestClass();

		public void publish(Object[] array) {
			array[0] = f;
		}
	}

	@BugsExpected
	public static class DetectXAStoreBug_ReferenceTransitivelyReferredByThis {
		private final TestClass f = new TestClass();

		public void publish(Object[] array) {
			Object object = new Object();
			f.klass = object;
			array[0] = object;
		}
	}

	@BugsExpected
	public static class DetectXAStoreBug_refersObjectThatIsReferredByThis {
		private final TestClass o = new TestClass();

		public void publish(Object[] array) {
			Object object = new Object();
			this.o.tc = new TestClass();
			this.o.tc.klass = object;
			TestClass testClass = new TestClass();
			testClass.klass = object;
			array[0] = testClass;
		}
	}

	@NoBugsExpected
	public static class DetectPutFieldBug_Int {
		private final int f = 10;

		public void publish(TestClass tc) {
			tc.i = f;
		}
	}

	@NoBugsExpected
	public static class DetectPutFieldBug_Null {
		public void publish(TestClass tc) {
			tc.klass = null;
		}
	}

	@NoBugsExpected
	public static class DetectPutFieldBug_ReferenceToExternalObject {
		public void publish(TestClass tc) {
			tc.klass = new Object();
		}
	}

	@NoBugsExpected
	public static class DetectPutFieldBug_ReferenceToInternalObject {
		public void publish() {
			TestClass tc = new TestClass();
			tc.klass = new Object();
		}
	}

	@BugsExpected
	public static class DetectPutFieldBug_ReferenceReferredByThis {
		private final Object f = new Object();

		public void publish(TestClass tc) {
			tc.klass = f;
		}
	}

	@NoBugsExpected
	public static class DetectPutFieldBug_ImmutableReferenceReferredByThis {
		private final Object f = new String();

		public void publish(TestClass tc) {
			tc.klass = f;
		}
	}

	@BugsExpected
	public static class DetectPutFieldBug_ReferenceTransitivelyReferredByThis {
		private final TestClass f = new TestClass();

		public void publish(TestClass tc) {
			Object object = new Object();
			f.klass = object;
			tc.klass = object;
		}
	}

	@BugsExpected
	public static class DetectPutFieldBug_refersObjectThatIsReferredByThis {
		private final TestClass o = new TestClass();

		public void publish(TestClass tc) {
			Object object = new Object();
			this.o.tc = new TestClass();
			this.o.tc.klass = object;
			TestClass testClass = new TestClass();
			testClass.klass = object;
			tc.klass = testClass;
		}
	}

	@NoBugsExpected
	public static class DetectPutStaticBug_Int {
		private final int f = 10;

		public void publish() {
			TestClassStatic.i = f;
		}
	}

	@NoBugsExpected
	public static class DetectPutStaticBug_Null {
		public void publish() {
			TestClassStatic.klass = null;
		}
	}

	@NoBugsExpected
	public static class DetectPutStaticBug_ReferenceToExternalObject {
		public void publish() {
			TestClassStatic.klass = new Object();
		}
	}

	@NoBugsExpected
	public static class DetectPutStaticBug_ReferenceToInternalObject {
		public void publish() {
			TestClass tc = new TestClass();
			tc.klass = new Object();
		}
	}

	@BugsExpected
	public static class DetectPutStaticBug_ReferenceReferredByThis {
		private final Object f = new Object();

		public void publish() {
			TestClassStatic.klass = f;
		}
	}

	@NoBugsExpected
	public static class DetectPutStaticBug_ImmutableReferenceReferredByThis {
		private final Object f = new String();

		public void publish() {
			TestClassStatic.klass = f;
		}
	}

	@BugsExpected
	public static class DetectPutStaticBug_This {
		public void publish() {
			TestClassStatic.klass = this;
		}
	}

	@BugsExpected
	public static class DetectPutStaticBug_ReferenceTransitivelyReferredByThis {
		private final TestClass f = new TestClass();

		public void publish() {
			Object object = new Object();
			f.klass = object;
			TestClassStatic.klass = object;
		}
	}

	@BugsExpected
	public static class DetectPutStaticBug_refersObjectThatIsReferredByThis {
		private final TestClass o = new TestClass();

		public void publish() {
			Object object = new Object();
			this.o.tc = new TestClass();
			this.o.tc.klass = object;
			TestClass testClass = new TestClass();
			testClass.klass = object;
			TestClassStatic.klass = testClass;
		}
	}

	@NoBugsExpected
	public static class DetectAReturnBug_Int {
		private final int f = 10;

		public int publish() {
			return f;
		}
	}

	@NoBugsExpected
	public static class DetectAReturnBug_Null {
		private final Object f = null;

		public Object publish() {
			return f;
		}
	}

	@NoBugsExpected
	public static class DetectAReturnBug_ReferenceToExternalObject {
		public Object publish() {
			return new Object();
		}
	}

	@BugsExpected
	public static class DetectAReturnBug_ReferenceReferredByThis {
		private final Object f = new Object();

		public Object publish(TestClass tc) {
			return f;
		}
	}

	@NoBugsExpected
	public static class DetectAReturnBug_ImmutableReferenceReferredByThis {
		private final Object f = new ImmutableTestClass();

		public Object publish(TestClass tc) {
			return f;
		}
	}

	@BugsExpected
	public static class DetectAReturnBug_ReferenceTransitivelyReferredByThis {
		private final TestClass f = new TestClass();

		public Object publish(TestClass tc) {
			Object object = new Object();
			f.klass = object;
			return object;
		}
	}

	@BugsExpected
	public static class DetectAReturnBug_refersObjectThatIsReferredByThis {
		private final TestClass o = new TestClass();

		public TestClass publish(TestClass tc) {
			Object object = new Object();
			this.o.tc = new TestClass();
			this.o.tc.klass = object;
			TestClass testClass = new TestClass();
			testClass.klass = object;
			return testClass;
		}
	}

	@Immutable
	private static class ImmutableTestClass {
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

	public static class PublishMethod {
		public void publish(int i) {
		}

		public void publish(Object o) {
		}
	}

}
