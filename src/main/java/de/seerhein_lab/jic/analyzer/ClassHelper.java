package de.seerhein_lab.jic.analyzer;

import static org.apache.bcel.Constants.CONSTRUCTOR_NAME;

import java.util.List;
import java.util.Vector;

import net.jcip.annotations.Immutable;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

public final class ClassHelper {
	private final static String IMMUTABLE_ANNOTATION = "Lnet/jcip/annotations/Immutable;";
	private final Method[] methods;

	public ClassHelper(JavaClass clazz) {
		if (clazz == null)
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

	List<Method> getConcreteNonPrivateNonStaticMethods() {
		List<Method> methodsButCtors = new Vector<Method>();
		for (Method method : methods)
			if (!method.getName().equals(CONSTRUCTOR_NAME) && !method.isPrivate()
					&& !method.isStatic() & !method.isAbstract())
				methodsButCtors.add(method);
		return methodsButCtors;
	}

	Method getMethod(String name, Type[] types) {
		for (Method method : methods) {
			boolean different = false;
			Type[] methodTypes = method.getArgumentTypes();
			if (method.getName().equals(name) && methodTypes.length == types.length) {
				for (int i = 0; i < methodTypes.length; i++) {
					if (!methodTypes[i].getSignature().equals(types[i].getSignature())) {
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

	private static String[] immutableClasses = { "java.lang.String", "java.lang.Byte",
			"java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Double",
			"java.lang.Float", "java.lang.Boolean", "java.lang.Character", "byte", "short", "int",
			"long", "float", "boolean", "char" };

	public static boolean isAnnotedAsImmutable(String className) {
		className = className.replace("[]", "");
		for (String immutableClass : immutableClasses) {
			if (className.equals(immutableClass))
				return true;
		}
		try {
			JavaClass clazz = Repository.lookupClass(className);
			for (AnnotationEntry entry : clazz.getAnnotationEntries()) {
				if (entry.getAnnotationType().equals(IMMUTABLE_ANNOTATION))
					return true;
			}
		} catch (ClassNotFoundException e) {
		} catch (IllegalArgumentException e) {
		}
		return false;
	}

	public static boolean isFinalAndAnnotedAsImmutable(String className) {
		JavaClass clazz = null;
		try {
			clazz = Repository.lookupClass(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return clazz.isFinal() && isAnnotedAsImmutable(className);
	}

}
