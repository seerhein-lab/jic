package de.seerhein_lab.jca.analyzer.fieldsNotPublishedAnalyzer;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.AALOAD;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;

import de.seerhein_lab.jca.AlreadyVisited;
import de.seerhein_lab.jca.Frame;
import de.seerhein_lab.jca.ResultValue;
import de.seerhein_lab.jca.ResultValue.Kind;
import de.seerhein_lab.jca.analyzer.BaseInstructionsAnalysisVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.heap.Array;
import de.seerhein_lab.jca.heap.ClassInstance;
import de.seerhein_lab.jca.heap.Heap;
import de.seerhein_lab.jca.heap.HeapObject;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.Slot;
import de.seerhein_lab.jca.slot.VoidSlot;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class FieldsNotPublishedAnalysisVisitor extends
		BaseInstructionsAnalysisVisitor {

	protected FieldsNotPublishedAnalysisVisitor(
			ClassContext classContext,
			Method method,
			Frame frame,
			ConstantPoolGen constantPoolGen,
			Set<AlreadyVisited<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			Set<AlreadyVisited<Method, Slot[]>> alreadyVisitedMethods,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers, int depth) {
		super(classContext, method, frame, constantPoolGen,
				alreadyVisitedIfBranch, alreadyVisitedMethods,
				instructionHandle, exceptionHandlers, depth);
	}

	public FieldsNotPublishedAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers,
			Set<AlreadyVisited<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		super(classContext, method, frame, constantPoolGen, instructionHandle,
				exceptionHandlers, alreadyVisitedMethods, depth);
	}

	@Override
	protected BaseInstructionsAnalysisVisitor getInstructionsAnalysisVisitor(
			Frame frame,
			Set<AlreadyVisited<InstructionHandle, Boolean>> alreadyVisitedIfBranch,
			InstructionHandle instructionHandle) {
		return new FieldsNotPublishedAnalysisVisitor(classContext, method,
				frame, constantPoolGen, alreadyVisitedIfBranch,
				alreadyVisitedMethods, instructionHandle, exceptionHandlers,
				depth);
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen) {
		return new FieldsNotPublishedMethodAnalyzer(classContext,
				targetMethodGen, alreadyVisitedMethods, depth);
	}

	@Override
	protected String getBugType() {
		// TODO
		return "FIELDS_PUBILSHED_BUG";
	}

	// TODO aaload

	@Override
	public void visitAALOAD(AALOAD obj) {
		logger.log(Level.FINEST, indentation + obj.toString(false));
		// pop array index
		frame.getStack().pop();
		// pop array reference
		ReferenceSlot arrayReference = (ReferenceSlot) frame.getStack().pop();
		Array array = (Array) frame.getHeap().get(arrayReference.getID());

		linkNewArray(obj, arrayReference, frame.getHeap());

		for (Iterator<HeapObject> iterator = array.getReferredIterator(); iterator
				.hasNext();) {
			Frame newFrame = new Frame(frame);
			newFrame.getStack().push(new ReferenceSlot(iterator.next()));
			BaseInstructionsAnalysisVisitor visitor = getInstructionsAnalysisVisitor(
					newFrame, alreadyVisitedIfBranch,
					instructionHandle.getNext());
			instructionHandle.getNext().accept(visitor);
			bugs.addAll(visitor.getBugs().getCollection());
			result.addAll(visitor.getResult());
		}
	};

	public void linkNewArray(AALOAD obj, ReferenceSlot o, Heap heap) {
		if (obj.getType(constantPoolGen).getType() == Constants.T_OBJECT)
			heap.linkObjects(o.getID(), null, heap.newClassInstance().getId());
		else if (obj.getType(constantPoolGen).getType() == Constants.T_ARRAY)
			heap.linkObjects(o.getID(), null, heap.newArray().getId());
		else
			throw new AssertionError();
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.3. FieldOrMethod <br>
	 * 10.3.2. GETFIELD <br>
	 * Called when a GETFIELD operation occurs. Pops an object reference from
	 * the stack and pushes the value of the specified field onto the stack.
	 * */
	@Override
	public void visitGETFIELD(GETFIELD obj) {
		logger.log(Level.FINEST, indentation + obj.toString(false));
		// Notation: gets o.f

		// pop object reference
		ReferenceSlot o = (ReferenceSlot) frame.getStack().pop();

		// obj.getSignature() refers to desired field
		Slot f = Slot.getDefaultSlotInstance(obj.getType(constantPoolGen));
		if (f instanceof ReferenceSlot) {
			Heap heap = frame.getHeap();
			if (o.getID().equals(heap.getExternalObject().getId())) {
				// if left side is external return external
				f = new ReferenceSlot(heap.getExternalObject());
			} else {
				// get the ClassInstance linked to the desired field
				if (((ClassInstance) heap.get(o.getID())).getField(obj
						.getFieldName(constantPoolGen)) == null)
					linkNewClassInstance(obj, o, heap);
				f = new ReferenceSlot(
						((ClassInstance) heap.get(o.getID())).getField(obj
								.getFieldName(constantPoolGen)));
			}
		}
		frame.pushStackByRequiredSlots(f);

		logger.log(
				Level.FINEST,
				indentation + "\t" + o + "."
						+ obj.getFieldName(constantPoolGen));

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * Create a new Array or ClassInstance and link it to the object.
	 */
	public void linkNewClassInstance(GETFIELD obj, ReferenceSlot o, Heap heap) {
		if (obj.getType(constantPoolGen).getType() == Constants.T_OBJECT)
			heap.linkObjects(o.getID(), obj.getFieldName(constantPoolGen), heap
					.newClassInstance().getId());
		else if (obj.getType(constantPoolGen).getType() == Constants.T_ARRAY)
			heap.linkObjects(o.getID(), obj.getFieldName(constantPoolGen), heap
					.newArray().getId());
		else
			throw new AssertionError();
	}

	@Override
	public void visitReturnInstruction(ReturnInstruction obj) {
		// TODO __CHECK
		// return 0xb1 (void)
		// areturn 0xb0
		// dreturn 0xaf
		// freturn 0xae
		// ireturn 0xac
		// lreturn 0xad

		logger.log(Level.FINE, indentation + obj.toString(false));
		Slot returnType = Slot.getDefaultSlotInstance(obj
				.getType(constantPoolGen));
		logger.log(Level.FINEST, indentation + "\t" + returnType);

		if (returnType instanceof VoidSlot)
			result.add(new ResultValue(Kind.REGULAR, returnType, frame
					.getHeap()));
		else {
			Slot returnSlot = frame.popStackByRequiredSlots();
			if (returnType instanceof ReferenceSlot)
				detectAReturnBug((ReferenceSlot) returnSlot);
			result.add(new ResultValue(Kind.REGULAR, returnSlot, frame
					.getHeap()));
		}
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		Heap heap = frame.getHeap();
		if (argument.getID().equals(heap.getThisInstance())) {
			// XXX problem or not?? Inheritance?!?
			addBug(Confidence.HIGH,
					"'this' is passed to a virtual method and published",
					instructionHandle);
		} else if (heap.get(argument.getID()).referredBy(
				heap.getThisInstance().getId(), heap)) {
			addBug(Confidence.HIGH,
					"a field of 'this' is passed to a virtual mehtod and published",
					instructionHandle);
		}
	}

	@Override
	protected void detectAStoreBug(ReferenceSlot arrayReference,
			Slot valueToStore) {
		if (!(valueToStore instanceof ReferenceSlot)) {
			// if the value is not a reference we do not analyze
			return;
		}

		ReferenceSlot referenceToStore = (ReferenceSlot) valueToStore;
		Heap heap = frame.getHeap();
		if (arrayReference.getID().equals(heap.getExternalObject())
				&& heap.get(referenceToStore.getID()).referredBy(
						heap.getThisInstance().getId(), heap)) {
			// a field of this is assigned to an external object
			addBug(Confidence.HIGH,
					"field of 'this' is published by assignment to an external array",
					instructionHandle);
		}
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference,
			Slot valueToPut) {
		if (!(valueToPut instanceof ReferenceSlot)) {
			// if the value is not a reference we do not analyze
			return;
		}

		ReferenceSlot referenceToPut = (ReferenceSlot) valueToPut;
		Heap heap = frame.getHeap();
		if (targetReference.getID().equals(heap.getExternalObject())
				&& heap.get(referenceToPut.getID()).referredBy(
						heap.getThisInstance().getId(), heap)) {
			// a field of this is assigned to an external object
			addBug(Confidence.HIGH,
					"a field of 'this' is published by assignment to an external object",
					instructionHandle);
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		Heap heap = frame.getHeap();
		if (referenceToPut.getID().equals(heap.getThisInstance())) {
			// XXX only a problem if it is a static field of the class we
			// analyze
			addBug(Confidence.HIGH,
					"'this' is published by assignment to a static field",
					instructionHandle);
		} else if (heap.get(referenceToPut.getID()).referredBy(
				heap.getThisInstance().getId(), heap)) {
			addBug(Confidence.HIGH,
					"a field of 'this' is published by assignment to a static field",
					instructionHandle);
		}
	}

	protected void detectAReturnBug(ReferenceSlot returnValue) {
		Heap heap = frame.getHeap();
		HeapObject returnObject = heap.get(returnValue.getID());
		if (returnObject == null)
			return;
		if (returnObject.referredBy(heap.getThisInstance().getId(), heap)) {
			addBug(Confidence.HIGH, "a field of 'this' is published by return",
					instructionHandle);
		}
	}
}
