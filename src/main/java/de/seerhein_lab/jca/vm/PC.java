package de.seerhein_lab.jca.vm;

import net.jcip.annotations.NotThreadSafe;

import org.apache.bcel.generic.InstructionHandle;

@NotThreadSafe
// This class must be used thread-confined

public final class PC {
	private InstructionHandle ih;
	
	public PC(InstructionHandle ih) { 
		this.ih = ih;
	}
	
	public void setInstruction(InstructionHandle ih) { 
		this.ih = ih;
	}
	
	public InstructionHandle getCurrentInstruction() {
		return ih;
	}
	
	public void advance() {
		ih = ih.getNext();
	}
	
	public void invalidate() {
		ih = null;
	}
	
	public boolean isValid() {
		return ih != null;
	}
	
}
