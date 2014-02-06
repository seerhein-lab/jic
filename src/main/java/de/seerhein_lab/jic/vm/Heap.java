package de.seerhein_lab.jic.vm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class representing a heap. Contains HeapObjects and has special HeapObjects
 * for "this" and the "external".
 */
public class Heap {
	public static AtomicLong count = new AtomicLong();
	private final Map<UUID, HeapObject> objects = new HashMap<UUID, HeapObject>();
	private final Set<UUID> publishedObjects = new HashSet<UUID>();

	private final UUID thisID;
	private final UUID externalID;

	/**
	 * Constructor. Initializes the "this" and the "external" HeapObject.
	 */
	public Heap() {
		HeapObject thisObject = new ClassInstance(this);
		thisID = thisObject.getId();
		objects.put(thisID, thisObject);

		ExternalObject externalObject = new ExternalObject(this);
		externalID = externalObject.getId();
		objects.put(externalID, externalObject);
		count.incrementAndGet();
	}

	/**
	 * Copy-Constructor.
	 * 
	 * @param original
	 *            The heap to copy from.
	 */
	public Heap(Heap original) {
		for (UUID id : original.objects.keySet()) {
			objects.put(id, original.objects.get(id).copy(this));
		}

		publishedObjects.addAll(original.publishedObjects);

		thisID = original.thisID;
		externalID = original.externalID;
		count.incrementAndGet();
	}

	/**
	 * Get the HeapObject for the specified UUID. Checks if the id was published
	 * in this heap, then returns the "external".
	 * 
	 * @param id
	 *            The heap to copy from.
	 * @return The HeapObject for the id.
	 */
	public HeapObject get(UUID id) {
		if (id == null)
			return null;

		if (publishedObjects.contains(id))
			return objects.get(externalID);

		if (objects.containsKey(id))
			return objects.get(id);

		throw new NoSuchElementException("HeapObject not found in this Heap");
	}

	public ClassInstance getThisInstance() {
		return (ClassInstance) get(thisID);
	}

	public HeapObject getExternalObject() {
		return get(externalID);
	}

	/**
	 * Creates a new ClassInstance and registers it in the heap.
	 * 
	 * @return The created ClassInstance.
	 */
	public ClassInstance newClassInstance() {
		ClassInstance object = new ClassInstance(this);
		objects.put(object.getId(), object);
		return object;
	}

	/**
	 * Creates a new Array and registers it in the heap.
	 * 
	 * @return The created Array.
	 */
	public Array newArray() {
		Array object = new Array(this);
		objects.put(object.getId(), object);
		return object;
	}

	/**
	 * Publishes all objects that belong to this complex object. The published
	 * objects are replaced by the external object in all referred links,
	 * removed from this heap's set of regular objects, and added to the list of
	 * this heap's published objects.
	 * 
	 * @param obj
	 *            The entry to this complex object to be published
	 */
	public void publish(HeapObject obj) {
		if (obj == null)
			return;

		for (HeapObject o : obj.getClosure()) {
			if (!o.equals(getThisInstance()) && !o.equals(getExternalObject())) {
				// don't publish this in order not to cover further bugs
				// don't publish the external object

				List<HeapObject> referring = new Vector<HeapObject>();

				for (HeapObject referringObj : o.getReferringObjects()) {
					if (!referringObj.equals(getExternalObject()))
						referring.add(referringObj);
				}

				for (int i = 0; i < referring.size(); i++) {
					referring.get(i).replaceReferredObject(o, getExternalObject());
				}

				publishedObjects.add(o.getId());
				objects.remove(o.getId());
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((externalID == null) ? 0 : externalID.hashCode());
		result = prime * result + ((objects == null) ? 0 : objects.hashCode());
		result = prime * result + ((publishedObjects == null) ? 0 : publishedObjects.hashCode());
		result = prime * result + ((thisID == null) ? 0 : thisID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Heap))
			return false;

		Heap other = (Heap) obj;

		if (externalID == null) {
			if (other.externalID != null)
				return false;
		} else if (!externalID.equals(other.externalID))
			return false;

		if (thisID == null) {
			if (other.thisID != null)
				return false;
		} else if (!thisID.equals(other.thisID))
			return false;

		if (!objects.equals(other.objects))
			return false;

		return (publishedObjects.equals(other.publishedObjects));
	}

}
