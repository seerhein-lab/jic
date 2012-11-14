package de.htwg_konstanz.in.jca;

import java.util.Stack;

public class Frame {
	private Slot[] localVars;
	private Stack<Slot> stack;

	public Frame(int maxLocals, Stack<Slot> callerStack, int numSlots) {
		localVars = new Slot[maxLocals];

		for (int i = numSlots - 1; i >= 0; i--)
			localVars[i] = callerStack.pop();

		stack = new Stack<Slot>();
	}

	@SuppressWarnings("unchecked")
	public Frame(Frame frame) {
		this.localVars = frame.getLocalVars().clone();
		this.stack = (Stack<Slot>) frame.getStack().clone();
	}

	public Stack<Slot> getStack() {
		return stack;
	}

	public void pushStackByDataType(Slot slot) {
		for (int i = 0; i < slot.getDataType().getNumSlots(); i++) {
			stack.push(slot);
		}
	}

	public Slot[] getLocalVars() {
		return localVars;
	}

}
