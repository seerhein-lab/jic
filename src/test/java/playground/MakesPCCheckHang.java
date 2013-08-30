package playground;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.seerhein_lab.jca.heap.ClassInstance;
import de.seerhein_lab.jca.heap.ExternalObject;
import de.seerhein_lab.jca.heap.HeapObject;

public class MakesPCCheckHang {

	private final Map<UUID, HeapObject> objects = new HashMap<UUID, HeapObject>();
	private final Set<UUID> publishedObjects = new HashSet<UUID>();

	public MakesPCCheckHang() {
		HeapObject thisObject = new ClassInstance(null);
		ExternalObject externalObject = new ExternalObject(null);
	}
}
