package de.seerhein_lab.jca;

import org.apache.bcel.classfile.JavaClass;

import de.seerhein_lab.jca.analyzer.ClassAnalyzer;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.ba.ClassContext;

public final class ProperConstructionDetector implements Detector {
	private BugReporter reporter;

	public ProperConstructionDetector(BugReporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public void report() {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitClassContext(ClassContext classContext) {
		JavaClass clazz = classContext.getJavaClass();
		// System.out.println("**************************");
		// System.out.println("clazz: " + clazz);
		// System.out.println("**************************");

		// reporter.reportBug(new BugInstance("TUTORIAL_BUG",
		// Priorities.NORMAL_PRIORITY)
		// .addClass("de.htwg_konstanz.in.jca.testclasses.UtilsTestClass"));

		BugCollection bugs = new ClassAnalyzer(clazz, classContext)
				.properlyConstructed();

		for (BugInstance bug : bugs) {
			System.out.println("bug: " + bug);
			reporter.reportBug(bug);
		}

	}
}
