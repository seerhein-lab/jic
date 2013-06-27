package de.seerhein_lab.jca.heap;

public final class ExternalObject extends HeapObject {
	
	public ExternalObject(Heap heap) {
		super(heap);
	}
	
	public ExternalObject(ExternalObject external, Heap heap) {
		super(external, heap);
	}

	
}
