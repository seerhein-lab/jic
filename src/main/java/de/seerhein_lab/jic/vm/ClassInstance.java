package de.seerhein_lab.jic.vm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Class whose instances represent class instances. Beside the components
 * defined in the superclass <code>HeapObject</code>, a class instance has a
 * mapping that maps field names to referred objects. This map only contains
 * entries for non-null reference values. If a field has a primitive type, or if
 * its reference is null, it is not contained in the map.
 */
public final class ClassInstance extends HeapObject {
	private Map<String, UUID> refers = new HashMap<String, UUID>();

	/**
	 * Constructor.
	 * 
	 * @param heap
	 *            Heap this class instance resides on. Must not be null.
	 */
	public ClassInstance(Heap heap) {
		super(heap);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param original
	 *            Class instance to copy from. Must not be null.
	 * @param heap
	 *            Heap this class instance resides on. Must not be null.
	 */
	ClassInstance(ClassInstance original, Heap heap) {
		super(original, heap);
		refers.putAll(original.refers);
	}

	/**
	 * Replace all occurrences of oldObject by newObject.
	 */
	@Override
	public void replaceAllOccurrencesOfReferredObjectByExternal(HeapObject oldObject) {
		for (String field : refers.keySet()) {
			if (refers.get(field) != null && refers.get(field).equals(oldObject.getId()))
				refers.put(field, heap.getExternalObject().getId());
		}
	}

	// protected void replaceAllOccurrencesOfReferredObject(HeapObject oldObj,
	// HeapObject newObj) {
	// if (oldObj == null)
	// return;
	//
	// for (String field : refers.keySet()) {
	// if (refers.get(field).equals(oldObject.getId()))
	// refers.put(field, heap.getExternalObject().getId());
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#copy(de.seerhein_lab.jic.vm.Heap)
	 */
	@Override
	protected HeapObject copy(Heap heap) {
		return new ClassInstance(this, heap);
	}

	@Override
	protected HeapObject deepCopy(Heap heap, Map<HeapObject, HeapObject> visited) {
		ClassInstance copiedObject = heap.newClassInstance();

		for (Entry<String, UUID> entry : this.refers.entrySet()) {
			HeapObject referred = this.heap.get(entry.getValue());
			if (referred == null) {
				copiedObject.setField(entry.getKey(), null);
				continue;
			}
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

	public void copyReferredObjectsTo(HeapObject origin, Heap heap) {
		if (origin instanceof ExternalObject) {
			heap.publish(this);
			return;
		}

		ClassInstance originClassInstance = (ClassInstance) origin;

		Map<HeapObject, HeapObject> visited = new HashMap<HeapObject, HeapObject>();

		for (Entry<String, UUID> entry : originClassInstance.refers.entrySet()) {
			HeapObject referred = originClassInstance.heap.get(entry.getValue());
			visited.put(originClassInstance, this);
			if (referred == null) {
				this.setField(entry.getKey(), null);
				continue;
			}
			this.setField(entry.getKey(), referred.deepCopy(heap, visited));
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
					Iterator<UUID> idIterator = refers.values().iterator();

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

	/**
	 * Sets the field <code>field</code> of this object to the object
	 * <code>obj</code>. Also sets the backward reference from obj pointing to
	 * this object. If the field already has a value, the old value is
	 * overwritten, and if no other field refers to the old value either, the
	 * backward reference from the old value to this object is removed. If
	 * <code>obj</code> is null, then <code>
	 * field</code> is removed from the map of referred objects.
	 * 
	 * @param field
	 *            field to be set
	 * @param obj
	 *            object to be set to
	 */
	public void setField(String field, HeapObject obj) {
		HeapObject oldTarget = refers.containsKey(field) ? heap.get(refers.get(field)) : null;

		if (obj == null)
			refers.remove(field);
		else {
			refers.put(field, obj.getId());
			obj.addReferringObject(this);
		}

		if (oldTarget != null && !refers.containsValue(oldTarget))
			oldTarget.removeReferringObj(this);
	}

	/**
	 * Return the object that the field <code>field</code> of this object refers
	 * to, or null if <code>field</code> is not in the refers map.
	 * 
	 * @param field
	 * @return object that <code>field</code> refers to
	 */
	public HeapObject getField(String field) {
		return refers.containsKey(field) ? heap.get(refers.get(field)) : null;
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
		if (!(obj instanceof ClassInstance))
			return false;
		ClassInstance other = (ClassInstance) obj;

		return refers.equals(other.refers);
	}

}
