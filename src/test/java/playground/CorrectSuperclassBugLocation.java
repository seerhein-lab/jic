package playground;

import net.jcip.annotations.Immutable;

@SuppressWarnings("serial")
//@Immutable
public class CorrectSuperclassBugLocation extends Exception {

	CorrectSuperclassBugLocation() {
		super("ahem");
	}
	
	
}
