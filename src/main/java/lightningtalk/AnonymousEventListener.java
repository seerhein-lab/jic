package lightningtalk;

public class AnonymousEventListener {
	private final int state;

	public AnonymousEventListener(EventSource source) {
		source.registerListener(new EventListener() {
			@Override
			public void onEvent() {
				doSomething();
			}
		});
		this.state = 42;
	}

	private void doSomething() {
		if (this.state != 42) {
			throw new AssertionError("AnonymousEventListener in invalid state.");
		}
	}
}
