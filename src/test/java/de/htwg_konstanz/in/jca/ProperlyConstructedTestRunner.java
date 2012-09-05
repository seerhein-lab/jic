package de.htwg_konstanz.in.jca;

import edu.umd.cs.findbugs.BugCollection;

public class ProperlyConstructedTestRunner extends AbstractTestRunner {

	public ProperlyConstructedTestRunner(Class<?> testClass) {
		super(testClass);
	}

	@Override
	protected BugCollection runCheckMethod(ClassAnalyzer analyzer) {
		return analyzer.isImmutable();
	}

}
