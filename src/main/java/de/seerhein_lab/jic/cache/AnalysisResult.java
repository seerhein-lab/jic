package de.seerhein_lab.jic.cache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.seerhein_lab.jic.ResultValue;
import de.seerhein_lab.jic.cache.AnalysisCache.Check;
import de.seerhein_lab.jic.slot.ReferenceSlot;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.HeapObject;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;

public class AnalysisResult {
	private Collection<BugInstance>[] bugs;
	private Set<ResultValue> results;

	@SuppressWarnings("unchecked")
	public AnalysisResult(Set<ResultValue> results, Slot target) {
		this.bugs = (Collection<BugInstance>[]) new Collection<?>[AnalysisCache.Check.values().length];
		this.results = new HashSet<ResultValue>();

		for (ResultValue result : results) {
			Heap cacheHeap = new Heap(); // TODO reuse ?

			HeapObject deepCopy = result.getHeap().getObject(((ReferenceSlot) target))
					.deepCopy(cacheHeap);
			this.results.add(new ResultValue(result.getKind(), ReferenceSlot
					.createNewInstance(deepCopy), cacheHeap));
		}
	}

	public Collection<BugInstance> getBugs(Check check) {
		if (check == null)
			return new SortedBugCollection().getCollection();
		return bugs[check.ordinal()];
	}

	public void setBugs(Check check, Collection<BugInstance> bugs) {
		if (check != null)
			this.bugs[check.ordinal()] = bugs;
	}

	public Set<ResultValue> getResults() {
		return results;
	}

	public boolean isCached(Check check) {
		if (check == null)
			return true;
		return bugs[check.ordinal()] != null;
	}
}