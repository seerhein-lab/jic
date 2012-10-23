package lightningtalk;

public class SimpleEventListener implements EventListener {
	private final int state;

	public SimpleEventListener(EventSource source) {
		source.registerListener(this);
		this.state = 42;
	}

	private void doSomething() {
		if (this.state != 42) {
			throw new AssertionError("SimpleEventListener in invalid state.");
		}
	}

	@Override
	public void onEvent() {
		doSomething();

	}
}
