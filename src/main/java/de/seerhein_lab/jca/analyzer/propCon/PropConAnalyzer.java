package de.seerhein_lab.jca.analyzer.propCon;

import java.util.Set;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jca.Pair;
import de.seerhein_lab.jca.analyzer.BaseInstructionsVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.slot.Slot;
import de.seerhein_lab.jca.vm.Frame;
import de.seerhein_lab.jca.vm.Heap;
import de.seerhein_lab.jca.vm.PC;
import edu.umd.cs.findbugs.ba.ClassContext;

public class PropConAnalyzer extends BaseMethodAnalyzer {

	public PropConAnalyzer(ClassContext classContext, MethodGen methodGen) {
		super(classContext, methodGen);
	}

	public PropConAnalyzer(ClassContext classContext,
			MethodGen methodGen,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		super(classContext, methodGen, alreadyVisitedMethods, depth);
	}

	protected BaseInstructionsVisitor getInstructionVisitor(Frame frame,
			Heap heap, PC pc) {
		return new PropConInstructionsVisitor(classContext, method, frame,
				heap, new ConstantPoolGen(method.getConstantPool()),
				pc.getCurrentInstruction(), exceptionHandlers,
				alreadyVisitedMethods, depth);
	}

	@Override
	public Heap getHeap() {
		return new Heap();
	}
}
