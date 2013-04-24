package de.htwg_konstanz.in.jca;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import de.htwg_konstanz.in.jca.slot.HeapObject;
import de.htwg_konstanz.in.jca.slot.ReferenceSlot;

public class Heap {

	private final Set<HeapObject> objects = new HashSet<HeapObject>();
	private final Set<Container> set = new HashSet<Container>();

	private static class Container {
		private final UUID object;
		private final ReferenceSlot referenceSlot;

		public Container(UUID object, ReferenceSlot referenceSlot) {
			this.object = object;
			this.referenceSlot = referenceSlot;
		}

		/**
		 * @return the object
		 */
		public UUID getObject() {
			return object;
		}

		/**
		 * @return the referenceSlot
		 */
		public ReferenceSlot getReferenceSlot() {
			return referenceSlot;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((object == null) ? 0 : object.hashCode());
			result = prime * result
					+ ((referenceSlot == null) ? 0 : referenceSlot.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Container))
				return false;
			Container other = (Container) obj;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			if (referenceSlot == null) {
				if (other.referenceSlot != null)
					return false;
			} else if (!referenceSlot.equals(other.referenceSlot))
				return false;
			return true;
		}
	}

	public Heap() {
	}

	public Heap(Heap original) {
		for (HeapObject object : original.objects) {
			objects.add(new HeapObject(object));
		}
		set.addAll(original.set);
	}

	public void registerObject(ReferenceSlot container, HeapObject object) {
		objects.add(object);
		set.add(new Container(object.getId(), container));
		container.addPossibleObject(object.getId());
	}

	public ReferenceSlot getContainer(UUID object) {
		for (Container container : set) {
			if (container.object.equals(object)) {
				return container.referenceSlot;
			}
		}
		return null;
	}

	public void linkReferences(ReferenceSlot left, ReferenceSlot right) {
		for (UUID possibleObject : left.getPossibleObjects()) {

		}
	}

}
