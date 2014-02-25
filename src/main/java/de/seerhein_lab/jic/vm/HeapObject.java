package de.seerhein_lab.jic.vm;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import de.seerhein_lab.jic.EmercencyBrakeException;

/**
 * Abstract class representing the common part of objects on the heap
 * (consistent with the JVM spec, an object can be a class instance or an
 * array). This common part comprises a unique ID, a reference to the object's
 * heap, and the set of other objects that refer this object (a set of reverse
 * links).
 */
public abstract class HeapObject {
	private final static int HEAP_EMERCENCY_BREAK = 200000;
	private final static int MEMORY_THRESHOLD = 30000000;
	public static long objects = 0;
	private final UUID id;
	protected final Set<UUID> referredBy = new HashSet<UUID>();
	public final Heap heap;
	private final boolean immutable;

	/**
	 * Constructor.
	 * 
	 * @param heap
	 *            Heap this objects resides on. Must not be null.
	 */
	protected HeapObject(Heap heap, boolean immutable) {
		if (heap == null)
			throw new NullPointerException("heap must not be null");

		objects++;
		if (objects > HEAP_EMERCENCY_BREAK)
			throw new EmercencyBrakeException();

		// if (Runtime.getRuntime().freeMemory() < MEMORY_THRESHOLD)
		// throw new EmercencyBrakeException();

		id = UUID.randomUUID();
		this.immutable = immutable;
		this.heap = heap;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param original
	 *            Heap object to copy from. Must not be null.
	 * @param heap
	 *            Heap this object resides on. Must not be null.
	 */
	protected HeapObject(HeapObject original, Heap heap) {
		if (original == null || heap == null)
			throw new NullPointerException("arguments must not be null");

		objects++;
		if (objects > HEAP_EMERCENCY_BREAK)
			throw new EmercencyBrakeException();

		// if (Runtime.getRuntime().freeMemory() < MEMORY_THRESHOLD)
		// throw new EmercencyBrakeException();

		id = original.id;
		referredBy.addAll(original.referredBy);
		this.immutable = original.immutable;
		this.heap = heap;
	}

	public boolean isImmutable() {
		return immutable;
	}

	public boolean isExternal() {
		return false;
	}

	/**
	 * Gets this object's ID.
	 * 
	 * @return this object's ID
	 */
	protected final UUID getId() {
		return id;
	}

	/**
	 * Overriding method implementations are expected to copy an instance of the
	 * respective subclass of HeapObject onto the given heap.
	 * 
	 * @param heap
	 *            the heap that the new object belongs to; must not be null.
	 * @return newly copied object
	 */
	protected abstract HeapObject copy(Heap heap);

	/**
	 * Adds the object <code>obj</code> to the set of objects that refer this
	 * object. If <code>obj</code> is already contained in the set, the method
	 * has no effect.
	 * 
	 * @param obj
	 *            object to be added as referring object; must not be null.
	 */
	final protected void addReferringObject(HeapObject obj) {
		if (obj == null)
			throw new NullPointerException("argument must not be null");

		referredBy.add(obj.id);
	}

	/**
	 * Removes the object <code>obj</code> from the set of objects that refer
	 * this object. If the set does not contain the object <code>obj</code>, the
	 * method has no effect.
	 * 
	 * @param obj
	 *            object to be removed from set of referring objects; must not
	 *            be null.
	 */
	final protected void removeReferringObj(HeapObject obj) {
		if (obj == null)
			throw new NullPointerException("argument must not be null");

		referredBy.remove(obj.id);
	}

	/**
	 * Overriding method implementations are expected to replace all occurrences
	 * of the referred object <code>oldObj</code> by <code>newObj</code>. The
	 * method has no effect if <code>oldObject</code> is not referred by this
	 * object.
	 * 
	 * @param oldObject
	 *            referred object that is to be replaced; must not be null.
	 * @param newObject
	 *            newly referred object; must not be null.
	 */
	protected abstract void replaceReferredObject(HeapObject oldObj, HeapObject newObj);

	/**
	 * Overriding method implementations are expected to returns an iterable of
	 * the heap objects that this object refers. The iterable can be used in
	 * foreach loops.
	 * 
	 * @return iterable of the heap objects that this object refers.
	 */
	public abstract Iterable<HeapObject> getReferredObjects();

	/**
	 * Returns an iterable of the heap objects that refer this object. The
	 * iterable can be used in foreach loops.
	 * 
	 * @return iterable of the heap objects that refer this object
	 */
	protected final Iterable<HeapObject> getReferringObjects() {
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

	/**
	 * Returns the set of all objects that belong to the complex object starting
	 * with this object, .i.e. all objects that are directly or indirectly
	 * referred by this object, as well as this object itself.
	 * 
	 * @return set of all objects belonging to this complex object.
	 */
	public final Set<HeapObject> getClosure() {
		Set<HeapObject> closure = new HashSet<HeapObject>();
		Queue<HeapObject> queue = new ArrayDeque<HeapObject>();

		queue.add(this);

		while (!queue.isEmpty()) {
			HeapObject obj = queue.poll();

			for (HeapObject next : obj.getReferredObjects()) {

				if (!queue.contains(next) && !closure.contains(next))
					queue.add(next);
			}
			closure.add(obj);
		}
		return closure;
	}

	/**
	 * Checks if the object <code>target</code> is reachable from this object
	 * via a path of references; the path must have at least length 1, i.e. for
	 * this object as the target the check returns true only if this object has
	 * a reference to itself.
	 * 
	 * @param target
	 * @return true if <code>target</code> is reachable from this object, false
	 *         otherwise
	 */
	public final boolean isReachable(HeapObject target) {
		return (!this.equals(target)) && getClosure().contains(target);
	}

	/**
	 * Checks if a part of the complex object starting at this object is
	 * reachable by the object <code>source</code>.
	 * 
	 * @param source
	 *            object from where this complex object is to be reachable
	 * @return true if complex object is reachable, false otherwise
	 */
	public final boolean complexObjectIsReachableBy(HeapObject source) {
		for (HeapObject referredObject : this.getClosure()) {
			if (source.isReachable(referredObject)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Template method to copy the complex object starting at this object onto
	 * the heap <code>heap</code>. This method uses the primitive operation
	 * <code>deepCopy(Heap, Map<HeapObject, HeapObject>)</code>. The parts of
	 * the new complex objects will not have the same IDs as the parts of this
	 * complex object.
	 * 
	 * @param heap
	 *            the heap to copy the complex object onto
	 * @return newly copied complex object
	 */
	public final HeapObject deepCopy(Heap heap) {
		Map<HeapObject, HeapObject> visited = new HashMap<HeapObject, HeapObject>();
		return deepCopy(heap, visited);
	}

	/**
	 * Primitive operation for the template method <code>deepCopy(Heap)</code>.
	 * 
	 * @param heap
	 *            the heap to copy the complex object onto
	 * @param visited
	 *            set of mappings of already deeply copied parts.
	 * @return newly copied complex object
	 */
	protected abstract HeapObject deepCopy(Heap heap, Map<HeapObject, HeapObject> visited);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (this.equals(heap.getThisInstance()))
			return "This";
		if (this.isExternal())
			return (immutable ? "immutable" : "mutable") + "External";
		return (immutable ? "immutable" : "mutable") + "Internal (" + id + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id.hashCode();
		result = prime * result + referredBy.hashCode();
		result = prime * result + (immutable ? 1 : 0);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof HeapObject))
			return false;

		HeapObject other = (HeapObject) obj;

		if (!id.equals(other.id))
			return false;

		if (immutable != other.immutable)
			return false;

		return referredBy.equals(other.referredBy);
	}

}
