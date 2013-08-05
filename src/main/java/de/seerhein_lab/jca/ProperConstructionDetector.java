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

		BugCollection bugs = new ClassAnalyzer(clazz, classContext).properlyConstructed();
		

		for (BugInstance bug : bugs) {
			System.out.println("bug: " + bug);
			reporter.reportBug(bug);
		}

	}
}
