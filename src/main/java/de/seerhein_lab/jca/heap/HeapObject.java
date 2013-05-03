package de.seerhein_lab.jca.heap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HeapObject {
	protected final UUID id;
	protected Set<UUID> referredBy = new HashSet<UUID>();

	static class AlreadyVisited {
		private final HeapObject from;
		private final HeapObject to;

		public AlreadyVisited(HeapObject from, HeapObject to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof AlreadyVisited)) {
				return false;
			}
			AlreadyVisited other = (AlreadyVisited) obj;
			if (from == null) {
				if (other.from != null) {
					return false;
				}
			} else if (!from.equals(other.from)) {
				return false;
			}
			if (to == null) {
				if (other.to != null) {
					return false;
				}
			} else if (!to.equals(other.to)) {
				return false;
			}
			return true;
		}

	}

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

	public Set<UUID> getReferringObjects() {
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

	public boolean referredBy(UUID toSearch, Heap heap) {
		return referredBy(toSearch, heap, new HashSet<AlreadyVisited>());
	}

	boolean referredBy(UUID toSearch, Heap heap,
			HashSet<AlreadyVisited> alreadyVisited) {
		for (UUID object : referredBy) {
			if (alreadyVisited.add(new AlreadyVisited(this, heap.get(object)))) {
				// if it was not in the set
				if (object.equals(toSearch)
						|| heap.get(object).referredBy(toSearch, heap,
								alreadyVisited)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean refers(UUID toSearch, Heap heap) {
		return refers(toSearch, heap, new HashSet<AlreadyVisited>());
	}

	boolean refers(UUID toSearch, Heap heap,
			HashSet<AlreadyVisited> alreadyVisited) {
		if (toSearch.equals(heap.getExternalID())
				&& id.equals(heap.getExternalID()))
			return true;
		throw new AssertionError("Must not be called in this context");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((referredBy == null) ? 0 : referredBy.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof HeapObject))
			return false;
		HeapObject other = (HeapObject) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (referredBy == null) {
			if (other.referredBy != null)
				return false;
		} else if (!referredBy.equals(other.referredBy))
			return false;
		return true;
	}
}