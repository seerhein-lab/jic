package de.seerhein_lab.jic.analyzer.unmod.method;

import java.util.Set;

import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.QualifiedMethod;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.cache.AnalysisCache.Check;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.vm.ExternalObject;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.HeapObject;
import de.seerhein_lab.jic.vm.PC;
import de.seerhein_lab.jic.vm.ReferenceSlot;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class MethodUnmodifiableVisitor extends BaseVisitor {

	protected MethodUnmodifiableVisitor(ClassContext classContext, MethodGen methodGen,
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
		return new MethodUnmodifiableAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods,
				depth, cache, methodInvocationDepth);
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {

		// FieldsNotPublished

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
		} else if (argumentObject.complexObjectIsReachableBy(heap.getThisInstance())) {
			// publish Object that refers Object referedBy 'this'
			addBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"an Object that refers an Object refered by 'this' is passed"
							+ " to a virtual method and published", pc.getCurrentInstruction());
		}
	}

	@Override
	protected void detectXAStoreBug(ReferenceSlot arrayReference, Slot valueToStore) {

		// NoMutators

		// array is referred by 'this'
		if (heap.getThisInstance().isReachable(arrayReference.getObject(heap))) {
			addBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"the value of an array referred by a field of 'this' is modified",
					pc.getCurrentInstruction());
		}

		// FieldsNotPublished

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

		// NoMutators

		// left side is referred by a field of this
		if (heap.getThisInstance().isReachable(targetReference.getObject(heap))) {
			addBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"the value of an object referred by a field of 'this' is modified",
					pc.getCurrentInstruction());
		}

		// FieldsNotPublished

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

		// FieldsNotPublished

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

	@Override
	protected void detectAReturnBug(ReferenceSlot returnSlot) {

		// FieldsNotPublished

		if (returnSlot.isNullReference())
			return;

		HeapObject returnObject = returnSlot.getObject(heap);

		if (heap.getThisInstance().isReachable(returnObject)) {
			if (!returnObject.isImmutable()) {
				addBug("IMMUTABILITY_BUG", Confidence.HIGH,
						"a mutable field of 'this' is returned and published",
						pc.getCurrentInstruction());
			}
		} else {
			Set<HeapObject> thisClosure = heap.getThisInstance().getClosure();
			thisClosure.remove(heap.getThisInstance());
			Set<HeapObject> retClosure = returnObject.getClosure();
			boolean published = false;

			for (HeapObject obj : thisClosure) {
				if (retClosure.contains(obj)) {
					if (!obj.isImmutable()) {
						published = true;
						break;
					}
				}
			}

			if (published) {
				addBug("IMMUTABILITY_BUG",
						Confidence.HIGH,
						"an object that refers a mutable field of 'this' is returned and published",
						pc.getCurrentInstruction());
			}
		}

	}
}
