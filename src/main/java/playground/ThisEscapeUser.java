package playground;

import java.util.Observable;
import java.util.Observer;

public class ThisEscapeUser extends Observable {

	public static void main(String[] args) {
		new ThisEscape(new ThisEscapeUser());
	}

	@Override
	public void addObserver(Observer observer) {
		System.out.println(observer);
	}

}
