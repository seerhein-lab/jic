package de.seerhein_lab.jca.analyzer.stateUnmodRefPublished;

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

public class StateUnmodRefPublishedAnalysisVisitor extends
		BaseInstructionsAnalysisVisitor {

	protected StateUnmodRefPublishedAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			Set<AlreadyVisitedIfInstruction> alreadyVisited,
			Set<AlreadyVisitedMethod> alreadyVisitedMethods,
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
			Set<AlreadyVisitedMethod> alreadyVisitedMethods, int depth) {
		super(classContext, method, frame, constantPoolGen, instructionHandle,
				exceptionHandlers, alreadyVisitedMethods, depth);
	}

	@Override
	protected BaseInstructionsAnalysisVisitor getInstructionsAnalysisVisitor(
			Frame frame, Set<AlreadyVisitedIfInstruction> alreadyVisited,
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
		if (argument.getID().equals(frame.getHeap().getThisID())) {
			// XXX problem or not?? Inheritance?!?
			addBug(Confidence.HIGH,
					"this is passed to a virtual method and published",
					instructionHandle);
		} else if (referredByThis(frame.getHeap().get(argument.getID()))) {
			addBug(Confidence.HIGH,
					"a field of this is passed to a virtual mehtod and published",
					instructionHandle);
		}
	}

	@Override
	protected void detectAStoreBug(ReferenceSlot arrayReference,
			Slot valueToStore) {
		if (!(valueToStore instanceof ReferenceSlot)) {
			// if the value is not a reference we do not analyze
			return;
		}

		ReferenceSlot referenceToStore = (ReferenceSlot) valueToStore;
		if (arrayReference.getID().equals(frame.getHeap().getExternalID())
				&& referredByThis(frame.getHeap().get(referenceToStore.getID()))) {
			// a field of this is assigned to an external object
			addBug(Confidence.HIGH,
					"field of this is published by assignment to an external array",
					instructionHandle);
		}
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference,
			Slot valueToPut) {
		if (!(valueToPut instanceof ReferenceSlot)) {
			// if the value is not a reference we do not analyze
			return;
		}

		ReferenceSlot referenceToPut = (ReferenceSlot) valueToPut;
		if (targetReference.getID().equals(frame.getHeap().getExternalID())
				&& referredByThis(frame.getHeap().get(referenceToPut.getID()))) {
			// a field of this is assigned to an external object
			addBug(Confidence.HIGH,
					"a field of this is published by assignment to an external object",
					instructionHandle);
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		if (referenceToPut.getID().equals(frame.getHeap().getThisID())) {
			// XXX only a problem if it is a static field of the class we
			// analyze
			addBug(Confidence.HIGH,
					"this is published by assignment to a static field",
					instructionHandle);
		} else if (referredByThis(frame.getHeap().get(referenceToPut.getID()))) {
			addBug(Confidence.HIGH,
					"a field of this is published by assignment to a static field",
					instructionHandle);
		}
	}
}
