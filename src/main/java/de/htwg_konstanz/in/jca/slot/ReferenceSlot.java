package de.htwg_konstanz.in.jca.slot;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ReferenceSlot extends Slot {

	private final Set<UUID> possibleObjects = new HashSet<UUID>();

	public ReferenceSlot() {
	}

	private ReferenceSlot(ReferenceSlot original) {
		possibleObjects.addAll(original.possibleObjects);
	}

	@Override
	public int getNumSlots() {
		return 1;
	}

	@Override
	public Slot copy() {
		return new ReferenceSlot(this);
	}

}
