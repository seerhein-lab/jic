package de.htwg_konstanz.in.jca;

import edu.umd.cs.findbugs.BugCollection;

public class ProperlyConstructedClassTestRunner extends AbstractTestRunner {

	public ProperlyConstructedClassTestRunner(Class<?> testClass) {
		super(testClass);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected BugCollection runCheckMethod(ClassAnalyzer analyzer) {
		return analyzer.isImmutable();
	}

}
