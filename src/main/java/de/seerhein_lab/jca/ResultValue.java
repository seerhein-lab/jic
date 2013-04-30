package de.seerhein_lab.jca;

import de.seerhein_lab.jca.slot.Slot;

/**
 * This class is used for the return values of called methods or results of
 * different branches occurring in PropConInstructionsAnalysisVisitor. It
 * contains a Kind which says, if the return value is regular or a thrown
 * exception and a Slot standing for the returned DataType.
 */
public class ResultValue {

	/**
	 * Internal enumeration. EXCEPTION is used for all kinds of thrown
	 * exceptions, REGULAR for all other.
	 */
	public enum Kind {
		REGULAR, EXCEPTION;
	}

	private final Kind kind;
	private final Slot slot;

	/**
	 * Simple constructor.
	 * 
	 * @param kind
	 *            EXCEPTION for thrown exceptions, REGULAR for all others.
	 * @param slot
	 *            The returned value represented by a Slot.
	 */
	public ResultValue(Kind kind, Slot slot) {
		this.kind = kind;
		this.slot = slot;
	}

	/**
	 * Simple auto-generated hashCode method.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((slot == null) ? 0 : slot.hashCode());
		return result;
	}

	/**
	 * Simple auto.generated equals method.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ResultValue))
			return false;
		ResultValue other = (ResultValue) obj;
		if (kind != other.kind)
			return false;
		if (slot != other.slot)
			return false;
		return true;
	}

	// public static Set<ResultValue> combineReferences(
	// Set<ResultValue> resultValues) {
	// Set<ResultValue> correctedSet = new HashSet<ResultValue>();
	//
	// // values to compare
	// ResultValue notThisValue = new ResultValue(Kind.REGULAR,
	// Slot.notThisReference);
	// ResultValue maybeThisValue = new ResultValue(Kind.REGULAR,
	// Slot.maybeThisReference);
	// ResultValue thisValue = new ResultValue(Kind.REGULAR,
	// Slot.thisReference);
	//
	// for (ResultValue value : resultValues) {
	// ResultValue correctedValue = value;
	// if (value.kind.equals(ResultValue.Kind.REGULAR)
	// && value.slot.getDataType().equals(DataType.referenceType)) {
	// if (value.slot.equals(Slot.notThisReference)) {
	// if (resultValues.contains(maybeThisValue)
	// || resultValues.contains(thisValue))
	// correctedValue = maybeThisValue;
	// }
	//
	// if (value.slot.equals(Slot.thisReference)) {
	// if (resultValues.contains(notThisValue)
	// || resultValues.contains(maybeThisValue))
	// correctedValue = maybeThisValue;
	// }
	// }
	//
	// correctedSet.add(correctedValue);
	// }
	// return correctedSet;
	// }

	public Kind getKind() {
		return kind;
	}

	public Slot getSlot() {
		return slot;
	}

}
