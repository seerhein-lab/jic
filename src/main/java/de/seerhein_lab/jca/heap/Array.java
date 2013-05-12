package de.seerhein_lab.jca.heap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Array extends HeapObject {
	private Set<UUID> refers = new HashSet<UUID>();

	public Array(Heap heap) {
		super(heap);
	}

	public Array(Array original, Heap heap) {
		super(original, heap);
		refers.addAll(original.getReferredObjects());
	}

	@Override
	HeapObject copy(Heap heap) {
		return new Array(this, heap);
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

	void addReferredObject(UUID id) {
		refers.add(id);
	}

	@Override
	boolean refers(UUID toSearch, Heap heap,
			HashSet<AlreadyVisited> alreadyVisited) {
		for (UUID entry : refers) {
			if (alreadyVisited.add(new AlreadyVisited(this, heap.get(entry)))) {
				// if was not visited before
				if (entry.equals(toSearch)
						|| heap.get(entry).refers(toSearch, heap,
								alreadyVisited)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((refers == null) ? 0 : refers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Array))
			return false;
		Array other = (Array) obj;
		if (refers == null) {
			if (other.refers != null)
				return false;
		} else if (!refers.equals(other.refers))
			return false;
		return true;
	}
}
