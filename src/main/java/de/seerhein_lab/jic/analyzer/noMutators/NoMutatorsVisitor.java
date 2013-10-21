package de.seerhein_lab.jic.analyzer.noMutators;

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
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.PC;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class NoMutatorsVisitor extends BaseVisitor {

	protected NoMutatorsVisitor(ClassContext classContext, MethodGen methodGen, Frame frame,
			Heap heap, ConstantPoolGen constantPoolGen, PC pc,
			CodeExceptionGen[] exceptionHandlers, Set<MethodInvocation> alreadyVisitedMethods,
			int depth, Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch) {
		super(classContext, methodGen, frame, heap, constantPoolGen, alreadyVisitedIfBranch,
				alreadyVisitedMethods, pc, exceptionHandlers, depth);
	}

	// public NoMutatorsVisitor(ClassContext classContext,
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
	// return new NoMutatorsVisitor(classContext, methodGen,
	// frame, heap, constantPoolGen, instructionHandle,
	// exceptionHandlers, alreadyVisitedMethods, depth,
	// alreadyVisitedIfBranch);
	// }

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen,
			Set<MethodInvocation> alreadyVisitedMethods) {
		return new NoMutatorsAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods, depth);
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		// nothing of interest can happen
	}

	@Override
	protected void detectXAStoreBug(ReferenceSlot arrayReference, Slot valueToStore) {
		// array is referred by 'this'
		if (heap.get(arrayReference.getID()).isTransitivelyReferredBy(heap.getThisInstance())) {
			addBug(Confidence.HIGH,
					"the value of an array referred by a field of 'this' is modified",
					pc.getCurrentInstruction());
		}
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference, Slot valueToPut) {
		// left side is referred by a field of this
		if (heap.get(targetReference.getID()).isTransitivelyReferredBy(heap.getThisInstance())) {
			addBug(Confidence.HIGH,
					"the value of an object referred by a field of 'this' is modified",
					pc.getCurrentInstruction());
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		// nothing of interest can happen
	}
}