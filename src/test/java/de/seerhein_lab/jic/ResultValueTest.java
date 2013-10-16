package de.seerhein_lab.jic;

import org.junit.Test;

/**
 * JUnit test for class ResultValue. Not testing auto-generated methods and
 * simple getter and setter.
 */
public class ResultValueTest {

	@Test
	public void dummy() {
	}

	// /**
	// * Tests combineReferences(Set) with matching reference pair but different
	// * kind.
	// */
	// @Test
	// public void testCombineReferences0() {
	// Set<ResultValue> setToTest = new HashSet<ResultValue>();
	// ResultValue v1, v2, v3;
	// v1 = new ResultValue(Kind.REGULAR, SlotOld.notThisReference);
	// v2 = new ResultValue(Kind.REGULAR, SlotOld.someFloat);
	// v3 = new ResultValue(Kind.EXCEPTION, SlotOld.maybeThisReference);
	//
	// setToTest.add(v1);
	// setToTest.add(v2);
	// setToTest.add(v3);
	//
	// Set<ResultValue> combinedSet = ResultValue.combineReferences(setToTest);
	//
	// assertEquals(3, combinedSet.size());
	// assertEquals(true, combinedSet.contains(v1));
	// assertEquals(true, combinedSet.contains(v2));
	// assertEquals(true, combinedSet.contains(v3));
	// }
	//
	// /**
	// * Tests combineReferences(Set) with matching reference pair (notThis,
	// * maybeThis) and same kind.
	// */
	// @Test
	// public void testCombineReferences1() {
	// Set<ResultValue> setToTest = new HashSet<ResultValue>();
	// ResultValue v1, v2, v3;
	// v1 = new ResultValue(Kind.REGULAR, SlotOld.notThisReference);
	// v2 = new ResultValue(Kind.REGULAR, SlotOld.someFloat);
	// v3 = new ResultValue(Kind.REGULAR, SlotOld.maybeThisReference);
	//
	// setToTest.add(v1);
	// setToTest.add(v2);
	// setToTest.add(v3);
	//
	// Set<ResultValue> combinedSet = ResultValue.combineReferences(setToTest);
	//
	// assertEquals(2, combinedSet.size());
	// assertEquals(false, combinedSet.contains(v1));
	// assertEquals(true, combinedSet.contains(v2));
	// assertEquals(true, combinedSet.contains(v3));
	// }
	//
	// /**
	// * Tests combineReferences(Set) with matching reference pair (notThis,
	// this)
	// * and same kind.
	// */
	// @Test
	// public void testCombineReferences2() {
	// Set<ResultValue> setToTest = new HashSet<ResultValue>();
	// ResultValue v1, v2, v3;
	// v1 = new ResultValue(Kind.REGULAR, SlotOld.notThisReference);
	// v2 = new ResultValue(Kind.REGULAR, SlotOld.someFloat);
	// v3 = new ResultValue(Kind.REGULAR, SlotOld.thisReference);
	//
	// setToTest.add(v1);
	// setToTest.add(v2);
	// setToTest.add(v3);
	//
	// Set<ResultValue> combinedSet = ResultValue.combineReferences(setToTest);
	//
	// assertEquals(2, combinedSet.size());
	// assertEquals(false, combinedSet.contains(v1));
	// assertEquals(true, combinedSet.contains(v2));
	// assertEquals(false, combinedSet.contains(v3));
	// assertEquals(true, combinedSet.contains(new ResultValue(Kind.REGULAR,
	// SlotOld.maybeThisReference)));
	// }
	//
	// /**
	// * Tests combineReferences(Set) with matching reference pair (maybeThis,
	// * this) and same kind.
	// */
	// @Test
	// public void testCombineReferences3() {
	// Set<ResultValue> setToTest = new HashSet<ResultValue>();
	// ResultValue v1, v2, v3;
	// v1 = new ResultValue(Kind.REGULAR, SlotOld.maybeThisReference);
	// v2 = new ResultValue(Kind.REGULAR, SlotOld.someFloat);
	// v3 = new ResultValue(Kind.REGULAR, SlotOld.thisReference);
	//
	// setToTest.add(v1);
	// setToTest.add(v2);
	// setToTest.add(v3);
	//
	// Set<ResultValue> combinedSet = ResultValue.combineReferences(setToTest);
	//
	// assertEquals(2, combinedSet.size());
	// assertEquals(true, combinedSet.contains(v1));
	// assertEquals(true, combinedSet.contains(v2));
	// assertEquals(false, combinedSet.contains(v3));
	// }
	//
	// /**
	// * Tests combineReferences(Set) with matching reference pair (notThis,
	// * maybeThis) and same kind. Kind is set to EXCEPTION and values should
	// not
	// * be eliminated in combinedSet.
	// */
	// @Test
	// public void testCombineReferences4() {
	// Set<ResultValue> setToTest = new HashSet<ResultValue>();
	// ResultValue v1, v2, v3;
	// v1 = new ResultValue(Kind.EXCEPTION, SlotOld.notThisReference);
	// v2 = new ResultValue(Kind.REGULAR, SlotOld.someFloat);
	// v3 = new ResultValue(Kind.EXCEPTION, SlotOld.maybeThisReference);
	//
	// setToTest.add(v1);
	// setToTest.add(v2);
	// setToTest.add(v3);
	//
	// Set<ResultValue> combinedSet = ResultValue.combineReferences(setToTest);
	//
	// assertEquals(3, combinedSet.size());
	// assertEquals(true, combinedSet.contains(v1));
	// assertEquals(true, combinedSet.contains(v2));
	// assertEquals(true, combinedSet.contains(v3));
	// }
}
