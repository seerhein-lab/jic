package de.htwg_konstanz.in.jca;

import net.jcip.annotations.Immutable;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class ImmutableClassTestRunner extends Runner {

	private final Class<?> testClass;

	public ImmutableClassTestRunner(Class<?> testClass) {
		this.testClass = testClass;
	}
	
	@Override
	public Description getDescription() {
		Description description = Description.createSuiteDescription(testClass);
		Class<?>[] classes = testClass.getClasses();
		for (Class<?> classToTest : classes) {
			Description testDescription = Description.createTestDescription(classToTest, classToTest.getSimpleName());
			description.addChild(testDescription);
		}
		return description;
	}

	@Override
	public void run(RunNotifier notifier) {
		Class<?>[] classes = testClass.getClasses();
		for (Class<?> classToTest : classes) {
			Description testDescription = Description.createTestDescription(classToTest, classToTest.getSimpleName());
			notifier.fireTestStarted(testDescription);
			try {
				boolean ignoreTest = classToTest.isAnnotationPresent(Ignore.class);
				if(ignoreTest) {
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

	void runTest(RunNotifier notifier, Description testDescription, Class<?> classToTest) throws Exception {
		JavaClass javaClass = Repository.lookupClass(classToTest);
		ClassAnalyzer analyzer = new ClassAnalyzer(javaClass);
		boolean expectedImmutableType = classToTest.isAnnotationPresent(Immutable.class);
		ThreeValueBoolean immutable = analyzer.isImmutable();
		if(immutable.equals(ThreeValueBoolean.unknown)) {
			notifier.fireTestIgnored(testDescription);
		} else {
			if(expectedImmutableType) {
				Assert.assertEquals("Class should be immutable.", ThreeValueBoolean.yes, analyzer.isImmutable());
			} else {
				Assert.assertEquals("Class should is not immutable.", ThreeValueBoolean.no, analyzer.isImmutable());
			}
		}
	}
	
	
}
