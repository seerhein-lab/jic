package de.seerhein_lab.jic.vm;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Class whose instances represent the external object that doubles as both an
 * external class instance and an external array. An external object only has
 * referring objects as defined in the superclass <code>HeapObject</code>, but
 * no referred objects, because an object that is referred by the external
 * object is itself external.
 * 
 */
public final class ExternalObject extends HeapObject {

	/**
	 * Constructor.
	 * 
	 * @param heap
	 *            Heap this external object resides on. Must not be null.
	 * @param immutable
	 */
	public ExternalObject(Heap heap, boolean immutable) {
		super(heap, immutable);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param original
	 *            external object to copy from. Must not be null.
	 * @param heap
	 *            Heap this external object resides on. Must not be null.
	 */
	public ExternalObject(ExternalObject external, Heap heap) {
		super(external, heap);
	}

	@Override
	public boolean isExternal() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject# replaceReferredObject
	 * (de.seerhein_lab.jic.vm.HeapObject, de.seerhein_lab.jic.vm.HeapObject)
	 */
	@Override
	protected void replaceReferredObject(HeapObject oldObject, HeapObject newObject) {
		throw new AssertionError("must not be called.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#copy(de.seerhein_lab.jic.vm.Heap)
	 */
	@Override
	protected ExternalObject copy(Heap heap) {
		return new ExternalObject(this, heap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.seerhein_lab.jic.vm.HeapObject#deepCopy(de.seerhein_lab.jic.vm.Heap,
	 * java.util.Map)
	 */
	@Override
	protected HeapObject deepCopy(Heap heap, Map<HeapObject, HeapObject> visited) {
		return heap.getExternalObject(this.isImmutable());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#getReferredObjects()
	 */
	@Override
	public Iterable<HeapObject> getReferredObjects() {
		return new Iterable<HeapObject>() {
			@Override
			public Iterator<HeapObject> iterator() {
				return new Iterator<HeapObject>() {
					@Override
					public boolean hasNext() {
						return false;
					}

					public HeapObject next() {
						throw new NoSuchElementException();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}

		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		return (obj instanceof ExternalObject);
	}

}
