package de.seerhein_lab.jca.heap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Array extends HeapObject {
	private Set<UUID> refers = new HashSet<UUID>();

	public Array() {
		super();
	}

	public Array(Array original) {
		super(original);
		refers.addAll(original.getReferredObjects());
	}

	@Override
	HeapObject copy() {
		return new Array(this);
	}

	@Override
	public Collection<UUID> getReferredObjects() {
		return refers;
	}

	@Override
	void replaceReferredObject(UUID oldID, UUID newID) {
		refers.remove(oldID);
		refers.add(newID);
	}

}
