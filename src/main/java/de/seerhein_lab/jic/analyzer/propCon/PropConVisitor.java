package de.seerhein_lab.jic.analyzer.propCon;

import java.util.Set;

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
import de.seerhein_lab.jic.vm.HeapObject;
import de.seerhein_lab.jic.vm.PC;
import de.seerhein_lab.jic.vm.ReferenceSlot;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class PropConVisitor extends BaseVisitor {

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

	private String containsThis(HeapObject object) {
		if (object.equals(heap.getThisInstance()))
			return "'this'";
		if (object.isReachable(heap.getThisInstance()))
			return "an object containing 'this'";
		return null;
	}

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		if (argument.isNullReference())
			return;

		String contains = containsThis(argument.getObject(heap));
		if (contains != null)
			addBug("PROPER_CONSTRUCTION_BUG", Confidence.HIGH, contains
					+ " is passed into a virtual method letting 'this' escape",
					pc.getCurrentInstruction());
	}

	@Override
	protected void detectXAStoreBug(ReferenceSlot arrayReference, Slot valueToStore) {
		if (!(valueToStore instanceof ReferenceSlot))
			return;
		ReferenceSlot referenceToStore = (ReferenceSlot) valueToStore;
		if (referenceToStore.isNullReference())
			return;
		if (arrayReference.isNullReference()
				|| !(arrayReference.getObject(heap) instanceof ExternalObject))
			return;

		String contains = containsThis(referenceToStore.getObject(heap));
		if (contains != null)
			addBug("PROPER_CONSTRUCTION_BUG", Confidence.HIGH, contains
					+ " is assigned to an external array letting 'this' escape",
					pc.getCurrentInstruction());
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference, Slot valueToPut) {
		if (!(valueToPut instanceof ReferenceSlot))
			return;
		ReferenceSlot referenceToPut = (ReferenceSlot) valueToPut;
		if (referenceToPut.isNullReference())
			return;
		if (targetReference.isNullReference()
				|| !(targetReference.getObject(heap) instanceof ExternalObject))
			return;

		String contains = containsThis(referenceToPut.getObject(heap));
		if (contains != null)
			addBug("PROPER_CONSTRUCTION_BUG", Confidence.HIGH, contains
					+ " is assigned to an external object letting 'this' escape",
					pc.getCurrentInstruction());
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		if (referenceToPut.isNullReference())
			return;

		String contains = containsThis(referenceToPut.getObject(heap));
		if (contains != null)
			addBug("PROPER_CONSTRUCTION_BUG", Confidence.HIGH, contains
					+ " is assigned to a static field letting 'this' escape",
					pc.getCurrentInstruction());
	}

	@Override
	protected void detectAReturnBug(ReferenceSlot returnSlot) {
	}
}
