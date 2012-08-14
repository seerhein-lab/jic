package de.htwg_konstanz.in.jca;

import org.apache.bcel.classfile.LocalVariable;

public class LocalVars {
    private Entry[] entries;
    private int[] indexes;
    
    LocalVars(LocalVariable[] localVarTable, int numArgs, Stack callerStack) {
	if ( localVarTable == null ) {
	    entries = new Entry[0];
	    indexes = new int[0];
	}
	else {
	    entries = new Entry[localVarTable.length];
	    indexes = new int[localVarTable.length];
	
	    for ( int i = numArgs -1; i >= 0; i-- ) 
		entries[i] = callerStack.pop();
	
	    for ( int i = numArgs; i < entries.length; i++ ) 
		entries[i] = Entry.getInstance(localVarTable[i].getSignature()); 
	
	    for ( int i = 0 ; i < indexes.length; i++ )
		indexes[i] = localVarTable[i].getIndex();
	}
	
    }
    
    private int index2i(int index) {
	for ( int i = 0 ; i < indexes.length; i++ ) {
	    if ( indexes[i] == index )
		return i;
	}
	throw new AssertionError("invalid index");
    }
    
    public Entry getForIndex(int index) {
	return entries[index2i(index)];
    }
    
    public void setForIndex(int index, Entry entry) {
	entries[index2i(index)] = entry;
    }
    
//    void set(int i, Entry entry) { entries[i] = entry; }
//    Entry get(int i) { return entries[i]; }

}
