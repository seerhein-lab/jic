package de.htwg_konstanz.in.jca.slot;

import java.util.HashSet;
import java.util.Set;

public class CopyOfReferenceSlotOld extends Slot {
	private static CopyOfReferenceSlotOld thisReference;
	private static CopyOfReferenceSlotOld nullReference;

	// private static Set<ReferenceSlot> rootElements;
	// private static Set<ReferenceSlot> leafElements;
	// private static Set<RefContainer> alreadyVisited;
	private static boolean somethingChanged;
	private static CopyOfReferenceSlotOld currentStartElement;

	private final Set<CopyOfReferenceSlotOld> referedBy = new HashSet<CopyOfReferenceSlotOld>();
	private final Set<CopyOfReferenceSlotOld> references = new HashSet<CopyOfReferenceSlotOld>();

	private final boolean isExternal;

	private boolean refersThis = false;
	private boolean referedByThis = false;
	private boolean refersExternal = false;
	private boolean referedByExternal = false;
	private boolean refersField = false;
	private boolean referedByField = false;

	public static class RefContainer {
		private final CopyOfReferenceSlotOld caller;
		private final CopyOfReferenceSlotOld callee;
		private final CopyOfReferenceSlotOld startElement;

		public RefContainer(CopyOfReferenceSlotOld caller, CopyOfReferenceSlotOld callee,
				CopyOfReferenceSlotOld startElement) {
			this.caller = caller;
			this.callee = callee;
			this.startElement = startElement;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((callee == null) ? 0 : callee.hashCode());
			result = prime * result
					+ ((caller == null) ? 0 : caller.hashCode());
			result = prime * result
					+ ((startElement == null) ? 0 : startElement.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof RefContainer)) {
				return false;
			}
			RefContainer other = (RefContainer) obj;
			if (callee == null) {
				if (other.callee != null) {
					return false;
				}
			} else if (!callee.equals(other.callee)) {
				return false;
			}
			if (caller == null) {
				if (other.caller != null) {
					return false;
				}
			} else if (!caller.equals(other.caller)) {
				return false;
			}
			if (startElement == null) {
				if (other.startElement != null) {
					return false;
				}
			} else if (!startElement.equals(other.startElement)) {
				return false;
			}
			return true;
		}

	}

	private String text = "someReference";

	private CopyOfReferenceSlotOld(boolean isExternal) {
		this.isExternal = isExternal;
	}

	public static CopyOfReferenceSlotOld getInternalInstance() {
		return new CopyOfReferenceSlotOld(false);
	}

	public static CopyOfReferenceSlotOld getExternalInstance() {
		CopyOfReferenceSlotOld externalReference = new CopyOfReferenceSlotOld(true);
		externalReference.text = "externalReference";
		return externalReference;
	}

	public static void initSpecialReferences() {
		thisReference = new CopyOfReferenceSlotOld(false);
		thisReference.text = "thisReference";

		nullReference = new CopyOfReferenceSlotOld(false);
		nullReference.text = "nullReference";
	}

	public static CopyOfReferenceSlotOld getThisReference() {
		return thisReference;
	}

	public static CopyOfReferenceSlotOld getNullReference() {
		return nullReference;
	}

	public void linkWithSubdependencies(CopyOfReferenceSlotOld target) {
		for (CopyOfReferenceSlotOld referenced : this.references) {
			target.references.add(referenced);
			referenced.referedBy.add(target);
		}
		this.linkReferences(target);
	}

	public void linkReferences(CopyOfReferenceSlotOld child) {
		if (this.references.add(child) | child.referedBy.add(this)) {
			Set<CopyOfReferenceSlotOld> rootElements = new HashSet<CopyOfReferenceSlotOld>();
			Set<CopyOfReferenceSlotOld> leafElements = new HashSet<CopyOfReferenceSlotOld>();
			Set<RefContainer> alreadyVisited = new HashSet<RefContainer>();
			somethingChanged = false;

			this.parentToChild(child);
			child.visitReferenceTree(rootElements, leafElements, alreadyVisited);
		}
	}

	private void visitReferenceTree(Set<CopyOfReferenceSlotOld> rootElements,
			Set<CopyOfReferenceSlotOld> leafElements, Set<RefContainer> alreadyVisited) {
		currentStartElement = this;
		this.bottomUp(rootElements, leafElements, alreadyVisited);
		do {
			somethingChanged = false;
			alreadyVisited.clear();
			for (CopyOfReferenceSlotOld rootElement : rootElements) {
				currentStartElement = rootElement;
				rootElement.topDown(rootElements, leafElements, alreadyVisited);
			}
			alreadyVisited.clear();
			for (CopyOfReferenceSlotOld leafElement : leafElements) {
				currentStartElement = leafElement;
				leafElement
						.bottomUp(rootElements, leafElements, alreadyVisited);
			}
		} while (somethingChanged);
	}

	private void bottomUp(Set<CopyOfReferenceSlotOld> rootElements,
			Set<CopyOfReferenceSlotOld> leafElements, Set<RefContainer> alreadyVisited) {
		if (this.referedBy.isEmpty()) {
			rootElements.add(this);
		} else {
			for (CopyOfReferenceSlotOld parent : this.referedBy) {
				if (alreadyVisited.add(new RefContainer(this, parent,
						currentStartElement)) && !this.equals(nullReference)) {
					parent.childToParent(this);
					parent.bottomUp(rootElements, leafElements, alreadyVisited);
				}
			}
		}
	}

	private void topDown(Set<CopyOfReferenceSlotOld> rootElements,
			Set<CopyOfReferenceSlotOld> leafElements, Set<RefContainer> alreadyVisited) {
		if (this.references.isEmpty()) {
			leafElements.add(this);
		} else {
			for (CopyOfReferenceSlotOld child : this.references) {
				if (alreadyVisited.add(new RefContainer(this, child,
						currentStartElement)) && !child.equals(nullReference)) {
					this.parentToChild(child);
					child.topDown(rootElements, leafElements, alreadyVisited);
				}
			}
		}
	}

	private void childToParent(CopyOfReferenceSlotOld child) {
		boolean newRefersThis = (this.refersThis || child.equals(thisReference) || child.refersThis);
		boolean newRefersExternal = (this.refersExternal || child.isExternal
				|| child.refersExternal || child.referedByExternal);
		boolean newRefersField = (this.refersField || child.refersField);
		if (!somethingChanged
				&& (newRefersThis != this.refersThis
						|| newRefersExternal != this.refersExternal || newRefersField != this.refersField)) {
			somethingChanged = true;
		}
		this.refersThis = newRefersThis;
		this.refersExternal = newRefersExternal;
		this.refersField = newRefersField;
	}

	private void parentToChild(CopyOfReferenceSlotOld child) {
		boolean newReferedByThis = child.referedByThis
				|| this.equals(CopyOfReferenceSlotOld.getThisReference())
				|| this.referedByThis;
		boolean newReferedByExternal = child.referedByExternal
				|| this.isExternal || this.referedByExternal;
		boolean newReferedByField = child.referedByField || this.referedByField;
		if (!somethingChanged
				&& (newReferedByThis != child.referedByThis
						|| newReferedByExternal != child.referedByExternal || newReferedByField != child.referedByField)) {
			somethingChanged = true;
		}
		child.referedByThis = newReferedByThis;
		child.referedByExternal = newReferedByExternal;
		child.referedByField = newReferedByField;
	}

	@Override
	public int getNumSlots() {
		return 1;
	}

	@Override
	public Slot copy() {
		// TODO COPY SET
		return null;
	}

	public CopyOfReferenceSlotOld copyWithoutDependencies() {
		CopyOfReferenceSlotOld copy = new CopyOfReferenceSlotOld(this.isExternal);
		copy.refersThis = this.refersThis;
		copy.referedByThis = this.referedByThis;
		copy.refersExternal = this.refersExternal;
		copy.refersField = this.refersField;
		copy.referedByField = this.referedByField;
		copy.text = this.text;
		return copy;
	}

	@Override
	public String toString() {
		return text;
	}

	/**
	 * @return the refersThis
	 */
	public boolean isRefersThis() {
		return refersThis;
	}

	/**
	 * @return the referedByThis
	 */
	public boolean isReferedByThis() {
		return referedByThis;
	}

	/**
	 * @return the refersExternal
	 */
	public boolean isRefersExternal() {
		return refersExternal;
	}

	/**
	 * @return the referedByExternal
	 */
	public boolean isReferedByExternal() {
		return referedByExternal;
	}

	/**
	 * @return the refersField
	 */
	public boolean isRefersField() {
		return refersField;
	}

	/**
	 * @param refersField
	 *            the refersField to set
	 */
	public void setRefersField(boolean refersField) {
		this.refersField = refersField;
	}

	/**
	 * @return the referedByField
	 */
	public boolean isReferedByField() {
		return referedByField;
	}

	/**
	 * @param referedByField
	 *            the referedByField to set
	 */
	public void setReferedByField(boolean referedByField) {
		this.referedByField = referedByField;
	}

	/**
	 * @return the isExternal
	 */
	public boolean isExternal() {
		return isExternal;
	}

	/**
	 * @return the referedBy
	 */
	public Set<CopyOfReferenceSlotOld> getReferedBy() {
		return referedBy;
	}

	/**
	 * @return the references
	 */
	public Set<CopyOfReferenceSlotOld> getReferences() {
		return references;
	}
}
