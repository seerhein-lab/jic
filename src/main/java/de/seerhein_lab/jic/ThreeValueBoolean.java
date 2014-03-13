package de.seerhein_lab.jic;

public enum ThreeValueBoolean {
	yes, no, unknown;

	public ThreeValueBoolean and(ThreeValueBoolean other) {
		if (this == no || other == no) {
			return no;
		}
		if (this == yes && other == yes) {
			return yes;
		}
		return unknown;
	}

	public ThreeValueBoolean and(boolean other) {
		if (other) {
			return this;
		}
		return no;
	}

	public ThreeValueBoolean or(ThreeValueBoolean other) {
		if (this == yes || other == yes) {
			return yes;
		}
		if (this == no && other == no) {
			return no;
		}
		return unknown;
	}

	public ThreeValueBoolean or(boolean other) {
		if (other)
			return yes;
		return this;
	}

	public ThreeValueBoolean not() {
		if (this == yes)
			return no;
		if (this == no)
			return yes;
		return unknown;
	}

	public boolean maybeTrue() {
		return this != no;
	}

	public boolean maybeFalse() {
		return this != yes;
	}

	public static ThreeValueBoolean fromBoolean(boolean value) {
		return value ? ThreeValueBoolean.yes : ThreeValueBoolean.no;
	}

}
