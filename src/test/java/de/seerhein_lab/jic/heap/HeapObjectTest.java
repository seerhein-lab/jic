package de.seerhein_lab.jic.heap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import de.seerhein_lab.jic.vm.Array;
import de.seerhein_lab.jic.vm.ClassInstance;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.HeapObject;

public class HeapObjectTest {
	private Heap heap;
	private ClassInstance a;
	private ClassInstance b;
	private ClassInstance c;
	private ClassInstance d;
	private ClassInstance e;
	private Array f;

	public void setUpHeap() {
		heap = new Heap();

		a = heap.newClassInstance(false);
		b = heap.newClassInstance(false);
		c = heap.newClassInstance(false);
		d = heap.newClassInstance(false);
		e = heap.newClassInstance(false);
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
		f.addReferredObject(d);
	}

	@Test
	public void testDeepCopy() {
		HeapObject copyA = a.deepCopy(new Heap());

		Iterator<HeapObject> referredIterator;
		referredIterator = copyA.getReferredObjects().iterator();
		assertTrue(referredIterator.hasNext());
		HeapObject copyB = referredIterator.next();

		referredIterator = copyB.getReferredObjects().iterator();
		assertTrue(referredIterator.hasNext());
		HeapObject copyC = referredIterator.next();

		assertTrue(referredIterator.hasNext());
		HeapObject copyD = referredIterator.next();

		assertFalse(referredIterator.hasNext());

		referredIterator = copyC.getReferredObjects().iterator();
		assertTrue(referredIterator.hasNext());
		HeapObject copyE = referredIterator.next();

		assertFalse(referredIterator.hasNext());

		referredIterator = copyD.getReferredObjects().iterator();
		assertTrue(referredIterator.hasNext());
		assertTrue(referredIterator.next().equals(copyE));

		assertFalse(referredIterator.hasNext());

		referredIterator = copyE.getReferredObjects().iterator();
		assertTrue(referredIterator.hasNext());
		HeapObject copyF = referredIterator.next();

		assertFalse(referredIterator.hasNext());

		referredIterator = copyF.getReferredObjects().iterator();
		assertTrue(referredIterator.hasNext());
		assertTrue(referredIterator.next().equals(copyD));

		assertFalse(referredIterator.hasNext());
	}

	@Test
	public void testLinkObjects() {
		assertTrue(a.isReachable(b));
		assertTrue(b.isReachable(c));
		assertTrue(b.isReachable(d));
		assertTrue(c.isReachable(e));
		assertTrue(d.isReachable(e));
		assertTrue(e.isReachable(f));
		assertTrue(f.isReachable(d));

		assertTrue(a.isReachable(b));
		assertTrue(b.isReachable(c));
		assertTrue(b.isReachable(d));
		assertTrue(c.isReachable(e));
		assertTrue(d.isReachable(e));
		assertTrue(e.isReachable(f));
		assertTrue(f.isReachable(d));
	}

	@Test
	public void testLinkObjectsReplaceField() {
		assertTrue(a.isReachable(b));
		a.setField("f", e);
		assertTrue(a.isReachable(e));
		assertFalse(a.isReachable(b));
	}

	@Test
	public void testLinkObjectsAddReferingField() {
		assertTrue(a.isReachable(b));
		a.setField("f2", e);
		assertTrue(a.isReachable(e));
		assertTrue(a.isReachable(b));
	}

	@Test
	public void testGetReferredIterator() {
		setUpHeap();

		a.setField("f0", b);
		a.setField("f1", null);
		a.setField("f2", null);
		a.setField("f3", null);
		a.setField("f4", c);
		a.setField("f5", d);

		HashSet<HeapObject> referredObjects = new HashSet<HeapObject>();
		referredObjects.add(b);
		referredObjects.add(c);
		referredObjects.add(d);

		checkReferredIterator(referredObjects);
	}

	@Test
	public void testGetReferredIterator2() {
		setUpHeap();

		a.setField("f0", null);
		a.setField("f1", null);
		a.setField("f2", null);
		a.setField("f3", null);
		a.setField("f4", null);
		a.setField("f5", null);

		HashSet<HeapObject> referredObjects = new HashSet<HeapObject>();

		checkReferredIterator(referredObjects);
	}

	@Test
	public void testGetReferredIterator3() {
		setUpHeap();

		a.setField("f0", null);
		a.setField("f1", null);
		a.setField("f2", null);
		a.setField("f3", null);
		a.setField("f4", null);
		a.setField("f5", b);

		HashSet<HeapObject> referredObjects = new HashSet<HeapObject>();
		referredObjects.add(b);

		checkReferredIterator(referredObjects);
	}

	@Test
	public void testGetReferredIterator4() {
		setUpHeap();

		a.setField("f0", b);
		a.setField("f1", null);
		a.setField("f2", null);
		a.setField("f3", null);
		a.setField("f4", null);
		a.setField("f5", null);

		HashSet<HeapObject> referredObjects = new HashSet<HeapObject>();
		referredObjects.add(b);

		checkReferredIterator(referredObjects);
	}

	public void checkReferredIterator(HashSet<HeapObject> referredObjects) {

		Iterator<HeapObject> referredIterator = a.getReferredObjects().iterator();

		while (referredIterator.hasNext()) {
			assertTrue(referredObjects.contains(referredIterator.next()));
		}

		assertFalse(referredIterator.hasNext());
	}

}
