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

	@Override
	public String toString() {
		return clazz.getClassName() + method.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AnalyzedMethod)) {
			return false;
		}
		AnalyzedMethod other = (AnalyzedMethod) obj;
		if (clazz == null) {
			if (other.clazz != null) {
				return false;
			}
		} else if (!clazz.equals(other.clazz)) {
			return false;
		}
		if (method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!method.equals(other.method)) {
			return false;
		}
		return true;
	}
}