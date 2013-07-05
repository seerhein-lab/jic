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

		a.setField("f", b);
		b.setField("f", c);
		b.setField("f2", d);
		c.setField("f", e);
		d.setField("f", e);
		e.setField("f", f);
		
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

		assertTrue(a.transitivelyRefers(b));
		assertTrue(b.transitivelyRefers(c));
		assertTrue(b.transitivelyRefers(d));
		assertTrue(c.transitivelyRefers(e));
		assertTrue(d.transitivelyRefers(e));
		assertTrue(e.transitivelyRefers(f));
		assertTrue(f.transitivelyRefers(d));

		assertTrue(b.isTransitivelyReferredBy(a));
		assertTrue(c.isTransitivelyReferredBy(b));
		assertTrue(d.isTransitivelyReferredBy(b));
		assertTrue(e.isTransitivelyReferredBy(c));
		assertTrue(e.isTransitivelyReferredBy(d));
		assertTrue(f.isTransitivelyReferredBy(e));
		assertTrue(d.isTransitivelyReferredBy(f));
	}

	@Test
	public void testLinkObjectsReplaceField() {
		assertTrue(a.transitivelyRefers(b));
		a.setField("f", e);
		assertTrue(a.transitivelyRefers(e));
		assertFalse(a.transitivelyRefers(b));
	}

	@Test
	public void testLinkObjectsAddReferingField() {
		assertTrue(a.transitivelyRefers(b));
		a.setField("f2", e);
		assertTrue(a.transitivelyRefers(e));
		assertTrue(a.transitivelyRefers(b));
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

//	@Test
//	public void testCopyConstructor() {
//		Heap heapCopy = new Heap(heap);
//		assertEquals(heap, heapCopy);
//	}
}
