package de.seerhein_lab.jic.vm;

import java.util.Stack;

import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.slot.VoidSlot;

/**
 * Class whose instances represent operand stacks. An operand stack contains
 * slot entries; please note that an operand stack never contains entries of
 * type VoidEntry, but only entries of size 1 or 2.
 * 
 */
public final class OpStack {
	private final Stack<Slot> stack;

	/**
	 * Constructor that creates an empty operand stack.
	 */
	public OpStack() {
		stack = new Stack<Slot>();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param original
	 *            operand stack to copy from. Must nor be null.
	 */
	public OpStack(OpStack original) {
		if (original == null)
			throw new NullPointerException("original stack must not be null");

		Slot[] stackArray = original.stack.toArray(new Slot[0]);
		this.stack = new Stack<Slot>();
		for (Slot slot : stackArray) {
			stack.add(slot.copy());
		}
	}

	/**
	 * Pushes the slot <code>slot</code> onto this operand stack.
	 * <code>slot</code> must not be null and cannot be of type
	 * <code>VoidSlot</code>.
	 * 
	 * @param slot
	 *            slot to be pushed onto this operand stack.
	 */
	public OpStack push(Slot slot) {
		if (slot == null)
			throw new NullPointerException("slot must not be null");

		if (slot instanceof VoidSlot)
			throw new IllegalArgumentException(
					"a VoidSlot cannot be pushed onto the operand stack.");

		stack.push(slot);
		return this;
	}

	/**
	 * Pops and returns the topmost slot from this operand stack.
	 * 
	 * @return topmost slot from this operand stack
	 */
	public Slot pop() {
		return stack.pop();
	}

	/**
	 * Returns the topmost slot from this operand stack, while leaving this
	 * stack unchanged.
	 * 
	 * @return topmost slot from this operand stack
	 */
	public Slot peek() {
		return stack.peek();
	}

	/**
	 * Gets the i-th slot from this operand stack. The bottom entry of this
	 * stack has the index 0.
	 * 
	 * @param index
	 * @return i-th slot from this operand stack.
	 */
	public Slot get(int index) {
		return stack.get(index);
	}

	/**
	 * Deletes all entries from this operand stack, leaving an empty stack.
	 */
	public void clear() {
		stack.clear();
	}

	/**
	 * Gets this operand stack's size, i.e. number of entries.
	 * 
	 * @return this operand stack's size
	 */
	public int size() {
		return stack.size();
	}

	/**
	 * Pushes the slot n times onto the stack, where n is 0 for VoidSlot, 2 for
	 * DoubleSlot and LongSlot, and 1 in all other cases.
	 * 
	 * @param slot
	 *            The slot to push.
	 */
	public void pushByRequiredSize(Slot slot) {
		if (slot == null)
			throw new NullPointerException("slot must not be null");

		for (int i = 0; i < slot.getNumSlots(); i++) {
			push(slot);
		}
	}

	/**
	 * Pops 1 or 2 slots from the stack and returns the first value popped. If
	 * the top value is a DoubleSlot or a LongSlot, then 2 slots are popped,
	 * otherwise only 1 slot is popped.
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
