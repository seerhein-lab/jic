package de.htwg_konstanz.in.jca;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.apache.bcel.generic.Type;

import de.htwg_konstanz.in.jca.slot.BooleanSlot;
import de.htwg_konstanz.in.jca.slot.ByteSlot;
import de.htwg_konstanz.in.jca.slot.CharSlot;
import de.htwg_konstanz.in.jca.slot.DoubleSlot;
import de.htwg_konstanz.in.jca.slot.FloatSlot;
import de.htwg_konstanz.in.jca.slot.IntSlot;
import de.htwg_konstanz.in.jca.slot.LongSlot;
import de.htwg_konstanz.in.jca.slot.ReferenceSlot;
import de.htwg_konstanz.in.jca.slot.ShortSlot;
import de.htwg_konstanz.in.jca.slot.Slot;
import de.htwg_konstanz.in.jca.slot.VoidSlot;

public class Utils {

	public static String formatLoggingOutput(int depth) {
		StringBuilder indentation = new StringBuilder("");

		for (int i = 0; i < depth; i++)
			indentation.append("\t");

		return indentation.toString();
	}

	public static Slot getDefaultSlotInstance(Type type) {
		if (type.equals(Type.INT))
			return IntSlot.getInstance();
		if (type.equals(Type.LONG))
			return LongSlot.getInstance();
		if (type.equals(Type.CHAR))
			return CharSlot.getInstance();
		if (type.equals(Type.BYTE))
			return ByteSlot.getInstance();
		if (type.equals(Type.BOOLEAN))
			return BooleanSlot.getInstance();
		if (type.equals(Type.SHORT))
			return ShortSlot.getInstance();
		if (type.equals(Type.FLOAT))
			return FloatSlot.getInstance();
		if (type.equals(Type.DOUBLE))
			return DoubleSlot.getInstance();
		if (type.equals(Type.VOID))
			return VoidSlot.getInstance();
		return ReferenceSlot.getInternalInstance();
	}
	

	public static Logger setUpLogger(String loggerName, String logFilePath) throws IOException {
		Logger globalLogger = Logger.getLogger("");
		Handler[] globalLoggerHandlers = globalLogger.getHandlers();
		for (Handler handler : globalLoggerHandlers) {
			globalLogger.removeHandler(handler);
		}
		StreamHandler streamHandler = new StreamHandler(
				System.out,
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

	@Deprecated
	public static Slot getDefaultSlotInstance(String signature) {
		return getDefaultSlotInstance(Type.getType(signature));
	}

	// public Deque<SlotOld> getTopPortionOfStack(Deque<SlotOld> stack, int
	// size) {
	// ArrayDeque<SlotOld> substack = new ArrayDeque<SlotOld>();
	// Iterator<SlotOld> iterator = stack.iterator();
	// for (int i = 0; i < size; i++) {
	// substack.offer(iterator.next());
	// }
	// return substack;
	// }
}
