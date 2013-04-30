package de.seerhein_lab.jca.analyzer.stateUnmodFieldsModify;

import java.util.Set;
import java.util.UUID;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jca.Frame;
import de.seerhein_lab.jca.analyzer.BaseInstructionsAnalysisVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer.AlreadyVisitedMethod;
import de.seerhein_lab.jca.heap.HeapObject;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.Slot;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class StateUnmodFieldsModifyAnalysisVisitor extends
		BaseInstructionsAnalysisVisitor {

	protected StateUnmodFieldsModifyAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			Set<AlreadyVisitedIfInstruction> alreadyVisited,
			Set<AlreadyVisitedMethod> alreadyVisitedMethods,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers, int depth) {
		super(classContext, method, frame, constantPoolGen, alreadyVisited,
				alreadyVisitedMethods, instructionHandle, exceptionHandlers,
				depth);
	}

	public StateUnmodFieldsModifyAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers,
			Set<AlreadyVisitedMethod> alreadyVisitedMethods, int depth) {
		super(classContext, method, frame, constantPoolGen, instructionHandle,
				exceptionHandlers, alreadyVisitedMethods, depth);
	}

	@Override
	protected BaseInstructionsAnalysisVisitor getInstructionsAnalysisVisitor(
			Frame frame, Set<AlreadyVisitedIfInstruction> alreadyVisited,
			InstructionHandle instructionHandle) {
		return new StateUnmodFieldsModifyAnalysisVisitor(classContext, method,
				frame, constantPoolGen, alreadyVisited, alreadyVisitedMethods,
				instructionHandle, exceptionHandlers, depth);
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen) {
		return new StateUnmodFieldsModifyMethodAnalyzer(classContext,
				targetMethodGen, alreadyVisitedMethods, depth);
	}

	@Override
	protected String getBugType() {
		// TODO
		return "to be defined";
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	boolean referredByThis(HeapObject obj) {
		for (UUID referring : obj.getReferringObjects()) {
			if (referring.equals(frame.getHeap().getThisID())) {
				return true;
			}
			return referredByThis(frame.getHeap().get(referring));
		}
		return false;
	}

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		// an object referred by a field of this is passed to a virtual method
		if (referredByThis(frame.getHeap().get(argument.getID()))) {
			addBug(Confidence.HIGH,
					"an object referred by a field of this is passed to a virtual method and might be modified",
					instructionHandle);
		}

	}

	@Override
	protected void detectAStoreBug(ReferenceSlot arrayReference,
			Slot valueToStore) {
		// array is referred by a field of this
		if (referredByThis(frame.getHeap().get(arrayReference.getID()))) {
			addBug(Confidence.HIGH,
					"the value of an array referred by a field of this is modified",
					instructionHandle);
		}
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference,
			Slot valueToPut) {
		// left side is referred by a field of this
		if (referredByThis(frame.getHeap().get(targetReference.getID()))) {
			addBug(Confidence.HIGH,
					"the value of an object referred by a field of this is modified",
					instructionHandle);
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		// nothing of interest can happen
	}
}
