package de.htwg_konstanz.in.jca.slot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.seerhein_lab.jca.slot.ReferenceSlot;

public class ReferenceSlotTest {

	private ReferenceSlot internalA;
	private ReferenceSlot internalB;
	private ReferenceSlot internalC;
	private ReferenceSlot internalD;
	private ReferenceSlot externalA;
	private ReferenceSlot thisRef;

	@Before
	public void setUp() {
		internalA = ReferenceSlot.getInternalInstance();
		internalB = ReferenceSlot.getInternalInstance();
		internalC = ReferenceSlot.getInternalInstance();
		internalD = ReferenceSlot.getInternalInstance();
		externalA = ReferenceSlot.getExternalInstance();
		thisRef = ReferenceSlot.getThisReference();
	}

	@Test
	public void testGetNumSlots() {
		assertEquals(1, internalA.getNumSlots());
	}

	@Test
	public void testCopy() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetInternalInstance() {
		assertEquals(internalA.isExternal(), false);
	}

	@Test
	public void testGetExternalInstance() {
		assertEquals(externalA.isExternal(), true);
	}

	@Test
	public void testInitSpecialReferences() {
		ReferenceSlot.initSpecialReferences();
		assertNotNull(ReferenceSlot.getThisReference());
		assertNotNull(ReferenceSlot.getNullReference());
	}

	@Test
	public void testLinkWithSubdependencies() {
		fail("Not yet implemented");
	}

	@Test
	public void testLinkReferences0() {
		linkSomeReferences();

		assertTrue(internalC.getReferedBy().contains(internalA)
				&& internalC.getReferedBy().contains(internalB));
		assertTrue(internalD.getReferedBy().contains(internalC));
		
		assertTrue(internalA.getReferences().contains(internalC)
				&& internalB.getReferences().contains(internalC));
		assertTrue(internalC.getReferences().contains(internalD));
	}
	
	@Test
	public void testLinkReferences1() {
		linkSomeReferences();
		
		ReferenceSlot.linkReferences(internalD, externalA);
		
		assertTrue(internalA.isRefersExternal());
		assertTrue(internalB.isRefersExternal());
		assertTrue(internalC.isRefersExternal());
		assertTrue(internalD.isRefersExternal());
	}


	@Test
	public void testLinkReferences2() {
		linkSomeReferences();

		ReferenceSlot.linkReferences(internalD, thisRef);
		
		assertTrue(internalA.isRefersThis());
		assertTrue(internalB.isRefersThis());
		assertTrue(internalC.isRefersThis());
		assertTrue(internalD.isRefersThis());
	}

	@Test
	public void testLinkReferences3() {
		linkSomeReferences();

		ReferenceSlot.linkReferences(externalA, internalA);
		
		assertTrue(internalA.isReferedByExternal());
		assertTrue(internalC.isReferedByExternal());
		assertTrue(internalD.isReferedByExternal());
		assertFalse(internalB.isReferedByExternal());
		
		assertTrue(internalB.isRefersExternal());
		assertTrue(externalA.isRefersExternal());
	}

	@Test
	public void testLinkReferences4() {
		linkSomeReferences();

		ReferenceSlot.linkReferences(thisRef, internalA);
		
		assertTrue(internalA.isReferedByThis());
		assertTrue(internalC.isReferedByThis());
		assertTrue(internalD.isReferedByThis());
		assertFalse(internalB.isReferedByThis());
	}
	
	private void linkSomeReferences() {
		ReferenceSlot.linkReferences(internalA, internalC);
		ReferenceSlot.linkReferences(internalB, internalC);
		ReferenceSlot.linkReferences(internalC, internalD);
	}

	/**
	 * zyclic, refers external
	 */
	@Test
	public void testLinkReferences5() {
		ReferenceSlot.linkReferences(internalA, internalB);
		ReferenceSlot.linkReferences(internalB, internalC);
		ReferenceSlot.linkReferences(internalC, internalA);

		ReferenceSlot.linkReferences(internalB, externalA);

		assertTrue(internalA.isRefersExternal());
		assertTrue(internalB.isRefersExternal());
		assertTrue(internalC.isRefersExternal());
	}

