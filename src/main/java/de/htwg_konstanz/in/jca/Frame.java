package de.htwg_konstanz.in.jca;

import java.util.Stack;

public class Frame {
	private Slot[] localVars;
	private Stack<Slot> stack;

	/**
	 * Constructor. Pops numSlots values from the callerStack and stores them
	 * into an array localVars with size maxLocals. maxLocals must be a positive
	 * number.
	 * 
	 * @param maxLocals
	 *            The size of the localVars array.
	 * @param callerStack
	 *            The caller's stack.
	 * @param numSlots
	 *            Number of values to be popped from the callerStack.
	 */
	public Frame(int maxLocals, Stack<Slot> callerStack, int numSlots) {
		localVars = new Slot[maxLocals];

		for (int i = numSlots - 1; i >= 0; i--)
			localVars[i] = callerStack.pop();

		stack = new Stack<Slot>();
	}

	/**
	 * Simple copy constructor. Creates a copy of a given frame.
	 * 
	 * @param frame
	 *            The frame to copy.
	 */
	@SuppressWarnings("unchecked")
	public Frame(Frame frame) {
		this.localVars = frame.getLocalVars().clone();
		this.stack = (Stack<Slot>) frame.getStack().clone();
	}

	public Stack<Slot> getStack() {
		return stack;
	}

	/**
	 * Pushes the slot n times onto the stack, where n is 0 for noSlot, 2 for
	 * someHalfDouble and someHalfLong and 1 in all other cases.
	 * 
	 * @param slot
	 *            The Slot to push.
	 */
	public void pushStackByRequiredSlots(Slot slot) {
		for (int i = 0; i < slot.getDataType().getNumSlots(); i++) {
			stack.push(slot);
		}
	}

	/**
	 * Pushes the corresponding default Slot instance n times onto the stack for
	 * a given DataType, where n is 0 for voidType, 2 for doubleType and
	 * longType and 1 in all other cases.
	 * 
	 * @param dataType
	 *            The DataType of the default Slot instance to push.
	 */
	public void pushStackByDataType(DataType dataType) {
		pushStackByRequiredSlots(Slot.getDefaultInstance(dataType));
	}

	/**
	 * Pops up to 2 Slots from the stack and returns the first value popped. If
	 * the first value is someHalfDouble or someHalfLong an additional pop is
	 * executed.
	 * 
	 * @return The top stack value.
	 */
	public Slot popStackByRequiredSlots() {
		Slot poppedValue = stack.pop();
		if (poppedValue.getDataType().getNumSlots() == 2) {
			stack.pop();
		}
		return poppedValue;
	}

	public Slot[] getLocalVars() {
		return localVars;
	}

}
