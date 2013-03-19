package de.htwg_konstanz.in.jca;

import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.Type;

import de.htwg_konstanz.in.jca.PropConMethodAnalyzer.AlreadyVisitedMethod;
import edu.umd.cs.findbugs.ba.ClassContext;

public class PropConInstructionsAnalysisVisitor extends
		BaseInstructionsAnalysisVisitor {

	protected PropConInstructionsAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			ArrayList<AlreadyVisitedIfInstruction> alreadyVisited,
			ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods,
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
			ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods, int depth) {
		super(classContext, method, frame, constantPoolGen, instructionHandle,
				exceptionHandlers, alreadyVisitedMethods, depth);
	}

	protected BaseInstructionsAnalysisVisitor getCorrectInstructionsAnalysisVisitor(
			ClassContext classContext, Method method, Frame frame,
			ConstantPoolGen constantPoolGen,
			ArrayList<AlreadyVisitedIfInstruction> alreadyVisited,
			ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers, int depth) {
		return new PropConInstructionsAnalysisVisitor(classContext, method,
				frame, constantPoolGen, alreadyVisited, alreadyVisitedMethods,
				instructionHandle, exceptionHandlers, depth);

	}

	// ******************************************************************//
	// Visit section //
	// ******************************************************************//

	/**
	 * used by visitINVOKEINTERFACE and visitINVOKEVIRTUAL
	 * 
	 * @param obj
	 *            node to be visited
	 */
	protected void handleMethodThatIsNotAnalyzed(InvokeInstruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		logger.log(Level.FINEST,
				indentation + "\t" + obj.getSignature(constantPoolGen));
		// get number of args
		Type[] type = obj.getArgumentTypes(constantPoolGen);
		// get return value

		Slot returnValue = Slot.getDefaultInstance(DataType.getDataType(obj
				.getReturnType(constantPoolGen)));

		Slot argument;
		// pop a value for each arg and 1 for the hidden reference
		for (int i = 0; i < type.length + 1; i++) {
			argument = frame.popStackByRequiredSlots();
			if (argument.equals(Slot.maybeThisReference)) {
				logger.log(
						Level.WARNING,
						"Warning: 'maybeThis' reference is passed into an alien method and might escape.");
				addBug(2,
						"Warning: 'maybeThis' reference is passed into an alien method and might escape.",
						instructionHandle);
				if (returnValue.getDataType().equals(DataType.referenceType)) {
					returnValue = Slot.maybeThisReference;
				}
			}
			if (argument.equals(Slot.thisReference)) {
				logger.log(Level.WARNING,
						"Warning: 'this' reference is passed into an alien method and might escape.");
				addBug(1,
						"Warning: 'this' reference is passed into an alien method and might escape.",
						instructionHandle);
				if (returnValue.getDataType().equals(DataType.referenceType)) {
					returnValue = Slot.maybeThisReference;
				}
			}
		}

		// works also for void results, because number of required slots = 0
		frame.pushStackByRequiredSlots(returnValue);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.3. FieldOrMethod <br>
	 * 10.3.4. PUTFIELD <br>
	 * Called when a PUTFIELD operation occurs. Pops a value and an object
	 * reference from the stack. The field in the object reference is set to the
	 * value.
	 * */
	@Override
	public void visitPUTFIELD(PUTFIELD obj) {
		logger.log(Level.FINEST, indentation + obj.toString(false));

		// right side of assignment
		Slot right = frame.popStackByRequiredSlots();
		String logPart = "";
		logPart += (right.getDataType().equals(DataType.doubleType) || right
				.getDataType().equals(DataType.longType)) ? right + ", "
				+ right : right;

		if (right.equals(Slot.thisReference)) {
			logger.log(Level.WARNING,
					"Error: 'this' reference is assigned to some object's field and escapes.");
			addBug(3,
					"Error: 'this' reference is assigned to some object's field and escapes.",
					instructionHandle);
		}
		if (right.equals(Slot.maybeThisReference)) {
			logger.log(
					Level.WARNING,
					"Warning: 'this' reference might be assigned to some object's field and might escape.");
			addBug(4,
					"Warning: 'this' reference might be assigned to some object's field and might escape.",
					instructionHandle);
		}
		// pop left side of assignment off the stack, too
		Slot left = frame.getStack().pop();

		logger.log(Level.FINEST,
				indentation + left + "." + obj.getFieldName(constantPoolGen)
						+ " <--" + logPart);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.3. FieldOrMethod <br>
	 * 10.3.4. PUTSTATIC <br>
	 * Called when a PUTSTATIC operation occurs. Pops a value from the stack and
	 * sets a static field in a class to the popped value.
	 */
	@Override
	public void visitPUTSTATIC(PUTSTATIC obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));

		// popping value from stack
		Slot toPut = frame.popStackByRequiredSlots();

		// writing log
		String log = "\t";
		log += obj.getReferenceType(constantPoolGen) + ".";
		log += obj.getName(constantPoolGen) + " <-- ";
		log += (toPut.getDataType().getNumSlots() == 2) ? toPut + ", " + toPut
				: toPut;
		logger.log(Level.FINEST, indentation + log);

		if (toPut.equals(Slot.thisReference)) {
			logger.log(Level.SEVERE,
					"Error: 'this' reference is assigned to a static field and escapes.");
			addBug(1,
					"Error: 'this' reference is assigned to a static field and escapes.",
					instructionHandle);
		}
		if (toPut.equals(Slot.maybeThisReference)) {
			logger.log(
					Level.WARNING,
					"Warning: 'this' reference might be assigned to a static field and might escape.");
			addBug(2,
					"Warning: 'this' reference might be assigned to a static field and might escape.",
					instructionHandle);
		}

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

}
