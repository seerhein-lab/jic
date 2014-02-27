package de.seerhein_lab.jic.analyzer.unmod.method;

import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.QualifiedMethod;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.PC;
import edu.umd.cs.findbugs.ba.ClassContext;

@ThreadSafe
// Superclass is thread-safe, this sub-class doesn't add any public methods
public class MethodUnmodifiableAnalyzer extends BaseMethodAnalyzer {
	private Heap heap;

	public MethodUnmodifiableAnalyzer(ClassContext classContext, MethodGen methodGen, Heap heap,
			AnalysisCache cache, int methodInvocationDepth) {
		this(classContext, methodGen, new HashSet<QualifiedMethod>(), -1, cache,	methodInvocationDepth);
		alreadyVisitedMethods.add(new QualifiedMethod(classContext.getJavaClass(), methodGen
				.getMethod()));
		this.heap = heap;
	}

	protected MethodUnmodifiableAnalyzer(ClassContext classContext, MethodGen methodGen,
			Set<QualifiedMethod> alreadyVisitedMethods, int depth, AnalysisCache cache,	int methodInvocationDepth) {
		super(classContext, methodGen, alreadyVisitedMethods, depth, cache, methodInvocationDepth);
	}

	protected BaseVisitor getInstructionVisitor(Frame frame, Heap heap, PC pc,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch) {
		return new MethodUnmodifiableVisitor(classContext, methodGen, frame, heap,
				methodGen.getConstantPool(), pc, exceptionHandlers, alreadyVisitedMethods, depth,
				alreadyVisitedIfBranch, cache, methodInvocationDepth);
	}

	@Override
	protected Heap getHeap() {
		return heap;
	}

}
