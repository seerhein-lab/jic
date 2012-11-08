package playground;

public class PropConstTestClass {

	public static Object x;

	public PropConstTestClass() {
		int k = 0;
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < i; j++) {
				k = i + j;
			}
			double l = i;
			i++;
		}
	}
}
