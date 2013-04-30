package de.seerhein_lab.jca.analyzer.stateUnmodParmsAreCopied;

import java.util.ArrayList;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jca.Frame;
import de.seerhein_lab.jca.analyzer.BaseInstructionsAnalysisVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer.AlreadyVisitedMethod;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import edu.umd.cs.findbugs.ba.ClassContext;

public class StateUnmodCtorAnalysisVisitor extends
		BaseInstructionsAnalysisVisitor {

	protected StateUnmodCtorAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			ArrayList<AlreadyVisitedIfInstruction> alreadyVisited,
			ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers, int depth) {
		super(classContext, method, frame, constantPoolGen, alreadyVisited,
				alreadyVisitedMethods, instructionHandle, exceptionHandlers,
				depth);
	}

	public StateUnmodCtorAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers,
			ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods, int depth) {
		super(classContext, method, frame, constantPoolGen, instructionHandle,
				exceptionHandlers, alreadyVisitedMethods, depth);
	}

	@Override
	protected BaseInstructionsAnalysisVisitor getInstructionsAnalysisVisitor(
			Frame frame, ArrayList<AlreadyVisitedIfInstruction> alreadyVisited,
			InstructionHandle instructionHandle) {
		return new StateUnmodCtorAnalysisVisitor(classContext, method, frame,
				constantPoolGen, alreadyVisited, alreadyVisitedMethods,
				instructionHandle, exceptionHandlers, depth);
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen) {
		return new StateUnmodCtorAnalyzer(classContext, targetMethodGen,
				alreadyVisitedMethods, depth);
	}

	@Override
	protected String getBugType() {
		return "to be defined";
	}

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void detectAAStoreBug(ReferenceSlot arrayReference,
			ReferenceSlot referenceToStore) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference,
			ReferenceSlot referenceToPut) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		// TODO Auto-generated method stub

	}

}
