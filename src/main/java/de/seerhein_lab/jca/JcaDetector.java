package de.seerhein_lab.jca;

import org.apache.bcel.classfile.JavaClass;

import de.seerhein_lab.jca.analyzer.ClassAnalyzer;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public final class JcaDetector implements Detector {
	private BugReporter reporter;

	public JcaDetector(BugReporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public void report() {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitClassContext(ClassContext classContext) {
		JavaClass clazz = classContext.getJavaClass();
		
		BugCollection bugs = null;
		
		try {
			bugs = new ClassAnalyzer(clazz, classContext).isImmutable();
		} catch ( Throwable e) {
			bugs = new SortedBugCollection();
			BugInstance bug = new BugInstance("IMMUTABILITY_BUG", 
						Confidence.HIGH.getConfidenceValue());
			bug.addString("Class cannot be analyzed owing to internal problem.");
//			StringBuilder builder = new StringBuilder();
//			for ( StackTraceElement element : e.getStackTrace())
//				builder.append(element.toString());
//			bug.addString(builder.toString());
			
			bug.addClass(clazz);
			bug.addSourceLine(SourceLineAnnotation.createUnknown(clazz.getClassName()));
			bugs.add(bug);
		}
			
		for (BugInstance bug : bugs) {
			System.out.println("bug: " + bug);
			reporter.reportBug(bug);
		}

	}
}
