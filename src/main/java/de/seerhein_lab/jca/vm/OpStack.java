package de.seerhein_lab.jca.vm;

import java.util.Stack;
import de.seerhein_lab.jca.slot.Slot;

public final class OpStack {
	private final Stack<Slot> stack;
	
	public OpStack() {
		stack = new Stack<Slot>();
	}
	
	public OpStack(OpStack original) {
		Slot[] stackArray = original.stack.toArray(new Slot[0]);
		this.stack = new Stack<Slot>();
		for (Slot slot : stackArray) {
			stack.add(slot.copy());
		}
	}
	
	public void push(Slot slot) {
		stack.push(slot);
	}
	
	public Slot pop() {
		return stack.pop();
	}
	
	public void clear() {
		stack.clear();
	}
	
	public int size() {
		return stack.size();
	}
}