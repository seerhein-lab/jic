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
	public void testDeepCopy() {
		HeapObject copyA = a.deepCopy(new Heap());

		Iterator<HeapObject> referredIterator;

		referredIterator = copyA.getReferredIterator();
		assertTrue(referredIterator.hasNext());
		HeapObject copyB = referredIterator.next();

		referredIterator = copyB.getReferredIterator();
		assertTrue(referredIterator.hasNext());
		HeapObject copyC = referredIterator.next();

		assertTrue(referredIterator.hasNext());
		HeapObject copyD = referredIterator.next();

		assertFalse(referredIterator.hasNext());

		referredIterator = copyC.getReferredIterator();
		assertTrue(referredIterator.hasNext());
		HeapObject copyE = referredIterator.next();

		assertFalse(referredIterator.hasNext());

		referredIterator = copyD.getReferredIterator();
		assertTrue(referredIterator.hasNext());
		assertTrue(referredIterator.next().equals(copyE));

		assertFalse(referredIterator.hasNext());

		referredIterator = copyE.getReferredIterator();
		assertTrue(referredIterator.hasNext());
		HeapObject copyF = referredIterator.next();

		assertFalse(referredIterator.hasNext());

		referredIterator = copyF.getReferredIterator();
		assertTrue(referredIterator.hasNext());
		assertTrue(referredIterator.next().equals(copyD));

		assertFalse(referredIterator.hasNext());
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

		Iterator<HeapObject> referredIterator = a.getReferredIterator();

		while (referredIterator.hasNext()) {
			assertTrue(referredObjects.contains(referredIterator.next()));
		}

		assertFalse(referredIterator.hasNext());
	}

}
