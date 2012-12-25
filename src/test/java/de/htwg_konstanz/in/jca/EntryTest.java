package de.htwg_konstanz.in.jca;


public class EntryTest {
	//
	// @Test
	// public void testGetInstance() {
	// assertEquals(Slot.getInstance("I"), Slot.someInt);
	// assertEquals(Slot.getInstance("J"), Slot.someLong);
	// assertEquals(Slot.getInstance("C"), Slot.someChar);
	// assertEquals(Slot.getInstance("B"), Slot.someByte);
	// assertEquals(Slot.getInstance("Z"), Slot.someBoolean);
	// assertEquals(Slot.getInstance("S"), Slot.someShort);
	// assertEquals(Slot.getInstance("F"), Slot.someFloat);
	// assertEquals(Slot.getInstance("D"), Slot.someDouble);
	// assertEquals(Slot.getInstance("O"), Slot.notThisReference);
	// }
	//
	// /**
	// * Checks that if the outcome of another execution path is null
	// * that the value is unchanged.
	// */
	// @Test
	// public void testCombineWithOther_NullValue() {
	// assertEquals(Slot.someByte, Slot.someByte.combineWithOther(null));
	// assertEquals(Slot.someShort, Slot.someShort.combineWithOther(null));
	// assertEquals(Slot.someInt, Slot.someInt.combineWithOther(null));
	// assertEquals(Slot.someLong, Slot.someLong.combineWithOther(null));
	// assertEquals(Slot.someFloat, Slot.someFloat.combineWithOther(null));
	// assertEquals(Slot.someDouble, Slot.someDouble.combineWithOther(null));
	// assertEquals(Slot.someChar, Slot.someChar.combineWithOther(null));
	// assertEquals(Slot.someBoolean, Slot.someBoolean.combineWithOther(null));
	// assertEquals(Slot.notThisReference,
	// Slot.notThisReference.combineWithOther(null));
	// assertEquals(Slot.maybeThisReference,
	// Slot.maybeThisReference.combineWithOther(null));
	// assertEquals(Slot.thisReference,
	// Slot.thisReference.combineWithOther(null));
	// }
	// /**
	//
	// * Checks that if the outcome of another execution path is the same value
	// * that the value is unchanged.
	// */
	// @Test
	// public void testCombineWithOther_SameValue() {
	// assertEquals(Slot.someByte,
	// Slot.someByte.combineWithOther(Slot.someByte));
	// assertEquals(Slot.someShort,
	// Slot.someShort.combineWithOther(Slot.someShort));
	// assertEquals(Slot.someInt, Slot.someInt.combineWithOther(Slot.someInt));
	// assertEquals(Slot.someLong,
	// Slot.someLong.combineWithOther(Slot.someLong));
	// assertEquals(Slot.someFloat,
	// Slot.someFloat.combineWithOther(Slot.someFloat));
	// assertEquals(Slot.someDouble,
	// Slot.someDouble.combineWithOther(Slot.someDouble));
	// assertEquals(Slot.someChar,
	// Slot.someChar.combineWithOther(Slot.someChar));
	// assertEquals(Slot.someBoolean,
	// Slot.someBoolean.combineWithOther(Slot.someBoolean));
	// assertEquals(Slot.notThisReference,
	// Slot.notThisReference.combineWithOther(Slot.notThisReference));
	// assertEquals(Slot.maybeThisReference,
	// Slot.maybeThisReference.combineWithOther(Slot.maybeThisReference));
	// assertEquals(Slot.thisReference,
	// Slot.thisReference.combineWithOther(Slot.thisReference));
	// }
	//
	// /**
	// * Checks that if the outcome of another execution path is correct if
	// * the own value value is a notThisReference.
	// */
	// @Test
	// public void testCombineWithOther_NotThisReference() {
	// assertEquals(Slot.notThisReference,
	// Slot.notThisReference.combineWithOther(Slot.notThisReference));
	// assertEquals(Slot.maybeThisReference,
	// Slot.notThisReference.combineWithOther(Slot.maybeThisReference));
	// assertEquals(Slot.maybeThisReference,
	// Slot.notThisReference.combineWithOther(Slot.thisReference));
	// try
	// {
	// Slot.notThisReference.combineWithOther(Slot.someByte);
	// fail("Exception expected");
	// }
	// catch (IllegalArgumentException e)
	// {
	// assertEquals(Slot.notThisReference
	// + " cannot be combined with " + Slot.someByte,
	// e.getMessage());
	// }
	// }
	//
	// /**
	// * Checks that if the outcome of another execution path is correct if
	// * the own value value is a maybeThisReference.
	// */
	// @Test
	// public void testCombineWithOther_MaybeThisReference() {
	// assertEquals(Slot.maybeThisReference,
	// Slot.maybeThisReference.combineWithOther(Slot.maybeThisReference));
	// assertEquals(Slot.maybeThisReference,
	// Slot.maybeThisReference.combineWithOther(Slot.maybeThisReference));
	// assertEquals(Slot.maybeThisReference,
	// Slot.maybeThisReference.combineWithOther(Slot.thisReference));
	// try
	// {
	// Slot.maybeThisReference.combineWithOther(Slot.someByte);
	// fail("Exception expected");
	// }
	// catch (IllegalArgumentException e)
	// {
	// assertEquals(Slot.maybeThisReference
	// + " cannot be combined with " + Slot.someByte,
	// e.getMessage());
	// }
	// }
	//
	// /**
	// * Checks that if the outcome of another execution path is correct if
	// * the own value value is a thisReference.
	// */
	// @Test
	// public void testCombineWithOther_thisReference() {
	// assertEquals(Slot.maybeThisReference,
	// Slot.thisReference.combineWithOther(Slot.maybeThisReference));
	// assertEquals(Slot.maybeThisReference,
	// Slot.thisReference.combineWithOther(Slot.maybeThisReference));
	// assertEquals(Slot.thisReference,
	// Slot.thisReference.combineWithOther(Slot.thisReference));
	// try
	// {
	// Slot.thisReference.combineWithOther(Slot.someByte);
	// fail("Exception expected");
	// }
	// catch (IllegalArgumentException e)
	// {
	// assertEquals(Slot.thisReference
	// + " cannot be combined with " + Slot.someByte,
	// e.getMessage());
	// }
	// }
	//
	// /**
	// * Checks that if the outcome of another execution path is correct if
	// * the own value value is a thisReference.
	// */
	// @Test
	// public void testCombineWithOther_IllegalKnownTypes() {
	// List<Slot> types = new ArrayList<Slot>();
	// types.add(Slot.someByte);
	// types.add(Slot.someShort);
	// types.add(Slot.someInt);
	// types.add(Slot.someLong);
	// types.add(Slot.someFloat);
	// types.add(Slot.someDouble);
	// types.add(Slot.someChar);
	// types.add(Slot.someBoolean);
	// for (Slot own : types)
	// {
	// for (Slot other : types)
	// {
	// if (own == other)
	// {
	// continue;
	// }
	// try
	// {
	// own.combineWithOther(other);
	// fail("Exception expected for combination "
	// + own + " : " + other);
	// }
	// catch (IllegalArgumentException e)
	// {
	// assertEquals(own + " cannot be combined with " + other,
	// e.getMessage());
	// }
	// }
	//
	// }
	// }
	//
	// /**
	// * Checks sample Execution for the
	// */
	// @Test
	// public void testCombineWithOthers() {
	// List<Slot> others = new ArrayList<Slot>();
	// others.add(Slot.someByte);
	// assertEquals(Slot.someByte, Slot.someByte.combineWithOthers(others));
	// }
}
