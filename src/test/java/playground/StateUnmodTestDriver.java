package playground;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

import de.seerhein_lab.jca.Utils;
import de.seerhein_lab.jca.analyzer.ClassAnalyzer;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;

public class StateUnmodTestDriver {
	private static final String LOGFILEPATH = "./log.txt";
	private static final boolean analyzeCtorCopy = true;
	private static final boolean analyzeFieldsMutate = true;
	private static final boolean analyzeFieldsArePuplished = true;

	public static void main(String[] args) throws ClassNotFoundException,
			SecurityException, IOException {

		Logger logger = Utils.setUpLogger("ProperlyConstructedTestDriver",
				LOGFILEPATH);

		JavaClass clazz = Repository
				.lookupClass("playground.StateUnmodTestClass");

		SortedBugCollection bugs = new SortedBugCollection();
		ClassAnalyzer classAlalyzer = new ClassAnalyzer(clazz, null);
		if (analyzeCtorCopy) {
			logger.log(Level.FINE, "Analyzing CtorCopy");
			bugs.addAll(classAlalyzer.ctorParamsAreCopied().getCollection());
		}
		if (analyzeFieldsMutate) {
			logger.log(Level.FINE, "Analyzing FieldsMutate");
			bugs.addAll(classAlalyzer.stateUnmodified().getCollection());
		}
		if (analyzeFieldsArePuplished) {
			logger.log(Level.FINE, "Analyzing FieldsNotPublished");
			bugs.addAll(classAlalyzer.fieldsAreNotPublished().getCollection());
		}

		logger.log(Level.SEVERE, "bugs: ");
		for (BugInstance bug : bugs) {
			logger.log(Level.SEVERE,
					" " + bug.getType() + " (" + bug.getPriorityString() + ")");
		}

		logger.log(Level.SEVERE, "end bugs");

	}
}
