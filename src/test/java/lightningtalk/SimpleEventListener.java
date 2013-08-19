package lightningtalk;

import net.jcip.annotations.Immutable;

@Immutable
public class SimpleEventListener implements EventListener {
	private final int state;

	public SimpleEventListener(EventSource source) {
		source.registerListener(this);
		this.state = 42;
	}

	@Override
	public void onEvent() {
		if (this.state != 42) {
			throw new AssertionError("SimpleEventListener in invalid state.");
		}
	}
}
