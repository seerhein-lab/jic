package de.seerhein_lab.jic.analyzer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

import de.seerhein_lab.jic.Utils;
import de.seerhein_lab.jic.cache.AnalysisCache;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.ba.ClassContext;

public class StateUnmodTestDriver {
	private static final String LOGFILEPATH = "./log.txt";
	private static final boolean analyzeCtorCopy = true;
	private static final boolean analyzeFieldsMutate = true;
	private static final boolean analyzeFieldsArePuplished = true;

	public static void main(String[] args) throws ClassNotFoundException, SecurityException,
			IOException {

		Logger logger = Utils.setUpLogger("StateUnmodTestDriver", LOGFILEPATH, Level.ALL);

		JavaClass clazz = Repository.lookupClass("playground.StateUnmodTestClass");

		SortedBugCollection bugs = new SortedBugCollection();

		ClassContext classContextMock = mock(ClassContext.class);

		when(classContextMock.getJavaClass()).thenReturn(clazz);

		ClassAnalyzer classAnalyzer = new ClassAnalyzer(classContextMock, new AnalysisCache());
		if (analyzeCtorCopy) {
			logger.fine("Analyzing CtorCopy");
			bugs.addAll(classAnalyzer.ctorArgsAreCopied());
		}
		if (analyzeFieldsMutate) {
			logger.fine("Analyzing FieldsMutate");
			bugs.addAll(classAnalyzer.noMutators());
		}
		if (analyzeFieldsArePuplished) {
			logger.fine("Analyzing FieldsNotPublished");
			bugs.addAll(classAnalyzer.fieldsAreNotPublished());
		}

		logger.severe("bugs: ");
		for (BugInstance bug : bugs) {
			logger.severe(" " + bug.getType() + " (" + bug.getPriorityString() + ")");
		}

		logger.severe("end bugs");

	}
}
