package de.htwg_konstanz.in.jca;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.htwg_konstanz.in.jca.slot.HeapObject;
import de.htwg_konstanz.in.jca.slot.ReferenceSlot;

public class Heap {

	private final Set<HeapObject> objects = new HashSet<HeapObject>();
	private final Map<UUID, ReferenceSlot> map = new HashMap<UUID, ReferenceSlot>();

	public Heap() {
	}

	public Heap(Heap original) {
		for (HeapObject object : original.objects) {
			objects.add(new HeapObject(object));
		}
		map.putAll(original.map);
	}
}
