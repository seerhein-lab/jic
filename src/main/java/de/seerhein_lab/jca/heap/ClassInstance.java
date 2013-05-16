package de.seerhein_lab.jca.heap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ClassInstance extends HeapObject {
	private Map<String, UUID> refers = new HashMap<String, UUID>();

	public ClassInstance(Heap heap) {
		super(heap);
	}

	ClassInstance(ClassInstance original, Heap heap) {
		super(original, heap);
		refers.putAll(original.refers);
	}

	@Override
	void replaceReferredObject(HeapObject oldObject, HeapObject newObject) {
		for (String field : refers.keySet())
			if (refers.get(field).equals(oldObject.id))
				refers.put(field, newObject.id);
	}

	void addReferredObject(Heap heap, String field, UUID id) {
		if (refers.containsKey(field)) {
			// remove the old assignment
			heap.get(refers.get(field)).referredBy.remove(id);
		}
		refers.put(field, id);
	}

	@Override
	HeapObject copy(Heap heap) {
		return new ClassInstance(this, heap);
	}

	// @Override
	// public Collection<UUID> getReferredObjects() {
	// return refers.values();
	// }

	@Override
	public Iterator<HeapObject> getReferredIterator() {
		return new Iterator<HeapObject>() {
			Iterator<UUID> idIterator = refers.values().iterator();

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

	public HeapObject getField(String name) {
		return heap.get(refers.get(name));
	}

	@Override
	boolean refers(UUID toSearch, Heap heap,
			HashSet<AlreadyVisited> alreadyVisited) {
		for (UUID field : refers.values()) {
			if (alreadyVisited.add(new AlreadyVisited(this, heap.get(field)))) {
				// if was not visited before
				if (field.equals(toSearch)
						|| heap.get(field).refers(toSearch, heap,
								alreadyVisited)) {
					return true;
				}
			}
		}
		return false;
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
		if (!(obj instanceof ClassInstance))
			return false;
		ClassInstance other = (ClassInstance) obj;
		if (refers == null) {
			if (other.refers != null)
				return false;
		} else if (!refers.equals(other.refers))
			return false;
		return true;
	}
}
