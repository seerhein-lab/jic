package de.seerhein_lab.jic.analyzer;

import static org.apache.bcel.Constants.CONSTRUCTOR_NAME;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.Type;

public final class ClassHelper {
	private final static String IMMUTABLE_SUFFIX = "/Immutable;";

	private final Method[] methods;
	private final JavaClass clazz;

	public ClassHelper(JavaClass clazz) {
		if (clazz == null)
			throw new NullPointerException("JavaClass must not be null.");

		this.clazz = clazz;
		this.methods = clazz.getMethods();
	}

	List<Method> getConstructors() {
		List<Method> ctors = new Vector<Method>();
		for (Method method : methods)
			if (method.getName().equals(CONSTRUCTOR_NAME))
				ctors.add(method);
		return ctors;
	}

	public boolean supposedlyImmutable() {
		for (AnnotationEntry annotation : clazz.getAnnotationEntries())
			if (annotation.getAnnotationType().endsWith(IMMUTABLE_SUFFIX))
				return true;
		return false;
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

	public static boolean isImmutable(Type type) {
		if (type instanceof ArrayType)
			return false;
		return isImmutable(type.toString());
	}

	public static boolean isImmutable(String type) {
		for (String immutableClass : immutableClasses) {
			if (type.toString().equals(immutableClass))
				return true;
		}
		try {
			JavaClass clazz = Repository.lookupClass(type);
			return new ClassHelper(clazz).supposedlyImmutable();

		} catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}
	}

	public static boolean isImmutableAndFinal(Type type) {
		if (type instanceof ArrayType)
			return false;
		JavaClass clazz = null;
		try {
			clazz = Repository.lookupClass(type.toString());
		} catch (ClassNotFoundException e) {
			Logger.getLogger("").info(e.getStackTrace().toString());
			throw new AssertionError(e);
		}
		return clazz.isFinal() && isImmutable(type);
	}

}
