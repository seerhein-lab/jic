package de.htwg_konstanz.in.jca.analyzer.stateUnmodRefPublished;

import java.util.ArrayList;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.htwg_konstanz.in.jca.Frame;
import de.htwg_konstanz.in.jca.analyzer.BaseInstructionsAnalysisVisitor;
import de.htwg_konstanz.in.jca.analyzer.BaseMethodAnalyzer;
import de.htwg_konstanz.in.jca.analyzer.BaseMethodAnalyzer.AlreadyVisitedMethod;
import de.htwg_konstanz.in.jca.slot.ReferenceSlot;
import edu.umd.cs.findbugs.ba.ClassContext;

public class StateUnmodRefPublishedAnalysisVisitor extends
		BaseInstructionsAnalysisVisitor {

	protected StateUnmodRefPublishedAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			ArrayList<AlreadyVisitedIfInstruction> alreadyVisited,
			ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers, int depth) {
		super(classContext, method, frame, constantPoolGen, alreadyVisited,
				alreadyVisitedMethods, instructionHandle, exceptionHandlers,
				depth);
	}

	public StateUnmodRefPublishedAnalysisVisitor(ClassContext classContext,
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
		return new StateUnmodRefPublishedAnalysisVisitor(classContext, method,
				frame, constantPoolGen, alreadyVisited, alreadyVisitedMethods,
				instructionHandle, exceptionHandlers, depth);
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen) {
		return new StateUnmodRefPublishedMethodAnalyzer(classContext,
				targetMethodGen, alreadyVisitedMethods, depth);
	}

	@Override
	protected void detectMethodThatIsNotAnalyzedBug(ReferenceSlot argument) {
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
