package de.seerhein_lab.jca;

public enum ThreeValueBoolean {
	yes, no, unknown;

	public ThreeValueBoolean and(ThreeValueBoolean other) {
		if (this.equals(no) || other.equals(no)) {
			return no;
		}
		if (this.equals(yes) && other.equals(yes)) {
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
		if (this.equals(yes) || other.equals(yes)) {
			return yes;
		}
		if (this.equals(no) && other.equals(no)) {
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
		if (this.equals(yes))
			return no;
		if (this.equals(no))
			return yes;
		return unknown;
	}

	public static ThreeValueBoolean fromBoolean(boolean value) {
		return value ? ThreeValueBoolean.yes : ThreeValueBoolean.no;
	}

}
