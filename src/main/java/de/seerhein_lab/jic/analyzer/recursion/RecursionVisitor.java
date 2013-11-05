package de.seerhein_lab.jic.analyzer.recursion;

import java.util.Set;
import java.util.logging.Level;

import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.MethodInvocation;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.slot.ReferenceSlot;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.PC;
import edu.umd.cs.findbugs.ba.ClassContext;

public class RecursionVisitor extends BaseVisitor {

	public RecursionVisitor(ClassContext classContext, MethodGen methodGen, Frame frame, Heap heap,
			ConstantPoolGen constantPoolGen, PC pc, CodeExceptionGen[] exceptionHandlers,
			Set<MethodInvocation> alreadyVisitedMethods, int depth,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch, AnalysisCache cache) {
		super(classContext, methodGen, frame, heap, constantPoolGen, alreadyVisitedIfBranch,
				alreadyVisitedMethods, pc, exceptionHandlers, depth, cache);
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen,
			Set<MethodInvocation> alreadyVisitedMethods) {
		return new RecursionAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods, depth,
				cache);
	}

	@Override
	protected void handleRecursion(InvokeInstruction obj, MethodGen targetMethodGen) {
		logger.log(Level.FINE, indentation + "Branch without break condition: not analyzing again.");
		pc.invalidate();
	}

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
	}

	@Override
	protected void detectXAStoreBug(ReferenceSlot arrayReference, Slot valueToStore) {
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference, Slot valueToPut) {
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
	}

}
