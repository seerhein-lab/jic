package playground;

public class PropConstTestClass {

	public void f() {
	}

	PropConstTestClass(PropConstTestClass pctc) {
		pctc.f();
	}
}

class PropConstSubClass extends PropConstTestClass {
	PropConstSubClass(PropConstTestClass pctc) {
		super(pctc);
	}

	@Override
	public void f() {

	}
}