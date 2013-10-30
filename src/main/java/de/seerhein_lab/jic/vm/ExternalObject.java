package de.seerhein_lab.jic.vm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ExternalObject extends HeapObject {
	private final Set<UUID> refers = new HashSet<UUID>();

	public ExternalObject(Heap heap) {
		super(heap);
		refers.add(getId());
	}

	public ExternalObject(ExternalObject external, Heap heap) {
		super(external, heap);
	}

	@Override
	public void replaceAllOccurrencesOfReferredObject(HeapObject oldObject, HeapObject newObject) {
		throw new AssertionError("must not be called.");
	}

	@Override
	public Iterator<HeapObject> getReferredIterator() {
		return new Iterator<HeapObject>() {
			Iterator<UUID> idIterator = refers.iterator();

			UUID lookAhead;
			{
				lookAhead();
			}

			private void lookAhead() {
				lookAhead = null;
				while (lookAhead == null && idIterator.hasNext()) {
					lookAhead = idIterator.next();
				}
			}

			@Override
			public boolean hasNext() {
				return lookAhead != null;
			}

			public HeapObject next() {
				HeapObject result = heap.get(lookAhead);
				lookAhead();
				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	ExternalObject copy(Heap heap) {
		return new ExternalObject(this, heap);
	}

	@Override
	public HeapObject deepCopy(Heap heap) {
		return heap.getExternalObject();
	}

	@Override
	protected HeapObject deepCopy(Heap heap, Map<HeapObject, HeapObject> visited) {
		return heap.getExternalObject();
	}

}
