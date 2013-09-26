package de.seerhein_lab.jca.analyzer;

import static org.apache.bcel.Constants.CONSTRUCTOR_NAME;

import java.util.List;
import java.util.Vector;

import net.jcip.annotations.Immutable;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

@Immutable
public final class ClassHelper {
	private final Method[] methods; 

	public ClassHelper(JavaClass clazz) {
		if ( clazz == null ) 
			throw new NullPointerException("JavaClass must not be null.");
		
		this.methods = clazz.getMethods();
	}

	List<Method> getConstructors() {
		List<Method> ctors = new Vector<Method>();
		for (Method method : methods)
			if (method.getName().equals(CONSTRUCTOR_NAME))
				ctors.add(method);
		return ctors;
	}
	
	List<Method> getNonPrivateNonStaticMethods() {
		List<Method> methodsButCtors = new Vector<Method>();
		for (Method method : methods)
			if (!method.getName().equals(CONSTRUCTOR_NAME) && !method.isPrivate() && !method.isStatic())
				methodsButCtors.add(method);
		return methodsButCtors;
	}

	Method getMethod(String name, Type[] types) {
		for (Method method : methods) {
			boolean different = false;
			Type[] methodTypes = method.getArgumentTypes();
			if (method.getName().equals(name)
					&& methodTypes.length == types.length) {
				for (int i = 0; i < methodTypes.length; i++) {
					if (!methodTypes[i].getSignature().equals(
							types[i].getSignature())) {
						different = true;
						break;
					}
				}
				if (!different)
					return method;
			}
		}
		return null;
	}


}
