package de.seerhein_lab.jca.heap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Heap {

	private final Map<UUID, HeapObject> objects = new HashMap<UUID, HeapObject>();
	private final Set<UUID> publishedObjects = new HashSet<UUID>();

	private final UUID thisID;
	private final UUID externalID;

	public Heap() {
		HeapObject thisObject = new ClassInstance(this);
		thisID = thisObject.getId();
		objects.put(thisID, thisObject);

		HeapObject externalObject = new HeapObject(this);
		externalID = externalObject.getId();
		objects.put(externalID, externalObject);
	}

	public Heap(Heap original) {
		for (UUID id : original.objects.keySet()) {
			objects.put(id, original.objects.get(id).copy(this));
		}

		publishedObjects.addAll(original.publishedObjects);

		thisID = original.thisID;
		externalID = original.externalID;
	}

	public HeapObject get(UUID id) {
		return publishedObjects.contains(id) ? objects.get(externalID)
				: objects.get(id);
	}

	// public void linkReferences(ReferenceSlot left, ReferenceSlot right) {
	// for (UUID possibleObject : left.getPossibleObjects()) {
	//
	// }
	// }
	//
	public ClassInstance getThisInstance() {
		return (ClassInstance) get(thisID);
	}

	public HeapObject getExternalObject() {
		return get(externalID);
	}

	// public UUID getNullID() {
	// return nullID;
	// }

	public ClassInstance newClassInstance() {
		ClassInstance object = new ClassInstance(this);
		objects.put(object.getId(), object);
		return object;
	}

	public Array newArray() {
		Array object = new Array(this);
		objects.put(object.getId(), object);
		return object;
	}

	public void publish(UUID id) {
		if (id.equals(thisID)) {
			// do not publish 'this' in order not to cover further bugs
			return;
		}

		publishedObjects.add(id);

		HeapObject object = objects.get(id);
		HeapObject external = objects.get(externalID);

		for (Iterator<HeapObject> iterator = object.getReferringIterator(); iterator
				.hasNext();) {
			UUID referringObject = iterator.next().getId();
			get(referringObject).replaceReferredObject(id, externalID);
			external.addReferringObject(get(referringObject));
		}

		for (Iterator<HeapObject> iterator = object.getReferredIterator(); iterator
				.hasNext();) {
			publish(iterator.next().getId());
		}
	}

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
