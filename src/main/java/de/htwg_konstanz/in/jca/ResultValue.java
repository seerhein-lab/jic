package de.htwg_konstanz.in.jca;

import java.util.List;
import java.util.Vector;

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

	private static List<ResultValue> combineReferences(
			List<ResultValue> resultValues) {
		List<ResultValue> correctedList = new Vector<ResultValue>();
		for (ResultValue value : resultValues) {
			ResultValue correctedValue = value;
			if (value.kind.equals(ResultValue.Kind.REGULAR)
					&& value.slot.getDataType().equals(DataType.referenceType)) {
				if (value.slot.equals(Slot.notThisReference)) {
					for (ResultValue resultValue : resultValues) {
						if (resultValue.kind.equals(ResultValue.Kind.REGULAR)
								&& (resultValue.slot.equals(Slot.thisReference) || resultValue.slot
										.equals(Slot.maybeThisReference)))
							correctedValue = new ResultValue(Kind.REGULAR,
									Slot.maybeThisReference);
					}
				}

				if (value.slot.equals(Slot.thisReference)) {
					for (ResultValue resultValue : resultValues) {
						if (resultValue.kind.equals(ResultValue.Kind.REGULAR)
								&& (resultValue.slot
										.equals(Slot.notThisReference) || resultValue.slot
										.equals(Slot.maybeThisReference)))
							correctedValue = new ResultValue(Kind.REGULAR,
									Slot.maybeThisReference);
					}
				}
			}
			correctedList.add(correctedValue);
		}
		return correctedList;
	}

	private static List<ResultValue> deduplicate(
			List<ResultValue> combinedValues) {
		List<ResultValue> deduplicatedList = new Vector<ResultValue>();
		for (ResultValue value : combinedValues) {
			if (!deduplicatedList.contains(value))
				deduplicatedList.add(value);
		}
		return deduplicatedList;
	}

	public static List<ResultValue> normalize(List<ResultValue> resultValues) {
		return deduplicate(combineReferences(resultValues));
	}

	public Kind getKind() {
		return kind;
	}

	public Slot getSlot() {
		return slot;
	}

}
