package de.seerhein_lab.jic.vm;

import org.apache.bcel.generic.InstructionHandle;

public final class VM {
	private InstructionHandle pc;
	
	public void setPC(InstructionHandle pc) { 
		this.pc = pc;
	}
	
	public InstructionHandle getPC() {
		return pc;
	}
	
}
