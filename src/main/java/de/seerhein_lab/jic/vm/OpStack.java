package de.seerhein_lab.jic.vm;

import java.util.Stack;

import de.seerhein_lab.jic.slot.Slot;

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

	public Slot peek() {
		return stack.peek();
	}

	public Slot get(int index) {
		return stack.get(index);
	}

	public void clear() {
		stack.clear();
	}

	public int size() {
		return stack.size();
	}

	/**
	 * Pushes the slot n times onto the stack, where n is 0 for VoidSlot, 2 for
	 * DoubleSlot and LongSlot and 1 in all other cases.
	 * 
	 * @param slot
	 *            The Slot to push.
	 */
	public void pushByRequiredSize(Slot slot) {
		for (int i = 0; i < slot.getNumSlots(); i++) {
			push(slot);
		}
	}

	/**
	 * Pops up to 2 Slots from the stack and returns the first value popped. If
	 * the first value is DoubleSlot or LongSlot an additional pop is executed.
	 * 
	 * @return The top stack value.
	 */
	public Slot popByRequiredSize() {
		Slot poppedValue = pop();
		if (poppedValue.getNumSlots() == 2) {
			pop();
		}
		return poppedValue;
	}
}
