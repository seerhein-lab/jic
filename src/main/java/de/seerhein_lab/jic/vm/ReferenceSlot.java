package de.seerhein_lab.jic.vm;

import java.util.UUID;

import net.jcip.annotations.Immutable;
import de.seerhein_lab.jic.slot.Slot;

/**
 * Class whose instances represent slot entries that contain references to heap
 * objects.
 * 
 */
@Immutable
public class ReferenceSlot extends Slot {
	private final static ReferenceSlot nullReference = new ReferenceSlot();

	private final UUID objectID;

	/**
	 * Constructor that is internally called, exactly once to create a null
	 * reference.
	 */
	private ReferenceSlot() {
		objectID = null;
	}

	/**
	 * Constructor that creates a reference pointing to the heap object
	 * <code>object</code>.
	 * 
	 * @param object
	 *            the object this reference points to.
	 */
	public ReferenceSlot(HeapObject object) {
		if (object == null)
			throw new NullPointerException("argument must not be null");

		this.objectID = object.getId();
	}

	/**
	 * Returns the object this reference points to in the indicated heap.
	 * 
	 * @param heap
	 *            the heap the object this reference points to belongs to.
	 * @return object this reference points to.
	 */
	public HeapObject getObject(Heap heap) {
		return heap.get(objectID);
	}

	/**
	 * Returns a reference to the 'this' object in the indicated heap.
	 * 
	 * @param heap
	 *            the heap a reference to whose 'this' object is to be returned.
	 * @return a reference to the 'this' object
	 */
	public static ReferenceSlot getThisReference(Heap heap) {
		return new ReferenceSlot(heap.getThisInstance());
	}

	/**
	 * Returns a reference to the external object in the indicated heap.
	 * 
	 * @param heap
	 *            the heap a reference to whose external object is to be
	 *            returned.
	 * @param immutable
	 * @return a reference to the external object
	 */
	public static ReferenceSlot getExternalReference(Heap heap, boolean immutable) {
		return new ReferenceSlot(heap.getExternalObject(immutable));
	}

	/**
	 * Returns the null reference.
	 * 
	 * @return the null reference
	 */
	public static ReferenceSlot getNullReference() {
		return nullReference;
	}

	/**
	 * Checks whether this reference is the null reference.
	 * 
	 * @return true if this reference is the null reference, false otherwise.
	 */
	public boolean isNullReference() {
		return this == ReferenceSlot.getNullReference();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.Slot#getNumSlots()
	 */
	@Override
	public int getNumSlots() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.Slot#copy()
	 */
	@Override
	public Slot copy() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.Slot#toString()
	 */
	@Override
	public String toString() {
		if (this.isNullReference())
			return "NullReferenceSlot";
		return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.Slot#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((objectID == null) ? 0 : objectID.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.Slot#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof ReferenceSlot))
			return false;
		ReferenceSlot other = (ReferenceSlot) obj;
		if (objectID == null) {
			if (other.objectID != null)
				return false;
		} else if (!objectID.equals(other.objectID))
			return false;
		return true;
	}

}
