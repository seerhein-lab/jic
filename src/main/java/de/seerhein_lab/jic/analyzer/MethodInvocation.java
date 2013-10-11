package de.seerhein_lab.jic.analyzer;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public final class MethodInvocation {
	private final JavaClass clazz;
	private final Method method;

	public MethodInvocation(JavaClass clazz, Method method) {
		if ( clazz == null || method == null ) {
			throw new NullPointerException("params must not be null");
		}
		
		this.clazz = clazz;
		this.method = method;
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
		if (this == obj)
			return true;
		if (!(obj instanceof MethodInvocation))
			return false;
		
		MethodInvocation other = (MethodInvocation) obj;
		
		if (!clazz.equals(other.clazz))
			return false;
		
		return method.equals(other.method);
	}


	
	
}
