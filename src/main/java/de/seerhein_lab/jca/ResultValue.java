package de.seerhein_lab.jca;

import de.seerhein_lab.jca.heap.Heap;
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
	private final Heap heap;

	/**
	 * Simple constructor.
	 * 
	 * @param kind
	 *            EXCEPTION for thrown exceptions, REGULAR for all others.
	 * @param slot
	 *            The returned value represented by a Slot.
	 */
	public ResultValue(Kind kind, Slot slot, Heap heap) {
		if (kind == null || slot == null || heap == null) {
			throw new IllegalArgumentException("parameters must not be null");
		}

		this.kind = kind;
		this.slot = slot;
		this.heap = heap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((heap == null) ? 0 : heap.hashCode());
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
		if (heap == null) {
			if (other.heap != null)
				return false;
		} else if (!heap.equals(other.heap))
			return false;
		if (kind != other.kind)
			return false;
		if (slot == null) {
			if (other.slot != null)
				return false;
		} else if (!slot.equals(other.slot))
			return false;
		return true;
	}

	public Kind getKind() {
		return kind;
	}

	public Slot getSlot() {
		return slot;
	}

	public Heap getHeap() {
		return heap;
	}
}
