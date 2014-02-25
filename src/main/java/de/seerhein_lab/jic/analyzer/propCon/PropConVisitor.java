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

		if (argument.getObject(heap).equals(heap.getThisInstance())) {
			// 'this' is passed into a virtual method
			addBug("PROPER_CONSTRUCTION_BUG", Confidence.HIGH,
					"'this' is passed into a virtual method and escapes",
					pc.getCurrentInstruction());
			return;
		}
		if (argument.getObject(heap).isReachable(heap.getThisInstance())) {
			// argument that refers to 'this' is passed into a virtual method
			addBug("PROPER_CONSTRUCTION_BUG",
					Confidence.HIGH,
					"an object containing 'this' is passed into a virtual method letting 'this' escape",
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
		if (arrayReference.getObject(heap) instanceof ExternalObject) {
			// the array is externally known
			if (referenceToStore.getObject(heap) != null
					&& referenceToStore.getObject(heap).equals(heap.getThisInstance())) {
				// this is assigned to the array
				addBug("PROPER_CONSTRUCTION_BUG", Confidence.HIGH,
						"'this' is assigned to an external array and escapes",
						pc.getCurrentInstruction());
			} else if (arrayReference.getObject(heap) != null
					&& arrayReference.getObject(heap).isReachable(heap.getThisInstance())) {
				addBug("PROPER_CONSTRUCTION_BUG",
						Confidence.HIGH,
						"an object containing 'this' is assigned to an external array letting 'this' escape",
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
		if (targetReference.getObject(heap) instanceof ExternalObject) {
			// the left side of the assignment is externally known
			// if (referenceToPut.getID() != null
			// && referenceToPut.getObject(heap).equals(heap.getThisInstance()))
			// {
			if (referenceToPut.equals(ReferenceSlot.getThisReference(heap))) {
				addBug("PROPER_CONSTRUCTION_BUG", Confidence.HIGH,
						"'this' is assigned to an external object and escapes",
						pc.getCurrentInstruction());
			} else if (referenceToPut.getObject(heap) != null
					&& referenceToPut.getObject(heap).isReachable(heap.getThisInstance())) {
				addBug("PROPER_CONSTRUCTION_BUG",
						Confidence.HIGH,
						"an object containing 'this' is assigned to an external object letting 'this' escape",
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

		if (referenceToPut.getObject(heap).equals(heap.getThisInstance())) {
			addBug("PROPER_CONSTRUCTION_BUG", Confidence.HIGH,
					"'this' is assigned to a static field and escapes", pc.getCurrentInstruction());
		} else if (referenceToPut.getObject(heap).isReachable(heap.getThisInstance())) {
			addBug("PROPER_CONSTRUCTION_BUG",
					Confidence.HIGH,
					"an object containing 'this' is assigned to a static field letting 'this' escape",
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

	@Override
	protected void detectAReturnBug(ReferenceSlot returnSlot) {
	}
}
