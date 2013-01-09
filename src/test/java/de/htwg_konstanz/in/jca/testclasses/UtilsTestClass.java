package de.htwg_konstanz.in.jca.testclasses;

public class UtilsTestClass {

	UtilsTestClass() {
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
