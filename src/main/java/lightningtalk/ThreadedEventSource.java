package lightningtalk;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadedEventSource extends EventSource implements Runnable {
	private final BlockingQueue<EventListener> listeners = new LinkedBlockingQueue<EventListener>();

	public void run() {
		while (true) {
			try {
				listeners.take().onEvent();
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public void registerListener(EventListener eventListener) {
		listeners.add(eventListener);
	}

	public static void main(String[] args) {
		ThreadedEventSource es = new ThreadedEventSource();
		new Thread(es).start();
		while (true) {
			new AnonymousEventListener(es);
		}
	}

}
