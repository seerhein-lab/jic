package playground;

public class TestClass extends TestSuper {

	TestClass(int i, float f) {
		super(i);
		char c = 'c';
		int j = 11 + f(3.14);
	}

	int f(double d) {
		return 47;
	}

	void g() {
	}

}
