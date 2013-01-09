package de.htwg_konstanz.in.jca;

public class Utils {

	public static String formatLoggingOutput(int depth) {
		String indentation = "";

		for (int i = 0; i < depth; i++)
			indentation += "\t";

		return indentation;
	}
}
