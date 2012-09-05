package de.htwg_konstanz.in.jca;

import edu.umd.cs.findbugs.BugCollection;

public class ImmutableClassTestRunner extends AbstractTestRunner {

	public ImmutableClassTestRunner(Class<?> testClass) {
		super(testClass);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected BugCollection runCheckMethod(ClassAnalyzer analyzer) {
		return analyzer.properlyConstructed();
	}
}
