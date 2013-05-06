package de.seerhein_lab.jca.testclasses;

public class UtilsTestClass {

	private static UtilsTestClass staticReference;

	UtilsTestClass() {
		staticReference = this;
	}

	UtilsTestClass(int i) {
		@SuppressWarnings("unused")
		int j;
	}

	void f() {
	}

	void f(int i) {
		@SuppressWarnings("unused")
		int j;
	}

	static void g() {
	}

	static void g(int i) {
		@SuppressWarnings("unused")
		int j;
	}

}
