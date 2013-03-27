package playground;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

import de.htwg_konstanz.in.jca.analyzer.ClassAnalyzer;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;

public class StateUnmodTestDriver {
	private static final String LOGFILEPATH = "/home/seerheinlab/Arbeitsfläche/log.txt";
	private static final boolean analyzeCtorCopy = true;
	private static final boolean analyzeFieldsMutate = true;
	private static final boolean analyzeFieldsArePuplished = true;

	public static void main(String[] args) throws ClassNotFoundException,
			SecurityException, IOException {
		// logging stuff
		Logger globalLogger = Logger.getLogger("");
		Handler[] globalLoggerHandlers = globalLogger.getHandlers();
		for (Handler handler : globalLoggerHandlers) {
			globalLogger.removeHandler(handler);
		}
		StreamHandler streamHandler = new StreamHandler(
				System.out,
				new StateUnmodTestDriver().new ProperlyConstructedTestDriverFormater());
		streamHandler.setLevel(Level.ALL);
		globalLogger.addHandler(streamHandler);
		globalLogger.setLevel(Level.ALL);
		FileHandler fh = new FileHandler(LOGFILEPATH);
		fh.setFormatter(new StateUnmodTestDriver().new ProperlyConstructedTestDriverFormater());
		fh.setLevel(Level.ALL);
		globalLogger.addHandler(fh);
		Logger logger = Logger.getLogger("ProperlyConstructedTestDriver");
		// end logging set up

		JavaClass clazz = Repository
				.lookupClass("playground.PropConstTestClass");

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

	public class ProperlyConstructedTestDriverFormater extends Formatter {

		@Override
		public String format(LogRecord arg0) {
			return arg0.getMessage() + "\n";
		}

	}

}
