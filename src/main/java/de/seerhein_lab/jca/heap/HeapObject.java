package de.seerhein_lab.jca.heap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class HeapObject {
	protected final UUID id;
	protected Set<UUID> referredBy = new HashSet<UUID>();

	HeapObject() {
		id = UUID.randomUUID();
	}

	HeapObject(HeapObject original) {
		id = original.id;
		referredBy.addAll(original.referredBy);
	}

	abstract void replaceReferredObject(UUID oldID, UUID newID);

	abstract Collection<UUID> getReferredObjects();

	void addReferringObject(UUID id) {
		referredBy.add(id);
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

	abstract HeapObject copy();

}