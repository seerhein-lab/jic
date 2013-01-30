package lightningtalk;

public class HiddenThis {
	private final int state;

	public HiddenThis(EventSource source) {
		source.registerListener(new EventListener() {
			@Override
			public void onEvent() {
				doSomething();
			}
		});
		this.state = 42;
	}

	public void doSomething() {
		if (this.state != 42) {
			throw new AssertionError("SimpleEventListener in invalid state.");
		}
	}
}
