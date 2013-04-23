package de.htwg_konstanz.in.jca.slot;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HeapObject {
	public final static HeapObject THIS_REFERENCE = new HeapObject();
	public final static HeapObject EXTERNAL_REFERENCE = new HeapObject();
	public final static HeapObject NULL_REFERENCE = new HeapObject();

	private final UUID id;
	private Set<UUID> refers = new HashSet<UUID>();
	private Set<UUID> referedBy = new HashSet<UUID>();

	public HeapObject() {
		id = UUID.randomUUID();
	}

	public HeapObject(HeapObject original) {
		id = original.id;
		refers.addAll(original.refers);
		referedBy.addAll(original.referedBy);
	}

	/**
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

}