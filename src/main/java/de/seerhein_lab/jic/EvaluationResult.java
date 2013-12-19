package de.seerhein_lab.jic;

import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.vm.Heap;

/**
 * This class is used for the return values of called methods or results of
 * different branches occurring in PropConInstructionsAnalysisVisitor. It
 * contains a Kind which says, if the return value is regular or a thrown
 * exception and a Slot standing for the returned DataType.
 */
public class EvaluationResult {

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
	public EvaluationResult(Kind kind, Slot slot, Heap heap) {
		if (kind == null || slot == null || heap == null)
			throw new NullPointerException("parameters must not be null");

		this.kind = kind;
		this.slot = slot;
		this.heap = heap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + heap.hashCode();
		result = prime * result + kind.hashCode();
		result = prime * result + slot.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof EvaluationResult))
			return false;
		EvaluationResult other = (EvaluationResult) obj;

		return heap.equals(other.heap) && kind == other.kind && slot.equals(other.slot);
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

	@Override
	public String toString() {
		if (kind == Kind.EXCEPTION)
			return "EvaluationResult[" + kind + "]";
		return "EvaluationResult[" + kind + ", " + slot + ", " + heap + "]";
	}

}
