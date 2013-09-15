package playground;

@SuppressWarnings("unused")
public class StateUnmodTestClass {
	private static Object i;

	public StateUnmodTestClass() {
		i = this;
	}
}