package de.seerhein_lab.jca.analyzer.stateUnmodParmsAreCopied;

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

public class StateUnmodCtorAnalysisVisitor extends
		BaseInstructionsAnalysisVisitor {

	protected StateUnmodCtorAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			Set<AlreadyVisitedIfInstruction> alreadyVisited,
			Set<AlreadyVisitedMethod> alreadyVisitedMethods,
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
			Set<AlreadyVisitedMethod> alreadyVisitedMethods, int depth) {
		super(classContext, method, frame, constantPoolGen, instructionHandle,
				exceptionHandlers, alreadyVisitedMethods, depth);
	}

	@Override
	protected BaseInstructionsAnalysisVisitor getInstructionsAnalysisVisitor(
			Frame frame, Set<AlreadyVisitedIfInstruction> alreadyVisited,
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

	boolean refersExternal(HeapObject obj) {
		for (UUID refered : obj.getReferredObjects()) {
			if (refered.equals(frame.getHeap().getExternalID())) {
				return true;
			}
			return refersExternal(frame.getHeap().get(refered));
		}
		return false;
	}

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		if (referredByThis(frame.getHeap().get(argument.getID()))) {
			// an object referred by this is passed to a method that can not be
			// analyzed
			addBug(Confidence.HIGH,
					"this reference is passed to a method that can not be analyzed by static analyzis and escapes",
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
		if (referredByThis(frame.getHeap().get(arrayReference.getID()))) {
			// array is referred by this
			if (referenceToStore.getID()
					.equals(frame.getHeap().getExternalID())) {
				// external reference is assigned to an array referred by this
				addBug(Confidence.HIGH,
						"an external reference is assigned to an array referred by this",
						instructionHandle);
			} else if (refersExternal(frame.getHeap().get(
					referenceToStore.getID()))) {
				// a reference containing an external reference is assigned to
				// an array referred by this
				addBug(Confidence.HIGH,
						"a reference containing an external reference is assigned to an array referred by this",
						instructionHandle);
			}
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
		if (targetReference.getID().equals(frame.getHeap().getThisID())) {
			// left side is this
			if (referenceToPut.getID().equals(frame.getHeap().getExternalID())) {
				// right is external
				addBug(Confidence.HIGH,
						"an external object is assigned to this",
						instructionHandle);
			} else if (refersExternal(frame.getHeap().get(
					referenceToPut.getID()))) {
				// right refers external
				addBug(Confidence.HIGH,
						"an object containing an external reference is assigned to this",
						instructionHandle);
			}
		}
		if (referredByThis(frame.getHeap().get(targetReference.getID()))) {
			// left is referred by this
			if (referenceToPut.getID().equals(frame.getHeap().getExternalID())) {
				// right is external
				addBug(Confidence.HIGH,
						"an external reference is assigned to an object referred by this",
						instructionHandle);
			} else if (refersExternal(frame.getHeap().get(
					referenceToPut.getID()))) {
				// right refers external
				addBug(Confidence.HIGH,
						"a reference containing an external reference is assigned to an object referred by this",
						instructionHandle);
			}
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		// XXX this assigned to a static field?? Only starting class?!?
		if (referenceToPut.getID().equals(frame.getHeap().getThisID())) {
			// this is published
			addBug(Confidence.HIGH,
					"this is published by assignment to a static field",
					instructionHandle);
		}

		if (referredByThis(frame.getHeap().get(referenceToPut.getID()))) {
			// a field referred by this is published
			addBug(Confidence.HIGH,
					"an object referred by this is published by assignment to a static field",
					instructionHandle);
		}
	}

}
