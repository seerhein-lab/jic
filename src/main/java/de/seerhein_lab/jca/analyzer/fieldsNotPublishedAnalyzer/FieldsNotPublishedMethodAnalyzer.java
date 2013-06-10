package de.seerhein_lab.jca.analyzer.fieldsNotPublishedAnalyzer;

import static org.apache.bcel.Constants.CONSTRUCTOR_NAME;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import de.seerhein_lab.jca.Pair;
import de.seerhein_lab.jca.Frame;
import de.seerhein_lab.jca.ResultValue;
import de.seerhein_lab.jca.analyzer.BaseInstructionsAnalysisVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.analyzer.propConAnalyzer.PropConMethodAnalyzer;
import de.seerhein_lab.jca.heap.Heap;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.Slot;
import edu.umd.cs.findbugs.ba.ClassContext;

public class FieldsNotPublishedMethodAnalyzer extends BaseMethodAnalyzer {

	private JavaClass clazz;

	public FieldsNotPublishedMethodAnalyzer(ClassContext classContext,
			MethodGen methodGen, JavaClass clazz) {
		super(classContext, methodGen);
		this.clazz = clazz;
	}

	public FieldsNotPublishedMethodAnalyzer(ClassContext classContext,
			MethodGen methodGen,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		super(classContext, methodGen, alreadyVisitedMethods, depth);
	}

	protected BaseInstructionsAnalysisVisitor getInstructionAnalysisVisitor(
			Frame frame, InstructionHandle instructionHandle) {
		return new FieldsNotPublishedAnalysisVisitor(classContext, method,
				frame, new ConstantPoolGen(method.getConstantPool()),
				instructionHandle, exceptionHandlers, alreadyVisitedMethods,
				depth);
	}

	private Set<Heap> createCtorHeaps() {
		List<Method> ctors = getConstructors();
		Set<Heap> heaps = new HashSet<Heap>();

		for (Method ctor : ctors) {
			MethodGen ctorGen = new MethodGen(ctor, clazz.getClassName(),
					new ConstantPoolGen(clazz.getConstantPool()));

			BaseMethodAnalyzer ctorAnalyzer = new PropConMethodAnalyzer(
					classContext, ctorGen);
			ctorAnalyzer.analyze();
			if (ctorAnalyzer.getBugs().getCollection().isEmpty()) {
				for (ResultValue result : ctorAnalyzer.getResult()) {
					heaps.add(result.getHeap());
				}
			}
		}
		return heaps;
	}

	private List<Method> getConstructors() {
		List<Method> ctors = new Vector<Method>();
		Method[] methods = clazz.getMethods();
		for (Method method : methods)
			if (method.getName().equals(CONSTRUCTOR_NAME))
				ctors.add(method);
		return ctors;
	}

	@Override
	public void analyze() {
		Set<Heap> heaps = createCtorHeaps();
		for (Heap callerHeap : heaps) {
			Stack<Slot> callerStack = new Stack<Slot>();

			Slot externalReference = new ReferenceSlot(
					callerHeap.getExternalObject());

			// push args + this (if not static) onto the stack
			if (!method.isStatic()) {
				Slot thisReference = new ReferenceSlot(
						callerHeap.getThisInstance());
				callerStack.push(thisReference);
			}

			Type[] argTypes = method.getArgumentTypes();

			for (Type argType : argTypes) {
				Slot argument = Slot.getDefaultSlotInstance(argType);
				if (argument instanceof ReferenceSlot) {
					argument = externalReference;
				}
				for (int i = 0; i < argument.getNumSlots(); i++) {
					callerStack.push(argument);
				}
			}

			Frame callerFrame = new Frame(callerStack, callerHeap);

			analyze(callerFrame);
		}
	}
}
