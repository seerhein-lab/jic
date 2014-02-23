package de.seerhein_lab.jic.analyzer.fieldsNotPublished;

import java.util.Set;

import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;

import de.seerhein_lab.jic.EvaluationResult;
import de.seerhein_lab.jic.EvaluationResult.Kind;
import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.QualifiedMethod;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.cache.AnalysisCache.Check;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.slot.VoidSlot;
import de.seerhein_lab.jic.vm.ExternalObject;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.HeapObject;
import de.seerhein_lab.jic.vm.PC;
import de.seerhein_lab.jic.vm.ReferenceSlot;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class FieldsNotPublishedVisitor extends BaseVisitor {

	protected FieldsNotPublishedVisitor(ClassContext classContext, MethodGen methodGen,
			Frame frame, Heap heap, ConstantPoolGen constantPoolGen, PC pc,
			CodeExceptionGen[] exceptionHandlers, Set<QualifiedMethod> alreadyVisitedMethods,
			int depth, Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			AnalysisCache cache, int methodInvocationDepth) {
		super(classContext, methodGen, frame, heap, constantPoolGen, alreadyVisitedIfBranch,
				alreadyVisitedMethods, pc, exceptionHandlers, depth, cache, methodInvocationDepth);
	}

	@Override
	protected Check getCheck() {
		return null;
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen,
			Set<QualifiedMethod> alreadyVisitedMethods, int methodInvocationDepth) {
		return new FieldsNotPublishedAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods,
				depth, cache, methodInvocationDepth);
	}

	@Override
	public void visitReturnInstruction(ReturnInstruction obj) {
		// return 0xb1 (void)
		// areturn 0xb0
		// dreturn 0xaf
		// freturn 0xae
		// ireturn 0xac
		// lreturn 0xad

		logger.fine(indentation + obj.toString(false));
		Slot returnType = Slot.getDefaultSlotInstance(obj.getType(constantPoolGen));
		logger.finest(indentation + "\t" + returnType);

		if (returnType instanceof VoidSlot)
			result.add(new EvaluationResult(Kind.REGULAR, returnType, heap));
		else {
			Slot returnSlot = frame.getStack().popByRequiredSize();
			if (returnType instanceof ReferenceSlot)
				detectAReturnBug((ReferenceSlot) returnSlot, obj.getType().toString());
			result.add(new EvaluationResult(Kind.REGULAR, returnSlot, heap));
		}
		pc.invalidate();
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		if (argument.isNullReference() || argument.getObject(heap).isImmutable())
			return;

		HeapObject argumentObject = argument.getObject(heap);
		// if (argumentObject.equals(heap.getThisInstance())) {
		// // XXX problem or not?? Inheritance?!?
		// addBug("IMMUTABILITY_BUG", Confidence.HIGH,
		// "'this' is passed to a virtual method and published",
		// pc.getCurrentInstruction());
		// } else
		if (heap.getThisInstance().isReachable(argumentObject)) {
			addBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"a field of 'this' is passed to a virtual method and published",
					pc.getCurrentInstruction());
		}
		// else if
		// (argumentObject.complexObjectIsReachableBy(heap.getThisInstance())) {
		// // publish Object that refers Object referedBy 'this'
		// addBug("IMMUTABILITY_BUG", Confidence.HIGH,
		// "an Object that refers an Object refered by 'this' is passed"
		// + " to a virtual method and published", pc.getCurrentInstruction());
		// }
	}

	@Override
	protected void detectXAStoreBug(ReferenceSlot arrayReference, Slot valueToStore) {
		if (!(valueToStore instanceof ReferenceSlot))
			return;

		ReferenceSlot referenceToStore = (ReferenceSlot) valueToStore;
		if (referenceToStore.isNullReference() || referenceToStore.getObject(heap).isImmutable())
			return;

		HeapObject objectToStore = referenceToStore.getObject(heap);
		// array is the "external"
		if (arrayReference.getObject(heap) instanceof ExternalObject) {
			if (heap.getThisInstance().isReachable(objectToStore)) {
				// a field of this is assigned to an external object
				addBug("IMMUTABILITY_BUG", Confidence.HIGH,
						"field of 'this' is published by assignment to an external array",
						pc.getCurrentInstruction());
			} else if (objectToStore.complexObjectIsReachableBy(heap.getThisInstance()))
				// publish Object that refers Object referedBy 'this'
				addBug("IMMUTABILITY_BUG", Confidence.HIGH,
						"an Object that refers an Object refered by 'this' is published"
								+ " by assignment to an external array", pc.getCurrentInstruction());
		}
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference, Slot valueToPut) {
		if (!(valueToPut instanceof ReferenceSlot))
			return;

		ReferenceSlot referenceToPut = (ReferenceSlot) valueToPut;
		if (referenceToPut.isNullReference() || referenceToPut.getObject(heap).isImmutable())
			return;

		HeapObject objectToPut = referenceToPut.getObject(heap);
		// target is the "external"
		if (targetReference.getObject(heap) instanceof ExternalObject) {
			if (heap.getThisInstance().isReachable(objectToPut)) {
				// a field of this is assigned to an external object
				addBug("IMMUTABILITY_BUG", Confidence.HIGH,
						"a field of 'this' is published by assignment to an external object",
						pc.getCurrentInstruction());
			} else if (objectToPut.complexObjectIsReachableBy(heap.getThisInstance()))
				// publish Object that refers Object referedBy 'this'
				addBug("IMMUTABILITY_BUG", Confidence.HIGH,
						"an Object that refers an Object refered by 'this' is published"
								+ " by assignment to an external object",
						pc.getCurrentInstruction());
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		if (referenceToPut.isNullReference() || referenceToPut.getObject(heap).isImmutable())
			return;

		HeapObject objectToPut = referenceToPut.getObject(heap);
		// XXX only a problem if it is a static field of the class we analyze
		if (objectToPut.equals(heap.getThisInstance())) {
			addBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"'this' is published by assignment to a static field",
					pc.getCurrentInstruction());
		} else if (heap.getThisInstance().isReachable(objectToPut)) {
			// publish Object referedBy 'this'
			addBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"a field of 'this' is published by assignment to a static field",
					pc.getCurrentInstruction());
		} else if (objectToPut.complexObjectIsReachableBy(heap.getThisInstance()))
			// publish Object that refers Object referedBy 'this'
			addBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"an Object that refers an Object refered by 'this' is published"
							+ " by assignment to a static field", pc.getCurrentInstruction());
	}

	protected void detectAReturnBug(ReferenceSlot returnValue, String returnValueClass) {
		HeapObject returnObject = returnValue.getObject(heap);
		if (returnValue.isNullReference() || returnValue.getObject(heap).isImmutable())
			return;

		if (heap.getThisInstance().isReachable(returnObject)) {
			// publish Object referedBy 'this'
			addBug("IMMUTABILITY_BUG", Confidence.HIGH, "a field of 'this' is published by return",
					pc.getCurrentInstruction());
		} else if (returnObject.complexObjectIsReachableBy(heap.getThisInstance()))
			// publish Object that refers Object referedBy 'this'
			addBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"an Object that refers an Object refered by 'this' is published by return",
					pc.getCurrentInstruction());

	}
}
