package de.seerhein_lab.jic.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.seerhein_lab.jic.analyzer.QualifiedMethod;

public final class AnalysisCache {
	public static enum Check {
		PropCon, CtorArgsCopied;
	}

	private final Map<QualifiedMethod, AnalysisResult> map = new ConcurrentHashMap<QualifiedMethod, AnalysisResult>();

	public boolean contains(QualifiedMethod method) {
		// if (method.getMethod().getName().equals(CONSTRUCTOR_NAME)
		// && method.getMethod().getArgumentTypes().length == 0) {
		return map.containsKey(method);
		// }
		// return false;

	}

	public AnalysisResult get(QualifiedMethod method) {
		return map.get(method);
	}

	public void add(QualifiedMethod method, AnalysisResult result, Check check) {
		if (!map.containsKey(method)) {
			map.put(method, result);
		} else {
			map.get(method).setBugs(check, result.getBugs(check));
		}

	}
}
