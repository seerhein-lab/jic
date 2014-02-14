package presentation;

public class SomeState {
	private String message;

	public SomeState(String message) {
		this.message = message;
	}

	public SomeState(SomeState original) {
		this.message = original.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
