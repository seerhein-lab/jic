package de.seerhein_lab.jca.heap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import de.seerhein_lab.jca.AlreadyVisited;

public class HeapObject {
	protected final UUID id;
	protected final Set<UUID> referredBy = new HashSet<UUID>();
	protected final Heap heap;

	HeapObject(Heap heap) {
		id = UUID.randomUUID();
		this.heap = heap;
	}

	HeapObject(HeapObject original, Heap heap) {
		id = original.id;
		referredBy.addAll(original.referredBy);
		this.heap = heap;
	}

	void replaceReferredObject(HeapObject oldObject, HeapObject newObject) {
		throw new AssertionError("Must not be called on a HeapObject instance");
	}

	// public Collection<UUID> getReferredObjects() {
	// throw new AssertionError("Must not be called on a HeapObject instance");
	// }

	public Iterator<HeapObject> getReferredIterator() {
		throw new AssertionError("Must not be called on a HeapObject instance");
	}

	void addReferringObject(HeapObject obj) {
		referredBy.add(obj.getId());
	}

	public Iterator<HeapObject> getReferringIterator() {
		return new Iterator<HeapObject>() {
			Iterator<UUID> idIterator = referredBy.iterator();

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
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

	HeapObject copy(Heap heap) {
		return new HeapObject(this, heap);
	}

	public boolean referredBy(UUID toSearch, Heap heap) {
		return referredBy(toSearch, heap,
				new HashSet<AlreadyVisited<HeapObject, HeapObject>>());
	}

	boolean referredBy(UUID toSearch, Heap heap,
			HashSet<AlreadyVisited<HeapObject, HeapObject>> alreadyVisited) {
		for (UUID object : referredBy) {
			if (alreadyVisited.add(new AlreadyVisited<HeapObject, HeapObject>(
					this, heap.get(object)))) {
				// if it was not in the set
				if (object.equals(toSearch)
						|| heap.get(object).referredBy(toSearch, heap,
								alreadyVisited)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean refers(UUID toSearch, Heap heap) {
		return refers(toSearch, heap,
				new HashSet<AlreadyVisited<HeapObject, HeapObject>>());
	}

	boolean refers(UUID toSearch, Heap heap,
			HashSet<AlreadyVisited<HeapObject, HeapObject>> alreadyVisited) {
		if (toSearch.equals(heap.getExternalObject())
				&& id.equals(heap.getExternalObject()))
			return true;
		throw new AssertionError("Must not be called in this context");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((referredBy == null) ? 0 : referredBy.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof HeapObject))
			return false;
		HeapObject other = (HeapObject) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (referredBy == null) {
			if (other.referredBy != null)
				return false;
		} else if (!referredBy.equals(other.referredBy))
			return false;
		return true;
	}
}