package de.seerhein_lab.jca.slot;

import java.util.UUID;

public class ReferenceSlot extends Slot {
	private final UUID objectID;

	public ReferenceSlot() {
		objectID = null;
	}

	public ReferenceSlot(UUID objectID) {
		this.objectID = objectID;
	}

	@Override
	public int getNumSlots() {
		return 1;
	}

	@Override
	public Slot copy() {
		return this;
	}

	/**
	 * @return the possibleObjects
	 */
	public UUID getID() {
		return objectID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((objectID == null) ? 0 : objectID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ReferenceSlot))
			return false;
		ReferenceSlot other = (ReferenceSlot) obj;
		if (objectID == null) {
			if (other.objectID != null)
				return false;
		} else if (!objectID.equals(other.objectID))
			return false;
		return true;
	}
}
