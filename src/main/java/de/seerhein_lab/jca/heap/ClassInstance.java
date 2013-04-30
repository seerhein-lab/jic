package de.seerhein_lab.jca.heap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClassInstance extends HeapObject {
	private Map<String, UUID> refers = new HashMap<String, UUID>();

	public ClassInstance() {
		super();
	}

	ClassInstance(ClassInstance original) {
		super(original);
		refers.putAll(original.refers);
	}

	@Override
	void replaceReferredObject(UUID oldID, UUID newID) {
		for (String field : refers.keySet())
			if (refers.get(field).equals(oldID))
				refers.put(field, newID);
	}

	@Override
	HeapObject copy() {
		return new ClassInstance(this);
	}

	@Override
	public Collection<UUID> getReferredObjects() {
		return refers.values();
	}

}