	/**
	 * zyclic, refered by external
	 */
	@Test
	public void testLinkReferences6() {
		ReferenceSlot.linkReferences(internalA, internalB);
		ReferenceSlot.linkReferences(internalB, internalC);
		ReferenceSlot.linkReferences(internalC, internalA);

		ReferenceSlot.linkReferences(externalA, internalA);

		assertTrue(internalA.isReferedByExternal());
		assertTrue(internalB.isReferedByExternal());
		assertTrue(internalC.isReferedByExternal());
	}

	/**
	 * link to itself
	 */
	@Test
	public void testLinkReferences7() {
		ReferenceSlot.linkReferences(internalA, internalA);
		// TODO assert
	}

	/**
	 * references refer all other references, refered by all
	 */
	@Test
	public void testLinkReferences8() {
		List<ReferenceSlot> references = new LinkedList<ReferenceSlot>();
		references.add(internalA);
		references.add(internalB);
		references.add(internalC);
		references.add(internalD);

		linkAllReferences();

		assertTrue(internalA.getReferences().containsAll(references));
		assertTrue(internalA.getReferedBy().containsAll(references));

		assertFalse(internalA.isReferedByExternal()
				|| internalA.isRefersExternal());
		assertFalse(internalB.isReferedByExternal()
				|| internalA.isRefersExternal());
		assertFalse(internalC.isReferedByExternal()
				|| internalA.isRefersExternal());
		assertFalse(internalD.isReferedByExternal()
				|| internalA.isRefersExternal());

	}

	@Test
	public void testLinkReferences9() {
		linkAllReferences();

		ReferenceSlot.linkReferences(externalA, internalA);
		assertTrue(internalA.isReferedByExternal());
		assertTrue(internalB.isReferedByExternal());
		assertTrue(internalC.isReferedByExternal());
		assertTrue(internalD.isReferedByExternal());
	}

	@Test
	public void testLinkReferences10() {
		linkAllReferences();

		ReferenceSlot.linkReferences(internalA, externalA);
		assertTrue(internalA.isRefersExternal());
		assertTrue(internalB.isRefersExternal());
		assertTrue(internalC.isRefersExternal());
		assertTrue(internalD.isRefersExternal());
	}
	
	@Test
	public void testLinkReferences11() {
		linkAllReferences();
		
		Set<ReferenceSlot> oldReferences = internalA.getReferences();
		Set<ReferenceSlot> oldReferedBy = internalB.getReferedBy();

		ReferenceSlot.linkReferences(internalA, internalB);
		
		assertEquals(oldReferences, internalA.getReferences());
		assertEquals(oldReferedBy, internalB.getReferedBy());
	}

	private void linkAllReferences() {
		ReferenceSlot.linkReferences(internalA, internalA);
		ReferenceSlot.linkReferences(internalA, internalB);
		ReferenceSlot.linkReferences(internalA, internalC);
		ReferenceSlot.linkReferences(internalA, internalD);

		ReferenceSlot.linkReferences(internalB, internalA);
		ReferenceSlot.linkReferences(internalB, internalB);
		ReferenceSlot.linkReferences(internalB, internalC);
		ReferenceSlot.linkReferences(internalB, internalD);

		ReferenceSlot.linkReferences(internalC, internalA);
		ReferenceSlot.linkReferences(internalC, internalB);
		ReferenceSlot.linkReferences(internalC, internalC);
		ReferenceSlot.linkReferences(internalC, internalD);

		ReferenceSlot.linkReferences(internalD, internalA);
		ReferenceSlot.linkReferences(internalD, internalB);
		ReferenceSlot.linkReferences(internalD, internalC);
		ReferenceSlot.linkReferences(internalD, internalD);
	}


	@Test
	public void testCopyWithoutDependencies() {
		ReferenceSlot copy = internalA.copyWithoutDependencies();
		assertEquals(copy.isExternal(), internalA.isExternal());
		assertEquals(copy.isReferedByExternal(),
				internalA.isReferedByExternal());
		assertEquals(copy.isReferedByField(), internalA.isReferedByField());
		assertEquals(copy.isReferedByThis(), internalA.isReferedByThis());
		assertEquals(copy.isRefersExternal(), internalA.isRefersExternal());
		assertEquals(copy.isRefersField(), internalA.isRefersField());
		assertEquals(copy.isRefersThis(), internalA.isRefersThis());

	}

}
