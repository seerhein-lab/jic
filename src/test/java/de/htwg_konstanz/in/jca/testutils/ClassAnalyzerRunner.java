package de.htwg_konstanz.in.jca.testutils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import de.htwg_konstanz.in.jca.ClassAnalyzer;
import de.htwg_konstanz.in.jca.ThreeValueBoolean;
import edu.umd.cs.findbugs.BugCollection;

public class ClassAnalyzerRunner extends Runner {

	@Target(value = { ElementType.METHOD })
	@Retention(value = RetentionPolicy.RUNTIME)
	public static @interface BindAnalyzerMethod {
	}

	private final Class<?> testClass;

	public ClassAnalyzerRunner(Class<?> testClass) {
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
		ClassAnalyzer analyzer = new ClassAnalyzer(javaClass,
				null);

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

	BugCollection runCheckMethod(ClassAnalyzer analyzer) {
		Method method = findAnalyzerBindingSutMethod();

		return invokeBindMethod(method, analyzer);
	}

	BugCollection invokeBindMethod(Method method, ClassAnalyzer analyzer) {
		try {
			return (BugCollection) method.invoke(null, analyzer);
		} catch (Exception exp) {
			throw new RuntimeException(exp);
		}
	}

	Method findAnalyzerBindingSutMethod() {
		Method[] methods = testClass.getMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(BindAnalyzerMethod.class)) {
				if (!Modifier.isStatic(method.getModifiers())) {
					throw new RuntimeException(
							"The bind method must be static. "
									+ "Only static method should be marked with the Annotation: "
									+ BindAnalyzerMethod.class);
				} else if (!method.getReturnType().equals(BugCollection.class)) {
					throw new RuntimeException(
							"bind method has wrong return typ. "
									+ "The method should use BugCollection as return type.");
				} else if (!(method.getParameterTypes().length == 1)) {
					throw new RuntimeException(
							"bind method has no parameter. "
									+ "The method should have a ClassAnaylzer as parameter.");
				} else if (!method.getParameterTypes()[0]
						.equals(ClassAnalyzer.class)) {
					throw new RuntimeException(
							"The parameter type of the bind method must be ClassAnaylzer.");
				} else {
					return method;
				}
			}
		}
		throw new RuntimeException(
				"No static metod in the test class is marked with AnalyzerBindingSutMethod annotation.");
	}

	List<Class<?>> getTestClasses(Class<?> testCaseClass) {
		ArrayList<Class<?>> testClassesList = new ArrayList<Class<?>>();
		Class<?>[] classes = testCaseClass.getClasses();
		for (Class<?> klass : classes) {

			if (klass.isAnnotationPresent(Yes.class)
					|| klass.isAnnotationPresent(No.class)
					|| klass.isAnnotationPresent(UnKnown.class)) {
				testClassesList.add(klass);
			}

		}
		return testClassesList;
	}
}
