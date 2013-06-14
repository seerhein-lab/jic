package de.seerhein_lab.jca.heap;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import de.seerhein_lab.jca.Pair;

/**
 * Class representing a heapObject. A HeapObject has an Id, a reference to the
 * heap where its stored and a set of referring objects. This class should only
 * be instantiated for "the external" object, for other cases use Array or
 * ClassInstance.
 */
public class HeapObject {
	protected final UUID id;
	protected final Set<UUID> referredBy = new HashSet<UUID>();
	protected final Heap heap;

	/**
	 * Constructor.
	 * 
	 * @param heap
	 *            The heap where the object is stored.
	 */
	HeapObject(Heap heap) {
		id = UUID.randomUUID();
		this.heap = heap;
	}

	/**
	 * Copy-Constructor.
	 * 
	 * @param original
	 *            The HeapObject to copy from.
	 * @param heap
	 *            The heap where the object is stored.
	 */
	HeapObject(HeapObject original, Heap heap) {
		id = original.id;
		referredBy.addAll(original.referredBy);
		this.heap = heap;
	}

	/**
	 * Must not be called on a HeapObject instance, because a HeapObject can
	 * only be the "external", which can't refer anything.
	 */
	void replaceReferredObject(HeapObject oldObject, HeapObject newObject) {
		throw new AssertionError("Must not be called on a HeapObject instance");
	}

	/**
	 * Must not be called on a HeapObject instance, because a HeapObject can
	 * only be the "external", which can't refer anything.
	 */
	public Iterator<HeapObject> getReferredIterator() {
		throw new AssertionError("Must not be called on a HeapObject instance");
	}

	/**
	 * Adds "obj" as a referring Object.
	 * 
	 * @param obj
	 *            Obj which refers this.
	 */
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

	/**
	 * Is this referred by "toSearch" in the "heap".
	 * 
	 * @return If this is referred by "toSearch".
	 */
	public boolean referredBy(UUID toSearch, Heap heap) {
		return referredBy(toSearch, heap,
				new HashSet<Pair<HeapObject, HeapObject>>());
	}

	boolean referredBy(UUID toSearch, Heap heap,
			HashSet<Pair<HeapObject, HeapObject>> alreadyVisited) {
		for (UUID object : referredBy) {
			if (alreadyVisited.add(new Pair<HeapObject, HeapObject>(this, heap
					.get(object)))) {
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

	public boolean isTransitivelyReferredBy(HeapObject source) {
		Set<HeapObject> visited = new HashSet<HeapObject>();

		Queue<HeapObject> queue = new ArrayDeque<HeapObject>();
		queue.add(this);

		while (!queue.isEmpty()) {
			HeapObject obj = queue.poll();

			for (Iterator<HeapObject> it = obj.getReferringIterator(); it
					.hasNext();) {
				HeapObject referring = it.next();

				if (referring.equals(source))
					return true;
				if (!visited.contains(referring) && !queue.contains(referring))
					queue.add(referring);
			}
			visited.add(obj);
		}
		return false;
	}

	/**
	 * Does this refer "toSearch" in the "heap". Must not be called on a
	 * HeapObject instance, because a HeapObject can only be the "external",
	 * which can't refer anything.
	 * 
	 * @return If this refers "toSearch".
	 */
	public boolean refers(UUID toSearch, Heap heap) {
		return refers(toSearch, heap,
				new HashSet<Pair<HeapObject, HeapObject>>());
	}

	boolean refers(UUID toSearch, Heap heap,
			HashSet<Pair<HeapObject, HeapObject>> alreadyVisited) {
		if (heap.get(toSearch).equals(heap.getExternalObject())
				&& this.equals(heap.getExternalObject()))
			return true;
		return false;
	}

	public Set<HeapObject> getReferredClosure() {
		Set<HeapObject> closure = new HashSet<HeapObject>();
		closure.add(heap.getExternalObject());
		return closure;
	}

	public Set<HeapObject> getReferringClosure() {
		Set<HeapObject> closure = new HashSet<HeapObject>();

		Queue<HeapObject> queue = new ArrayDeque<HeapObject>();
		for (UUID id : referredBy) {
			queue.add(heap.get(id));
		}

		while (!queue.isEmpty()) {
			HeapObject obj = queue.poll();

			for (Iterator<HeapObject> it = obj.getReferringIterator(); it
					.hasNext();) {
				HeapObject referring = it.next();
				if (!queue.contains(referring) && !closure.contains(referring))
					queue.add(referring);
			}
			closure.add(obj);
		}
		return closure;
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