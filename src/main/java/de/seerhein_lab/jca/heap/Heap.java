package de.seerhein_lab.jca.heap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.seerhein_lab.jca.slot.ReferenceSlot;

/**
 * Class representing a heap. Contains HeapObjects and has special HeapObjects
 * for "this" and the "external".
 */
public class Heap {

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
		return publishedObjects.contains(id) ? objects.get(externalID)
				: objects.get(id);
	}

	public ClassInstance getThisInstance() {
		return (ClassInstance) get(thisID);
	}

	public HeapObject getExternalObject() {
		return get(externalID);
	}

	// public UUID getNullID() {
	// return nullID;
	// }

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

	public HeapObject getObject(ReferenceSlot reference) {
		return objects.get(reference.getID());
	}

	/**
	 * Publishes an Object. The published Object becomes the "external" Object,
	 * the references of all referring Objects are updated and all referred
	 * Objects are published recursively.
	 * 
	 * @param obj
	 *            The object to be published
	 */
	public void publish(HeapObject obj) {
		if (obj.getId().equals(thisID) || obj.getId().equals(externalID)) {
			// do not publish 'this' in order not to cover further bugs
			// do not publish 'external', is already published
			return;
		}

		publishedObjects.add(obj.getId());

		HeapObject external = objects.get(externalID);

		for (Iterator<HeapObject> iterator = obj.getReferringIterator(); iterator
				.hasNext();) {
			HeapObject referringObject = iterator.next();
			if (!referringObject.equals(external)) {
				referringObject.replaceAllOccurrencesOfReferredObject(obj, external);
				external.addReferringObject(referringObject);
			}
		}

		for (Iterator<HeapObject> iterator = obj.getReferredIterator(); iterator
				.hasNext();) {
			HeapObject referred = iterator.next();
			if (!referred.equals(external))
				publish(referred);
		}
	}

	/**
	 * Link two HeapObjects. The left object refers the right object (left.field
	 * = rigtht). If the left object is a ClassInstance "field" is the name of
	 * the field, if it is a
	 * 
	 * @param left
	 *            The left side of an assignment.
	 * @param field
	 *            The fieldname as String. If "left" is an Array this parameter
	 *            is ignored.
	 * @param right
	 *            The right side of an assignment.
	 */
	public void linkObjects(UUID left, String field, UUID right) {
		get(right).addReferringObject(get(left));

		HeapObject leftSide = get(left);
		if (leftSide instanceof Array) {
			((Array) leftSide).addReferredObject(right);
		} else if (leftSide instanceof ClassInstance) {
			((ClassInstance) leftSide).addReferredObject(this, field, right);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((objects == null) ? 0 : objects.hashCode());
		result = prime
				* result
				+ ((publishedObjects == null) ? 0 : publishedObjects.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Heap))
			return false;
		Heap other = (Heap) obj;
		if (objects == null) {
			if (other.objects != null)
				return false;
		} else if (!objects.equals(other.objects))
			return false;
		if (publishedObjects == null) {
			if (other.publishedObjects != null)
				return false;
		} else if (!publishedObjects.equals(other.publishedObjects))
			return false;
		return true;
	}
}
