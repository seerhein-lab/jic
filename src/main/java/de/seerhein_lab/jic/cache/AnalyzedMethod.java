package de.seerhein_lab.jic.cache;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public class AnalyzedMethod {

	private final JavaClass clazz;
	private final Method method;

	public AnalyzedMethod(JavaClass clazz, Method method) {
		this.clazz = clazz;
		this.method = method;
	}

	public Method getMethod() {
		return method;
	}
}