package de.htwg_konstanz.in.jca.analyzer;

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

import de.htwg_konstanz.in.jca.ThreeValueBoolean;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.ba.ClassContext;

public class ClassAnalyzer {
	private final JavaClass clazz;
	private final ClassContext classContext;

	public ClassAnalyzer(JavaClass clazz, ClassContext classContext) {
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

			BaseMethodAnalyzer ctorAnalyzer = new PropConMethodAnalyzer(
					classContext, ctorGen);
			ctorAnalyzer.analyze();
			bugs.addAll(ctorAnalyzer.getBugs().getCollection());
		}
		return bugs;
	}

	private List<Method> getAllMethodsWithoutCtors() {
		List<Method> methods = new Vector<Method>();
		Method[] allMethods = clazz.getMethods();
		for (Method method : allMethods)
			if (!method.getName().equals(CONSTRUCTOR_NAME))
				methods.add(method);
		return methods;
	}

	// TODO set private when testing is complete
	public SortedBugCollection ctorParamsAreCopied() {
		SortedBugCollection bugs = new SortedBugCollection();
		List<Method> ctors = getConstructors();

		for (Method ctor : ctors) {
			MethodGen ctorGen = new MethodGen(ctor, clazz.getClassName(),
					new ConstantPoolGen(clazz.getConstantPool()));

			BaseMethodAnalyzer ctorAnalyzer = new StateUnmodCtorAnalyzer(
					classContext, ctorGen);
			ctorAnalyzer.analyze();
			bugs.addAll(ctorAnalyzer.getBugs().getCollection());
		}
		return bugs;
	}

	// TODO set private when testing is complete
	public BugCollection fieldsAreNotPublished() {
		SortedBugCollection bugs = new SortedBugCollection();
		List<Method> methods = getAllMethodsWithoutCtors();

		for (Method method : methods) {
			MethodGen methodGen = new MethodGen(method, clazz.getClassName(),
					new ConstantPoolGen(clazz.getConstantPool()));

			BaseMethodAnalyzer methodAnalyzer = new StateUnmodRefPublishedMethodAnalyzer(
					classContext, methodGen);
			methodAnalyzer.analyze();
			bugs.addAll(methodAnalyzer.getBugs().getCollection());
		}
		return bugs;
	}

	// TODO set private when testing is complete
	public BugCollection stateUnmodified() {
		SortedBugCollection bugs = new SortedBugCollection();
		List<Method> methods = getAllMethodsWithoutCtors();

		for (Method method : methods) {
			MethodGen methodGen = new MethodGen(method, clazz.getClassName(),
					new ConstantPoolGen(clazz.getConstantPool()));

			BaseMethodAnalyzer methodAnalyzer = new StateUnmodFieldsModifyMethodAnalyzer(
					classContext, methodGen);
			methodAnalyzer.analyze();
			bugs.addAll(methodAnalyzer.getBugs().getCollection());
		}
		return bugs;
	}

	public BugCollection stateUnmodifiable() {
		SortedBugCollection bugs = new SortedBugCollection();

		bugs.addAll(ctorParamsAreCopied().getCollection());
		bugs.addAll(stateUnmodified().getCollection());
		bugs.addAll(fieldsAreNotPublished().getCollection());

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