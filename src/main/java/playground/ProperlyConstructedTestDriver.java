package playground;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

import de.htwg_konstanz.in.jca.PropConClassAnalyzer;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;

public class ProperlyConstructedTestDriver {

	public static void main(String[] args) throws ClassNotFoundException {
		// logging stuff
		Logger globalLogger = Logger.getLogger("");
		Handler[] globalLoggerHandlers = globalLogger.getHandlers();
		for (Handler handler : globalLoggerHandlers) {
			globalLogger.removeHandler(handler);
		}
		StreamHandler streamHandler = new StreamHandler(
				System.out,
				new ProperlyConstructedTestDriver().new ProperlyConstructedTestDriverFormater());
		streamHandler.setLevel(Level.ALL);
		globalLogger.addHandler(streamHandler);
		globalLogger.setLevel(Level.ALL);
		Logger logger = Logger.getLogger("ProperlyConstructedTestDriver");
		// end logging set up

		JavaClass clazz = Repository
				.lookupClass("playground.PropConstTestClass");

		BugCollection bugs = new PropConClassAnalyzer(clazz, null)
				.properlyConstructed();
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
