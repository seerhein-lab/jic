package playground;

public class PropConstTestClass {

	public static Object x;

	public PropConstTestClass() {
		x = this;
		Object i = x;
	}
}
