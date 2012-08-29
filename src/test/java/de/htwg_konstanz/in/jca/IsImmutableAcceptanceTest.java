package de.htwg_konstanz.in.jca;

import net.jcip.annotations.Immutable;

import org.junit.runner.RunWith;

import de.htwg_konstanz.in.jca.ImmutableClassTestRunner.Mutable;

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
 * runner {@link ImmutableClassTestRunner}.
 * 
 * <li>The definition of a immutable class see JavaDoc of annotation
 * {@link Immutable}.
 * </ul>
 * 
 * @see Immutable
 * @see ImmutableClassTestRunner
 */
@RunWith(ImmutableClassTestRunner.class)
public class IsImmutableAcceptanceTest {

	/**
	 * Simple class without fields and with no constructor.
	 */
	@Immutable
	public static class Story01_SimpleImmutableClass {
	}

	/**
	 * Simple class with empty constructor
	 */
	@Immutable
	public static class Story02_SimpleImmutableClassWithConstructor {
		public Story02_SimpleImmutableClassWithConstructor() {
		}
	}

	/**
	 * Immutable class with a final field and no constructor.
	 */
	@Immutable
	public static class Story03_ImmutableClassWithFinalField {
		final Object value = 01;
	}

	/**
	 * Class with a private field, the value of the private field could not be
	 * changed.
	 */
	@Immutable
	public static class Story04_ImmutableClassWithPrivateField {
		private String value = "01";
	}

	/**
	 * Not Immutable class because the field value could be changed.
	 */
	@Mutable
	public static class Story05_ClassWithNonFinalField {
		Object value = 01;
	}

	/**
	 * Not Immutable class because access to the this pointer is possible.
	 */
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
	@Immutable
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
	@Mutable
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
	@Mutable
	public static class Story09_ClassWithAccessByGetterToMutableState {

		public static final class StringHolder {
			String value = "default";
		}

		private final StringHolder value = new StringHolder();

		public StringHolder getValue() {
			return value;
		}

	}

}
