package de.htwg_konstanz.in.jca;

import java.util.List;

import org.apache.bcel.generic.Type;

/**
 * An entry in a frame stack.
 */
public enum Slot {

	/** A byte with an arbitrary value. */
	someByte(DataType.byteType),

	/** A short with an arbitrary value. */
	someShort(DataType.shortType),

	/** An int with an arbitrary value. */
	someInt(DataType.intType),

	/** A long with an arbitrary value. */
	someHalfLong(DataType.longType),

	/** A float with an arbitrary value. */
	someFloat(DataType.floatType),

	/** A double with an arbitrary value. */
	someHalfDouble(DataType.doubleType),

	/** A character with an arbitrary value. */
	someChar(DataType.charType),

	/** A boolean with an arbitrary value. */
	someBoolean(DataType.booleanType),

	/** A reference that certainly is not the 'this' reference */
	notThisReference(DataType.referenceType),

	/** A reference that might or might not be the 'this' reference */
	maybeThisReference(DataType.referenceType),

	/** The 'this reference */
	thisReference(DataType.referenceType);

	private DataType dataType;

	private Slot(DataType dataType) {
		this.dataType = dataType;
	}

	public static int numRequiredSlots(Type[] types) {
		int numRequiredSlots = 0;
		for (Type type : types) {
			numRequiredSlots += DataType.getDataType(type).getNumSlots();
		}
		return numRequiredSlots;
	}

	public DataType getDataType() {
		return dataType;

	}

	/**
	 * Combines the current value with the outcome of other execution paths.
	 * 
	 * @param others
	 *            the outcome of the other execution paths, not null, may be
	 *            empty.
	 * 
	 * @return the combined output.
	 */
	public Slot combineWithOthers(List<Slot> others) {
		if (others.isEmpty()) {
			return this;
		}
		return this.combineWithOther(others.get(0)).combineWithOthers(
				others.subList(1, others.size()));
	}

	/**
	 * Combines the current value with the outcome of another execution path.
	 * 
	 * @param other
	 *            the outcome of the other execution path, or null if the other
	 *            execution path has no outcome.
	 * 
	 * @return the combined output.
	 */
	public Slot combineWithOther(Slot other) {
		if (other == null) {
			return this;
		}
		switch (this) {
		case someByte:
		case someShort:
		case someInt:
		case someHalfLong:
		case someFloat:
		case someHalfDouble:
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
