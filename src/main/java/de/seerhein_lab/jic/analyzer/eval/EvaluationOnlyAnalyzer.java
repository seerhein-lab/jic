package de.seerhein_lab.jic.analyzer.eval;

import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.MethodInvocation;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.PC;
import edu.umd.cs.findbugs.ba.ClassContext;

@ThreadSafe
// Superclass is thread-safe, this sub-class doesn't add any public methods
public final class EvaluationOnlyAnalyzer extends BaseMethodAnalyzer {

	public EvaluationOnlyAnalyzer(ClassContext classContext, MethodGen methodGen,
			AnalysisCache cache) {
		this(classContext, methodGen, new HashSet<MethodInvocation>(), -1, cache);
		alreadyVisitedMethods.add(new MethodInvocation(classContext.getJavaClass(), methodGen
				.getMethod()));
	}

	protected EvaluationOnlyAnalyzer(ClassContext classContext, MethodGen methodGen,
			Set<MethodInvocation> alreadyVisitedMethods, int depth, AnalysisCache cache) {
		super(classContext, methodGen, alreadyVisitedMethods, depth, cache);
	}

	protected BaseVisitor getInstructionVisitor(Frame frame, Heap heap, PC pc,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch) {
		return new EvaluationOnlyVisitor(classContext, methodGen, frame, heap,
				methodGen.getConstantPool(), pc, exceptionHandlers, alreadyVisitedMethods, depth,
				alreadyVisitedIfBranch, cache);
	}

	@Override
	protected Heap getHeap() {
		return new Heap();
	}
}
