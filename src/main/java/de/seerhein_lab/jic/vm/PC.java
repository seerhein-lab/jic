package de.seerhein_lab.jic.vm;

import net.jcip.annotations.NotThreadSafe;

import org.apache.bcel.generic.InstructionHandle;

/**
 * The instances of this class represent program counters that indicate the
 * currently executed or net to be executed instruction. As the class is not
 * thread-safe, its instances must be used thread-confined.
 * 
 */
@NotThreadSafe
public final class PC {
	private InstructionHandle ih;

	/**
	 * Constructor that takes the instruction to start with.
	 * 
	 * @param ih
	 *            this program counter's initial instruction
	 */
	public PC(InstructionHandle ih) {
		this.ih = ih;
	}

	/**
	 * Sets this program counter
	 * 
	 * @param ih
	 *            new instruction for this program counter
	 */
	public void setInstruction(InstructionHandle ih) {
		this.ih = ih;
	}

	/**
	 * Gets this program counter's current instruction
	 * 
	 * @return current instruction
	 */
	public InstructionHandle getCurrentInstruction() {
		return ih;
	}

	/**
	 * Advances this program counter to the instruction immediately following
	 * the current instruction.
	 */
	public void advance() {
		ih = ih.getNext();
	}

	/**
	 * Invalidates this program counter.
	 */
	public void invalidate() {
		ih = null;
	}

	/**
	 * Checks if this program counter is in a valid state, or has been
	 * invalidated prior to this call.
	 * 
	 * @return true, if program counter is valid, falls otherwise.
	 */
	public boolean isValid() {
		return ih != null;
	}

}
