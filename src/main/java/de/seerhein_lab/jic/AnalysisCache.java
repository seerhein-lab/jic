package de.seerhein_lab.jic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AnalysisCache {
	private static class AnalyzedMethod {
	}

	private static class AnalysisResult {
	}

	private final Map<AnalyzedMethod, AnalysisResult> map = new ConcurrentHashMap<AnalyzedMethod, AnalysisResult>();

}
