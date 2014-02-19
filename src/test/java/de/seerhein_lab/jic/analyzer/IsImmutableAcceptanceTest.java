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
 * TODO: JavaDoc
 * 
 * @see IsImmutableTestRunner
 */
// @Ignore("activate this test class when the method IsImmutable will be implemented.")
@RunWith(ClassAnalyzerRunner.class)
@SuppressWarnings("unused")
public class IsImmutableAcceptanceTest {

	@BindAnalyzerMethod
	public static Collection<BugInstance> bindIsImmutable(ClassAnalyzer analyzer) {
		return analyzer.isImmutable();
	}

	/**
	 * Simple class without fields and with no constructor.
	 */
	@NoBugsExpected
	public static class Story01_SimpleImmutableClass {
	}

	/**
	 * Simple class with empty constructor
	 */
	@NoBugsExpected
	public static class Story02_SimpleImmutableClassWithConstructor {
		public Story02_SimpleImmutableClassWithConstructor() {
		}
	}

	/**
	 * Immutable class with a final field and no constructor.
	 */
	@NoBugsExpected
	public static class Story03_ImmutableClassWithFinalField {
		final private Object value = null;
	}

	@BugsExpected
	public static class Story03a_ImmutableClassWithFinalField {
		final Object value = null;
	}

	/**
	 * Class with a private field, the value of the private field could not be
	 * changed.
	 */
	@NoBugsExpected
	public static class Story04_ImmutableClassWithPrivateField {
		private final String value = "01";
	}

	@BugsExpected
	public static class Story04a_ImmutableClassWithPrivateField {
		private String value = "01";
	}

	/**
	 * Not Immutable class because the field value could be changed.
	 */
	@BugsExpected
	public static class Story05_ClassWithNonFinalField {
		Object value = null;
	}

	/**
	 * Not Immutable class because access to the this pointer is possible.
	 */
	@BugsExpected
	public static class Story06_ClassWithAccessToThisPointer {
		final Object thisValue;

		public Story06_ClassWithAccessToThisPointer() {
			thisValue = this;
		}
	}

	/**
	 * Immutable class because class with mutable state but the reference of to
	 * the state is not access able.
	 */
	@NoBugsExpected
	public static class Story07_ClassWithNoAccessToMutableState {

		public static class StringHolder {
			String value = "default";
		}

		private final StringHolder value = new StringHolder();

	}

	/**
	 * Not Immutable class because the reference to the mutable state could be
	 * access.
	 */
	@BugsExpected
	public static class Story08_ClassWithAccessToMutableState {

		public static final class StringHolder {
			String value = "default";
		}

		public final StringHolder value = new StringHolder();

	}

	/**
	 * Not Immutable class because the reference to the mutable state could be
	 * access.
	 */
	@BugsExpected
	public static class Story09_ClassWithAccessByGetterToMutableState {

		public static final class StringHolder {
			String value = "default";
		}

		private final StringHolder value = new StringHolder();

		public StringHolder getValue() {
			return value;
		}

	}

	/**
	 * Not Immutable class because the reference to the mutable state could be
	 * access.
	 */
	@Immutable
	@BugsExpected
	public static class Story10_SupposedlyImmutableClassWithMutableSuperClass extends
			Story09_ClassWithAccessByGetterToMutableState {

	}

}
