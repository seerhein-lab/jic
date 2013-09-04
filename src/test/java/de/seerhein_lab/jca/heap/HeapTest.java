package de.seerhein_lab.jca.heap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class HeapTest {
	private Heap heap;
	private ClassInstance a;
	private ClassInstance b;
	private ClassInstance c;
	private ClassInstance d;
	private ClassInstance e;
	private Array f;

	public void setUpHeap() {
		heap = new Heap();

		a = heap.newClassInstance();
		b = heap.newClassInstance();
		c = heap.newClassInstance();
		d = heap.newClassInstance();
		e = heap.newClassInstance();
		f = heap.newArray();
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
		assertEquals(a, heap.get(a.getId()));
		assertEquals(b, heap.get(b.getId()));
		assertEquals(c, heap.get(c.getId()));
		assertEquals(d, heap.get(d.getId()));
		assertEquals(e, heap.get(e.getId()));
		assertEquals(f, heap.get(f.getId()));

		heap.publish(b);

		assertEquals(a, heap.get(a.getId()));
		assertEquals(heap.getExternalObject(), heap.get(b.getId()));
		assertEquals(heap.getExternalObject(), heap.get(c.getId()));
		assertEquals(heap.getExternalObject(), heap.get(d.getId()));
		assertEquals(heap.getExternalObject(), heap.get(e.getId()));
		assertEquals(heap.getExternalObject(), heap.get(f.getId()));
	}

	@Test
	public void testNotPublishThis() {
		ClassInstance thisInstance = heap.getThisInstance();
		heap.publish(thisInstance);
		assertEquals(thisInstance, heap.get(thisInstance.getId()));
	}

	@Test
	public void testRepublish() {
		assertEquals(f, heap.get(f.getId()));
		heap.publish(heap.get(f.getId()));
		assertEquals(heap.getExternalObject(), heap.get(f.getId()));
		heap.publish(heap.get(f.getId()));
		assertEquals(heap.getExternalObject(), heap.get(f.getId()));
	}
}
