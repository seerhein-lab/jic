package de.htwg_konstanz.in.jca.slot;

public class ReferenceType extends Slot {

	public ReferenceType(Visibility visibility, boolean isThis,
			boolean containsThis, boolean containsExternal,
			boolean isOrContainsField) {
		this.visibility = visibility;
		this.isThis = isThis;
		this.containsThis = containsThis;
		this.containsExternal = containsExternal;
		this.isOrContainsField = isOrContainsField;
	}

	private static final ReferenceType thisReference = new ReferenceType(
			Visibility.INTERNAL, true, false, false, false);

	public static enum Visibility {
		INTERNAL, EXTERNAL;
	}

	private Visibility visibility;
	private final boolean isThis;
	private boolean containsThis;
	private boolean containsExternal;
	private boolean isOrContainsField;

	public static ReferenceType getDefaultInstance() {
		return new ReferenceType(Visibility.INTERNAL, false, false, false,
				false);
	}

	public static ReferenceType getThisReference() {
		return thisReference;
	}

	@Override
	public int getNumSlots() {
		return 1;
	}

	@Override
	public Slot getCopy() {
		if (this.isThis)
			return this;
		return new ReferenceType(visibility, isThis, containsThis,
				containsExternal, isOrContainsField);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (containsExternal ? 1231 : 1237);
		result = prime * result + (containsThis ? 1231 : 1237);
		result = prime * result + (isOrContainsField ? 1231 : 1237);
		result = prime * result + (isThis ? 1231 : 1237);
		result = prime * result
				+ ((visibility == null) ? 0 : visibility.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ReferenceType))
			return false;
		ReferenceType other = (ReferenceType) obj;
		if (containsExternal != other.containsExternal)
			return false;
		if (containsThis != other.containsThis)
			return false;
		if (isOrContainsField != other.isOrContainsField)
			return false;
		if (isThis != other.isThis)
			return false;
		if (visibility != other.visibility)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return isThis ? "this" : "someReference";
	}

	/**
	 * @return the containsThis
	 */
	public boolean containsThis() {
		return containsThis;
	}

	/**
	 * @param containsThis
	 *            the containsThis to set
	 */
	public void setContainsThis(boolean containsThis) {
		this.containsThis = containsThis;
	}

	/**
	 * @return the containsExternal
	 */
	public boolean containsExternal() {
		return containsExternal;
	}

	/**
	 * @param containsExternal
	 *            the containsExternal to set
	 */
	public void setContainsExternal(boolean containsExternal) {
		this.containsExternal = containsExternal;
	}

	/**
	 * @return the isOrContainsField
	 */
	public boolean isOrContainsField() {
		return isOrContainsField;
	}

	/**
	 * @param isOrContainsField
	 *            the isOrContainsField to set
	 */
	public void setIsOrContainsField(boolean isOrContainsField) {
		this.isOrContainsField = isOrContainsField;
	}

	/**
	 * @return the visibility
	 */
	public Visibility getVisibility() {
		return visibility;
	}

	/**
	 * @param visibility
	 *            the visibility to set
	 */
	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	/**
	 * @return the isThis
	 */
	public boolean isThis() {
		return isThis;
	}

}
