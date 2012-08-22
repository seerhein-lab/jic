package de.htwg_konstanz.in.jca.testclasses;

/*for TestLocalVars:
 *	private static final Class USED_CLASS = DefaultCtorInitializers.class;
 *
 *	private static void setExpectedData(){
 *  	Entry[] entry1 = { Entry.thisReference, Entry.someByte, Entry.someShort, Entry.someInt, Entry.someLong,
 *  Entry.someFloat, Entry.someDouble, Entry.someChar, Entry.someBoolean, Entry.someReference };
 *		EXPECTED_ENTRIES.add(entry1);
 *		int[] indexes1 = { 0, 1,2,3,4,6,7,9,10,11,12 };
 *		EXPECTED:INDEXES.add(indexes1);
 *	}
 */

public class DefaultCtorInitializers {

    final byte b = 7;
    final short s = 14;
    final int i = 815;
    final long l = 4711L;
    final float f = 3.14f;
    final double d = 2.18;
    final char c = 'x';
    final boolean bool = true;
    final Object o = new Object();
  
}
