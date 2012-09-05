package de.htwg_konstanz.in.jca;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.Immutable;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import edu.umd.cs.findbugs.BugCollection;

public abstract class AbstractTestRunner extends Runner {

	@Target(value = { ElementType.TYPE })
	@Retention(value = RetentionPolicy.RUNTIME)
	public static @interface Mutable {
	}

	private final Class<?> testClass;

	public AbstractTestRunner(Class<?> testClass) {
		this.testClass = testClass;
	}

	@Override
	public Description getDescription() {
		Description description = Description.createSuiteDescription(testClass);
		List<Class<?>> classes = getTestClasses(testClass);
		for (Class<?> classToTest : classes) {
			Description testDescription = Description.createTestDescription(
					classToTest, classToTest.getSimpleName());
			description.addChild(testDescription);
		}
		return description;
	}

	@Override
	public void run(RunNotifier notifier) {
		List<Class<?>> classes = getTestClasses(testClass);
		for (Class<?> classToTest : classes) {
			Description testDescription = Description.createTestDescription(
					classToTest, classToTest.getSimpleName());
			notifier.fireTestStarted(testDescription);
			try {
				boolean ignoreTest = classToTest
						.isAnnotationPresent(Ignore.class);
				if (ignoreTest) {
					notifier.fireTestIgnored(testDescription);
				} else {
					runTest(notifier, testDescription, classToTest);
				}
			} catch (Throwable thrownException) {
				Failure failure = new Failure(testDescription, thrownException);
				notifier.fireTestFailure(failure);
			} finally {
				notifier.fireTestFinished(testDescription);
			}
		}
	}

	void runTest(RunNotifier notifier, Description testDescription,
			Class<?> classToTest) throws Exception {
		JavaClass javaClass = Repository.lookupClass(classToTest);
		ClassAnalyzer analyzer = new ClassAnalyzer(javaClass);

		BugCollection bugs = runCheckMethod(analyzer);

		if (classToTest.isAnnotationPresent(Yes.class)) {
			Assert.assertEquals(ThreeValueBoolean.yes,
					ClassAnalyzer.indicatesSuccess(bugs));
		}

		if (classToTest.isAnnotationPresent(No.class)) {
			Assert.assertEquals(ThreeValueBoolean.no,
					ClassAnalyzer.indicatesSuccess(bugs));
		}

		if (classToTest.isAnnotationPresent(UnKnown.class)) {
			Assert.assertEquals(ThreeValueBoolean.unknown,
					ClassAnalyzer.indicatesSuccess(bugs));
		}
	}

	protected abstract BugCollection runCheckMethod(ClassAnalyzer analyzer);

	List<Class<?>> getTestClasses(Class<?> testCaseClass) {
		ArrayList<Class<?>> testClassesList = new ArrayList<Class<?>>();
		Class<?>[] classes = testCaseClass.getClasses();
		for (Class<?> klass : classes) {
			boolean isImmutableAnnotationPresent = klass
					.isAnnotationPresent(Immutable.class);
			boolean isMutableAnnotationPresent = klass
					.isAnnotationPresent(Mutable.class);
			if (isImmutableAnnotationPresent || isMutableAnnotationPresent) {
				testClassesList.add(klass);
			}
		}
		return testClassesList;
	}

}
