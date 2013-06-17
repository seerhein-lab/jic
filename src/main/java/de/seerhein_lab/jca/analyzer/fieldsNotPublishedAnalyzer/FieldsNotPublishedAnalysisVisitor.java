package de.seerhein_lab.jca.analyzer.fieldsNotPublishedAnalyzer;

import java.util.Set;
import java.util.logging.Level;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;

import de.seerhein_lab.jca.Frame;
import de.seerhein_lab.jca.Pair;
import de.seerhein_lab.jca.ResultValue;
import de.seerhein_lab.jca.ResultValue.Kind;
import de.seerhein_lab.jca.analyzer.BaseInstructionsAnalysisVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.heap.Heap;
import de.seerhein_lab.jca.heap.HeapObject;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.Slot;
import de.seerhein_lab.jca.slot.VoidSlot;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class FieldsNotPublishedAnalysisVisitor extends
		BaseInstructionsAnalysisVisitor {

	protected FieldsNotPublishedAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods,
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
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		super(classContext, method, frame, constantPoolGen, instructionHandle,
				exceptionHandlers, alreadyVisitedMethods, depth);
	}

	@Override
	protected BaseInstructionsAnalysisVisitor getInstructionsAnalysisVisitor(
			Frame frame,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
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
		return "FIELDS_PUBILSHED_BUG";
	}

	@Override
	public void visitReturnInstruction(ReturnInstruction obj) {
		// return 0xb1 (void)
		// areturn 0xb0
		// dreturn 0xaf
		// freturn 0xae
		// ireturn 0xac
		// lreturn 0xad

		logger.log(Level.FINE, indentation + obj.toString(false));
		Slot returnType = Slot.getDefaultSlotInstance(obj
				.getType(constantPoolGen));
		logger.log(Level.FINEST, indentation + "\t" + returnType);

		if (returnType instanceof VoidSlot)
			result.add(new ResultValue(Kind.REGULAR, returnType, frame
					.getHeap()));
		else {
			Slot returnSlot = frame.popStackByRequiredSlots();
			if (returnType instanceof ReferenceSlot)
				detectAReturnBug((ReferenceSlot) returnSlot);
			result.add(new ResultValue(Kind.REGULAR, returnSlot, frame
					.getHeap()));
		}
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		Heap heap = frame.getHeap();
		HeapObject argumentObject = heap.get(argument.getID());
		if (argumentObject.equals(heap.getThisInstance())) {
			// XXX problem or not?? Inheritance?!?
			addBug(Confidence.HIGH,
					"'this' is passed to a virtual method and published",
					instructionHandle);
		} else if (argumentObject.isTransitivelyReferredBy(heap
				.getThisInstance())) {
			addBug(Confidence.HIGH,
					"a field of 'this' is passed to a virtual mehtod and published",
					instructionHandle);
		} else if (argumentObject.isIndirectReferedBy(heap.getThisInstance())) {
			// publish Object that refers Object referedBy 'this'
			addBug(Confidence.HIGH,
					"an Object that refers an Object refered by 'this' is passed"
							+ " to a virtual mehtod and published",
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

		Heap heap = frame.getHeap();
		HeapObject objectToStore = heap.get(((ReferenceSlot) valueToStore)
				.getID());
		// array is the "external"
		if (arrayReference.getID().equals(heap.getExternalObject().getId())) {
			if (objectToStore.isTransitivelyReferredBy(heap.getThisInstance())) {
				// a field of this is assigned to an external object
				addBug(Confidence.HIGH,
						"field of 'this' is published by assignment to an external array",
						instructionHandle);
			} else if (objectToStore
					.isIndirectReferedBy(heap.getThisInstance()))
				// publish Object that refers Object referedBy 'this'
				addBug(Confidence.HIGH,
						"an Object that refers an Object refered by 'this' is published"
								+ " by assignment to an external array",
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

		Heap heap = frame.getHeap();
		HeapObject objectToPut = heap.get(((ReferenceSlot) valueToPut).getID());
		// target is the "external"
		if (targetReference.getID().equals(heap.getExternalObject().getId())) {
			if (objectToPut.isTransitivelyReferredBy(heap.getThisInstance())) {
				// a field of this is assigned to an external object
				addBug(Confidence.HIGH,
						"a field of 'this' is published by assignment to an external object",
						instructionHandle);
			} else if (objectToPut.isIndirectReferedBy(heap.getThisInstance()))
				// publish Object that refers Object referedBy 'this'
				addBug(Confidence.HIGH,
						"an Object that refers an Object refered by 'this' is published"
								+ " by assignment to an external object",
						instructionHandle);
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		Heap heap = frame.getHeap();
		HeapObject objectToPut = heap.get(referenceToPut.getID());
		// XXX only a problem if it is a static field of the class we analyze
		if (objectToPut.equals(heap.getThisInstance())) {
			addBug(Confidence.HIGH,
					"'this' is published by assignment to a static field",
					instructionHandle);
		} else if (objectToPut.isTransitivelyReferredBy(heap.getThisInstance())) {
			// publish Object referedBy 'this'
			addBug(Confidence.HIGH,
					"a field of 'this' is published by assignment to a static field",
					instructionHandle);
		} else if (objectToPut.isIndirectReferedBy(heap.getThisInstance()))
			// publish Object that refers Object referedBy 'this'
			addBug(Confidence.HIGH,
					"an Object that refers an Object refered by 'this' is published"
							+ " by assignment to a static field",
					instructionHandle);
	}

	protected void detectAReturnBug(ReferenceSlot returnValue) {
		Heap heap = frame.getHeap();
		HeapObject returnObject = heap.get(returnValue.getID());
		if (returnObject == null)
			return;
		if (returnObject.isTransitivelyReferredBy(heap.getThisInstance())) {
			// publish Object referedBy 'this'
			addBug(Confidence.HIGH, "a field of 'this' is published by return",
					instructionHandle);
		} else if (returnObject.isIndirectReferedBy(heap.getThisInstance()))
			// publish Object that refers Object referedBy 'this'
			addBug(Confidence.HIGH,
					"an Object that refers an Object refered by 'this' is published by return",
					instructionHandle);

	}
}
