package de.seerhein_lab.jic.analyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.generic.ARRAYLENGTH;
import org.apache.bcel.generic.ArithmeticInstruction;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.BREAKPOINT;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConversionInstruction;
import org.apache.bcel.generic.DCMPG;
import org.apache.bcel.generic.DCMPL;
import org.apache.bcel.generic.DCONST;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FCMPG;
import org.apache.bcel.generic.FCMPL;
import org.apache.bcel.generic.FCONST;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.IMPDEP1;
import org.apache.bcel.generic.IMPDEP2;
import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.LCMP;
import org.apache.bcel.generic.LCONST;
import org.apache.bcel.generic.MONITORENTER;
import org.apache.bcel.generic.MONITOREXIT;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.POP2;
import org.apache.bcel.generic.RET;
import org.apache.bcel.generic.SIPUSH;

import de.seerhein_lab.jic.Utils;
import de.seerhein_lab.jic.slot.DoubleSlot;
import de.seerhein_lab.jic.slot.FloatSlot;
import de.seerhein_lab.jic.slot.IntSlot;
import de.seerhein_lab.jic.slot.LongSlot;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.PC;

/**
 * Analyzes constructors whether the this-reference escapes or not. Therefore a
 * virtual machine is simulated and all occurring byte code operations are
 * performed in the corresponding visit-method.
 * <p>
 * This class does:
 * <ul>
 * <li>Check if the this reference escapes or not
 * <li>Provide a bug collection with the found errors
 * <li>Handle the type of values
 * <li>Handle if branches by static analysis
 * <li>Detect loops
 * <li>Handle switch cases
 * </ul>
 * </p>
 * <p>
 * This class does not:
 * <ul>
 * <li>Check if the entries have the expected type
 * <li>Consider exact values
 * <li>Consider storage into arrays
 * </ul>
 * </p>
 */
public class SimpleVisitor extends EmptyVisitor {
	protected static final Logger logger = Logger.getLogger("SimpleInstructionsAnalysisVisitor");

	private final static Map<Short, Slot> ARRAY_LOAD_INSTRUCTIONS = new HashMap<Short, Slot>();
	static {
		// load instructions
		ARRAY_LOAD_INSTRUCTIONS.put((short) 0x2e, IntSlot.getInstance()); // iaload
		ARRAY_LOAD_INSTRUCTIONS.put((short) 0x2f, LongSlot.getInstance()); // laload
		ARRAY_LOAD_INSTRUCTIONS.put((short) 0x30, FloatSlot.getInstance()); // faload
		ARRAY_LOAD_INSTRUCTIONS.put((short) 0x31, DoubleSlot.getInstance()); // daload
		// aaload - 0x32 - is treated specially
		ARRAY_LOAD_INSTRUCTIONS.put((short) 0x33, IntSlot.getInstance()); // baload
		ARRAY_LOAD_INSTRUCTIONS.put((short) 0x34, IntSlot.getInstance()); // caload
		ARRAY_LOAD_INSTRUCTIONS.put((short) 0x35, IntSlot.getInstance()); // saload

		// store instructions are treated specially
	}

	protected final String indentation;
	protected final int depth;
	protected final int methodInvocationDepth;
	protected Frame frame;
	protected Heap heap;
	protected final ConstantPoolGen constantPoolGen;
	// protected InstructionHandle instructionHandle;
	protected final PC pc;

	protected SimpleVisitor(Frame frame, Heap heap, ConstantPoolGen constantPoolGen, PC pc,
			int depth, int methodInvocationDepth) {
		this.frame = frame;
		this.heap = heap;
		this.constantPoolGen = constantPoolGen;
		this.pc = pc;
		// this.instructionHandle = pc.getCurrentInstruction();
		this.depth = depth;
		this.methodInvocationDepth = methodInvocationDepth;
		this.indentation = Utils.formatLoggingOutput(methodInvocationDepth);
	}

	// handle section

	protected void handleSimpleInstruction(Instruction obj, Slot slot) {
		logger.fine(indentation + obj.toString(false));
		StringBuilder log = new StringBuilder();
		log.append("\t" + "(");

		int consumed = obj.consumeStack(constantPoolGen);
		int produced = obj.produceStack(constantPoolGen);
		Slot entry;

		for (int i = 0; i < consumed; i++) {
			entry = frame.getStack().pop();
			log.append((i == 0) ? entry : ", " + entry);
		}
		log.append(") -> (");

		for (int i = 0; i < produced; i++) {
			frame.getStack().push(slot);
			log.append((i == 0) ? slot : ", " + slot);
		}

		log.append(")");
		logger.finest( indentation + log);

		pc.advance();

	}

	// ******************************************************************//
	// Visit section //
	// ******************************************************************//

