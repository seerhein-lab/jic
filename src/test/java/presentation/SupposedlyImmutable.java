package presentation;

import net.jcip.annotations.Immutable;

@Immutable
public class SupposedlyImmutable {
	public int intState;
	public final SomeState someState;

	public SupposedlyImmutable(Registrar registrar, SomeState someState, int intState) {
		this.someState = someState;
		this.intState = intState;
		registrar.register(this);
	}

	public SomeState getSomeState() {
		return someState;
	}

	public int getIntState() {
		return intState;
	}

	public void setSomeStateMessage(String message) {
		someState.setMessage(message);
	}
}
