package de.seerhein_lab.jic.analyzer.ctorArgsCopied;

import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.MethodInvocation;
import de.seerhein_lab.jic.slot.ReferenceSlot;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.vm.ExternalObject;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.PC;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class CtorArgsCopiedVisitor extends BaseVisitor {

	protected CtorArgsCopiedVisitor(ClassContext classContext, MethodGen methodGen, Frame frame,
			Heap heap, ConstantPoolGen constantPoolGen, PC pc,
			CodeExceptionGen[] exceptionHandlers, Set<MethodInvocation> alreadyVisitedMethods,
			int depth, Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch) {
		super(classContext, methodGen, frame, heap, constantPoolGen, alreadyVisitedIfBranch,
				alreadyVisitedMethods, pc, exceptionHandlers, depth);
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
			Set<MethodInvocation> alreadyVisitedMethods) {
		return new CtorArgsCopiedAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods,
				depth);
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		if (argument.isNullReference())
			return;

		if (heap.get(argument.getID()).isTransitivelyReferredBy(heap.getThisInstance())) {
			addBug(Confidence.HIGH, "a field of 'this' is passed to a virtual method and escapes",
					pc.getCurrentInstruction());
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

		if (heap.get(arrayReference.getID()).isTransitivelyReferredBy(heap.getThisInstance())) {
			// array is referred by this
			if (heap.getObject(referenceToStore) instanceof ExternalObject) {
				// external reference is assigned to an array referred by this
				addBug(Confidence.HIGH,
						"an external reference is assigned to an array referred by 'this'",
						pc.getCurrentInstruction());
			} else if (heap.getObject(referenceToStore)
					.transitivelyRefers(heap.getExternalObject())) {
				// a reference containing an external reference is assigned to
				// an array referred by this
				addBug(Confidence.HIGH,
						"a reference containing an external reference is assigned to an array referred by 'this'",
						pc.getCurrentInstruction());
			}
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

		if (targetReference.getID().equals(heap.getThisInstance().getId())) {
			// left side is this
			if (heap.getObject(referenceToPut) instanceof ExternalObject) {
				// right is external
				addBug(Confidence.HIGH, "an external object is assigned to 'this'",
						pc.getCurrentInstruction());
			} else if (heap.getObject(referenceToPut).transitivelyRefers(heap.getExternalObject())) {
				// right refers external
				addBug(Confidence.HIGH,
						"an object containing an external reference is assigned to 'this'",
						pc.getCurrentInstruction());
			}
		}
		if (heap.get(targetReference.getID()).isTransitivelyReferredBy(heap.getThisInstance())) {
			// left is referred by this
			if (heap.getObject(referenceToPut) instanceof ExternalObject) {
				// right is external
				addBug(Confidence.HIGH,
						"an external reference is assigned to an object referred by 'this'",
						pc.getCurrentInstruction());
			} else if (heap.getObject(referenceToPut).transitivelyRefers(heap.getExternalObject())) {
				// right refers external
				addBug(Confidence.HIGH,
						"a reference containing an external reference is assigned to an object referred by 'this'",
						pc.getCurrentInstruction());
			}
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		if (referenceToPut.isNullReference())
			return;

		// XXX this assigned to a static field?? Only starting class?!?
		if (referenceToPut.getID().equals(heap.getThisInstance().getId())) {
			// this is published
			addBug(Confidence.HIGH, "'this' is published by assignment to a static field",
					pc.getCurrentInstruction());
		}

		if (heap.get(referenceToPut.getID()).isTransitivelyReferredBy(heap.getThisInstance())) {
			// a field referred by this is published
			addBug(Confidence.HIGH,
					"an object referred by 'this' is published by assignment to a static field",
					pc.getCurrentInstruction());
		}
	}

}