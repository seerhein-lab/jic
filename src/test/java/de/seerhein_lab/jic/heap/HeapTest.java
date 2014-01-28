package de.seerhein_lab.jic.heap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import de.seerhein_lab.jic.vm.Array;
import de.seerhein_lab.jic.vm.ClassInstance;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.ReferenceSlot;

public class HeapTest {
	private Heap heap;
	private ClassInstance a;
	private ClassInstance b;
	private ClassInstance c;
	private ClassInstance d;
	private ClassInstance e;
	private Array f;

	private ReferenceSlot aRef;
	private ReferenceSlot bRef;
	private ReferenceSlot cRef;
	private ReferenceSlot dRef;
	private ReferenceSlot eRef;
	private ReferenceSlot fRef;

	public void setUpHeap() {
		heap = new Heap();

		a = heap.newClassInstance();
		b = heap.newClassInstance();
		c = heap.newClassInstance();
		d = heap.newClassInstance();
		e = heap.newClassInstance();
		f = heap.newArray();

		aRef = ReferenceSlot.createNewInstance(a);
		bRef = ReferenceSlot.createNewInstance(b);
		cRef = ReferenceSlot.createNewInstance(c);
		dRef = ReferenceSlot.createNewInstance(d);
		eRef = ReferenceSlot.createNewInstance(e);
		fRef = ReferenceSlot.createNewInstance(f);
	}

	@Before
	public void setUpReferences() {
		setUpHeap();

		a.setField("f", b);
		b.setField("f", c);
		b.setField("f2", d);
		c.setField("f", e);
		d.setField("f", e);
		e.setField("f", f);
		f.addComponent(d);
	}

	@Test
	public void testInitalization() {
		assertNotNull(heap.getExternalObject());
		assertNotNull(heap.getThisInstance());
	}

	@Test
	public void testPublishNull() {
		heap.publish(null);
	}

	@Test
	public void testPublish() {
		assertEquals(a, heap.getObject(aRef));
		assertEquals(b, heap.getObject(bRef));
		assertEquals(c, heap.getObject(cRef));
		assertEquals(d, heap.getObject(dRef));
		assertEquals(e, heap.getObject(eRef));
		assertEquals(f, heap.getObject(fRef));

		heap.publish(b);

		assertEquals(a, heap.getObject(aRef));
		assertEquals(heap.getExternalObject(), heap.getObject(bRef));
		assertEquals(heap.getExternalObject(), heap.getObject(cRef));
		assertEquals(heap.getExternalObject(), heap.getObject(dRef));
		assertEquals(heap.getExternalObject(), heap.getObject(eRef));
		assertEquals(heap.getExternalObject(), heap.getObject(fRef));
	}

	@Test
	public void testNotPublishThis() {
		ClassInstance thisInstance = heap.getThisInstance();
		heap.publish(thisInstance);
		// assertEquals(thisInstance, heap.get(thisInstance.getId()));
		assertEquals(thisInstance, heap.getThisInstance());
	}

	@Test
	public void testRepublish() {
		assertEquals(f, heap.getObject(fRef));
		heap.publish(f);
		assertEquals(heap.getExternalObject(), heap.getObject(fRef));
		heap.publish(f);
		assertEquals(heap.getExternalObject(), heap.getObject(fRef));
	}
}
