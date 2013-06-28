package de.seerhein_lab.jca.heap;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import de.seerhein_lab.jca.Pair;

/**
 * Class representing an Array. An Array has an Id, a reference to the heap
 * where its stored and a set of referring + referred objects.
 */
public class Array extends HeapObject {
	private Set<UUID> refers = new HashSet<UUID>();

	public Array(Heap heap) {
		super(heap);
	}

	/**
	 * Copy-Constructor.
	 * 
	 * @param original
	 *            The Array to copy from.
	 * @param heap
	 *            The heap where the object is stored.
	 */
	public Array(Array original, Heap heap) {
		super(original, heap);
		refers.addAll(original.refers);
	}

	@Override
	HeapObject copy(Heap heap) {
		return new Array(this, heap);
	}

	// @Override
	// public Collection<UUID> getReferredObjects() {
	// return refers;
	// }

	@Override
	public Iterator<HeapObject> getReferredIterator() {
		return new Iterator<HeapObject>() {
			Iterator<UUID> idIterator = refers.iterator();

			@Override
			public boolean hasNext() {
				return idIterator.hasNext();
			}

			@Override
			public HeapObject next() {
				return heap.get(idIterator.next());
			}

			@Override
			public void remove() {
				idIterator.remove();
			}
		};
	}

	/**
	 * Replace the oldObject by the newObject.
	 */
	@Override
	public void replaceAllOccurrencesOfReferredObject(HeapObject oldObject, HeapObject newObject) {
		refers.remove(oldObject.id);
		refers.add(newObject.id);
	}

	void addReferredObject(UUID id) {
		refers.add(id);
	}



//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = super.hashCode();
//		result = prime * result + ((refers == null) ? 0 : refers.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (!super.equals(obj))
//			return false;
//		if (!(obj instanceof Array))
//			return false;
//		Array other = (Array) obj;
//		if (refers == null) {
//			if (other.refers != null)
//				return false;
//		} else if (!refers.equals(other.refers))
//			return false;
//		return true;
//	}


}
