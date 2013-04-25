package de.htwg_konstanz.in.jca;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.htwg_konstanz.in.jca.slot.HeapObject;

public class Heap {

	private final Map<UUID, HeapObject> objects = new HashMap<UUID, HeapObject>();
	private final Map<UUID, UUID> replacedBy = new HashMap<UUID, UUID>();

	private final UUID thisID;
	private final UUID externalID;
	private final UUID nullID;

	public Heap() {
		HeapObject thisObject = new HeapObject();
		thisID = thisObject.getId();
		objects.put(thisID, thisObject);

		HeapObject externalObject = new HeapObject();
		externalID = externalObject.getId();
		objects.put(externalID, externalObject);

		HeapObject nullObject = new HeapObject();
		nullID = nullObject.getId();
		objects.put(nullID, nullObject);
	}

	public Heap(Heap original) {
		for (UUID id : original.objects.keySet()) {
			objects.put(id, new HeapObject(original.objects.get(id)));
		}

		replacedBy.putAll(original.replacedBy);

		thisID = original.thisID;
		externalID = original.externalID;
		nullID = original.nullID;
	}

	public HeapObject get(UUID id) {
		id = replacedBy.containsKey(id) ? replacedBy.get(id) : id;
		return objects.get(id);
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

}
