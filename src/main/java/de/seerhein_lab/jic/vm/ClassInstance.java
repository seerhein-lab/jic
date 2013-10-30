package de.seerhein_lab.jic.vm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Class representing a ClassInstance. A ClassInstance has an Id, a reference to
 * the heap where its stored and a set of referring + referred objects.
 */
/**
 * @author haase
 * 
 */
public final class ClassInstance extends HeapObject {
	private Map<String, UUID> refers = new HashMap<String, UUID>();

	public ClassInstance(Heap heap) {
		super(heap);
	}

	/**
	 * Copy-Constructor.
	 * 
	 * @param original
	 *            The ClassInstance to copy from.
	 * @param heap
	 *            The heap where the object is stored.
	 */
	ClassInstance(ClassInstance original, Heap heap) {
		super(original, heap);
		refers.putAll(original.refers);
	}

	/**
	 * Replace all occurrences of oldObject by newObject.
	 */
	@Override
	public void replaceAllOccurrencesOfReferredObject(HeapObject oldObject, HeapObject newObject) {
		for (String field : refers.keySet()) {
			if (refers.get(field) != null && refers.get(field).equals(oldObject.getId()))
				refers.put(field, newObject.getId());
		}
	}

	@Override
	HeapObject copy(Heap heap) {
		return new ClassInstance(this, heap);
	}

	@Override
	protected HeapObject deepCopy(Heap heap, Map<HeapObject, HeapObject> visited) {
		ClassInstance copiedObject = heap.newClassInstance();

		for (Entry<String, UUID> entry : this.refers.entrySet()) {
			HeapObject referred = this.heap.get(entry.getValue());
			if (visited.containsKey(referred)) {
				visited.put(this, copiedObject);
				copiedObject.setField(entry.getKey(), visited.get(referred));
			} else {
				visited.put(this, copiedObject);
				copiedObject.setField(entry.getKey(), referred.deepCopy(heap, visited));
			}
		}

		return copiedObject;
	}

	protected void copyReferredObjectsTo(ClassInstance object, Heap heap,
			Map<HeapObject, HeapObject> visited) {

		for (Entry<String, UUID> entry : this.refers.entrySet()) {
			HeapObject referred = this.heap.get(entry.getValue());
			visited.put(this, object);
			object.setField(entry.getKey(), referred.deepCopy(heap, visited));
		}
	}

	/*
	 * Note that the iterator returned by this method skips references to null,
	 * i.e. it returns only valid objects.
	 */
	@Override
	public Iterator<HeapObject> getReferredIterator() {
		return new Iterator<HeapObject>() {
			Iterator<UUID> idIterator = refers.values().iterator();
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

			@Override
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

	/**
	 * Makes the field named field of this object point to the object obj. Also
	 * sets the backward reference from obj pointing to this object. If the
	 * field already has a value, the old value is overwritten, and if no other
	 * field refers to the old value either, the backward reference from the old
	 * value to this object is removed.
	 * 
	 * 
	 * @param field
	 * @param obj
	 */

	public void setField(String field, HeapObject obj) {
		HeapObject oldTarget = (refers.containsKey(field)) ? heap.get(refers.get(field)) : null;

		if (obj == null) {
			refers.put(field, null);
		} else {
			refers.put(field, obj.getId());
			obj.addReferringObject(this);
		}

		if (oldTarget != null && !refers.containsValue(oldTarget))
			oldTarget.removeReferringObj(this);
	}

	public HeapObject getField(String name) {
		return heap.get(refers.get(name));
	}

	// @Override
	// public int hashCode() {
	// final int prime = 31;
	// int result = super.hashCode();
	// result = prime * result + ((refers == null) ? 0 : refers.hashCode());
	// return result;
	// }
	//
	// @Override
	// public boolean equals(Object obj) {
	// if (this == obj)
	// return true;
	// if (!super.equals(obj))
	// return false;
	// if (!(obj instanceof ClassInstance))
	// return false;
	// ClassInstance other = (ClassInstance) obj;
	// if (refers == null) {
	// if (other.refers != null)
	// return false;
	// } else if (!refers.equals(other.refers))
	// return false;
	// return true;
	// }
}
