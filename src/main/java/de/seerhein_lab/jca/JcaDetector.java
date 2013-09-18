package de.seerhein_lab.jca;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;

import de.seerhein_lab.jca.analyzer.ClassAnalyzer;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

@ThreadSafe
public final class JcaDetector implements Detector {
	private final static String IMMUTABLE_ANNOTATION = "Lnet/jcip/annotations/Immutable;";

	@GuardedBy("reporterLock")
	private final BugReporter reporter;
	private final Object reporterLock = new Object();
	
	public static int propConCounter;

	public JcaDetector(BugReporter reporter) {
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
		propConCounter = 0;

		JavaClass clazz = classContext.getJavaClass();
		boolean supposedlyImmutable = supposedlyImmutable(clazz);
		
		if ( clazz.isAnnotation() || clazz.isInterface() ) {
			if (  supposedlyImmutable ) 
				reporter.reportBug(Utils.createBug(
						Confidence.HIGH, "Type cannot be annotated as immutable", clazz));
			return;
		}

		SortedBugCollection bugs = new SortedBugCollection();
		
		if ( clazz.isAbstract() && supposedlyImmutable ) {
			reporter.reportBug(Utils.createBug(
					Confidence.HIGH, "Type cannot be annotated as immutable", clazz));
			supposedlyImmutable = false;
		} 

		try {
			bugs.addAll(supposedlyImmutable? new ClassAnalyzer(
					classContext).isImmutable() : new ClassAnalyzer(
					classContext).properlyConstructed());
		} catch (Throwable e) {
			reporter.reportBug(Utils.createBug(Confidence.HIGH, 
					"Class cannot be analyzed owing to internal problem (" + e + ")", 
					classContext.getJavaClass()));
		}
		
		for (BugInstance bug : bugs) {
			synchronized (reporterLock) {
				reporter.reportBug(bug);
			}
		}

	}
}
