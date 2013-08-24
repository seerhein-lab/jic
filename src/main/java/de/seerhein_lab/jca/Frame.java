package de.seerhein_lab.jca;

import java.util.Stack;

import de.seerhein_lab.jca.heap.Heap;
import de.seerhein_lab.jca.slot.Slot;

/**
 * Class representing a method frame. Contains LocalVars and a method stack.
 */
public class Frame {
	private final Slot[] localVars;
	private final Stack<Slot> stack;
	private final Heap heap;


	/**
	 * Constructor that copies numSlots entries from the callerStack into the newly created 
	 * localVars of size maxLocals. Note that the copied entries are consumed from the 
	 * callerStack. The new frame uses the same heap as the callerFrame.
	 * 
	 * @param maxLocals
	 *            The size of the localVars array.
	 * @param callerFrame
	 *            The caller's frame.
	 * @param numSlots
	 *            Number of values to be copied from the callerStack into the localVars array.
	 */
	public Frame(int maxLocals, Frame callerFrame, int numSlots) {
		localVars = new Slot[maxLocals];

		for (int i = numSlots - 1; i >= 0; i--)
			localVars[i] = callerFrame.getStack().pop();

		stack = new Stack<Slot>();
		this.heap = callerFrame.getHeap();
	}

	/**
	 * Simple copy constructor. Creates a deep copy of a given frame.
	 * 
	 * @param frame
	 *            The frame to copy.
	 */
	public Frame(Frame frame) {
		this(frame, new Heap(frame.getHeap()));
	}

	public Frame(Frame frame, Heap heap) {
		this.localVars = new Slot[frame.getLocalVars().length];
		for (int i = 0; i < frame.getLocalVars().length; i++) {
			this.localVars[i] = (frame.getLocalVars()[i] == null) ? null
					: frame.getLocalVars()[i].copy();
		}
		Slot[] stackArray = frame.getStack().toArray(new Slot[0]);
		this.stack = new Stack<Slot>();
		for (Slot slot : stackArray) {
			stack.add(slot.copy());
		}
		this.heap = heap;
	}

	public Frame(Stack<Slot> callerStack, Heap callerHeap) {
		this.stack = callerStack;
		this.localVars = new Slot[0];
		this.heap = callerHeap;
	}

	public Stack<Slot> getStack() {
		return stack;
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
			stack.push(slot);
		}
	}

	/**
	 * Pops up to 2 Slots from the stack and returns the first value popped. If
	 * the first value is DoubleSlot or LongSlot an additional pop is executed.
	 * 
	 * @return The top stack value.
	 */
	public Slot popStackByRequiredSlots() {
		Slot poppedValue = stack.pop();
		if (poppedValue.getNumSlots() == 2) {
			stack.pop();
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

	public Heap getHeap() {
		return heap;
	}
}
