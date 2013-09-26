package de.seerhein_lab.jca.analyzer.ctorArgsCopied;

import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jca.Pair;
import de.seerhein_lab.jca.analyzer.BaseVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.slot.Slot;
import de.seerhein_lab.jca.vm.Frame;
import de.seerhein_lab.jca.vm.Heap;
import de.seerhein_lab.jca.vm.PC;
import edu.umd.cs.findbugs.ba.ClassContext;

@ThreadSafe // Superclass is thread-safe, this sub-class doesn't add any public methods
public final class CtorArgsCopiedAnalyzer extends BaseMethodAnalyzer {

	public CtorArgsCopiedAnalyzer(ClassContext classContext, MethodGen methodGen) {
		super(classContext, methodGen);
	}

	protected CtorArgsCopiedAnalyzer(ClassContext classContext,
			MethodGen methodGen,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		super(classContext, methodGen, alreadyVisitedMethods, depth);
	}

	protected BaseVisitor getInstructionVisitor(Frame frame,
			Heap heap, PC pc) {
		return new CtorArgsCopiedVisitor(classContext, method, frame, heap,
				new ConstantPoolGen(method.getConstantPool()),
				pc, exceptionHandlers,
				alreadyVisitedMethods, depth);
	}

	@Override
	protected Heap getHeap() {
		return new Heap();
	}
}