	/**
	 * 2. ArithmeticInstruction <br>
	 * Called when an ArithmeticInstruction occurs and handles all
	 * ArithmeticInstructions. The type and the number of consumed and produced
	 * words are taken from the ArithmeticInstruction object.
	 */
	@Override
	public void visitArithmeticInstruction(ArithmeticInstruction obj) {
		handleSimpleInstruction(obj, Slot.getDefaultSlotInstance(obj.getType(constantPoolGen)));
	}

	// -----------------------------------------------------------------

	/**
	 * 3. ArrayInstruction<br>
	 * Called when an ArrayInstruction operation occurs. This visitor handles
	 * all ALOAD and ASTORE instructions distinguished by the opcode.
	 */
	@Override
	public void visitArrayInstruction(ArrayInstruction obj) {
		if (!ARRAY_LOAD_INSTRUCTIONS.containsKey(obj.getOpcode())) {
			// instruction is not handled with this method
			return;
		}
		handleSimpleInstruction(obj, ARRAY_LOAD_INSTRUCTIONS.get(obj.getOpcode()));
	}

	// -----------------------------------------------------------------
	/**
	 * 4. ARRAYLENGTH<br>
	 * Called when an ARRAYLENGTH operation occurs. Gets the length of an array.
	 * Pops an array reference from the stack and pushes the array length as an
	 * integer value.
	 */
	@Override
	public void visitARRAYLENGTH(ARRAYLENGTH obj) {
		handleSimpleInstruction(obj, IntSlot.getInstance());
	}

	// -----------------------------------------------------------------
	/**
	 * 6. BIPUSH<br>
	 * Called when a BIPUSH operation occurs. Pushes a byte onto the stack as an
	 * integer value.
	 */
	@Override
	public void visitBIPUSH(BIPUSH obj) {
		handleSimpleInstruction(obj, IntSlot.getInstance());
	}

	// -----------------------------------------------------------------
	/**
	 * 8. BREAKPOINT<br>
	 * Called when a BREAKPOINT operation occurs. The BREAKPOINT operation is
	 * reserved for Java debuggers and should not appear in any class file.
	 */
	@Override
	public void visitBREAKPOINT(BREAKPOINT obj) {
		throw new AssertionError(
				"The BREAKPOINT operation is reserved for Java debuggers and should not appear in any class file");
	}

