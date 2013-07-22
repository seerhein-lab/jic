package de.seerhein_lab.jca.analyzer;

import static org.apache.bcel.Constants.CONSTRUCTOR_NAME;

import java.util.List;
import java.util.Vector;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;


public class ClassHelper {
	private final JavaClass clazz;

	public ClassHelper(JavaClass clazz) {
		this.clazz = clazz;
	}

	List<Method> getConstructors() {
		List<Method> ctors = new Vector<Method>();
		Method[] methods = clazz.getMethods();
		for (Method method : methods)
			if (method.getName().equals(CONSTRUCTOR_NAME))
				ctors.add(method);
		return ctors;
	}
	
	List<Method> getAllMethodsButCtors() {
		List<Method> methods = new Vector<Method>();
		Method[] allMethods = clazz.getMethods();
		for (Method method : allMethods)
			if (!method.getName().equals(CONSTRUCTOR_NAME))
				methods.add(method);
		return methods;
	}

	Method getMethod(String name, Type[] types) {
		Method[] methods = clazz.getMethods();
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
