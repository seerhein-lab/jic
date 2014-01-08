package de.seerhein_lab.jic.vm;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Class representing a heapObject. A HeapObject has an Id, a reference to the
 * heap where its stored and a set of referring objects.
 */
public abstract class HeapObject {
	private final UUID id;
	protected final Set<UUID> referredBy = new HashSet<UUID>();
	protected final Heap heap;

	/**
	 * Constructor.
	 * 
	 * @param heap
	 *            The heap where the object is stored.
	 */
	protected HeapObject(Heap heap) {
		if (heap == null)
			throw new NullPointerException("heap must not be null");

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
	protected HeapObject(HeapObject original, Heap heap) {
		if (original == null || heap == null)
			throw new NullPointerException("arguments must not be null");

		id = original.id;
		referredBy.addAll(original.referredBy);
		this.heap = heap;
	}

	/**
	 * @return the id
	 */
	public final UUID getId() {
		return id;
	}

	abstract HeapObject copy(Heap heap);

	/**
	 * Adds "obj" as a referring Object.
	 * 
	 * @param obj
	 *            Obj which refers this.
	 */
	final void addReferringObject(HeapObject obj) {
		referredBy.add(obj.id);
	}

	final void removeReferringObj(HeapObject obj) {
		referredBy.remove(obj.id);
	}

	public abstract void replaceAllOccurrencesOfReferredObject(HeapObject oldObject,
			HeapObject newObject);

	public abstract Iterable<HeapObject> getReferredObjects();

	public final Iterable<HeapObject> getReferringObjects() {
		return new Iterable<HeapObject>() {
			@Override
			public Iterator<HeapObject> iterator() {
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
		};
	}

	private enum Direction {
		BACK, FORTH
	};

	private final boolean isTransitivelyReachable(HeapObject target, Direction direction) {
		Set<HeapObject> visited = new HashSet<HeapObject>();

		Queue<HeapObject> queue = new ArrayDeque<HeapObject>();
		queue.add(this);

		while (!queue.isEmpty()) {
			HeapObject obj = queue.poll();

			for (HeapObject next : (direction == Direction.FORTH) ? obj.getReferredObjects() : obj
					.getReferringObjects()) {

				if (next.equals(target))
					return true;
				if (!visited.contains(next) && !queue.contains(next))
					queue.add(next);
			}
			visited.add(obj);
		}
		return false;
	}

	public final boolean isTransitivelyReferredBy(HeapObject source) {
		return isTransitivelyReachable(source, Direction.BACK);
	}

	public final boolean transitivelyRefers(HeapObject sink) {
		return isTransitivelyReachable(sink, Direction.FORTH);
	}

	private final Set<HeapObject> getClosure(Direction direction) {
		Set<HeapObject> closure = new HashSet<HeapObject>();
		Queue<HeapObject> queue = new ArrayDeque<HeapObject>();

		for (HeapObject next : (direction == Direction.FORTH) ? getReferredObjects()
				: getReferringObjects()) {
			queue.add(next);
		}

		while (!queue.isEmpty()) {
			HeapObject obj = queue.poll();

			for (HeapObject next : (direction == Direction.FORTH) ? obj.getReferredObjects() : obj
					.getReferringObjects()) {

				if (!queue.contains(next) && !closure.contains(next))
					queue.add(next);
			}
			closure.add(obj);
		}
		return closure;
	}

	public final Set<HeapObject> getReferringClosure() {
		return getClosure(Direction.BACK);
	}

	public final Set<HeapObject> getReferredClosure() {
		return getClosure(Direction.FORTH);
	}

	public boolean refersObjectThatIsReferredBy(HeapObject source) {
		for (HeapObject referedObject : this.getReferredClosure()) {
			if (referedObject.isTransitivelyReferredBy(source)) {
				return true;
			}
		}
		return false;
	}

	public HeapObject deepCopy(Heap heap) {
		Map<HeapObject, HeapObject> visited = new HashMap<HeapObject, HeapObject>();
		return deepCopy(heap, visited);
	}

	protected abstract HeapObject deepCopy(Heap heap, Map<HeapObject, HeapObject> visited);

	@Override
	public String toString() {
		if (this.equals(heap.getThisInstance()))
			return "This";
		if (this.equals(heap.getExternalObject()))
			return "External";
		return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((referredBy == null) ? 0 : referredBy.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof HeapObject))
			return false;

		HeapObject other = (HeapObject) obj;

		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;

		return referredBy.equals(other.referredBy);
	}

}
