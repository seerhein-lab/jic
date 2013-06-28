package de.seerhein_lab.jca.heap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import de.seerhein_lab.jca.Pair;

public final class ExternalObject extends HeapObject {
	private final Set<UUID> refers = new HashSet<UUID>();
	
	
	public ExternalObject(Heap heap) {
		super(heap);
		refers.add(id);
	}
	
	public ExternalObject(ExternalObject external, Heap heap) {
		super(external, heap);
	}

	@Override
	public void replaceAllOccurrencesOfReferredObject(HeapObject oldObject,
			HeapObject newObject) {
	}

	@Override
	public Iterator<HeapObject> getReferredIterator() {
		return new Iterator<HeapObject>() {
			Iterator<UUID> idIterator = refers.iterator();

			@Override
			public boolean hasNext() {
				return idIterator.hasNext();
			}

			@Override
			public HeapObject next() {
				return heap.get(idIterator.next());
			}

			@Override
			public void remove() {
				idIterator.remove();
			}
		};
	}

	@Override
	ExternalObject copy(Heap heap) {
		return new ExternalObject(this, heap);
	}

	
}
