package de.seerhein_lab.jic.cache;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public class AnalyzedMethod {
	public static enum Check {
		PropCon;
	}

	private final JavaClass clazz;
	private final Method method;
	private final AnalyzedMethod.Check check;

	public AnalyzedMethod(JavaClass clazz, Method method, AnalyzedMethod.Check check) {
		this.clazz = clazz;
		this.method = method;
		this.check = check;
	}
}