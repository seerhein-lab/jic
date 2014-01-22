package de.seerhein_lab.jic.cache;

import static org.apache.bcel.Constants.CONSTRUCTOR_NAME;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.Type;

import de.seerhein_lab.jic.analyzer.QualifiedMethod;

public final class AnalysisCache {
	public static enum Check {
		PropCon, CtorArgsCopied;
	}

	private final Map<QualifiedMethod, AnalysisResults> map = new ConcurrentHashMap<QualifiedMethod, AnalysisResults>();

	public boolean contains(QualifiedMethod method) {
		// if (method.getMethod().getName().equals(CONSTRUCTOR_NAME)
		// && method.getMethod().getArgumentTypes().length == 0) {
		return map.containsKey(method);
		// }
		// return false;

	}

	public AnalysisResults get(QualifiedMethod method) {
		return map.get(method);
	}

	public void add(QualifiedMethod method, AnalysisResults result, Check check) {
		if (!map.containsKey(method)) {
			map.put(method, result);
		} else {
			map.get(method).setBugs(check, result.getBugs(check));
		}

	}

	public boolean isCacheable(QualifiedMethod targetMethod) {
		if (!targetMethod.getMethod().getName().equals(CONSTRUCTOR_NAME))
			return false;

		for (Type typ : targetMethod.getMethod().getArgumentTypes()) {
			if (!(typ instanceof BasicType)) {
				return false;
			}
		}
		return true;
	}
}
