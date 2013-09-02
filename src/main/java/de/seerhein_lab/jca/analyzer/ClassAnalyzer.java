package de.seerhein_lab.jca.analyzer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jca.ResultValue;
import de.seerhein_lab.jca.analyzer.ctorArgsCopiedAnalyzer.CtorArgsCopiedAnalyzer;
import de.seerhein_lab.jca.analyzer.fieldsNotModifiedAnalyzer.FieldsNotModifiedMethodAnalyzer;
import de.seerhein_lab.jca.analyzer.fieldsNotPublishedAnalyzer.FieldsNotPublishedMethodAnalyzer;
import de.seerhein_lab.jca.analyzer.propConAnalyzer.PropConMethodAnalyzer;
import de.seerhein_lab.jca.heap.Heap;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.ba.ClassContext;

public class ClassAnalyzer {
	private final ClassContext classContext;
	private final HashSet<Heap> heaps;
	private final ClassHelper classHelper;

	public ClassAnalyzer(ClassContext classContext) {
		this.classContext = classContext;
		heaps = new HashSet<Heap>();
		classHelper = new ClassHelper(classContext.getJavaClass());
	}

	private BugCollection allFieldsFinal() {
		BugCollection bugs = new SortedBugCollection();
		Field[] fields = classContext.getJavaClass().getFields();
		for (Field field : fields)
			if (!field.isStatic() && !field.isFinal()) {
				BugInstance bugInstance = new BugInstance("IMMUTABILITY_BUG", 2);
				bugInstance.addString("All fields must be final.");
				bugInstance.addClass(classContext.getJavaClass()); // TODO Bug handling
				bugInstance.addField(classContext.getJavaClass().getClassName(), field.getName(),
						field.getSignature(), false);
				bugs.add(bugInstance);
			}
		return bugs;
	}

	private BugCollection allReferenceFieldsPrivate() {
		BugCollection bugs = new SortedBugCollection();
		Field[] fields = classContext.getJavaClass().getFields();
		for (Field field : fields)
			if (!field.isStatic() && !(field.getType() instanceof BasicType)
					&& !field.isPrivate()) {
				BugInstance bugInstance = new BugInstance("IMMUTABILITY_BUG", 2);
				bugInstance.addString("Reference fields must be private.");
				bugInstance.addClass(classContext.getJavaClass()); // TODO Bug handling
				bugInstance.addField(classContext.getJavaClass().getClassName(), field.getName(),
						field.getSignature(), false);
				bugs.add(bugInstance);
			}
		return bugs;
	}

	public BugCollection properlyConstructed() {
		SortedBugCollection bugs = new SortedBugCollection();
		List<Method> ctors = classHelper.getConstructors();

		for (Method ctor : ctors) {
			MethodGen ctorGen = new MethodGen(ctor, classContext.getJavaClass().getClassName(),
					new ConstantPoolGen(classContext.getJavaClass().getConstantPool()));

			BaseMethodAnalyzer ctorAnalyzer = new PropConMethodAnalyzer(
					classContext, ctorGen);
			ctorAnalyzer.analyze();
			Collection<BugInstance> currentBugs = ctorAnalyzer.getBugs()
					.getCollection();
			bugs.addAll(currentBugs);
		}
		return bugs;
	}

	// TODO set private when testing is complete
	public SortedBugCollection ctorParamsAreCopied() {
		SortedBugCollection bugs = new SortedBugCollection();
		List<Method> ctors = classHelper.getConstructors();

		for (Method ctor : ctors) {
			MethodGen ctorGen = new MethodGen(ctor, classContext.getJavaClass().getClassName(),
					new ConstantPoolGen(classContext.getJavaClass().getConstantPool()));

			BaseMethodAnalyzer ctorAnalyzer = new CtorArgsCopiedAnalyzer(
					classContext, ctorGen);
			ctorAnalyzer.analyze();
			Collection<BugInstance> currentBugs = ctorAnalyzer.getBugs()
					.getCollection();
			bugs.addAll(currentBugs);

			if (currentBugs.isEmpty()) {
				for (ResultValue result : ctorAnalyzer.getResult())
					heaps.add(result.getHeap());
			}
		}
		return bugs;
	}

	// TODO set private when testing is complete
	public BugCollection fieldsAreNotPublished() {
		SortedBugCollection bugs = new SortedBugCollection();
		List<Method> methods = classHelper.getAllMethodsButCtors();
		if (heaps.isEmpty())
			return bugs;

		for (Method method : methods) {
			MethodGen methodGen = new MethodGen(method, classContext.getJavaClass().getClassName(),
					new ConstantPoolGen(classContext.getJavaClass().getConstantPool()));

			BaseMethodAnalyzer methodAnalyzer = new FieldsNotPublishedMethodAnalyzer(
					classContext, methodGen, getCopiedHeaps());
			methodAnalyzer.analyze();
			bugs.addAll(methodAnalyzer.getBugs().getCollection());
		}
		return bugs;
	}

	// TODO set private when testing is complete
	public BugCollection stateUnmodified() {
		SortedBugCollection bugs = new SortedBugCollection();
		List<Method> methods = classHelper.getAllMethodsButCtors();
		if (heaps.isEmpty())
			return bugs;

		for (Method method : methods) {
			MethodGen methodGen = new MethodGen(method, classContext.getJavaClass().getClassName(),
					new ConstantPoolGen(classContext.getJavaClass().getConstantPool()));

			BaseMethodAnalyzer methodAnalyzer = new FieldsNotModifiedMethodAnalyzer(
					classContext, methodGen, getCopiedHeaps());
			methodAnalyzer.analyze();
			bugs.addAll(methodAnalyzer.getBugs().getCollection());
		}
		return bugs;
	}

	private Set<Heap> getCopiedHeaps() {
		HashSet<Heap> copiedHeaps = new HashSet<Heap>();
		for (Heap heap : heaps) {
			copiedHeaps.add(new Heap(heap));
		}
		return copiedHeaps;
	}

	public BugCollection stateUnmodifiable() {
		SortedBugCollection bugs = new SortedBugCollection();

		bugs.addAll(ctorParamsAreCopied().getCollection());
		bugs.addAll(stateUnmodified().getCollection());
		bugs.addAll(fieldsAreNotPublished().getCollection());

		return bugs;
	}

	public BugCollection isImmutable() {
		SortedBugCollection bugs = new SortedBugCollection();
		bugs.addAll(allFieldsFinal().getCollection());
		bugs.addAll(allReferenceFieldsPrivate().getCollection());
		bugs.addAll(properlyConstructed().getCollection());
		bugs.addAll(stateUnmodifiable().getCollection());
		return bugs;
	}

}
