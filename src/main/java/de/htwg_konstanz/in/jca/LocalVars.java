package de.htwg_konstanz.in.jca;

import org.apache.bcel.classfile.LocalVariable;

public class LocalVars {
    private Entry[] entries;
    private int[] indexes;
    
    LocalVars(LocalVariable[] localVarTable) {
	if ( localVarTable == null ) {
	    entries = new Entry[0];
	    indexes = new int[0];
	}
	else {
	    entries = new Entry[localVarTable.length];
	    indexes = new int[localVarTable.length];
		
	    for ( int i = 0; i < entries.length; i++ ) 
		entries[i] = Entry.getInstance(localVarTable[i].getSignature()); 
	
	    for ( int i = 0 ; i < indexes.length; i++ )
		indexes[i] = localVarTable[i].getIndex();
	}
	
    }
    
    public void initWithArgs(Stack callerStack, int numArgs) {
	    for ( int i = numArgs -1; i >= 0; i-- ) 
		entries[i] = callerStack.pop();
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
    
    public int getIndex(int i) { return indexes[i]; }
    public Entry getEntry(int i) { return entries[i]; }
    public int getEntriesLength() { return entries.length; }

}
