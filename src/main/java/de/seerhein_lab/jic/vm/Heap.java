package de.seerhein_lab.jic.vm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.bcel.generic.Type;

import de.seerhein_lab.jic.analyzer.ClassHelper;

/**
 * Class whose instances represent heaps. A heap contains objects, some of which
 * have been published, as well as two special objects, i.e. the 'this' instance
 * and the external object. The higher layers can create objects only through a
 * heap.
 */
public class Heap {
	public static AtomicLong count = new AtomicLong();
	private final Map<UUID, HeapObject> objects = new HashMap<UUID, HeapObject>();
	private final Set<UUID> publishedMutableObjects = new HashSet<UUID>();
	private final Set<UUID> publishedImmutableObjects = new HashSet<UUID>();

	private final UUID thisID;
	private final UUID mutableExternalID;
	private final UUID immutableExternalID;

	/**
	 * Constructor. Creates the 'this' instance and the external object.
	 */
	public Heap(boolean immutable) {
		HeapObject thisObject = new ClassInstance(this, immutable);
		thisID = thisObject.getId();
		objects.put(thisID, thisObject);

		ExternalObject mutableExternalObject = new ExternalObject(this, false);
		mutableExternalID = mutableExternalObject.getId();
		objects.put(mutableExternalID, mutableExternalObject);

		ExternalObject immutableExternalObject = new ExternalObject(this, true);
		immutableExternalID = immutableExternalObject.getId();
		objects.put(immutableExternalID, immutableExternalObject);

		count.incrementAndGet();
	}

	public Heap() {
		this(false);
	}

	/**
	 * Copy-Constructor.
	 * 
	 * @param original
	 *            The heap to copy from.
	 */
	public Heap(Heap original) {
		for (UUID id : original.objects.keySet()) {
			objects.put(id, original.objects.get(id).copy(this));
		}

		publishedMutableObjects.addAll(original.publishedMutableObjects);
		publishedImmutableObjects.addAll(original.publishedImmutableObjects);

		thisID = original.thisID;
		mutableExternalID = original.mutableExternalID;
		immutableExternalID = original.immutableExternalID;
		count.incrementAndGet();
	}

	/**
	 * Returns the object with the given ID <code>id</code>. If <code>id</code>
	 * is null, the null is returned; if the object has been published, the
	 * external object is returned.
	 * 
	 * @param id
	 *            the ID of the object to be returned
	 * @return the object with the given ID,
	 */
	protected HeapObject get(UUID id) {
		if (id == null)
			return null;

		if (publishedMutableObjects.contains(id))
			return objects.get(mutableExternalID);

		if (publishedImmutableObjects.contains(id))
			return objects.get(immutableExternalID);

		if (objects.containsKey(id))
			return objects.get(id);

		throw new NoSuchElementException("HeapObject not found in this Heap");
	}

	/**
	 * Gets this heap's 'this' instance
	 * 
	 * @return this heap's 'this' instance
	 */
	public ClassInstance getThisInstance() {
		return (ClassInstance) get(thisID);
	}

	/**
	 * Gets this heap's mutable external object
	 * 
	 * @return this heap's mutable external object
	 */
	public HeapObject getMutableExternalObject() {
		return get(mutableExternalID);
	}

	/**
	 * Gets this heap's immutable external object
	 * 
	 * @return this heap's immutable external object
	 */
	public HeapObject getImmutableExternalObject() {
		return get(immutableExternalID);
	}

	public HeapObject getExternalObject(boolean immutable) {
		return immutable ? getImmutableExternalObject() : getMutableExternalObject();
	}

	/**
	 * Creates a new class instance and registers it in the heap.
	 * 
	 * @return The newly created class instance.
	 */
	public ClassInstance newClassInstance(boolean immutable) {
		ClassInstance object = new ClassInstance(this, immutable);
		objects.put(object.getId(), object);
		return object;
	}

	public ClassInstance newClassInstanceOfDynamicType(Type type) {
		return newClassInstance(ClassHelper.isImmutable(type));
	}

	public ClassInstance newClassInstanceOfStaticType(Type type) {
		return newClassInstance(ClassHelper.isImmutableAndFinal(type));
	}

	public ClassInstance newClassInstanceOfDynamicType(String type) {
		return newClassInstance(ClassHelper.isImmutable(type));
	}

	/**
	 * Creates a new array and registers it in the heap.
	 * 
	 * @return The newly created array.
	 */
	public Array newArray() {
		Array object = new Array(this);
		objects.put(object.getId(), object);
		return object;
	}

	/**
	 * Publishes all objects that belong to this complex object. The published
	 * objects are replaced by the external object in all referred links,
	 * removed from this heap's set of regular objects, and added to the list of
	 * this heap's published objects.
	 * 
	 * @param obj
	 *            The entry to this complex object to be published
	 */
	public void publish(HeapObject obj) {
		if (obj == null)
			return;

		if (obj.equals(getThisInstance()) || obj.isExternal())
			// don't publish this in order not to cover further bugs
			// don't publish the external object
			return;

		for (HeapObject o : obj.getClosure()) {
			if (!o.equals(getThisInstance()) && !o.isExternal()) {
				// don't publish this in order not to cover further bugs
				// don't publish the external object

				List<HeapObject> referring = new Vector<HeapObject>();

				for (HeapObject referringObj : o.getReferringObjects()) {
					if (!referringObj.isExternal())
						referring.add(referringObj);
				}

				for (int i = 0; i < referring.size(); i++) {
					referring.get(i).replaceReferredObject(o, getExternalObject(o.isImmutable()));
				}

				if (o.isImmutable())
					publishedImmutableObjects.add(o.getId());
				else
					publishedMutableObjects.add(o.getId());

				objects.remove(o.getId());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mutableExternalID == null) ? 0 : mutableExternalID.hashCode());
		result = prime * result + ((objects == null) ? 0 : objects.hashCode());
		result = prime * result
				+ ((publishedMutableObjects == null) ? 0 : publishedMutableObjects.hashCode());
		result = prime * result + ((thisID == null) ? 0 : thisID.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.seerhein_lab.jic.vm.HeapObject#hashCode()
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Heap))
			return false;

		Heap other = (Heap) obj;

		if (mutableExternalID == null) {
			if (other.mutableExternalID != null)
				return false;
		} else if (!mutableExternalID.equals(other.mutableExternalID))
			return false;

		if (thisID == null) {
			if (other.thisID != null)
				return false;
		} else if (!thisID.equals(other.thisID))
			return false;

		if (!objects.equals(other.objects))
			return false;

		return (publishedMutableObjects.equals(other.publishedMutableObjects));
	}

}
