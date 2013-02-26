package de.htwg_konstanz.in.jca;


public class Utils {

	public static String formatLoggingOutput(int depth) {
		StringBuilder indentation = new StringBuilder("");

		for (int i = 0; i < depth; i++)
			indentation.append("\t");

		return indentation.toString();
	}

	// public Deque<Slot> getTopPortionOfStack(Deque<Slot> stack, int size) {
	// ArrayDeque<Slot> substack = new ArrayDeque<Slot>();
	// Iterator<Slot> iterator = stack.iterator();
	// for (int i = 0; i < size; i++) {
	// substack.offer(iterator.next());
	// }
	// return substack;
	// }
}
