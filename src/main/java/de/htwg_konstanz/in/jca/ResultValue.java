package de.htwg_konstanz.in.jca;

import java.util.List;
import java.util.Vector;

public class ResultValue {
	public enum Kind {
		REGULAR, EXCEPTION;
	}

	private final Kind kind;
	private final Slot slot;

	public ResultValue(Kind kind, Slot slot) {
		this.kind = kind;
		this.slot = slot;
	}

	// private boolean subsumedBy(ResultValue other) {
	// if (equals(kind.equals(Kind.EXCEPTION)))
	// return other.kind.equals(Kind.EXCEPTION);
	//
	// // kind.equals(Kind.REGULAR) remains
	// return slot.subsumedBy(other.slot);
	//
	// }
	//
	// private boolean subsumedBy(List<ResultValue> resultValues) {
	// for (ResultValue other : resultValues) {
	// if (subsumedBy(other))
	// return true;
	// }
	// return false;
	// }

	// private static List<ResultValue> normalize(List<ResultValue> survivors,
	// List<ResultValue> contenders) {
	// if (contenders.isEmpty())
	// return survivors;
	//
	// ResultValue head = contenders.get(0);
	// List<ResultValue> tail = contenders.subList(1, contenders.size());
	//
	// if (!head.subsumedBy(survivors) && !head.subsumedBy(tail))
	// survivors.add(head);
	//
	// return normalize(survivors, tail);
	// }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((slot == null) ? 0 : slot.hashCode());
		return result;
	}

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
