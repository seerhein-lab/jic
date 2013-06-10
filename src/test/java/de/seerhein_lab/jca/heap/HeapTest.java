package de.seerhein_lab.jca.heap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

	@Before
	public void setUp() {
		heap = new Heap();

		a = heap.newClassInstance();
		b = heap.newClassInstance();
		c = heap.newClassInstance();
		d = heap.newClassInstance();
		e = heap.newClassInstance();
		f = heap.newArray();

		heap.linkObjects(a.getId(), "f", b.getId());
		heap.linkObjects(b.getId(), "f", c.getId());
		heap.linkObjects(b.getId(), "f2", d.getId());
		heap.linkObjects(c.getId(), "f", e.getId());
		heap.linkObjects(d.getId(), "f", e.getId());
		heap.linkObjects(e.getId(), "f", f.getId());
		heap.linkObjects(f.getId(), "f", d.getId());
	}

	@Test
	public void testInitalization() {
		assertNotNull(heap.getExternalObject());
		assertNotNull(heap.getThisInstance());
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
	public void testLinkObjects() {

		assertTrue(a.refers(b.getId(), heap));
		assertTrue(b.refers(c.getId(), heap));
		assertTrue(b.refers(d.getId(), heap));
		assertTrue(c.refers(e.getId(), heap));
		assertTrue(d.refers(e.getId(), heap));
		assertTrue(e.refers(f.getId(), heap));
		assertTrue(f.refers(d.getId(), heap));

		assertTrue(b.referredBy(a.getId(), heap));
		assertTrue(c.referredBy(b.getId(), heap));
		assertTrue(d.referredBy(b.getId(), heap));
		assertTrue(e.referredBy(c.getId(), heap));
		assertTrue(e.referredBy(d.getId(), heap));
		assertTrue(f.referredBy(e.getId(), heap));
		assertTrue(d.referredBy(f.getId(), heap));
	}

	@Test
	public void testLinkObjectsReplaceField() {
		assertTrue(a.refers(b.getId(), heap));
		heap.linkObjects(a.getId(), "f", e.getId());
		assertTrue(a.refers(e.getId(), heap));
		assertFalse(a.refers(b.getId(), heap));
	}

	@Test
	public void testLinkObjectsAddReferingField() {
		assertTrue(a.refers(b.getId(), heap));
		heap.linkObjects(a.getId(), "f2", e.getId());
		assertTrue(a.refers(e.getId(), heap));
		assertTrue(a.refers(b.getId(), heap));
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

	@Test
	public void testCopyConstructor() {
		Heap heapCopy = new Heap(heap);
		assertEquals(heap, heapCopy);
	}
}
