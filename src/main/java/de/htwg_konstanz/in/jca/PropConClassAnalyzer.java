package de.htwg_konstanz.in.jca;

import static org.apache.bcel.Constants.CONSTRUCTOR_NAME;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.ba.ClassContext;

public class PropConClassAnalyzer {
	private final JavaClass clazz;
	private final ClassContext classContext;

	public PropConClassAnalyzer(JavaClass clazz, ClassContext classContext) {
		this.clazz = clazz;
		this.classContext = classContext;
	}

	private List<Method> getConstructors() {
		List<Method> ctors = new Vector<Method>();
		Method[] methods = clazz.getMethods();
		for (Method method : methods)
			if (method.getName().equals(CONSTRUCTOR_NAME))
				ctors.add(method);
		return ctors;
	}

	public Method getMethod(String name, Type[] types) {
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			Type[] methodTypes = method.getArgumentTypes();
			if (method.getName().equals(name)
					&& methodTypes.length == types.length) {
				for (int i = 0; i < methodTypes.length; i++)
					if (!methodTypes[i].getSignature().equals(
							types[i].getSignature()))
						break;
				return method;
			}
		}
		return null;
	}

	public Method getMethodOld(String name, Type[] types) {
		Method[] methods = clazz.getMethods();
		boolean found = false;
		for (Method method : methods) {
			Type[] methodTypes = method.getArgumentTypes();
			if (method.getName().equals(name)
					&& methodTypes.length == types.length) {
				for (int i = 0; i < methodTypes.length; i++) {
					found = methodTypes[i].getSignature().equals(
							types[i].getSignature());
					if (!found)
						continue;
				}
				if (types.length == 0) {
					found = true;
				}
			}

			if (found)
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
			MethodGen ctorGen = new MethodGen(ctor, clazz.getClassName(),
					new ConstantPoolGen(clazz.getConstantPool()));

			// MethodGen ctorGen = new MethodGen(ctor,
			// "playground.PropConstTestClass", new ConstantPoolGen(
			// clazz.getConstantPool()));

			PropConMethodAnalyzer ctorAnalyzer = new PropConMethodAnalyzer(
					classContext, ctorGen, -1);
			ctorAnalyzer.analyze();
			bugs.addAll(ctorAnalyzer.getBugs().getCollection());
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
