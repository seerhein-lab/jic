package de.seerhein_lab.jic.analyzer.propCon;

import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.AnalysisCache;
import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.MethodInvocation;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.OpStack;
import de.seerhein_lab.jic.vm.PC;
import edu.umd.cs.findbugs.ba.ClassContext;

@ThreadSafe
// Superclass is thread-safe, this sub-class doesn't add any public methods
public final class PropConAnalyzer extends BaseMethodAnalyzer {
	public PropConAnalyzer(ClassContext classContext, MethodGen methodGen, AnalysisCache cache) {
		this(classContext, methodGen, new HashSet<MethodInvocation>(), -1, cache);
		alreadyVisitedMethods.add(new MethodInvocation(classContext.getJavaClass(), methodGen
				.getMethod()));
	}

	protected PropConAnalyzer(ClassContext classContext, MethodGen methodGen,
			Set<MethodInvocation> alreadyVisitedMethods, int depth, AnalysisCache cache) {
		super(classContext, methodGen, alreadyVisitedMethods, depth, cache);
	}

	protected BaseVisitor getInstructionVisitor(Frame frame, Heap heap, PC pc,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch) {
		// if ( JicDetector.propConCounter > 5000 )
		// throw new
		// OutOfMemoryError("emergency break to avoid out of memory error.");

		// JicDetector.propConCounter++;
		return new PropConVisitor(classContext, methodGen, frame, heap,
				methodGen.getConstantPool(), pc, exceptionHandlers, alreadyVisitedMethods, depth,
				alreadyVisitedIfBranch, cache);
	}

	@Override
	public final synchronized void analyze(OpStack callerStack, Heap heap) {
		// if ( cache.contains(classContext.getClass(), methodGen.getMethod(),
		// AnalysisCache.Check.PropCon ) ) {
		// decide whether bugs matter (if first param != this, they dont)
		// put cache.result into place
		// put cache.bugs into place
		// return;
		//
		super.analyze(callerStack, heap);
		// cache.putIfAbsent(result, bugs);
	}

	@Override
	protected Heap getHeap() {
		return new Heap();
	}
}
