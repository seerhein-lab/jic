package de.seerhein_lab.jic.analyzer;

import net.jcip.annotations.Immutable;

import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.InstructionHandle;

@Immutable
public final class MethodHelper {
	public static boolean protectsInstruction(CodeExceptionGen exceptionHandler,
			InstructionHandle instruction) {
		for (InstructionHandle protectedInstruction = exceptionHandler.getStartPC(); !protectedInstruction
				.equals(exceptionHandler.getEndPC()); protectedInstruction = protectedInstruction
				.getNext()) {
			if (protectedInstruction.getPosition() == instruction.getPosition())
				return true;
		}
		return (exceptionHandler.getEndPC().getPosition() == instruction.getPosition());
	}
}
