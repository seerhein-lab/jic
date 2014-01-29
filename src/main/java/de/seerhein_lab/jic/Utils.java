package de.seerhein_lab.jic;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.apache.bcel.classfile.JavaClass;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.annotations.Confidence;

public final class Utils {

	private Utils() {
		new AssertionError("must not be called.");
	}

	public static BugInstance createBug(String pattern, Confidence confidence, String message,
			JavaClass clazz) {
		return new BugInstance(pattern, confidence.getConfidenceValue()).addString(message)
				.addClass(clazz);
	}

	public static String formatLoggingOutput(int depth) {
		StringBuilder indentation = new StringBuilder("");
		for (int i = 0; i < depth; i++)
			indentation.append("\t");
		return indentation.toString();
	}

	public static Logger setUpLogger(String loggerName, String logFilePath, Level level)
			throws IOException {
		Logger globalLogger = Logger.getLogger("");
		Handler[] globalLoggerHandlers = globalLogger.getHandlers();
		for (Handler handler : globalLoggerHandlers) {
			globalLogger.removeHandler(handler);
		}
		StreamHandler streamHandler = new StreamHandler(System.out, new TestDriverFormater());
		streamHandler.setLevel(level);
		globalLogger.addHandler(streamHandler);
		globalLogger.setLevel(level);
		FileHandler fh = new FileHandler(logFilePath);
		fh.setFormatter(new TestDriverFormater());
		fh.setLevel(level);
		globalLogger.addHandler(fh);
		Logger logger = Logger.getLogger(loggerName);
		return logger;
	}

	public static class TestDriverFormater extends Formatter {
		@Override
		public String format(LogRecord arg0) {
			return arg0.getMessage() + "\n";
		}

	}
}