	// -----------------------------------------------------------------
	/**
	 * 9. ConversionInstruction <br>
	 * Called when a ConversionInstruction operation occurs.Converts the type of
	 * a value to another one. Therefore a value is popped, converted and the
	 * new one pushed back onto the stack.
	 */
	@Override
	public void visitConversionInstruction(ConversionInstruction obj) {
		handleSimpleInstruction(obj, Slot.getDefaultSlotInstance(obj.getType(constantPoolGen)));
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.5. INSTANCEOF <br>
	 */
	@Override
	public void visitINSTANCEOF(INSTANCEOF obj) {
		handleSimpleInstruction(obj, IntSlot.getInstance());
	}

	// -----------------------------------------------------------------
	/**
	 * 11. DCMPG <br>
	 * Called when a DCMPG operation occurs. Pops two double values from the
	 * stack, compares them and pushes the integer result onto the stack. If
	 * value1 is greater than value2 the result is 1, if value1 is equal to
	 * value2 the result is 0 and if value1 is smaller than value2 the result is
	 * -1. If value1 or value2 is NaN the result is 1.
	 */
	@Override
	public void visitDCMPG(DCMPG obj) {
		handleSimpleInstruction(obj, IntSlot.getInstance());
	}

	// -----------------------------------------------------------------
	/**
	 * 12. DCMPL <br>
	 * Called when a DCMPG operation occurs. Pops two double values from the
	 * stack, compares them and pushes the integer result onto the stack. If
	 * value1 is greater than value2 the result is 1, if value1 is equal to
	 * value2 the result is 0 and if value1 is smaller than value2 the result is
	 * -1. If value1 or value2 is NaN the result is -1.
	 */
	@Override
	public void visitDCMPL(DCMPL obj) {
		handleSimpleInstruction(obj, IntSlot.getInstance());
	}

	// -----------------------------------------------------------------
	/**
	 * 13. DCONST <br>
	 * Called when a DECONST operation occurs. Pushes the double constant 0.0 or
	 * 1.0 onto the stack.
	 * */
	@Override
	public void visitDCONST(DCONST obj) {
		handleSimpleInstruction(obj, DoubleSlot.getInstance());
	}

	// -----------------------------------------------------------------

	/**
	 * 14. FCMPG <br>
	 * Called when a FCMPG operation occurs. Pops two float values from the
	 * stack, compares them and pushes the integer result onto the stack. If
	 * value1 is greater than value2 the result is 1, if value1 is equal to
	 * value2 the result is 0 and if value1 is smaller than value2 the result is
	 * -1. If value1 or value2 is NaN the result is 1.
	 */
	@Override
	public void visitFCMPG(FCMPG obj) {
		handleSimpleInstruction(obj, IntSlot.getInstance());
	}

	// -----------------------------------------------------------------

	/**
	 * 15. FCMPL <br>
	 * Called when a DCMPG operation occurs. Pops two float values from the
	 * stack, compares them and pushes the integer result onto the stack. If
	 * value1 is greater than value2 the result is 1, if value1 is equal to
	 * value2 the result is 0 and if value1 is smaller than value2 the result is
	 * -1. If value1 or value2 is NaN the result is -1.
	 */
	@Override
	public void visitFCMPL(FCMPL obj) {
		handleSimpleInstruction(obj, IntSlot.getInstance());
	}

	// -----------------------------------------------------------------
	/**
	 * 16. FCONST <br>
	 * Called when a FCONST operation occurs. Pushes 0.0f, 1.0f or 2.0f on the
	 * stack.
	 */
	@Override
	public void visitFCONST(FCONST obj) {
		handleSimpleInstruction(obj, FloatSlot.getInstance());
	}

	// -----------------------------------------------------------------
	/**
	 * 17. ICONST <br>
	 * Called when an ICONST operation occurs. Loads the integer value -1, 0, 1,
	 * 2, 3, 4 or 5 onto the stack.
	 */
	@Override
	public void visitICONST(ICONST obj) {
		handleSimpleInstruction(obj, IntSlot.getInstance());
	}

	// -----------------------------------------------------------------
	/**
	 * 18. IMPDEP1 <br>
	 * Called when an IMPDEP1 operation occurs. This method is reserved for
	 * implementation-dependent operations within debuggers and should not
	 * appear in any class file.
	 */
	@Override
	public void visitIMPDEP1(IMPDEP1 obj) {
		throw new AssertionError(
				"IMPDEP1 is reserved for implementation-dependent operations within debuggers "
						+ "and should not appear in any class file.");
	}

	// -----------------------------------------------------------------
	/**
	 * 19. IMPDEP2 <br>
	 * Called when an IMPDEP2 operation occurs. This method is reserved for
	 * implementation-dependent operations within debuggers and should not
	 * appear in any class file.
	 */
	@Override
	public void visitIMPDEP2(IMPDEP2 obj) {
		throw new AssertionError(
				"IMPDEP2 is reserved for implementation-dependent operations within debuggers "
						+ "and should not appear in any class file.");
	}

	// -----------------------------------------------------------------
	/**
	 * 20. LCMP <br>
	 * Called when a LCMP operation occurs. Pops two long values from the stack,
	 * compares them and pushes the integer result onto the stack.
	 */
	@Override
	public void visitLCMP(LCMP obj) {
		handleSimpleInstruction(obj, IntSlot.getInstance());
	}

	// -----------------------------------------------------------------
	/**
	 * 21. LCONST <br>
	 * Called when a LCONST operation occurs. Pushes the long 0L or 1L onto the
	 * stack.
	 */
	@Override
	public void visitLCONST(LCONST obj) {
		handleSimpleInstruction(obj, LongSlot.getInstance());
	}

	// ---LocalVariableInstruction--------------------------------------

	@Override
	public void visitIINC(IINC obj) {
		handleSimpleInstruction(obj, null);
	}

	// -----------------------------------------------------------------
	/**
	 * 23. MONITORENTER<br>
	 * Called when a MONITORENTER occurs. Pops an object reference from the
	 * stack and holds it as a lock for synchronization.
	 */
	@Override
	public void visitMONITORENTER(MONITORENTER obj) {
		handleSimpleInstruction(obj, null);
	}

	// -----------------------------------------------------------------
	/**
	 * 24. MONITOREXIT<br>
	 * Called when a MONITOREXIT operation occurs. Releases the lock from a
	 * synchronized section for a popped object reference.
	 */
	@Override
	public void visitMONITOREXIT(MONITOREXIT obj) {
		handleSimpleInstruction(obj, null);
	}

	// -----------------------------------------------------------------
	/**
	 * 26. NOP <br>
	 * Called when a NOP operation occurs. Performs no operation.
	 */
	@Override
	public void visitNOP(NOP obj) {
		handleSimpleInstruction(obj, null);
	}

	// -----------------------------------------------------------------
	/**
	 * 27. RET <br>
	 * Continues execution from address taken from a local variable #index (the
	 * asymmetry with jsr is intentional).
	 */
	@Override
	public void visitRET(RET obj) {
		handleSimpleInstruction(obj, null);
	}

	// -----------------------------------------------------------------
	/**
	 * 29. SIPUSH<br>
	 * Called when a SIPUSH operation occurs. Pushes a short identified by 2
	 * index bytes onto the stack.
	 */
	@Override
	public void visitSIPUSH(SIPUSH obj) {
		handleSimpleInstruction(obj, IntSlot.getInstance());
	}

	@Override
	public void visitPOP(POP obj) {
		handleSimpleInstruction(obj, null);
	}

	@Override
	public void visitPOP2(POP2 obj) {
		handleSimpleInstruction(obj, null);
	}

}
