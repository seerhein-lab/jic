package de.seerhein_lab.jic.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class AnalysisCache {
	private final Map<AnalyzedMethod, AnalysisResult> map = new ConcurrentHashMap<AnalyzedMethod, AnalysisResult>();

	public boolean contains(AnalyzedMethod method) {
		return map.containsKey(method);
	}

}
