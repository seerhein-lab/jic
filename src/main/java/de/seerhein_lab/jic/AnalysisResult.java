package de.seerhein_lab.jic;

import java.util.Collection;
import java.util.Set;

import edu.umd.cs.findbugs.BugInstance;

public class AnalysisResult {
	private final Set<EvaluationResult> results;
	private final Collection<BugInstance> bugs;

	public AnalysisResult(Set<EvaluationResult> results, Collection<BugInstance> bugs) {
		this.results = results;
		this.bugs = bugs;
	}

	public Collection<BugInstance> getBugs() {
		return bugs;
	}

	public Set<EvaluationResult> getResults() {
		return results;
	}

}
