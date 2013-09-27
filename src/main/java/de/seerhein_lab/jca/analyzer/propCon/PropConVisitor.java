package de.seerhein_lab.jca.analyzer.propCon;

import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jca.Pair;
import de.seerhein_lab.jca.analyzer.BaseVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.Slot;
import de.seerhein_lab.jca.vm.ExternalObject;
import de.seerhein_lab.jca.vm.Frame;
import de.seerhein_lab.jca.vm.Heap;
import de.seerhein_lab.jca.vm.PC;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class PropConVisitor extends BaseVisitor {

	protected PropConVisitor(ClassContext classContext, MethodGen methodGen, 
			Frame frame, Heap heap, ConstantPoolGen constantPoolGen,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods,
			int depth, Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch) {
		super(classContext, methodGen, frame, heap, constantPoolGen,
				alreadyVisitedIfBranch, alreadyVisitedMethods,
				instructionHandle, exceptionHandlers, depth);
	}

	public PropConVisitor(ClassContext classContext, MethodGen methodGen,
			Frame frame, Heap heap, ConstantPoolGen constantPoolGen,
			PC pc, CodeExceptionGen[] exceptionHandlers,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		this(classContext, methodGen, frame, heap, constantPoolGen, 
				pc.getCurrentInstruction(), exceptionHandlers, 
				alreadyVisitedMethods, depth, new HashSet<Pair<InstructionHandle, Boolean>>());
	}

	@Override
	protected BaseVisitor getInstructionsAnalysisVisitor(
			Frame frame, Heap heap,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			InstructionHandle instructionHandle) {
		return new PropConVisitor(classContext, methodGen, frame,
				heap, constantPoolGen, instructionHandle,
				exceptionHandlers, alreadyVisitedMethods, depth,
				alreadyVisitedIfBranch);
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen) {
		return new PropConAnalyzer(classContext, targetMethodGen,
				alreadyVisitedMethods, depth);
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		if (argument.isNullReference())
			return;

		if (argument.getID().equals(heap.getThisInstance().getId())) {
			// 'this' is passed into a virtual method
			addBug(Confidence.HIGH,
					"'this' is passed into a virtual method and escapes",
					instructionHandle);
		} else if (heap.get(argument.getID()).transitivelyRefers(
				heap.getThisInstance())) {
			// argument that refers to 'this' is passed into a virtual method
			addBug(Confidence.HIGH,
					"a reference that refers to 'this' is passed into a virtual method letting 'this' escape",
					instructionHandle);
		}
	}

	@Override
	protected void detectXAStoreBug(ReferenceSlot arrayReference,
			Slot valueToStore) {
		if (!(valueToStore instanceof ReferenceSlot)) {
			// if the value is not a reference we do not analyze
			return;
		}

		ReferenceSlot referenceToStore = (ReferenceSlot) valueToStore;
		if (heap.getObject(arrayReference) instanceof ExternalObject) {
			// the array is externally known
			if (referenceToStore.getID() != null
					&& referenceToStore.getID().equals(
							heap.getThisInstance().getId())) {
				// this is assigned to the array
				addBug(Confidence.HIGH,
						"'this' is assigned to an external array and escapes",
						instructionHandle);
			} else if (arrayReference.getID() != null
					&& heap.get(arrayReference.getID()).transitivelyRefers(
							heap.getThisInstance())) {
				// a reference containing this is assigned to the array
				addBug(Confidence.HIGH,
						"a reference containing 'this' is assigned to an external array and 'this' escapes",
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
		if (heap.getObject(targetReference) instanceof ExternalObject) {
			// the left side of the assignment is externally known
			if (referenceToPut.getID() != null
					&& referenceToPut.getID().equals(
							heap.getThisInstance().getId())) {
				// this is on the right side
				addBug(Confidence.HIGH,
						"'this' is assigned to an external field and escapes",
						instructionHandle);
			} else if (referenceToPut.getID() != null
					&& heap.get(referenceToPut.getID()).transitivelyRefers(
							heap.getThisInstance())) {
				// this is contained in the right side
				addBug(Confidence.HIGH,
						"a reference containing 'this' is assigned to an external field and 'this' escapes",
						instructionHandle);
			}

		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		if (referenceToPut.isNullReference())
			return;

		if (referenceToPut.getID().equals(heap.getThisInstance().getId())) {
			addBug(Confidence.HIGH,
					"'this' is assigned to a static field and escapes",
					instructionHandle);
		} else if (heap.get(referenceToPut.getID()).transitivelyRefers(
				heap.getThisInstance())) {
			// the reference contains this
			addBug(Confidence.HIGH,
					"a reference containing 'this' is assigned to a static field and 'this' escapes",
					instructionHandle);
		}
	}
}
