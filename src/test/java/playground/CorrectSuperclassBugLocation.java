package playground;

@SuppressWarnings("serial")
// @Immutable
public class CorrectSuperclassBugLocation extends Exception {

	CorrectSuperclassBugLocation() {
		super("ahem");
	}

}
