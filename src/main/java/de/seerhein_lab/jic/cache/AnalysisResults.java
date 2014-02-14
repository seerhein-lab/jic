package de.seerhein_lab.jic.cache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.seerhein_lab.jic.EvaluationResult;
import de.seerhein_lab.jic.cache.AnalysisCache.Check;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.HeapObject;
import de.seerhein_lab.jic.vm.ReferenceSlot;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;

public class AnalysisResults {
	private Collection<BugInstance>[] bugs;
	private Set<EvaluationResult> results;

	@SuppressWarnings("unchecked")
	public AnalysisResults(Set<EvaluationResult> results, Slot target) {
		this.bugs = (Collection<BugInstance>[]) new Collection<?>[AnalysisCache.Check.values().length];
		this.results = new HashSet<EvaluationResult>();

		for (EvaluationResult result : results) {
			Heap cacheHeap = new Heap(); // TODO reuse ?

			HeapObject deepCopy = ((ReferenceSlot) target).getObject(result.getHeap()).deepCopy(
					cacheHeap);
			this.results.add(new EvaluationResult(result.getKind(), new ReferenceSlot(deepCopy),
					cacheHeap));
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

	public Set<EvaluationResult> getResults() {
		return results;
	}

	public boolean isCached(Check check) {
		if (check == null)
			return true;
		return bugs[check.ordinal()] != null;
	}
}