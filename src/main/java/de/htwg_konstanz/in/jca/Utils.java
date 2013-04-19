package de.htwg_konstanz.in.jca;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;



public class Utils {

	public static String formatLoggingOutput(int depth) {
		StringBuilder indentation = new StringBuilder("");

		for (int i = 0; i < depth; i++)
			indentation.append("\t");

		return indentation.toString();
	}

	public static Logger setUpLogger(String loggerName, String logFilePath)
			throws IOException {
		Logger globalLogger = Logger.getLogger("");
		Handler[] globalLoggerHandlers = globalLogger.getHandlers();
		for (Handler handler : globalLoggerHandlers) {
			globalLogger.removeHandler(handler);
		}
		StreamHandler streamHandler = new StreamHandler(System.out,
				new Utils().new TestDriverFormater());
		streamHandler.setLevel(Level.ALL);
		globalLogger.addHandler(streamHandler);
		globalLogger.setLevel(Level.ALL);
		FileHandler fh = new FileHandler(logFilePath);
		fh.setFormatter(new Utils().new TestDriverFormater());
		fh.setLevel(Level.ALL);
		globalLogger.addHandler(fh);
		Logger logger = Logger.getLogger(loggerName);
		return logger;
	}

	public class TestDriverFormater extends Formatter {

		@Override
		public String format(LogRecord arg0) {
			return arg0.getMessage() + "\n";
		}

	}

}
