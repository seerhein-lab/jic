package de.htwg_konstanz.in.jca.slot;

import java.util.UUID;

public class ReferenceSlot extends Slot {

	private final UUID objectID;

	public ReferenceSlot() {
		objectID = null;
	}

	public ReferenceSlot(UUID objectID) {
		this.objectID = objectID;
	}

	private ReferenceSlot(ReferenceSlot original) {
		objectID = original.objectID;
	}

	@Override
	public int getNumSlots() {
		return 1;
	}

	@Override
	public Slot copy() {
		return new ReferenceSlot(this);
	}

	/**
	 * @return the possibleObjects
	 */
	public UUID getID() {
		return objectID;
	}

}
