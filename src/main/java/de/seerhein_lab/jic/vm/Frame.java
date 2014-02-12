package de.seerhein_lab.jic.vm;

import java.util.concurrent.atomic.AtomicLong;

import de.seerhein_lab.jic.slot.Slot;

/**
 * Class representing a method frame. Contains the local vars and the operand
 * stack.
 */
public class Frame {
	public static AtomicLong count = new AtomicLong();
	private final Slot[] localVars;
	private final OpStack opStack;

	/**
	 * Constructor that copies <code>numSlots</code> entries from the caller
	 * stack into the local vars (of size <code>maxLocals</code>) of this frame.
	 * Note that the copied entries are consumed from the caller stack.
	 * 
	 * @param maxLocals
	 *            The size of the localVars array.
	 * @param callerOpStack
	 *            The caller operand stack.
	 * @param numSlots
	 *            Number of values to be copied from the callerStack into the
	 *            localVars array.
	 */
	public Frame(int maxLocals, OpStack callerOpStack, int numSlots) {
		localVars = new Slot[maxLocals];

		for (int i = numSlots - 1; i >= 0; i--)
			localVars[i] = callerOpStack.pop();

		opStack = new OpStack();
		count.incrementAndGet();
	}

	/**
	 * Copy constructor
	 * 
	 * @param original
	 *            original frame to copy from
	 */
	public Frame(Frame original) {
		this.localVars = new Slot[original.getLocalVars().length];
		for (int i = 0; i < original.getLocalVars().length; i++) {
			this.localVars[i] = (original.getLocalVars()[i] == null) ? null : original
					.getLocalVars()[i].copy();
		}
		opStack = new OpStack(original.getStack());
		count.incrementAndGet();
	}

	/**
	 * Gets this frame's operand stack
	 * 
	 * @return this frame's operand stack
	 */
	public OpStack getStack() {
		return opStack;
	}

	/**
	 * Gets this frame's local vars array
	 * 
	 * @return this frame's local vars array
	 */
	public Slot[] getLocalVars() {
		return localVars;
	}
}
