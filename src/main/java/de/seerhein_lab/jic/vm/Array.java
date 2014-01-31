package de.seerhein_lab.jic.vm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Class whose instances represent arrays. Beside the components defined in the
 * superclass <code>HeapObject</code>, an array has a set of referred objects.
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
		super(heap);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#
	 * replaceAllOccurrencesOfReferredObjectByExternal
	 * (de.seerhein_lab.jic.vm.HeapObject)
	 */
	@Override
	public void replaceAllOccurrencesOfReferredObjectByExternal(HeapObject oldObject) {
		if (refers.remove(oldObject.getId()))
			refers.add(heap.getExternalObject().getId());
	}

	public void addComponent(HeapObject obj) {
		if (obj != null && refers.add(obj.getId()))
			obj.addReferringObject(this);
	}

	@Override
	public Iterable<HeapObject> getReferredObjects() {
		return new Iterable<HeapObject>() {
			@Override
			public Iterator<HeapObject> iterator() {
				return new Iterator<HeapObject>() {
					Iterator<UUID> idIterator = refers.iterator();
					UUID lookAhead;
					{
						lookAhead();
					}

					private void lookAhead() {
						lookAhead = null;
						while (lookAhead == null && idIterator.hasNext()) {
							lookAhead = idIterator.next();
						}
					}

					@Override
					public boolean hasNext() {
						return lookAhead != null;
					}

					public HeapObject next() {
						HeapObject result = heap.get(lookAhead);
						lookAhead();
						return result;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}

		};
	}

	@Override
	public HeapObject deepCopy(Heap heap, Map<HeapObject, HeapObject> visited) {
		Array copiedArray = heap.newArray();

		for (UUID id : this.refers) {
			HeapObject referred = this.heap.get(id);
			if (visited.containsKey(referred)) {
				visited.put(this, copiedArray);
				copiedArray.addComponent(visited.get(referred));
			} else {
				visited.put(this, copiedArray);
				copiedArray.addComponent(referred.deepCopy(heap, visited));
			}
		}
		return copiedArray;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((refers == null) ? 0 : refers.hashCode());
		return result;
	}

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
