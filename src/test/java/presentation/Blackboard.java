package presentation;

public class Blackboard {
	public Object notice;

	public Blackboard() {
	}

	public Blackboard(Blackboard original) {
		this.notice = original.notice;
	}
}
