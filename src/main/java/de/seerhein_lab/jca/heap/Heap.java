package de.seerhein_lab.jca.heap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Heap {

	private final Map<UUID, HeapObject> objects = new HashMap<UUID, HeapObject>();
	private final Set<UUID> publishedObjects = new HashSet<UUID>();

	private final UUID thisID;
	private final UUID externalID;
	// private final UUID nullID;
	private final UUID unknownID;

	public Heap() {
		HeapObject thisObject = new ClassInstance();
		thisID = thisObject.getId();
		objects.put(thisID, thisObject);

		HeapObject externalObject = new HeapObject();
		externalID = externalObject.getId();
		objects.put(externalID, externalObject);

		// HeapObject nullObject = new ClassInstance();
		// nullID = nullObject.getId();
		// objects.put(nullID, nullObject);

		HeapObject unknownObject = new ClassInstance();
		unknownID = unknownObject.getId();
		objects.put(unknownID, unknownObject);
	}

	public Heap(Heap original) {
		for (UUID id : original.objects.keySet()) {
			objects.put(id, original.objects.get(id).copy());
		}

		publishedObjects.addAll(original.publishedObjects);

		thisID = original.thisID;
		externalID = original.externalID;
		// nullID = original.nullID;
		unknownID = original.unknownID;
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
	public UUID getThisID() {
		return thisID;
	}

	public UUID getExternalID() {
		return externalID;
	}

	// public UUID getNullID() {
	// return nullID;
	// }

	public UUID newClassInstance() {
		ClassInstance object = new ClassInstance();
		UUID id = object.getId();
		objects.put(id, object);
		return id;
	}

	public UUID newArray() {
		Array object = new Array();
		UUID id = object.getId();
		objects.put(id, object);
		return id;
	}

	public void publish(UUID id) {
		publishedObjects.add(id);

		HeapObject object = objects.get(id);
		HeapObject external = objects.get(externalID);

		for (UUID referringObject : object.getReferringObjects()) {
			get(referringObject).replaceReferredObject(id, externalID);
			external.addReferringObject(referringObject);
		}

		for (UUID referredObject : object.getReferredObjects()) {
			publish(referredObject);
		}
	}

	public void linkObjects(UUID left, String field, UUID right) {
		get(right).addReferringObject(left);

		HeapObject leftSide = get(left);
		if (leftSide instanceof Array) {
			((Array) leftSide).addReferredObject(right);
		} else if (leftSide instanceof ClassInstance) {
			((ClassInstance) leftSide).addReferredObject(this, field, right);
		}
	}
}
