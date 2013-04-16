package de.htwg_konstanz.in.jca.slot;

import java.util.HashSet;
import java.util.Set;

public class ReferenceSlot extends Slot {

	private static ReferenceSlot thisReference;
	private static ReferenceSlot nullReference;

	private static Set<ReferenceSlot> rootElements;
	private static Set<ReferenceSlot> leafElements;
	private static Set<RefContainer> alreadyVisited;
	private static boolean somethingChanged;
	private static ReferenceSlot currentStartElement;

	public static class RefContainer {
		private final ReferenceSlot caller;
		private final ReferenceSlot callee;
		private final ReferenceSlot startElement;

		public RefContainer(ReferenceSlot caller, ReferenceSlot callee,
				ReferenceSlot startElement) {
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

	private final boolean isExternal;

	private final Set<ReferenceSlot> referedBy = new HashSet<ReferenceSlot>();
	private final Set<ReferenceSlot> references = new HashSet<ReferenceSlot>();

	private boolean refersThis = false;
	private boolean referedByThis = false;
	private boolean refersExternal = false;
	private boolean referedByExternal = false;
	private boolean refersField = false;
	private boolean referedByField = false;

	private String text = "someReference";

	private ReferenceSlot(boolean isExternal) {
		this.isExternal = isExternal;
	}

	public static ReferenceSlot getInternalInstance() {
		return new ReferenceSlot(false);
	}

	public static ReferenceSlot getExternalInstance() {
		ReferenceSlot externalReference = new ReferenceSlot(true);
		externalReference.text = "externalReference";
		return externalReference;
	}

	public static void initSpecialReferences() {
		thisReference = new ReferenceSlot(false);
		thisReference.text = "thisReference";

		nullReference = new ReferenceSlot(false);
		nullReference.text = "nullReference";
	}

	public static ReferenceSlot getThisReference() {
		return thisReference;
	}

	public static ReferenceSlot getNullReference() {
		return nullReference;
	}

	public static void linkWithSubdependencies(ReferenceSlot parent,
			ReferenceSlot target) {
		for (ReferenceSlot referenced : parent.references) {
			target.references.add(referenced);
			referenced.referedBy.add(target);
		}
		linkReferences(parent, target);
	}

	public static void linkReferences(ReferenceSlot parent, ReferenceSlot child) {
		if (parent.references.add(child) | child.referedBy.add(parent)) {
			rootElements = new HashSet<ReferenceSlot>();
			leafElements = new HashSet<ReferenceSlot>();
			alreadyVisited = new HashSet<RefContainer>();
			somethingChanged = false;

			parentToChild(parent, child);
			visitReferenceTree(child);
		}
	}

	private static void visitReferenceTree(ReferenceSlot child) {
		bottomUp(child);
		do {
			somethingChanged = false;
			alreadyVisited.clear();
			for (ReferenceSlot rootElement : rootElements) {
				currentStartElement = rootElement;
				topDown(rootElement);
			}
			alreadyVisited.clear();
			for (ReferenceSlot leafElement : leafElements) {
				currentStartElement = leafElement;
				bottomUp(leafElement);
			}
		} while (somethingChanged);
	}

	private static void bottomUp(ReferenceSlot current) {
		if (current.referedBy.isEmpty()) {
			rootElements.add(current);
		} else {
			for (ReferenceSlot parent : current.referedBy) {
				if (alreadyVisited.add(new RefContainer(current, parent,
						currentStartElement)) && !current.equals(nullReference)) {
					childToParent(parent, current);
					bottomUp(parent);
				}
			}
		}
	}

	private static void topDown(ReferenceSlot current) {
		if (current.references.isEmpty()) {
			leafElements.add(current);
		} else {
			for (ReferenceSlot child : current.references) {
				if (alreadyVisited.add(new RefContainer(current, child,
						currentStartElement)) && !child.equals(nullReference)) {
					parentToChild(current, child);
					topDown(child);
				}
			}
		}
	}

	private static void childToParent(ReferenceSlot parent, ReferenceSlot child) {
		boolean newRefersThis = (parent.refersThis
				|| child.equals(thisReference) || child.refersThis);
		boolean newRefersExternal = (parent.refersExternal || child.isExternal
				|| child.refersExternal || child.referedByExternal);
		boolean newRefersField = (parent.refersField || child.refersField);
		if (!somethingChanged
				&& (newRefersThis != parent.refersThis
						|| newRefersExternal != parent.refersExternal || newRefersField != parent.refersField)) {
			somethingChanged = true;
		}
		parent.refersThis = newRefersThis;
		parent.refersExternal = newRefersExternal;
		parent.refersField = newRefersField;
	}

	private static void parentToChild(ReferenceSlot parent, ReferenceSlot child) {

		boolean newReferedByThis = child.referedByThis
				|| parent.equals(ReferenceSlot.getThisReference())
				|| parent.referedByThis;
		boolean newReferedByExternal = child.referedByExternal
				|| parent.isExternal || parent.referedByExternal;
		boolean newReferedByField = child.referedByField
				|| parent.referedByField;
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

	public ReferenceSlot copyWithoutDependencies() {
		ReferenceSlot copy = new ReferenceSlot(this.isExternal);
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
	public Set<ReferenceSlot> getReferedBy() {
		return referedBy;
	}

	/**
	 * @return the references
	 */
	public Set<ReferenceSlot> getReferences() {
		return references;
	}
}
