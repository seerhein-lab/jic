package playground;

import java.util.Observable;
import java.util.Observer;

public class ThisEscape implements Observer {
	final int state;

	ThisEscape(Observable observable) {
		observable.addObserver(this);
		state = 42;
	}

	@Override
	public void update(Observable o, Object arg) {
	}

	@Override
	public String toString() {
		return "ThisEscape: state = " + state;
	}

}
