package de.htwg_konstanz.in.jca.slot;

import org.apache.bcel.generic.Type;

public abstract class Slot {

	public static int numRequiredSlots(Type[] types) {
		int numRequiredSlots = 0;
		for (Type type : types) {
			numRequiredSlots += getDefaultInstance(type).getNumSlots();
		}
		return numRequiredSlots;
	}

	public static Slot getDefaultInstance(String signature) {
		if (signature.equals("I"))
			return IntType.getDefaultInstance();
		if (signature.equals("J"))
			return LongType.getDefaultInstance();
		if (signature.equals("C"))
			return CharType.getDefaultInstance();
		if (signature.equals("B"))
			return ByteType.getDefaultInstance();
		if (signature.equals("Z"))
			return BooleanType.getDefaultInstance();
		if (signature.equals("S"))
			return ShortType.getDefaultInstance();
		if (signature.equals("F"))
			return FloatType.getDefaultInstance();
		if (signature.equals("D"))
			return DoubleType.getDefaultInstance();
		if (signature.equals("V"))
			return VoidType.getDefaultInstance();
		return ReferenceType.getDefaultInstance();
	}

	public static Slot getDefaultInstance(Type type) {
		return getDefaultInstance(type.getSignature());
	}

	public abstract int getNumSlots();

	public abstract Slot getCopy();

	public boolean isBooleanType() {
		return this instanceof BooleanType;
	}

	public boolean isByteType() {
		return this instanceof ByteType;
	}

	public boolean isCharType() {
		return this instanceof CharType;
	}

	public boolean isDoubleType() {
		return this instanceof DoubleType;
	}

	public boolean isFloatType() {
		return this instanceof FloatType;
	}

	public boolean isIntType() {
		return this instanceof IntType;
	}

	public boolean isLongType() {
		return this instanceof LongType;
	}

	public boolean isReferenceType() {
		return this instanceof ReferenceType;
	}

	public boolean isShortType() {
		return this instanceof ShortType;
	}

	public boolean isVoidType() {
		return this instanceof VoidType;
	}

	@Override
	public abstract boolean equals(Object object);

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public abstract String toString();

}
