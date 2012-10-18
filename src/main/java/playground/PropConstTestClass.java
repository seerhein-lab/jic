package playground;

public class PropConstTestClass {

	PropConstTestClass() {
		int i = f(4, false);
		int j = i;
	}

	private static int f(int i, boolean bo) {
		i = i + 1;
		return i;
	}
}
