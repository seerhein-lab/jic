package de.seerhein_lab.jic.analyzer.propCon;

import java.util.Set;

import org.apache.bcel.generic.AnnotationEntryGen;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jic.analyzer.BaseVisitor;
import de.seerhein_lab.jic.analyzer.QualifiedMethod;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.cache.AnalysisCache.Check;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.vm.ExternalObject;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.PC;
import de.seerhein_lab.jic.vm.ReferenceSlot;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class PropConVisitor extends BaseVisitor {

	private final static String IGNORE_ANNOTATION = "Lde/seerhein_lab/jic/IgnoreImmutabilityBug;";

	protected PropConVisitor(ClassContext classContext, MethodGen methodGen, Frame frame,
			Heap heap, ConstantPoolGen constantPoolGen, PC pc,
			CodeExceptionGen[] exceptionHandlers, Set<QualifiedMethod> alreadyVisitedMethods,
			int depth, Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			AnalysisCache cache, int methodInvocationDepth) {
		super(classContext, methodGen, frame, heap, constantPoolGen, alreadyVisitedIfBranch,
				alreadyVisitedMethods, pc, exceptionHandlers, depth, cache, methodInvocationDepth);
	}

	@Override
	protected Check getCheck() {
		return AnalysisCache.Check.PropCon;
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen,
			Set<QualifiedMethod> alreadyVisitedMethods, int methodInvocationDepth) {
		return new PropConAnalyzer(classContext, targetMethodGen, alreadyVisitedMethods, depth,
				cache, methodInvocationDepth);
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		if (hasIgnoreAnnotation())
			return;

		if (argument.isNullReference())
			return;

		if (heap.getObject(argument).equals(heap.getThisInstance())) {
			// 'this' is passed into a virtual method
			addBug(Confidence.HIGH, "'this' is passed into a virtual method and escapes",
					pc.getCurrentInstruction());
		} else if (heap.get(argument.getID()).transitivelyRefers(heap.getThisInstance())) {
			// argument that refers to 'this' is passed into a virtual method
			addBug(Confidence.HIGH,
					"a reference that refers to 'this' is passed into a virtual method letting 'this' escape",
					pc.getCurrentInstruction());
		}
	}

	@Override
	protected void detectXAStoreBug(ReferenceSlot arrayReference, Slot valueToStore) {
		if (hasIgnoreAnnotation())
			return;

		if (!(valueToStore instanceof ReferenceSlot)) {
			// if the value is not a reference we do not analyze
			return;
		}

		ReferenceSlot referenceToStore = (ReferenceSlot) valueToStore;
		if (heap.getObject(arrayReference) instanceof ExternalObject) {
			// the array is externally known
			if (referenceToStore.getID() != null && heap.getObject(referenceToStore).equals(heap.getThisInstance())) {
				// this is assigned to the array
				addBug(Confidence.HIGH, "'this' is assigned to an external array and escapes",
						pc.getCurrentInstruction());
			} else if (arrayReference.getID() != null
					&& heap.get(arrayReference.getID()).transitivelyRefers(heap.getThisInstance())) {
				// a reference containing this is assigned to the array
				addBug(Confidence.HIGH,
						"a reference containing 'this' is assigned to an external array and 'this' escapes",
						pc.getCurrentInstruction());
			}
		}
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference, Slot valueToPut) {
		if (hasIgnoreAnnotation())
			return;

		if (!(valueToPut instanceof ReferenceSlot)) {
			// if the value is not a reference we do not analyze
			return;
		}
		ReferenceSlot referenceToPut = (ReferenceSlot) valueToPut;
		if (heap.getObject(targetReference) instanceof ExternalObject) {
			// the left side of the assignment is externally known
			if (referenceToPut.getID() != null && heap.getObject(referenceToPut).equals(heap.getThisInstance())) {
				// this is on the right side
				addBug(Confidence.HIGH, "'this' is assigned to an external field and escapes",
						pc.getCurrentInstruction());
			} else if (referenceToPut.getID() != null
					&& heap.get(referenceToPut.getID()).transitivelyRefers(heap.getThisInstance())) {
				// this is contained in the right side
				addBug(Confidence.HIGH,
						"a reference containing 'this' is assigned to an external field and 'this' escapes",
						pc.getCurrentInstruction());
			}

		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		if (hasIgnoreAnnotation())
			return;

		if (referenceToPut.isNullReference())
			return;

		if (heap.getObject(referenceToPut).equals(heap.getThisInstance())) {
			addBug(Confidence.HIGH, "'this' is assigned to a static field and escapes",
					pc.getCurrentInstruction());
		} else if (heap.get(referenceToPut.getID()).transitivelyRefers(heap.getThisInstance())) {
			// the reference contains this
			addBug(Confidence.HIGH,
					"a reference containing 'this' is assigned to a static field and 'this' escapes",
					pc.getCurrentInstruction());
		}
	}

	private boolean hasIgnoreAnnotation() {
		try {
			for (AnnotationEntryGen annotationEntryGen : methodGen.getAnnotationEntries())
				if (annotationEntryGen.getAnnotation().getAnnotationType()
						.equals(IGNORE_ANNOTATION))
					return true;
		} catch (NullPointerException e) {
			logger.warning(indentation + "NullPointerException in handling Annotations: "
					+ "Assume no Annotations");
		}
		return false;
	}
}
