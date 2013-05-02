package de.seerhein_lab.jca.analyzer.propConAnalyzer;

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

public class PropConInstructionsAnalysisVisitor extends
		BaseInstructionsAnalysisVisitor {

	protected PropConInstructionsAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			Set<AlreadyVisitedIfInstruction> alreadyVisited,
			Set<AlreadyVisitedMethod> alreadyVisitedMethods,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers, int depth) {
		super(classContext, method, frame, constantPoolGen, alreadyVisited,
				alreadyVisitedMethods, instructionHandle, exceptionHandlers,
				depth);
	}

	public PropConInstructionsAnalysisVisitor(ClassContext classContext,
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
		return new PropConInstructionsAnalysisVisitor(classContext, method,
				frame, constantPoolGen, alreadyVisited, alreadyVisitedMethods,
				instructionHandle, exceptionHandlers, depth);
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen) {
		return new PropConMethodAnalyzer(classContext, targetMethodGen,
				alreadyVisitedMethods, depth);
	}

	@Override
	protected String getBugType() {
		return "PROPER_CONSTRUCTION_BUG";
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		if (argument.getID().equals(frame.getHeap().getThisID())) {
			// 'this' is passed into a virtual method
			addBug(Confidence.HIGH,
					"this reference is passed into a virtual method and escapes",
					instructionHandle);
		} else if (refersThis(frame.getHeap().get(argument.getID()))) {
			// argument that refers to 'this' is passed into a virtual method
			addBug(Confidence.HIGH,
					"a reference that refers to 'this' is passed into a virtual method letting 'this' escape",
					instructionHandle);
		}
	}

	boolean refersThis(HeapObject obj) {
		for (UUID referred : obj.getReferredObjects()) {
			if (referred.equals(frame.getHeap().getThisID())
					|| refersThis(frame.getHeap().get(referred))) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void detectAStoreBug(ReferenceSlot arrayReference,
			Slot valueToStore) {
		if (!(valueToStore instanceof ReferenceSlot)) {
			// if the value is not a reference we do not analyze
			return;
		}

		ReferenceSlot referenceToStore = (ReferenceSlot) valueToStore;
		if (arrayReference.getID().equals(frame.getHeap().getExternalID())) {
			// the array is externally known
			if (referenceToStore.equals(referenceToStore.getID().equals(
					frame.getHeap().getThisID()))) {
				// this is assigned to the array
				addBug(Confidence.HIGH,
						"this reference is assigned to an external array and escapes",
						instructionHandle);
			} else if (refersThis(frame.getHeap().get(referenceToStore.getID()))) {
				// a reference containing this is assigned to the array
				addBug(Confidence.HIGH,
						"a reference containing this is assigned to an external array and this escapes",
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
		if (targetReference.getID().equals(frame.getHeap().getExternalID())) {
			// the left side of the assignment is externally known
			if (referenceToPut.getID().equals(frame.getHeap().getThisID())) {
				// this is on the right side
				addBug(Confidence.HIGH,
						"this reference is assigned to an external field and escapes",
						instructionHandle);
			} else if (refersThis(frame.getHeap().get(referenceToPut.getID()))) {
				// this is contained in the right side
				addBug(Confidence.HIGH,
						"a reference containing this is assigned to an external field and this escapes",
						instructionHandle);
			}

		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		if (referenceToPut.getID().equals(frame.getHeap().getThisID())) {
			addBug(Confidence.HIGH,
					"this reference is assigned to a static field and escapes",
					instructionHandle);
		} else if (refersThis(frame.getHeap().get(referenceToPut.getID()))) {
			// the reference contains this
			addBug(Confidence.HIGH,
					"a reference containing this is assigned to a static field and this escapes",
					instructionHandle);
		}
	}
}
