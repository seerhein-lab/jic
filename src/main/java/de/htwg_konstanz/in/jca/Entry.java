package de.htwg_konstanz.in.jca;

/**
 * An entry in a frame stack.
 */
public enum Entry {

	/** A byte with an arbitrary value. */
	someByte,

	/** A short with an arbitrary value. */
	someShort,

	/** An int with an arbitrary value. */
	someInt,

	/** A long with an arbitrary value. */
	someLong,

	/** A float with an arbitrary value. */
	someFloat,

	/** A double with an arbitrary value. */
	someDouble,

	/** A character with an arbitrary value. */
	someChar,

	/** A boolean with an arbitrary value. */
	someBoolean,

	/** A reference that certainly is not the 'this' reference */
	notThisReference,

	/** A reference that might or might be the 'this' reference */
	maybeThisReference,

	/** The 'this reference */
	thisReference,

	/** An unknown value. */
	unknownValue;

	public static Entry getInstance(String signature) {
		if (signature.equals("I"))
			return someInt;
		if (signature.equals("J"))
			return someLong;
		if (signature.equals("C"))
			return someChar;
		if (signature.equals("B"))
			return someByte;
		if (signature.equals("Z"))
			return someBoolean;
		if (signature.equals("S"))
			return someShort;
		if (signature.equals("F"))
			return someFloat;
		if (signature.equals("D"))
			return someDouble;
		return notThisReference;
	}

}
