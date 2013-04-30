package de.seerhein_lab.jca.heap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HeapObject {
	private final UUID id;
	private Map<String, UUID> refers = new HashMap<String, UUID>();
	private Set<UUID> referredBy = new HashSet<UUID>();

	HeapObject() {
		id = UUID.randomUUID();
	}

	public HeapObject(HeapObject original) {
		id = original.id;
		refers.putAll(original.refers);
		referredBy.addAll(original.referredBy);
	}

	Collection<UUID> getReferredObjects() {
		return refers.values();
	}

	void addReferringObject(UUID id) {
		referredBy.add(id);
	}

	void replaceReferredObject(UUID oldID, UUID newID) {
		for (String field : refers.keySet())
			if (refers.get(field).equals(oldID))
				refers.put(field, newID);
	}

	Set<UUID> getReferringObjects() {
		return referredBy;
	}

	/**
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

}