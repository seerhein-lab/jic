package de.seerhein_lab.jic.analyzer.unmod.ctor;

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
import de.seerhein_lab.jic.vm.PC;
import de.seerhein_lab.jic.vm.ReferenceSlot;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class CtorUnmodifiableVisitor extends BaseVisitor {

	protected CtorUnmodifiableVisitor(ClassContext classContext, MethodGen methodGen, Frame frame,
			Heap heap, ConstantPoolGen constantPoolGen, PC pc,
			CodeExceptionGen[] exceptionHandlers, Set<QualifiedMethod> alreadyVisitedMethods,
			int depth, Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			AnalysisCache cache, int methodInvocationDepth) {
		super(classContext, methodGen, frame, heap, constantPoolGen, alreadyVisitedIfBranch,
				alreadyVisitedMethods, pc, exceptionHandlers, depth, cache, methodInvocationDepth);
	}

	@Override
	protected Check getCheck() {
		return AnalysisCache.Check.CtorArgsCopied;
	}

	// public CtorArgsCopiedVisitor(ClassContext classContext,
	// MethodGen methodGen, Frame frame, Heap heap, ConstantPoolGen
	// constantPoolGen,
	// PC pc,
	// CodeExceptionGen[] exceptionHandlers,
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
	// return new CtorArgsCopiedVisitor(classContext, methodGen, frame, heap,
	// constantPoolGen, instructionHandle, exceptionHandlers,
	// alreadyVisitedMethods, depth, alreadyVisitedIfBranch);
	// }

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen,
			Set<QualifiedMethod> alreadyVisitedMethods, int methodInvocationDepth) {
		return new CtorUnmodifiableAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods,
				depth, cache, methodInvocationDepth);
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		if (argument.isNullReference() || argument.getObject(heap).isImmutable())
			return;

		if (heap.getThisInstance().isReachable(argument.getObject(heap))) {
			addBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"a field of 'this' is passed into a virtual method and published",
					pc.getCurrentInstruction());
		}
	}

	@Override
	protected void detectXAStoreBug(ReferenceSlot arrayReference, Slot valueToStore) {
		if (!(valueToStore instanceof ReferenceSlot))
			return;

		ReferenceSlot referenceToStore = (ReferenceSlot) valueToStore;
		if (referenceToStore.isNullReference() || referenceToStore.getObject(heap).isImmutable())
			return;

		if (heap.getThisInstance().isReachable(arrayReference.getObject(heap))) {
			// array is referred by this
			if (referenceToStore.getObject(heap) instanceof ExternalObject) {
				// external reference is assigned to an array referred by this
				addBug("IMMUTABILITY_BUG", Confidence.HIGH,
						"an external reference is assigned to an array referred by 'this'",
						pc.getCurrentInstruction());
			} else if (referenceToStore.getObject(heap).isReachable(
					heap.getImmutableExternalObject())
					|| referenceToStore.getObject(heap)
							.isReachable(heap.getMutableExternalObject())) {
				// a reference containing an external reference is assigned to
				// an array referred by this
				addBug("IMMUTABILITY_BUG",
						Confidence.HIGH,
						"a reference containing an external reference is assigned to an array referred by 'this'",
						pc.getCurrentInstruction());
			}
		}
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference, Slot valueToPut) {
		if (!(valueToPut instanceof ReferenceSlot))
			return;

		ReferenceSlot referenceToPut = (ReferenceSlot) valueToPut;
		if (referenceToPut.isNullReference() || referenceToPut.getObject(heap).isImmutable())
			return;

		if (targetReference.getObject(heap).equals(heap.getThisInstance())) {
			// left side is this
			if (referenceToPut.getObject(heap).equals(heap.getMutableExternalObject())) {
				// right is external
				addBug("IMMUTABILITY_BUG", Confidence.HIGH,
						"an external object is assigned to 'this'", pc.getCurrentInstruction());
			} else if (referenceToPut.getObject(heap).isReachable(heap.getMutableExternalObject())) {
				// right refers external
				addBug("IMMUTABILITY_BUG", Confidence.HIGH,
						"an object containing an external reference is assigned to 'this'",
						pc.getCurrentInstruction());
			}
		}
		if (heap.getThisInstance().isReachable(targetReference.getObject(heap))) {
			// left is referred by this
			if (referenceToPut.getObject(heap).equals(heap.getMutableExternalObject())) {
				// right is external
				addBug("IMMUTABILITY_BUG", Confidence.HIGH,
						"an external reference is assigned to an object referred by 'this'",
						pc.getCurrentInstruction());
			} else if (referenceToPut.getObject(heap).isReachable(heap.getMutableExternalObject())) {
				// right refers external
				addBug("IMMUTABILITY_BUG",
						Confidence.HIGH,
						"a reference containing an external reference is assigned to an object referred by 'this'",
						pc.getCurrentInstruction());
			}
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		if (referenceToPut.isNullReference() || referenceToPut.getObject(heap).isImmutable())
			return;

		if (heap.getThisInstance().isReachable(referenceToPut.getObject(heap))) {
			// a field referred by this is published
			addBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"an object referred by 'this' is published by assignment to a static field",
					pc.getCurrentInstruction());
		}
	}

	@Override
	protected void detectAReturnBug(ReferenceSlot returnSlot) {
	}

}
