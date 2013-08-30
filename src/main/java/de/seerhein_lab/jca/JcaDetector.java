package de.seerhein_lab.jca;

import org.apache.bcel.classfile.AnnotationEntry;
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
	private final static String IMMUTABLE_ANNOTATION = "Lnet/jcip/annotations/Immutable;";
	private final BugReporter reporter;

	public JcaDetector(BugReporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public void report() {
	}

	private boolean supposedlyImmutable(JavaClass clazz) {
		for (AnnotationEntry annotation : clazz.getAnnotationEntries())
			if (annotation.getAnnotationType().equals(IMMUTABLE_ANNOTATION))
				return true;
		return false;
	}

	@Override
	public void visitClassContext(ClassContext classContext) {
		JavaClass clazz = classContext.getJavaClass();

		BugCollection bugs = null;

		try {
			bugs = supposedlyImmutable(clazz) ? new ClassAnalyzer(clazz,
					classContext).isImmutable() : new ClassAnalyzer(clazz,
					classContext).properlyConstructed();
		} catch (Throwable e) {
			bugs = new SortedBugCollection();
			bugs.add(new BugInstance("IMMUTABILITY_BUG", Confidence.HIGH
					.getConfidenceValue())
					.addString(
							"Class cannot be analyzed owing to internal problem.")
					.addClass(clazz)
					.addSourceLine(
							SourceLineAnnotation.createUnknown(clazz
									.getClassName())));
		}

		for (BugInstance bug : bugs)
			reporter.reportBug(bug);
	}
}
