package presentation;

import net.jcip.annotations.Immutable;

@Immutable
public class SupposedlyImmutable {
	public int state;
	public final Blackboard board;

	public SupposedlyImmutable(Blackboard board) {
		board.notice = this;
		state = 42;
		// this.board = board;
		this.board = new Blackboard(board);
		// this.board = new Blackboard();
	}

	public Blackboard getBoard() {
		// return this.board;
		return new Blackboard(this.board);
	}

	public void setNotice(Object notice) {
		this.board.notice = notice;
	}
}
