package de.htwg_konstanz.in.jca.testclasses;


/*for TestLocalVars:
 *	private static final Class USED_CLASS = ClassWithDiverseCtors.class;
 *
 *	private static void setExpectedData(){
 *  	Entry[] entry1 = { Entry.thisReference};
 *  	Entry[] entry2 = { Entry.thisReference, Entry.someInt };
 *  	Entry[] entry3 = { Entry.thisReference, Entry.someInt, Entry.someDouble, Entry.someChar, Entry.someReference};
 *		EXPECTED_ENTRIES.add(entry1);
 *		EXPECTED_ENTRIES.add(entry2);
 *		EXPECTED_ENTRIES.add(entry3);
 *		int[] indexes1 = { 0};
 *		int[] indexes2 = { 0, 1};
 *		int[] indexes3 = {0,1,2,4,5};
 *		EXPECTED_INDEXES.add(indexes1);
 *		EXPECTED_INDEXES.add(indexes2);
 *		EXPECTED_INDEXES.add(indexes3);
 *	}
 */
public class ClassWithDiverseCtors {

    ClassWithDiverseCtors() {}
    ClassWithDiverseCtors(int i) {}
    ClassWithDiverseCtors(int i, double x, char c, Object o) {}

}
