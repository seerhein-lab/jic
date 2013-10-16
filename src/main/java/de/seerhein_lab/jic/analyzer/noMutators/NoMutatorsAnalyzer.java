package de.seerhein_lab.jic.analyzer.noMutators;

import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.MethodInvocation;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.PC;
import edu.umd.cs.findbugs.ba.ClassContext;

@ThreadSafe
// Superclass is thread-safe, this sub-class doesn't add any public methods
public class NoMutatorsAnalyzer extends BaseMethodAnalyzer {
	private Heap heap;

	public NoMutatorsAnalyzer(ClassContext classContext, MethodGen methodGen, Heap heap) {
		this(classContext, methodGen, new HashSet<MethodInvocation>(), -1);
		alreadyVisitedMethods.add(new MethodInvocation(classContext.getJavaClass(), methodGen
				.getMethod()));
		this.heap = heap;
	}

	protected NoMutatorsAnalyzer(ClassContext classContext, MethodGen methodGen,
			Set<MethodInvocation> alreadyVisitedMethods, int depth) {
		super(classContext, methodGen, alreadyVisitedMethods, depth);
	}

	protected BaseVisitor getInstructionVisitor(Frame frame, Heap heap, PC pc,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch) {
		return new NoMutatorsVisitor(classContext, methodGen, frame, heap,
				methodGen.getConstantPool(), pc, exceptionHandlers, alreadyVisitedMethods, depth,
				alreadyVisitedIfBranch);
	}

	@Override
	protected Heap getHeap() {
		return heap;
	}
}
