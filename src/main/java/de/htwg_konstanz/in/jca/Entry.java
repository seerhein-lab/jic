package de.htwg_konstanz.in.jca;

import java.util.List;

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

	/** A reference that might or might not be the 'this' reference */
	maybeThisReference,

	/** The 'this reference */
	thisReference;

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

	/**
	 * Combines the current value with the outcome of other execution paths.
	 * 
	 * @param others the outcome of the other execution paths, not null,
	 *        may be empty.
	 *
	 * @return the combined output.
	 */
	public Entry combineWithOthers(List<Entry> others) {
		if (others.isEmpty()) {
			return this;
		}
		return this.combineWithOther(others.get(0)).combineWithOthers(
				others.subList(1, others.size()));
	}

	/**
	 * Combines the current value with the outcome of another execution path.
	 * 
	 * @param other the outcome of the other execution path, or null if the
	 *        other execution path has no outcome.
	 *
	 * @return the combined output.
	 */
	public Entry combineWithOther(Entry other) {
		if (other == null) {
			return this;
		}
		switch (this) {
		case someByte:
		case someShort:
		case someInt:
		case someLong:
		case someFloat:
		case someDouble:
		case someChar:
		case someBoolean:
			if (!this.equals(other)) {
				throw new IllegalArgumentException(this
						+ " cannot be combined with " + other);
			}
			return this;
		case notThisReference:
			switch (other) {
			case notThisReference:
				return notThisReference;
			case maybeThisReference:
			case thisReference:
				return maybeThisReference;
			default:
				throw new IllegalArgumentException(this
						+ " cannot be combined with " + other);
			}
		case maybeThisReference:
			switch (other) {
			case notThisReference:
			case maybeThisReference:
			case thisReference:
				return maybeThisReference;
			default:
				throw new IllegalArgumentException(this
						+ " cannot be combined with " + other);
			}
		case thisReference:
			switch (other) {
			case notThisReference:
			case maybeThisReference:
				return maybeThisReference;
			case thisReference:
				return thisReference;
			default:
				throw new IllegalArgumentException(this
						+ " cannot be combined with " + other);
			}
		default:
			throw new AssertionError("cannot happen");
		}
	}
}
