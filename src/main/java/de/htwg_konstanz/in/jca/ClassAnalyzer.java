package de.htwg_konstanz.in.jca;

import static org.apache.bcel.Constants.CONSTRUCTOR_NAME;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;

public class ClassAnalyzer {
	private final JavaClass clazz;

	public ClassAnalyzer(JavaClass clazz) {
		this.clazz = clazz;
	}

	private List<Method> getConstructors() {
		List<Method> ctors = new Vector<Method>();
		Method[] methods = clazz.getMethods();
		for (Method method : methods)
			if (method.getName().equals(CONSTRUCTOR_NAME))
				ctors.add(method);
		return ctors;
	}

	/**
	 * return the constructor that is specified by its arguments.
	 * 
	 * @param types
	 *            arguments of the constructor to be returned
	 * @return
	 */
	public Method getConstructor(Type[] types) {
		Method[] methods = clazz.getMethods();

		for (Method method : methods) {
			Type[] methodTypes = method.getArgumentTypes();
			if (method.getName().equals("<init>")
					&& methodTypes.length == types.length)
				for (int i = 0; i < types.length; i++)
					if (!types[i].equals(methodTypes[i]))
						break;
			return method;
		}
		return null;
	}

	private BugCollection allFieldsFinal() {
		BugCollection bugs = new SortedBugCollection();
		Field[] fields = clazz.getFields();
		for (Field field : fields)
			if (!field.isFinal())
				bugs.add(new BugInstance("Error: field must be final", 2));
		return bugs;
	}

	public BugCollection properlyConstructed() {
		SortedBugCollection bugs = new SortedBugCollection();
		List<Method> ctors = getConstructors();

		for (Method ctor : ctors) {
			CtorAnalyzer ctorAnalyzer = new CtorAnalyzer(ctor);
			bugs.addAll(ctorAnalyzer.doesThisReferenceEscape().getCollection());
		}
		return bugs;
	}

	private Field[] getMutableFields() {
		return clazz.getFields();
	}

	private BugCollection ctorParamIsCopied(Field field) {
		return new SortedBugCollection();
	}

	private BugCollection fieldIsNotPublished(Field field) {
		return new SortedBugCollection();
	}

	public BugCollection stateUnmodifiable() {
		SortedBugCollection bugs = new SortedBugCollection();

		Field[] mutableFields = getMutableFields();
		for (Field mutableField : mutableFields) {
			bugs.addAll(ctorParamIsCopied(mutableField).getCollection());
			bugs.addAll(fieldIsNotPublished(mutableField).getCollection());
		}

		bugs.add(new BugInstance("Warning: state might be modifiable", 1));
		return bugs;
	}

	public BugCollection isImmutable() {
		SortedBugCollection bugs = new SortedBugCollection();
		bugs.addAll(allFieldsFinal().getCollection());
		bugs.addAll(properlyConstructed().getCollection());
		bugs.addAll(stateUnmodifiable().getCollection());
		return bugs;
	}

	public static ThreeValueBoolean indicatesSuccess(BugCollection bugs) {
		if (bugs.getCollection().isEmpty())
			return ThreeValueBoolean.yes;

		Iterator<BugInstance> iterator = bugs.getCollection().iterator();
		while (iterator.hasNext())
			if (iterator.next().getPriority() == 2)
				return ThreeValueBoolean.no;

		return ThreeValueBoolean.unknown;
	}

}
