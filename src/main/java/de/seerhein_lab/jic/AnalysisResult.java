package de.seerhein_lab.jic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;

public class AnalysisResult {
	private final Set<EvaluationResult> results;
	private final Collection<BugInstance> bugs;

	public AnalysisResult(Set<EvaluationResult> results, Collection<BugInstance> bugs) {
		this.results = results;
		this.bugs = bugs;
	}

	public AnalysisResult() {
		this.results = new HashSet<EvaluationResult>();
		this.bugs = new SortedBugCollection().getCollection();
	}

	public Collection<BugInstance> getBugs() {
		return bugs;
	}

	public Set<EvaluationResult> getResults() {
		return results;
	}

}
