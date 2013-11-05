package de.seerhein_lab.jic.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public final class AnalysisCache {
	public static class AnalyzedMethod {
		public static enum Check {
			PropCon;
		}

		private final JavaClass clazz;
		private final Method method;
		private final Check check;

		public AnalyzedMethod(JavaClass clazz, Method method, Check check) {
			this.clazz = clazz;
			this.method = method;
			this.check = check;
		}
	}

	private static class AnalysisResult {
	}

	private final Map<AnalyzedMethod, AnalysisResult> map = new ConcurrentHashMap<AnalyzedMethod, AnalysisResult>();

	public boolean contains(AnalyzedMethod method) {
		return map.containsKey(method);
	}

}
