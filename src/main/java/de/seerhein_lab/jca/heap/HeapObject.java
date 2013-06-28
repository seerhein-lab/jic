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
public abstract class HeapObject {
	protected final UUID id;
	protected final Set<UUID> referredBy = new HashSet<UUID>();
	protected final Heap heap;

	/**
	 * Constructor.
	 * 
	 * @param heap
	 *            The heap where the object is stored.
	 */
	protected HeapObject(Heap heap) {
		if ( heap == null ) 
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
		if ( original == null || heap == null ) 
			throw new NullPointerException("arguments must not be null");

		id = original.id;
		referredBy.addAll(original.referredBy);
		this.heap = heap;
	}


	public abstract void replaceAllOccurrencesOfReferredObject(HeapObject oldObject, HeapObject newObject);

	public abstract Iterator<HeapObject> getReferredIterator();
	
	/**
	 * Adds "obj" as a referring Object.
	 * 
	 * @param obj
	 *            Obj which refers this.
	 */
	final void addReferringObject(HeapObject obj) {
		referredBy.add(obj.getId());
	}

	public final Iterator<HeapObject> getReferringIterator() {
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
	public final UUID getId() {
		return id;
	}

	abstract HeapObject copy(Heap heap);

	public final boolean isTransitivelyReferredBy(HeapObject source) {
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

	
	public final boolean transitivelyRefers(HeapObject sink) {
		Set<HeapObject> visited = new HashSet<HeapObject>();

		Queue<HeapObject> queue = new ArrayDeque<HeapObject>();
		queue.add(this);

		while (!queue.isEmpty()) {
			HeapObject obj = queue.poll();

			for (Iterator<HeapObject> it = obj.getReferredIterator(); it
					.hasNext();) {
				HeapObject referred = it.next();

				if (referred.equals(sink))
					return true;
				if (!visited.contains(referred) && !queue.contains(referred))
					queue.add(referred);
			}
			visited.add(obj);
		}
		return false;
	}
	

	public final Set<HeapObject> getReferringClosure() {
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

	public final Set<HeapObject> getReferredClosure() {
		Set<HeapObject> closure = new HashSet<HeapObject>();

		Queue<HeapObject> queue = new ArrayDeque<HeapObject>();
		for ( Iterator<HeapObject> it = this.getReferredIterator(); it
					.hasNext();) {
			queue.add(it.next());
		}
		

		while (!queue.isEmpty()) {
			HeapObject obj = queue.poll();

			for (Iterator<HeapObject> it = obj.getReferredIterator(); it
					.hasNext();) {
				HeapObject referred = it.next();
				if (!queue.contains(referred) && !closure.contains(referred))
					queue.add(referred);
			}
			closure.add(obj);
		}
		return closure;
	}
	
	
	public boolean refersObjectThatIsReferredBy(HeapObject source) {
		for (HeapObject referedObject : this.getReferredClosure()) {
			if (referedObject.isTransitivelyReferredBy(source)) {
				return true;
			}
		}
		return false;
	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((id == null) ? 0 : id.hashCode());
//		result = prime * result
//				+ ((referredBy == null) ? 0 : referredBy.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (!(obj instanceof HeapObject))
//			return false;
//		HeapObject other = (HeapObject) obj;
//		if (id == null) {
//			if (other.id != null)
//				return false;
//		} else if (!id.equals(other.id))
//			return false;
//		if (referredBy == null) {
//			if (other.referredBy != null)
//				return false;
//		} else if (!referredBy.equals(other.referredBy))
//			return false;
//		return true;
//	}
}