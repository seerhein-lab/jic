package de.seerhein_lab.jic.vm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Class whose instances represent arrays. Beside the components defined in the
 * superclass <code>HeapObject</code>, an array has a set of referred objects.
 * This set only contains the actually referred objects, there is no such thing
 * as null pointers on this level.
 */
public final class Array extends HeapObject {
	private Set<UUID> refers = new HashSet<UUID>();

	/**
	 * Constructor.
	 * 
	 * @param heap
	 *            Heap this array resides on. Must not be null.
	 */
	public Array(Heap heap) {
		super(heap, false);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param original
	 *            Array to copy from. Must not be null.
	 * @param heap
	 *            Heap this array resides on. Must not be null.
	 */
	public Array(Array original, Heap heap) {
		super(original, heap);
		refers.addAll(original.refers);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#copy(de.seerhein_lab.jic.vm.Heap)
	 */
	@Override
	protected Array copy(Heap heap) {
		return new Array(this, heap);
	}

	/**
	 * Adds the object <code>obj</code> to this object's set of referred
	 * objects. If <code>obj</code> is null, the set remains unchanged;
	 * likewise, if the set already contains the object <code>obj</code>, this
	 * operation has no effect. If the set of referred objects is modified as a
	 * consequence of this operation, then this object is added to
	 * <code>obj</code>'s set of referring objects.
	 * 
	 * @param obj
	 *            the object to be added as a referred object.
	 */
	public void addReferredObject(HeapObject obj) {
		if (obj != null && refers.add(obj.getId()))
			obj.addReferringObject(this);
	}

	// public void removeComponent(HeapObject obj) {
	// if (obj != null && refers.remove(obj.getId()))
	// obj.removeReferringObj(this);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject# replaceReferredObject
	 * (de.seerhein_lab.jic.vm.HeapObject, de.seerhein_lab.jic.vm.HeapObject)
	 */
	@Override
	protected void replaceReferredObject(HeapObject oldObj, HeapObject newObj) {
		if (oldObj != null && refers.remove(oldObj.getId())) {
			oldObj.removeReferringObj(this);
			if (newObj != null && refers.add(newObj.getId()))
				newObj.addReferringObject(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#getReferredObjects()
	 */
	@Override
	public Iterable<HeapObject> getReferredObjects() {
		return new Iterable<HeapObject>() {
			@Override
			public Iterator<HeapObject> iterator() {
				return new Iterator<HeapObject>() {
					Iterator<UUID> idIterator = refers.iterator();

					@Override
					public boolean hasNext() {
						return idIterator.hasNext();
					}

					public HeapObject next() {
						return heap.get(idIterator.next());
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}

		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.seerhein_lab.jic.vm.HeapObject#deepCopy(de.seerhein_lab.jic.vm.Heap,
	 * java.util.Map)
	 */
	@Override
	public HeapObject deepCopy(Heap heap, Map<HeapObject, HeapObject> visited) {
		Array copiedArray = heap.newArray();
		visited.put(this, copiedArray);

		for (UUID id : this.refers) {
			HeapObject referred = this.heap.get(id);
			copiedArray.addReferredObject(visited.containsKey(referred) ? visited.get(referred)
					: referred.deepCopy(heap, visited));
		}
		return copiedArray;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + refers.hashCode();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;

		if (!(obj instanceof Array))
			return false;
		Array other = (Array) obj;

		return refers.equals(other.refers);

	}

}
