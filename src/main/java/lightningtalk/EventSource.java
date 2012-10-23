package lightningtalk;

public class EventSource {
	public void registerListener(EventListener eventListener) {
		eventListener.onEvent();
	}

	public static void main(String[] args) {
		EventSource source = new EventSource();
		new SimpleEventListener(source);
	}
}
