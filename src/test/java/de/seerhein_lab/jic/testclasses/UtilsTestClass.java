package de.seerhein_lab.jic.testclasses;

@SuppressWarnings("unused")
public class UtilsTestClass {

	private static UtilsTestClass staticReference;

	UtilsTestClass() {
		staticReference = this;
	}

	UtilsTestClass(int i) {
		int j;
	}

	void f() {
	}

	void f(int i) {
		int j;
	}

	static void g() {
	}

	static void g(int i) {
		int j;
	}

}
