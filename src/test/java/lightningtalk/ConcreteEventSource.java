package lightningtalk;

public class ConcreteEventSource implements EventSource {
	@Override
	public void registerListener(EventListener eventListener) {
		eventListener.onEvent();
	}

	public static void main(String[] args) {
		EventSource source = new ConcreteEventSource();
		new SimpleEventListener(source);
	}
}
