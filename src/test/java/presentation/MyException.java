package presentation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class MyException extends Exception {
	private static final long serialVersionUID = 1L;

	@SuppressFBWarnings(value = "PC")
	public MyException(String msg) {
		super(msg);
	}

}
