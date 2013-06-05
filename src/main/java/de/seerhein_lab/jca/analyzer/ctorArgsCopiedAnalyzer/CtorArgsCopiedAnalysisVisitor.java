package de.seerhein_lab.jca.analyzer.ctorArgsCopiedAnalyzer;

import java.util.Set;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jca.Pair;
import de.seerhein_lab.jca.Frame;
import de.seerhein_lab.jca.analyzer.BaseInstructionsAnalysisVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.heap.Heap;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.Slot;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class CtorArgsCopiedAnalysisVisitor extends
		BaseInstructionsAnalysisVisitor {

	protected CtorArgsCopiedAnalysisVisitor(
			ClassContext classContext,
			Method method,
			Frame frame,
			ConstantPoolGen constantPoolGen,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers, int depth) {
		super(classContext, method, frame, constantPoolGen,
				alreadyVisitedIfBranch, alreadyVisitedMethods,
				instructionHandle, exceptionHandlers, depth);
	}

	public CtorArgsCopiedAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		super(classContext, method, frame, constantPoolGen, instructionHandle,
				exceptionHandlers, alreadyVisitedMethods, depth);
	}

	@Override
	protected BaseInstructionsAnalysisVisitor getInstructionsAnalysisVisitor(
			Frame frame,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			InstructionHandle instructionHandle) {
		return new CtorArgsCopiedAnalysisVisitor(classContext, method, frame,
				constantPoolGen, alreadyVisitedIfBranch, alreadyVisitedMethods,
				instructionHandle, exceptionHandlers, depth);
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen) {
		return new CtorArgsCopiedAnalyzer(classContext, targetMethodGen,
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

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		Heap heap = frame.getHeap();
		if (heap.get(argument.getID()).referredBy(
				heap.getThisInstance().getId(), heap)) {
			addBug(Confidence.HIGH,
					"a field of 'this' is passed to a virtual method and escapes",
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
		Heap heap = frame.getHeap();
		if (heap.get(arrayReference.getID()).referredBy(
				heap.getThisInstance().getId(), heap)) {
			// array is referred by this
			if (referenceToStore.getID().equals(
					frame.getHeap().getExternalObject())) {
				// external reference is assigned to an array referred by this
				addBug(Confidence.HIGH,
						"an external reference is assigned to an array referred by 'this'",
						instructionHandle);
			} else if (heap.get(referenceToStore.getID()).refers(
					heap.getExternalObject().getId(), heap)) {
				// a reference containing an external reference is assigned to
				// an array referred by this
				addBug(Confidence.HIGH,
						"a reference containing an external reference is assigned to an array referred by 'this'",
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
		Heap heap = frame.getHeap();
		if (targetReference.getID().equals(heap.getThisInstance())) {
			// left side is this
			if (referenceToPut.getID().equals(heap.getExternalObject())) {
				// right is external
				addBug(Confidence.HIGH,
						"an external object is assigned to 'this'",
						instructionHandle);
			} else if (heap.get(referenceToPut.getID()).refers(
					heap.getExternalObject().getId(), heap)) {
				// right refers external
				addBug(Confidence.HIGH,
						"an object containing an external reference is assigned to 'this'",
						instructionHandle);
			}
		}
		if (heap.get(targetReference.getID()).referredBy(
				heap.getThisInstance().getId(), heap)) {
			// left is referred by this
			if (referenceToPut.getID().equals(heap.getExternalObject())) {
				// right is external
				addBug(Confidence.HIGH,
						"an external reference is assigned to an object referred by 'this'",
						instructionHandle);
			} else if (heap.get(referenceToPut.getID()).refers(
					heap.getExternalObject().getId(), heap)) {
				// right refers external
				addBug(Confidence.HIGH,
						"a reference containing an external reference is assigned to an object referred by 'this'",
						instructionHandle);
			}
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		Heap heap = frame.getHeap();
		// XXX this assigned to a static field?? Only starting class?!?
		if (referenceToPut.getID().equals(heap.getThisInstance())) {
			// this is published
			addBug(Confidence.HIGH,
					"'this' is published by assignment to a static field",
					instructionHandle);
		}

		if (heap.get(referenceToPut.getID()).referredBy(
				heap.getThisInstance().getId(), heap)) {
			// a field referred by this is published
			addBug(Confidence.HIGH,
					"an object referred by 'this' is published by assignment to a static field",
					instructionHandle);
		}
	}

}
