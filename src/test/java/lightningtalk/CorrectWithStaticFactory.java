package lightningtalk;

public class CorrectWithStaticFactory {
	private final int state;
	private final EventListener listener;

	private CorrectWithStaticFactory() {
		listener = new EventListener() {
			@Override
			public void onEvent() {
				doSomething();
			}
		};
		this.state = 42;
	}

	public static CorrectWithStaticFactory getInstance(EventSource source) {
		CorrectWithStaticFactory instance = new CorrectWithStaticFactory();
		source.registerListener(instance.listener);
		return instance;

	}

	public void doSomething() {
		if (this.state != 42) {
			throw new AssertionError("SimpleEventListener in invalid state.");
		}
	}
}
