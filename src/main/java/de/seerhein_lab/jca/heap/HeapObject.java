package de.seerhein_lab.jca.heap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HeapObject {
	protected final UUID id;
	protected Set<UUID> referredBy = new HashSet<UUID>();

	HeapObject() {
		id = UUID.randomUUID();
	}

	HeapObject(HeapObject original) {
		id = original.id;
		referredBy.addAll(original.referredBy);
	}

	void replaceReferredObject(UUID oldID, UUID newID) {
		throw new AssertionError("Must not be called on a HeapObject instance");
	}

	public Collection<UUID> getReferredObjects() {
		throw new AssertionError("Must not be called on a HeapObject instance");
	}

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

	HeapObject copy() {
		return this;
	}

}