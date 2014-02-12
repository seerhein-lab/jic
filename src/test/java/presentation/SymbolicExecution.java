package presentation;

import net.jcip.annotations.Immutable;

@Immutable
public class SymbolicExecution {
	@SuppressWarnings("unused")
	private final Object ref;

	public SymbolicExecution(int n, Object ex) {
		if (n > 0)
			ref = new Object();
		else
			ref = ex;
	}

}
