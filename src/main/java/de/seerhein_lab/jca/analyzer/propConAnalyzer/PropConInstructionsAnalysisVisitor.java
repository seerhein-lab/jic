package de.seerhein_lab.jca.analyzer.propConAnalyzer;

import java.util.Set;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jca.Frame;
import de.seerhein_lab.jca.analyzer.BaseInstructionsAnalysisVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer.AlreadyVisitedMethod;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

public class PropConInstructionsAnalysisVisitor extends
		BaseInstructionsAnalysisVisitor {

	protected PropConInstructionsAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			Set<AlreadyVisitedIfInstruction> alreadyVisited,
			Set<AlreadyVisitedMethod> alreadyVisitedMethods,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers, int depth) {
		super(classContext, method, frame, constantPoolGen, alreadyVisited,
				alreadyVisitedMethods, instructionHandle, exceptionHandlers,
				depth);
	}

	public PropConInstructionsAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers,
			Set<AlreadyVisitedMethod> alreadyVisitedMethods, int depth) {
		super(classContext, method, frame, constantPoolGen, instructionHandle,
				exceptionHandlers, alreadyVisitedMethods, depth);
	}

	@Override
	protected BaseInstructionsAnalysisVisitor getInstructionsAnalysisVisitor(
			Frame frame, Set<AlreadyVisitedIfInstruction> alreadyVisited,
			InstructionHandle instructionHandle) {
		return new PropConInstructionsAnalysisVisitor(classContext, method,
				frame, constantPoolGen, alreadyVisited, alreadyVisitedMethods,
				instructionHandle, exceptionHandlers, depth);
	}

	@Override
	protected BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen) {
		return new PropConMethodAnalyzer(classContext, targetMethodGen,
				alreadyVisitedMethods, depth);
	}

	@Override
	protected String getBugType() {
		return "PROPER_CONSTRUCTION_BUG";
	}

	// ******************************************************************//
	// Bug detection section //
	// ******************************************************************//

	@Override
	protected void detectVirtualMethodBug(ReferenceSlot argument) {
		if (argument.equals(ReferenceSlot.getThisReference())) {
			// this is passed to a method that can not be analyzed
			addBug(Confidence.MEDIUM,
					"this reference is passed to a method that can not be analyzed by static analyzis and might escape",
					instructionHandle);
		}
		if (argument.isRefersThis()) {
			// the reference contains this and it might be published
			addBug(Confidence.MEDIUM,
					"a reference containing this is passed to a method that can not be analyzed by static analyzis and this might escape",
					instructionHandle);
		}
		if (argument.isReferedByThis()
				&& (!argument.isExternal() || !argument.isReferedByExternal())) {
			// the reference belongs to this and was not externally known before
			addBug(Confidence.MEDIUM,
					"a reference referenced by this is passed to a method that can not be analyzed by static analyzis "
							+ "and other objects may see an inconsistent state",
					instructionHandle);
		}
	}

	@Override
	protected void detectAAStoreBug(ReferenceSlot arrayReference,
			ReferenceSlot referenceToStore) {
		if (arrayReference.isExternal() || arrayReference.isReferedByExternal()) {
			// the array is externally known
			if (referenceToStore.equals(ReferenceSlot.getThisReference())) {
				// this is assigned to the array
				addBug(Confidence.HIGH,
						"this reference is assigned to an external array and escapes",
						instructionHandle);
			}
			if (referenceToStore.isRefersThis()) {
				// a reference containing this is assigned to the array
				addBug(Confidence.HIGH,
						"a reference containing this is assigned to an external array and this escapes",
						instructionHandle);
			}
			if (referenceToStore.isReferedByThis()
					&& (!referenceToStore.isExternal() || !referenceToStore
							.isReferedByExternal())) {
				// the reference is contained by this and was not externally
				// known before
				addBug(Confidence.MEDIUM,
						"a field of this object is published to an externally known array and other objects may see an inconsistent state",
						instructionHandle);
			}
		}
	}

	@Override
	protected void detectPutFieldBug(ReferenceSlot targetReference,
			ReferenceSlot referenceToPut) {

		if (targetReference.isExternal()
				|| targetReference.isReferedByExternal()) {
			// the left side of the assignment is externally known
			if (referenceToPut.equals(ReferenceSlot.getThisReference())) {
				// this is on the right side
				addBug(Confidence.HIGH,
						"this reference is assigned to an external field and escapes",
						instructionHandle);
			}
			if (referenceToPut.isRefersThis()) {
				// this is contained in the right side
				addBug(Confidence.HIGH,
						"a reference containing this is assigned to an external field and this escapes",
						instructionHandle);
			}
			if (referenceToPut.isReferedByThis()
					&& (!referenceToPut.isReferedByExternal() || !referenceToPut
							.isExternal())) {
				// the reference is contained by this and was not externally
				// known before
				addBug(Confidence.MEDIUM,
						"a field of this object is published to an externally known field and other objects may see an inconsistent state",
						instructionHandle);
			}
		}
	}

	@Override
	protected void detectPutStaticBug(ReferenceSlot referenceToPut) {
		if (referenceToPut.equals(ReferenceSlot.getThisReference())) {
			addBug(Confidence.HIGH,
					"this reference is assigned to a static field and escapes",
					instructionHandle);
		}
		if (!(referenceToPut.isExternal() || referenceToPut
				.isReferedByExternal())) {
			// the reference was not externally known before
			if (referenceToPut.isRefersThis()) {
				// the reference contains this
				addBug(Confidence.HIGH,
						"a reference containing this is assigned to a static field and this escapes",
						instructionHandle);
			}
			if (referenceToPut.isReferedByThis()) {
				// the reference is contained by this
				addBug(Confidence.MEDIUM,
						"a field of of this object is published via a static field and other objects may see an inconsistent state",
						instructionHandle);
			}
		}
	}

}
