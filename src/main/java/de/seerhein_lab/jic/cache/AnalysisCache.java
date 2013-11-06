package de.seerhein_lab.jic.cache;

import static org.apache.bcel.Constants.CONSTRUCTOR_NAME;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AnalysisCache {
	public static enum Check {
		PropCon, CtorArgsCopied;
	}

	private final Map<AnalyzedMethod, AnalysisResult> map = new ConcurrentHashMap<AnalyzedMethod, AnalysisResult>();

	public boolean contains(AnalyzedMethod method) {
		if (method.getMethod().getName().equals(CONSTRUCTOR_NAME)
				&& method.getMethod().getArgumentTypes().length == 0) {
			return map.containsKey(method);
		}
		return false;
	}

	public AnalysisResult get(AnalyzedMethod method) {
		return map.get(method);
	}

	public void add(AnalyzedMethod method, AnalysisResult result, Check check) {
		if (!map.containsKey(method)) {
			map.put(method, result);
		} else {
			map.get(method).setBugs(check, result.getBugs(check));
		}

	}
}
