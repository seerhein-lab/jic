package de.seerhein_lab.jic.analyzer.recursion;

import java.util.Set;

import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.AnalysisResult;
import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.QualifiedMethod;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.cache.AnalysisCache.Check;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.PC;
import de.seerhein_lab.jic.vm.ReferenceSlot;
import edu.umd.cs.findbugs.ba.ClassContext;

public class RecursionVisitor extends BaseVisitor {

	public RecursionVisitor(ClassContext classContext, MethodGen methodGen, Frame frame, Heap heap,
			ConstantPoolGen constantPoolGen, PC pc, CodeExceptionGen[] exceptionHandlers,
			Set<QualifiedMethod> alreadyVisitedMethods, int depth,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch, AnalysisCache cache,
			int methodInvocationDepth) {
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
		return new RecursionAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods, depth,
				cache, methodInvocationDepth);
	}

	@Override
	protected AnalysisResult handleRecursion(MethodGen targetMethodGen) {
		logger.fine(indentation + "Branch without break condition: not analyzing again.");
		return new AnalysisResult();
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

	@Override
	protected void detectAReturnBug(ReferenceSlot returnSlot) {
	}

}
