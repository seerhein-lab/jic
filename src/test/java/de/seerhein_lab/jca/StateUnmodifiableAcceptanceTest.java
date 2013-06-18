package de.seerhein_lab.jca;

import org.junit.runner.RunWith;

import de.seerhein_lab.jca.analyzer.ClassAnalyzer;
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
public class StateUnmodifiableAcceptanceTest {

	@BindAnalyzerMethod
	public static BugCollection bindClassAnalyzerToProperlyConstructed(
			ClassAnalyzer analyzer) {
		return analyzer.stateUnmodifiable();
	}

	/**
	 * Simple class without fields and with the default constructor.
	 */
	@NoBugsExpected
	public static class Story000_ClassWithDefaultCtorEmpty {
	}

}
