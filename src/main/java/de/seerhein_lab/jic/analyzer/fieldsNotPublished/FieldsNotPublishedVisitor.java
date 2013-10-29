package de.seerhein_lab.jic.analyzer.fieldsNotPublished;

import java.util.Set;
import java.util.logging.Level;

import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;

import de.seerhein_lab.jic.AnalysisCache;
import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.ResultValue;
import de.seerhein_lab.jic.ResultValue.Kind;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.MethodInvocation;
import de.seerhein_lab.jic.slot.ReferenceSlot;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.slot.VoidSlot;
import de.seerhein_lab.jic.vm.ExternalObject;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.HeapObject;
import de.seerhein_lab.jic.vm.PC;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class FieldsNotPublishedVisitor extends BaseVisitor {

	protected FieldsNotPublishedVisitor(ClassContext classContext, MethodGen methodGen,
			Frame frame, Heap heap, ConstantPoolGen constantPoolGen, PC pc,
			CodeExceptionGen[] exceptionHandlers, Set<MethodInvocation> alreadyVisitedMethods,
			int depth, Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			AnalysisCache cache) {
		super(classContext, methodGen, frame, heap, constantPoolGen, alreadyVisitedIfBranch,
				alreadyVisitedMethods, pc, exceptionHandlers, depth, cache);
	}

	// public FieldsNotPublishedVisitor(ClassContext classContext,
	// MethodGen methodGen, Frame frame, Heap heap, ConstantPoolGen
	// constantPoolGen,
	// PC pc, CodeExceptionGen[] exceptionHandlers,
	// Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
	// this(classContext, methodGen, frame, heap, constantPoolGen, pc,
	// exceptionHandlers, alreadyVisitedMethods, depth, new
	// HashSet<Pair<InstructionHandle, Boolean>>());
	// }

	// @Override
	// protected BaseVisitor getInstructionsAnalysisVisitor(
	// Frame frame, Heap heap,
	// Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
	// InstructionHandle instructionHandle) {
	// return new FieldsNotPublishedVisitor(classContext, methodGen,
	// frame, heap, constantPoolGen, instructionHandle,
	// exceptionHandlers, alreadyVisitedMethods, depth,
	// alreadyVisitedIfBranch);
	// }

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen,
			Set<MethodInvocation> alreadyVisitedMethods) {
		return new FieldsNotPublishedAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods,
				depth, cache);
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
		Slot returnType = Slot.getDefaultSlotInstance(obj.getType(constantPoolGen));
		logger.log(Level.FINEST, indentation + "\t" + returnType);

		if (returnType instanceof VoidSlot)
			result.add(new ResultValue(Kind.REGULAR, returnType, heap));
		else {
			Slot returnSlot = frame.popStackByRequiredSlots();
			if (returnType instanceof ReferenceSlot)
				detectAReturnBug((ReferenceSlot) returnSlot);
			result.add(new ResultValue(Kind.REGULAR, returnSlot, heap));
		}
		pc.invalidate();
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		if (argument.isNullReference())
			return;

		HeapObject argumentObject = heap.get(argument.getID());
		if (argumentObject.equals(heap.getThisInstance())) {
			// XXX problem or not?? Inheritance?!?
			addBug(Confidence.HIGH, "'this' is passed to a virtual method and published",
					pc.getCurrentInstruction());
		} else if (argumentObject.isTransitivelyReferredBy(heap.getThisInstance())) {
			addBug(Confidence.HIGH,
					"a field of 'this' is passed to a virtual mehtod and published",
					pc.getCurrentInstruction());
		} else if (argumentObject.refersObjectThatIsReferredBy(heap.getThisInstance())) {
			// publish Object that refers Object referedBy 'this'
			addBug(Confidence.HIGH, "an Object that refers an Object refered by 'this' is passed"
					+ " to a virtual mehtod and published", pc.getCurrentInstruction());
		}
	}

	@Override
	protected void detectXAStoreBug(ReferenceSlot arrayReference, Slot valueToStore) {
		if (!(valueToStore instanceof ReferenceSlot)) {
			// if the value is not a reference we do not analyze
			return;
		}

		ReferenceSlot referenceToStore = (ReferenceSlot) valueToStore;
		if (referenceToStore.isNullReference())
			return;

		HeapObject objectToStore = heap.get(referenceToStore.getID());
		// array is the "external"
		if (heap.getObject(arrayReference) instanceof ExternalObject) {
			if (objectToStore.isTransitivelyReferredBy(heap.getThisInstance())) {
				// a field of this is assigned to an external object
				addBug(Confidence.HIGH,
						"field of 'this' is published by assignment to an external array",
						pc.getCurrentInstruction());
			} else if (objectToStore.refersObjectThatIsReferredBy(heap.getThisInstance()))
				// publish Object that refers Object referedBy 'this'
				addBug(Confidence.HIGH,
						"an Object that refers an Object refered by 'this' is published"
								+ " by assignment to an external array", pc.getCurrentInstruction());
		}
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference, Slot valueToPut) {
		if (!(valueToPut instanceof ReferenceSlot)) {
			// if the value is not a reference we do not analyze
			return;
		}

		ReferenceSlot referenceToPut = (ReferenceSlot) valueToPut;
		if (referenceToPut.isNullReference())
			return;

		HeapObject objectToPut = heap.get(referenceToPut.getID());
		// target is the "external"
		if (heap.getObject(targetReference) instanceof ExternalObject) {
			if (objectToPut.isTransitivelyReferredBy(heap.getThisInstance())) {
				// a field of this is assigned to an external object
				addBug(Confidence.HIGH,
						"a field of 'this' is published by assignment to an external object",
						pc.getCurrentInstruction());
			} else if (objectToPut.refersObjectThatIsReferredBy(heap.getThisInstance()))
				// publish Object that refers Object referedBy 'this'
				addBug(Confidence.HIGH,
						"an Object that refers an Object refered by 'this' is published"
								+ " by assignment to an external object",
						pc.getCurrentInstruction());
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		if (referenceToPut.isNullReference())
			return;

		HeapObject objectToPut = heap.get(referenceToPut.getID());
		// XXX only a problem if it is a static field of the class we analyze
		if (objectToPut.equals(heap.getThisInstance())) {
			addBug(Confidence.HIGH, "'this' is published by assignment to a static field",
					pc.getCurrentInstruction());
		} else if (objectToPut.isTransitivelyReferredBy(heap.getThisInstance())) {
			// publish Object referedBy 'this'
			addBug(Confidence.HIGH,
					"a field of 'this' is published by assignment to a static field",
					pc.getCurrentInstruction());
		} else if (objectToPut.refersObjectThatIsReferredBy(heap.getThisInstance()))
			// publish Object that refers Object referedBy 'this'
			addBug(Confidence.HIGH,
					"an Object that refers an Object refered by 'this' is published"
							+ " by assignment to a static field", pc.getCurrentInstruction());
	}

	protected void detectAReturnBug(ReferenceSlot returnValue) {
		HeapObject returnObject = heap.get(returnValue.getID());
		if (returnObject == null)
			return;

		if (returnObject.isTransitivelyReferredBy(heap.getThisInstance())) {
			// publish Object referedBy 'this'
			addBug(Confidence.HIGH, "a field of 'this' is published by return",
					pc.getCurrentInstruction());
		} else if (returnObject.refersObjectThatIsReferredBy(heap.getThisInstance()))
			// publish Object that refers Object referedBy 'this'
			addBug(Confidence.HIGH,
					"an Object that refers an Object refered by 'this' is published by return",
					pc.getCurrentInstruction());

	}
}
