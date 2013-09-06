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
import de.seerhein_lab.jca.analyzer.ctorArgsCopied.CtorArgsCopiedAnalyzer;
import de.seerhein_lab.jca.analyzer.fieldsNotPublished.FieldsNotPublishedMethodAnalyzer;
import de.seerhein_lab.jca.analyzer.noMutators.NoMutatorsMethodAnalyzer;
import de.seerhein_lab.jca.analyzer.propCon.PropConMethodAnalyzer;
import de.seerhein_lab.jca.heap.Heap;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;


public final class ClassAnalyzer {
	private final ClassContext classContext;
	private final JavaClass clazz;
	private final HashSet<Heap> heaps = new HashSet<Heap>();
	private final ClassHelper classHelper;

	public ClassAnalyzer(ClassContext classContext) {
		if ( classContext == null ) 
			throw new NullPointerException("ClassContext must not be null.");
		
		this.classContext = classContext;
		this.clazz = classContext.getJavaClass();
		classHelper = new ClassHelper(clazz);
	}

	private BugCollection allFieldsFinal() {
		BugCollection bugs = new SortedBugCollection();
		Field[] fields = clazz.getFields();
		for (Field field : fields)
			if (!field.isStatic() && !field.isFinal())
				bugs.add(new BugInstance("IMMUTABILITY_BUG", Confidence.HIGH
						.getConfidenceValue())
						.addString("All fields must be final.")
						.addClass(clazz)
						.addField(clazz.getClassName(),
								field.getName(), field.getSignature(), false));
		return bugs;
	}

	private BugCollection allReferenceFieldsPrivate() {
		BugCollection bugs = new SortedBugCollection();
		Field[] fields = clazz.getFields();
		for (Field field : fields)
			if (!field.isStatic() && !(field.getType() instanceof BasicType)
					&& !field.isPrivate())
				bugs.add(new BugInstance("IMMUTABILITY_BUG", Confidence.HIGH
						.getConfidenceValue())
						.addString("Reference fields must be private.")
						.addClass(clazz)
						.addField(clazz.getClassName(),
								field.getName(), field.getSignature(), false));
		return bugs;
	}

	public BugCollection properlyConstructed() {
		SortedBugCollection bugs = new SortedBugCollection();
		List<Method> ctors = classHelper.getConstructors();

		for (Method ctor : ctors) {
			MethodGen ctorGen = new MethodGen(ctor, clazz
					.getClassName(), new ConstantPoolGen(clazz.getConstantPool()));

			BaseMethodAnalyzer ctorAnalyzer = new PropConMethodAnalyzer(
					classContext, ctorGen);
			ctorAnalyzer.analyze();
			bugs.addAll(ctorAnalyzer.getBugs().getCollection());
		}
		return bugs;
	}

	// package private for testing purposes
	SortedBugCollection ctorArgsAreCopied() {
		SortedBugCollection bugs = new SortedBugCollection();
		List<Method> ctors = classHelper.getConstructors();

		for (Method ctor : ctors) {
			MethodGen ctorGen = new MethodGen(ctor, clazz
					.getClassName(), new ConstantPoolGen(clazz.getConstantPool()));

			BaseMethodAnalyzer ctorAnalyzer = new CtorArgsCopiedAnalyzer(
					classContext, ctorGen);
			ctorAnalyzer.analyze();
			Collection<BugInstance> currentBugs = ctorAnalyzer.getBugs()
					.getCollection();
			bugs.addAll(ctorAnalyzer.getBugs().getCollection());

			if (currentBugs.isEmpty()) {
				for (ResultValue result : ctorAnalyzer.getResult())
					heaps.add(result.getHeap());
			}
		}
		return bugs;
	}

	// package private for testing purposes
	BugCollection fieldsAreNotPublished() {
		SortedBugCollection bugs = new SortedBugCollection();
		List<Method> methods = classHelper.getAllMethodsButCtors();
		if (heaps.isEmpty())
			return bugs;

		for (Method method : methods) {
			MethodGen methodGen = new MethodGen(method, clazz.getClassName(), new ConstantPoolGen(
					clazz.getConstantPool()));

			BaseMethodAnalyzer methodAnalyzer = new FieldsNotPublishedMethodAnalyzer(
					classContext, methodGen, getCopiedHeaps());
			methodAnalyzer.analyze();
			bugs.addAll(methodAnalyzer.getBugs().getCollection());
		}
		return bugs;
	}

	// package private for testing purposes
	BugCollection noMutators() {
		SortedBugCollection bugs = new SortedBugCollection();
		List<Method> methods = classHelper.getAllMethodsButCtors();
		if (heaps.isEmpty())
			return bugs;

		for (Method method : methods) {
			MethodGen methodGen = new MethodGen(method, clazz.getClassName(), new ConstantPoolGen(
					clazz.getConstantPool()));

			BaseMethodAnalyzer methodAnalyzer = new NoMutatorsMethodAnalyzer(
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

	private BugCollection stateUnmodifiable() {
		SortedBugCollection bugs = new SortedBugCollection();
		bugs.addAll(ctorArgsAreCopied().getCollection());
		bugs.addAll(allReferenceFieldsPrivate().getCollection());
		bugs.addAll(noMutators().getCollection());
		bugs.addAll(fieldsAreNotPublished().getCollection());
		return bugs;
	}

	public BugCollection isImmutable() {
		SortedBugCollection bugs = new SortedBugCollection();
		bugs.addAll(allFieldsFinal().getCollection());
		bugs.addAll(properlyConstructed().getCollection());
		bugs.addAll(stateUnmodifiable().getCollection());
		return bugs;
	}

}
