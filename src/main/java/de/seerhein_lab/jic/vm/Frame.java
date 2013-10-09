package de.seerhein_lab.jic.vm;

import de.seerhein_lab.jic.slot.Slot;

/**
 * Class representing a method frame. Contains LocalVars and a method stack.
 */
public class Frame {
	private final Slot[] localVars;
	private final OpStack opStack;

	/**
	 * Constructor that copies numSlots entries from the callerStack into the
	 * newly created localVars of size maxLocals. Note that the copied entries
	 * are consumed from the callerStack. The new frame uses the same heap as
	 * the callerFrame.
	 * 
	 * @param maxLocals
	 *            The size of the localVars array.
	 * @param callerFrame
	 *            The caller's frame.
	 * @param numSlots
	 *            Number of values to be copied from the callerStack into the
	 *            localVars array.
	 */
	public Frame(int maxLocals, OpStack callerOpStack, int numSlots) {
		localVars = new Slot[maxLocals];

		for (int i = numSlots - 1; i >= 0; i--)
			localVars[i] = callerOpStack.pop();

		opStack = new OpStack();
	}

	public Frame(Frame frame) {
		this.localVars = new Slot[frame.getLocalVars().length];
		for (int i = 0; i < frame.getLocalVars().length; i++) {
			this.localVars[i] = (frame.getLocalVars()[i] == null) ? null
					: frame.getLocalVars()[i].copy();
		}
		opStack = new OpStack(frame.getStack());
	}

	public OpStack getStack() {
		return opStack;
	}

	/**
	 * Pushes the slot n times onto the stack, where n is 0 for VoidSlot, 2 for
	 * DoubleSlot and LongSlot and 1 in all other cases.
	 * 
	 * @param slot
	 *            The Slot to push.
	 */
	public void pushStackByRequiredSlots(Slot slot) {
		for (int i = 0; i < slot.getNumSlots(); i++) {
			opStack.push(slot);
		}
	}

	/**
	 * Pops up to 2 Slots from the stack and returns the first value popped. If
	 * the first value is DoubleSlot or LongSlot an additional pop is executed.
	 * 
	 * @return The top stack value.
	 */
	public Slot popStackByRequiredSlots() {
		Slot poppedValue = opStack.pop();
		if (poppedValue.getNumSlots() == 2) {
			opStack.pop();
		}
		return poppedValue;
	}

	/**
	 * Simple getter for LocalVars.
	 * 
	 * @return The LocalVars as an array of type Slot
	 */
	public Slot[] getLocalVars() {
		return localVars;
	}
}
