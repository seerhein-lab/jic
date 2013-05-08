package de.seerhein_lab.jca.analyzer;

import java.util.HashMap;
import java.util.Map;

import de.seerhein_lab.jca.slot.DoubleSlot;
import de.seerhein_lab.jca.slot.FloatSlot;
import de.seerhein_lab.jca.slot.IntSlot;
import de.seerhein_lab.jca.slot.LongSlot;
import de.seerhein_lab.jca.slot.Slot;

public class SimpleInstructions {
	private final Map<Short, Slot> simpleInstructions;

	public static Map<Short, Slot> getSimpleInstructions() {
		return new SimpleInstructions().simpleInstructions;
	}

	private SimpleInstructions() {
		simpleInstructions = new HashMap<Short, Slot>();
		// arraylength
		simpleInstructions.put((short) 0xbe, IntSlot.getInstance());
		// baload
		simpleInstructions.put((short) 0x33, IntSlot.getInstance());
		// bastore
		simpleInstructions.put((short) 0x54, null);
		// bipush
		simpleInstructions.put((short) 0x10, IntSlot.getInstance());
		// breakpoint
		simpleInstructions.put((short) 0xca, null);
		// caload
		simpleInstructions.put((short) 0x34, IntSlot.getInstance());
		// castore
		simpleInstructions.put((short) 0x55, null);
		// d2f
		simpleInstructions.put((short) 0x90, FloatSlot.getInstance());
		// d2i
		simpleInstructions.put((short) 0x8e, IntSlot.getInstance());
		// d2l
		simpleInstructions.put((short) 0x8f, LongSlot.getInstance());
		// dadd
		simpleInstructions.put((short) 0x63, DoubleSlot.getInstance());
		// daload
		simpleInstructions.put((short) 0x31, DoubleSlot.getInstance());
		// dastore
		simpleInstructions.put((short) 0x52, null);
		// dcmpg
		simpleInstructions.put((short) 0x98, IntSlot.getInstance());
		// dcmpl
		simpleInstructions.put((short) 0x97, IntSlot.getInstance());
		// dconst_0
		simpleInstructions.put((short) 0x0e, DoubleSlot.getInstance());
		// dconst_1
		simpleInstructions.put((short) 0x0f, DoubleSlot.getInstance());
		// ddiv
		simpleInstructions.put((short) 0x6f, DoubleSlot.getInstance());
		// dmul
		simpleInstructions.put((short) 0x6b, DoubleSlot.getInstance());
		// dneg
		simpleInstructions.put((short) 0x77, DoubleSlot.getInstance());
		// drem
		simpleInstructions.put((short) 0x73, DoubleSlot.getInstance());
		// dsub
		simpleInstructions.put((short) 0x67, DoubleSlot.getInstance());
		// f2d
		simpleInstructions.put((short) 0x8d, DoubleSlot.getInstance());
		// f2i
		simpleInstructions.put((short) 0x8b, IntSlot.getInstance());
		// f2l
		simpleInstructions.put((short) 0x8c, LongSlot.getInstance());
		// fadd
		simpleInstructions.put((short) 0x62, FloatSlot.getInstance());
		// faload
		simpleInstructions.put((short) 0x30, FloatSlot.getInstance());
		// fastore
		simpleInstructions.put((short) 0x51, null);
		// fcmpg
		simpleInstructions.put((short) 0x96, IntSlot.getInstance());
		// fcmpl
		simpleInstructions.put((short) 0x95, IntSlot.getInstance());
		// fconst_0
		simpleInstructions.put((short) 0x0b, FloatSlot.getInstance());
		// fconst_1
		simpleInstructions.put((short) 0x0c, FloatSlot.getInstance());
		// fconst_2
		simpleInstructions.put((short) 0x0d, FloatSlot.getInstance());
		// fdiv
		simpleInstructions.put((short) 0x6e, FloatSlot.getInstance());
		// fmul
		simpleInstructions.put((short) 0x6a, FloatSlot.getInstance());
		// fneg
		simpleInstructions.put((short) 0x76, FloatSlot.getInstance());
		// frem
		simpleInstructions.put((short) 0x72, FloatSlot.getInstance());
		// fsub
		simpleInstructions.put((short) 0x66, FloatSlot.getInstance());
		// i2b
		simpleInstructions.put((short) 0x91, IntSlot.getInstance());
		// i2c
		simpleInstructions.put((short) 0x92, IntSlot.getInstance());
		// i2d
		simpleInstructions.put((short) 0x87, DoubleSlot.getInstance());
		// i2f
		simpleInstructions.put((short) 0x86, FloatSlot.getInstance());
		// i2l
		simpleInstructions.put((short) 0x85, LongSlot.getInstance());
		// i2s
		simpleInstructions.put((short) 0x93, IntSlot.getInstance());
		// iadd
		simpleInstructions.put((short) 0x60, IntSlot.getInstance());
		// iaload
		simpleInstructions.put((short) 0x2e, IntSlot.getInstance());
		// iand
		simpleInstructions.put((short) 0x7e, IntSlot.getInstance());
		// iastore
		simpleInstructions.put((short) 0x4f, null);
		// iconst_0
		simpleInstructions.put((short) 0x3, IntSlot.getInstance());
		// iconst_1
		simpleInstructions.put((short) 0x4, IntSlot.getInstance());
		// iconst_2
		simpleInstructions.put((short) 0x5, IntSlot.getInstance());
		// iconst_3
		simpleInstructions.put((short) 0x6, IntSlot.getInstance());
		// iconst_4
		simpleInstructions.put((short) 0x7, IntSlot.getInstance());
		// iconst_5
		simpleInstructions.put((short) 0x8, IntSlot.getInstance());
		// iconst_m1
		simpleInstructions.put((short) 0x2, IntSlot.getInstance());
		// idiv
		simpleInstructions.put((short) 0x6c, IntSlot.getInstance());
		// iinc
		simpleInstructions.put((short) 0x84, null);
		// impdep1
		simpleInstructions.put((short) 0xfe, null);
		// impdep2
		simpleInstructions.put((short) 0xff, null);
		// imul
		simpleInstructions.put((short) 0x68, IntSlot.getInstance());
		// ineg
		simpleInstructions.put((short) 0x74, IntSlot.getInstance());
		// instanceof
		simpleInstructions.put((short) 0xc1, IntSlot.getInstance());
		// ior
		simpleInstructions.put((short) 0x80, IntSlot.getInstance());
		// irem
		simpleInstructions.put((short) 0x70, IntSlot.getInstance());
		// ishl
		simpleInstructions.put((short) 0x78, IntSlot.getInstance());
		// ishr
		simpleInstructions.put((short) 0x7a, IntSlot.getInstance());
		// isub
		simpleInstructions.put((short) 0x64, IntSlot.getInstance());
		// iushr
		simpleInstructions.put((short) 0x7c, IntSlot.getInstance());
		// ixor
		simpleInstructions.put((short) 0x82, IntSlot.getInstance());
		// l2d
		simpleInstructions.put((short) 0x8a, DoubleSlot.getInstance());
		// l2f
		simpleInstructions.put((short) 0x89, FloatSlot.getInstance());
		// l2i
		simpleInstructions.put((short) 0x88, IntSlot.getInstance());
		// ladd
		simpleInstructions.put((short) 0x61, LongSlot.getInstance());
		// laload
		simpleInstructions.put((short) 0x2f, LongSlot.getInstance());
		// land
		simpleInstructions.put((short) 0x7f, LongSlot.getInstance());
		// lastore
		simpleInstructions.put((short) 0x50, null);
		// lcmp
		simpleInstructions.put((short) 0x94, IntSlot.getInstance());
		// lconst_0
		simpleInstructions.put((short) 0x9, LongSlot.getInstance());
		// lconst_1
		simpleInstructions.put((short) 0x0a, LongSlot.getInstance());
		// ldiv
		simpleInstructions.put((short) 0x6d, LongSlot.getInstance());
		// lmul
		simpleInstructions.put((short) 0x69, LongSlot.getInstance());
		// lneg
		simpleInstructions.put((short) 0x75, LongSlot.getInstance());
		// lor
		simpleInstructions.put((short) 0x81, LongSlot.getInstance());
		// lrem
		simpleInstructions.put((short) 0x71, LongSlot.getInstance());
		// lshl
		simpleInstructions.put((short) 0x79, LongSlot.getInstance());
		// lshr
		simpleInstructions.put((short) 0x7b, LongSlot.getInstance());
		// lsub
		simpleInstructions.put((short) 0x65, LongSlot.getInstance());
		// lushr
		simpleInstructions.put((short) 0x7d, LongSlot.getInstance());
		// lxor
		simpleInstructions.put((short) 0x83, LongSlot.getInstance());
		// monitorenter
		simpleInstructions.put((short) 0xc2, null);
		// monitorexit
		simpleInstructions.put((short) 0xc3, null);
		// nop
		simpleInstructions.put((short) 0x0, null);
		// pop
		simpleInstructions.put((short) 0x57, null);
		// pop2
		simpleInstructions.put((short) 0x58, null);
		// saload
		simpleInstructions.put((short) 0x35, IntSlot.getInstance());
		// sastore
		simpleInstructions.put((short) 0x56, null);
		// sipush
		simpleInstructions.put((short) 0x11, IntSlot.getInstance());
	}
}
