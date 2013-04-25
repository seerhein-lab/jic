package de.htwg_konstanz.in.jca.heap;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HeapObject {
	private final UUID id;
	private Set<UUID> refers = new HashSet<UUID>();
	private Set<UUID> referredBy = new HashSet<UUID>();

	HeapObject() {
		id = UUID.randomUUID();
	}

	public HeapObject(HeapObject original) {
		id = original.id;
		refers.addAll(original.refers);
		referredBy.addAll(original.referredBy);
	}

	Set<UUID> getReferredObjects() {
		return refers;
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