package de.seerhein_lab.jic;

import de.seerhein_lab.jic.analyzer.ClassAnalyzer;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.SortedBugCollection;
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
		SortedBugCollection bugs = new SortedBugCollection();
		bugs.addAll(new ClassAnalyzer(classContext).properlyConstructed());

		for (BugInstance bug : bugs) {
			System.out.println("bug: " + bug);
			reporter.reportBug(bug);
		}

	}
}
