package de.seerhein_lab.jca.analyzer.fieldsNotPublishedAnalyzer;

import java.util.Set;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jca.AlreadyVisited;
import de.seerhein_lab.jca.Frame;
import de.seerhein_lab.jca.analyzer.BaseInstructionsAnalysisVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.heap.Heap;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.Slot;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class FieldsNotPublishedAnalysisVisitor extends
		BaseInstructionsAnalysisVisitor {

	protected FieldsNotPublishedAnalysisVisitor(
			ClassContext classContext,
			Method method,
			Frame frame,
			ConstantPoolGen constantPoolGen,
			Set<AlreadyVisited<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			Set<AlreadyVisited<Method, Slot[]>> alreadyVisitedMethods,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers, int depth) {
		super(classContext, method, frame, constantPoolGen,
				alreadyVisitedIfBranch, alreadyVisitedMethods,
				instructionHandle, exceptionHandlers, depth);
	}

	public FieldsNotPublishedAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers,
			Set<AlreadyVisited<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		super(classContext, method, frame, constantPoolGen, instructionHandle,
				exceptionHandlers, alreadyVisitedMethods, depth);
	}

	@Override
	protected BaseInstructionsAnalysisVisitor getInstructionsAnalysisVisitor(
			Frame frame,
			Set<AlreadyVisited<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			InstructionHandle instructionHandle) {
		return new FieldsNotPublishedAnalysisVisitor(classContext, method,
				frame, constantPoolGen, alreadyVisitedIfBranch,
				alreadyVisitedMethods, instructionHandle, exceptionHandlers,
				depth);
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen) {
		return new FieldsNotPublishedMethodAnalyzer(classContext,
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

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		Heap heap = frame.getHeap();
		if (argument.getID().equals(heap.getThisInstance())) {
			// XXX problem or not?? Inheritance?!?
			addBug(Confidence.HIGH,
					"'this' is passed to a virtual method and published",
					instructionHandle);
		} else if (heap.get(argument.getID()).referredBy(
				heap.getThisInstance().getId(), heap)) {
			addBug(Confidence.HIGH,
					"a field of 'this' is passed to a virtual mehtod and published",
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
		if (arrayReference.getID().equals(heap.getExternalObject())
				&& heap.get(referenceToStore.getID()).referredBy(
						heap.getThisInstance().getId(), heap)) {
			// a field of this is assigned to an external object
			addBug(Confidence.HIGH,
					"field of 'this' is published by assignment to an external array",
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
		Heap heap = frame.getHeap();
		if (targetReference.getID().equals(heap.getExternalObject())
				&& heap.get(referenceToPut.getID()).referredBy(
						heap.getThisInstance().getId(), heap)) {
			// a field of this is assigned to an external object
			addBug(Confidence.HIGH,
					"a field of 'this' is published by assignment to an external object",
					instructionHandle);
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		Heap heap = frame.getHeap();
		if (referenceToPut.getID().equals(heap.getThisInstance())) {
			// XXX only a problem if it is a static field of the class we
			// analyze
			addBug(Confidence.HIGH,
					"'this' is published by assignment to a static field",
					instructionHandle);
		} else if (heap.get(referenceToPut.getID()).referredBy(
				heap.getThisInstance().getId(), heap)) {
			addBug(Confidence.HIGH,
					"a field of 'this' is published by assignment to a static field",
					instructionHandle);
		}
	}
}