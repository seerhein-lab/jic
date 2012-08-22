package de.htwg_konstanz.in.jca;

import java.util.Arrays;

import de.htwg_konstanz.in.jca.Entry;

public class Stack { 

    private final Entry[] stack;
    private int index;

    public Stack(int maxHeight) {
	if ( maxHeight < 0 ) 
	    throw new IllegalArgumentException("stack cannot have negative height");
	stack = new Entry[maxHeight];
	index = 0;
    }
    
    public Stack(Stack old) {
	stack = new Entry[old.stack.length];
	index = old.index;
	System.arraycopy(old.stack, 0, stack, 0, index);
    }
    
    public void push(Entry value) {
	if ( index > stack.length -1 ) 
	    throw new IndexOutOfBoundsException();
	stack[index++] = value;
    }
        
    public Entry pop() {
	if ( index < 1 ) 
	    throw new IllegalStateException("empty stack cannot be popped"); 
	Entry top = stack[--index];
	stack[index] = null;
	return top;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + index;
	result = prime * result + Arrays.hashCode(stack);
	return result;
    }
    
    public int getHeight() { return index; }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!(obj instanceof Stack))
	    return false;
	Stack other = (Stack) obj;
	if (index != other.index)
	    return false;
	if ( stack.length != other.stack.length )
	    return false;
	for ( int i = 0; i < index; i++ ) 
	    if ( !stack[i].equals(other.stack[i]) )
		return false;
	return true;
    }
       

}
