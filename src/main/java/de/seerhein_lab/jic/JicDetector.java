package de.seerhein_lab.jic;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;

import de.seerhein_lab.jic.analyzer.ClassAnalyzer;
import de.seerhein_lab.jic.cache.AnalysisCache;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

// This class must either be used thread-confined, or reporter must be thread-safe, otherwise concurrent calls to
// reporter.reportBug() can result in race conditions.
public final class JicDetector implements Detector {
	private final static String IMMUTABLE_ANNOTATION = "Lnet/jcip/annotations/Immutable;";
	private final BugReporter reporter;
	private final AnalysisCache cache = new AnalysisCache();

	public JicDetector(BugReporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public void report() {
	}

	// package private for testing purposes
	boolean supposedlyImmutable(JavaClass clazz) {
		for (AnnotationEntry annotation : clazz.getAnnotationEntries())
			if (annotation.getAnnotationType().equals(IMMUTABLE_ANNOTATION))
				return true;
		return false;
	}

	@Override
	public void visitClassContext(ClassContext classContext) {
		JavaClass clazz = classContext.getJavaClass();
		boolean supposedlyImmutable = supposedlyImmutable(clazz);

		if (clazz.isAnnotation() || clazz.isInterface()) {
			if (supposedlyImmutable)
				reporter.reportBug(Utils.createBug(Confidence.HIGH,
						"Type cannot be annotated as immutable", clazz));
			return;
		}

		SortedBugCollection bugs = new SortedBugCollection();

		if (clazz.isAbstract() && supposedlyImmutable) {
			reporter.reportBug(Utils.createBug(Confidence.HIGH,
					"Type cannot be annotated as immutable", clazz));
			supposedlyImmutable = false;
		}

		try {
			bugs.addAll(supposedlyImmutable ? new ClassAnalyzer(classContext, cache).isImmutable()
					: new ClassAnalyzer(classContext, cache).properlyConstructed());
		} catch (Throwable e) {
			reporter.reportBug(Utils.createBug(Confidence.HIGH,
					"Class cannot be analyzed owing to internal problem (" + e + ")",
					classContext.getJavaClass()));
		}

		for (BugInstance bug : bugs) {
			reporter.reportBug(bug);
		}
	}
}
