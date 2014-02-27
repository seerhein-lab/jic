package de.seerhein_lab.jic;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.lang.time.DateFormatUtils;

import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.ClassAnalyzer;
import de.seerhein_lab.jic.analyzer.ClassHelper;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.HeapObject;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

// This class must either be used thread-confined, or reporter must be thread-safe, otherwise concurrent calls to
// reporter.reportBug() can result in race conditions.
public final class JicDetector implements Detector {
	private static final String LOGPATH = "/Users/haase/tmp/log/"; // Pfad
																	// anpassen
	protected static final Logger logger = Logger.getLogger("JicDetector");
	private final static String IMMUTABLE_ANNOTATION = "Lnet/jcip/annotations/Immutable;";
	private final BugReporter reporter;
	private final AnalysisCache cache = new AnalysisCache();

	public JicDetector(BugReporter reporter) {
		this.reporter = reporter;

		try {
			Utils.setUpLogger(
					"ProperlyConstructedTestDriver",
					LOGPATH
							+ "log-"
							+ DateFormatUtils.format(System.currentTimeMillis(),
									"dd-MM-yyyy_HH-mm-ss") + ".txt", Level.OFF);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void report() {
		logger.info("cache hits: " + BaseVisitor.cacheHits);
		logger.info("cache misses: " + BaseVisitor.cacheMisses);
		logger.info("heaps: " + Heap.count.get());
		logger.info("frames: " + Frame.count.get());
	}

	@Override
	public void visitClassContext(ClassContext classContext) {
		HeapObject.objects = 0;
		JavaClass clazz = classContext.getJavaClass();
		boolean supposedlyImmutable = new ClassHelper(clazz).supposedlyImmutable();

		if (clazz.isAnnotation() || clazz.isInterface()) {
			if (supposedlyImmutable)
				reporter.reportBug(Utils.createBug("IMMUTABILITY_BUG", Confidence.HIGH,
						"Type cannot be annotated as immutable", clazz));
			return;
		}

		try {
			Collection<BugInstance> bugs = supposedlyImmutable ? new ClassAnalyzer(classContext,
					cache).isImmutable() : new ClassAnalyzer(classContext, cache)
					.properlyConstructed();

			for (BugInstance bug : bugs) {
				reporter.reportBug(bug);
			}

		} catch (EmercencyBrakeException e) {
			reporter.reportBug(Utils.createBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"class is too complex for immutability analysis", classContext.getJavaClass()));
		} catch (Throwable e) {
			reporter.reportBug(Utils.createBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"Class cannot be analyzed owing to internal problem (" + e + ")",
					classContext.getJavaClass()));
		}

	}
}
