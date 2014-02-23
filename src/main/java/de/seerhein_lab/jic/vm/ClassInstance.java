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
	public ClassInstance(Heap heap, boolean immutable) {
		super(heap, immutable);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject# replaceReferredObject
	 * (de.seerhein_lab.jic.vm.HeapObject, de.seerhein_lab.jic.vm.HeapObject)
	 */
	@Override
	protected void replaceReferredObject(HeapObject oldObj, HeapObject newObj) {
		if (oldObj == null)
			return;

		for (String field : refers.keySet()) {
			if (refers.get(field).equals(oldObj.getId()))
				refers.put(field, newObj.getId());
		}
		oldObj.removeReferringObj(this);
		newObj.addReferringObject(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#copy(de.seerhein_lab.jic.vm.Heap)
	 */
	@Override
	protected HeapObject copy(Heap heap) {
		return new ClassInstance(this, heap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.seerhein_lab.jic.vm.HeapObject#deepCopy(de.seerhein_lab.jic.vm.Heap,
	 * java.util.Map)
	 */
	@Override
	protected HeapObject deepCopy(Heap heap, Map<HeapObject, HeapObject> visited) {
		ClassInstance copiedObject = heap.newClassInstance(this.isImmutable());
		visited.put(this, copiedObject);

		for (Entry<String, UUID> entry : this.refers.entrySet()) {
			HeapObject referred = this.heap.get(entry.getValue());
			copiedObject.setField(
					entry.getKey(),
					visited.containsKey(referred) ? visited.get(referred) : referred.deepCopy(heap,
							visited));
		}
		return copiedObject;
	}

	/**
	 * Deeply copies the subobjects of the complex object <code>origin</code>
	 * onto this object, such that this object gets the same complex object
	 * structure as <code>origin</code>. If <code>origin</code> is the external
	 * object, then this object gets published.
	 * 
	 * @param origin
	 *            original complex object whose structure is to be copied onto
	 *            this object.
	 */
	public void copyReferredObjectsTo(HeapObject origin) {
		if (origin instanceof ExternalObject) {
			this.heap.publish(this);
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
			this.setField(entry.getKey(), referred.deepCopy(this.heap, visited));
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
