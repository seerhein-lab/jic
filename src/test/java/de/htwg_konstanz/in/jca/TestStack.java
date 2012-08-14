package de.htwg_konstanz.in.jca;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TestStack {
    private Stack zeroLengthStack;
    private Stack emptyStack;
    private Stack partlyFullStack;
    private Stack fullStack;
    

    @Before
    public void setUp() throws Exception {
	zeroLengthStack = new Stack(0);
	emptyStack = new Stack(5);
	
	partlyFullStack = new Stack(5);
	for ( int i = 0; i < 2; i++ ) {
	    partlyFullStack.push(Entry.someByte);
	}
	
	fullStack = new Stack(5);
	for ( int i = 0; i < 5; i++ ) {
	    fullStack.push(Entry.someByte);
	}
	
    }

    @Test (expected=IllegalArgumentException.class)
    public void testStackInt() {
	new Stack(-1);
  }
    

    @Test
    public void testStackStack() {
	assertEquals(zeroLengthStack, new Stack(zeroLengthStack));
	assertEquals(emptyStack, new Stack(emptyStack));
	assertEquals(partlyFullStack, new Stack(partlyFullStack));
	assertEquals(fullStack, new Stack(fullStack));	
    }

    @Test (expected=IndexOutOfBoundsException.class)
    public void testPushZeroLengthStack() {
	zeroLengthStack.push(Entry.someByte);
    } 
    
    @Test
    public void testPushEmptyStack() {	
	emptyStack.push(Entry.someByte);
	assertEquals(emptyStack.getHeight(), 1);
    } 
    
    @Test
    public void testPushPartlyFullStack() {
	int height = partlyFullStack.getHeight();
	partlyFullStack.push(Entry.someByte);
	assertEquals(partlyFullStack.getHeight(), height +1);
    } 
    
    @Test (expected=IndexOutOfBoundsException.class)
    public void testPushFullStack() {
	fullStack.push(Entry.someByte);
    } 
    
    
    @Test (expected=IllegalStateException.class)
    public void testPopZeroLengthStack() {
	zeroLengthStack.pop();
    }

    @Test (expected=IllegalStateException.class)
    public void testPopEmptyStack() {
	zeroLengthStack.pop();
    }
    
    @Test
    public void testPopPartlyFullStack() {
	int height = partlyFullStack.getHeight();
	partlyFullStack.pop();
	assertEquals(partlyFullStack.getHeight(), height -1);
    }
    
    @Test
    public void testPopFullStack() {
	int height = fullStack.getHeight();
	fullStack.pop();
	assertEquals(fullStack.getHeight(), height -1);
    }
    
    @Test
    public void testPushPop() {
	partlyFullStack.push(Entry.someBoolean);
	assertEquals(partlyFullStack.pop(), Entry.someBoolean);
    }
    

}
