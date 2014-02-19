package de.seerhein_lab.jic.analyzer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import net.jcip.annotations.ThreadSafe;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jic.AnalysisResult;
import de.seerhein_lab.jic.EvaluationResult;
import de.seerhein_lab.jic.Utils;
import de.seerhein_lab.jic.analyzer.ctorArgsCopied.CtorArgsCopiedAnalyzer;
import de.seerhein_lab.jic.analyzer.fieldsNotPublished.FieldsNotPublishedAnalyzer;
import de.seerhein_lab.jic.analyzer.noMutators.NoMutatorsAnalyzer;
import de.seerhein_lab.jic.analyzer.propCon.PropConAnalyzer;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.vm.Heap;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

// This class is thread safe because it follows the java monitor pattern
@ThreadSafe
public final class ClassAnalyzer {
	private final ClassContext classContext;
	private final JavaClass clazz;
	private final HashSet<Heap> heaps = new HashSet<Heap>();
	private final ClassHelper classHelper;
	private final AnalysisCache cache;
	protected static final Logger logger = Logger.getLogger("ClassAnalyzer");

	public ClassAnalyzer(ClassContext classContext, AnalysisCache cache) {
		if (classContext == null)
			throw new NullPointerException("ClassContext must not be null.");

		this.classContext = classContext;
		this.clazz = classContext.getJavaClass();
		classHelper = new ClassHelper(clazz);
		this.cache = cache;
	}

	private Collection<BugInstance> allFieldsFinal() {
		BugCollection bugs = new SortedBugCollection();
		Field[] fields = clazz.getFields();
		for (Field field : fields)
			if (!field.isStatic() && !field.isFinal())
				bugs.add(Utils.createBug("IMMUTABILITY_BUG", Confidence.HIGH,
						"All fields must be final.", clazz).addField(clazz.getClassName(),
						field.getName(), field.getSignature(), false));
		return bugs.getCollection();
	}

	private Collection<BugInstance> allReferenceFieldsPrivate() {
		BugCollection bugs = new SortedBugCollection();
		Field[] fields = clazz.getFields();
		for (Field field : fields)
			if (!field.isStatic() && !(field.getType() instanceof BasicType)
					&& !ClassHelper.isFinalAndAnnotedAsImmutable(field.getType().toString())
					&& !field.isPrivate())
				bugs.add(Utils.createBug("IMMUTABILITY_BUG", Confidence.HIGH,
						"Reference fields must be private.", clazz).addField(clazz.getClassName(),
						field.getName(), field.getSignature(), false));
		return bugs.getCollection();
	}

	public synchronized Collection<BugInstance> properlyConstructed() {
		SortedBugCollection bugs = new SortedBugCollection();

		for (Method ctor : classHelper.getConstructors()) {
			MethodGen ctorGen = new MethodGen(ctor, clazz.getClassName(), new ConstantPoolGen(
					clazz.getConstantPool()));

			BaseMethodAnalyzer ctorAnalyzer = new PropConAnalyzer(classContext, ctorGen, cache, 0);
			// ctorAnalyzer.analyze();
			// bugs.addAll(ctorAnalyzer.getBugs());

			bugs.addAll(ctorAnalyzer.analyze().getBugs());
		}
		return bugs.getCollection();
	}

	// package private for testing purposes
	Collection<BugInstance> ctorArgsAreCopied() {
		SortedBugCollection bugs = new SortedBugCollection();

		for (Method ctor : classHelper.getConstructors()) {
			MethodGen ctorGen = new MethodGen(ctor, clazz.getClassName(), new ConstantPoolGen(
					clazz.getConstantPool()));

			BaseMethodAnalyzer ctorAnalyzer = new CtorArgsCopiedAnalyzer(classContext, ctorGen,
					cache, 0);
			// ctorAnalyzer.analyze();
			// Collection<BugInstance> currentBugs = ctorAnalyzer.getBugs();
			// bugs.addAll(ctorAnalyzer.getBugs());

			AnalysisResult analysisResult = ctorAnalyzer.analyze();
			bugs.addAll(analysisResult.getBugs());

			if (analysisResult.getBugs().isEmpty()) {
				for (EvaluationResult result : analysisResult.getResults())
					heaps.add(result.getHeap());
			}
		}
		return bugs.getCollection();
	}

	// package private for testing purposes
	Collection<BugInstance> fieldsAreNotPublished() {
		SortedBugCollection bugs = new SortedBugCollection();

		for (Method method : classHelper.getConcreteNonPrivateNonStaticMethods()) {
			if (method.isNative()) {
				bugs.add(Utils.createBug("IMMUTABILITY_BUG", Confidence.MEDIUM,
						"Native method might publish reference fields of 'this' object",
						classContext.getJavaClass()));
				continue;
			}

			for (Heap heap : heaps) {
				MethodGen methodGen = new MethodGen(method, clazz.getClassName(),
						new ConstantPoolGen(clazz.getConstantPool()));

				BaseMethodAnalyzer methodAnalyzer = new FieldsNotPublishedAnalyzer(classContext,
						methodGen, new Heap(heap), cache, 0);
				bugs.addAll(methodAnalyzer.analyze().getBugs());
			}
		}
		return bugs.getCollection();
	}

	// package private for testing purposes
	Collection<BugInstance> noMutators() {
		SortedBugCollection bugs = new SortedBugCollection();

		for (Method method : classHelper.getConcreteNonPrivateNonStaticMethods()) {
			if (method.isNative()) {
				bugs.add(Utils.createBug("IMMUTABILITY_BUG", Confidence.MEDIUM,
						"Native method might modify 'this' object", classContext.getJavaClass()));
				continue;
			}

			for (Heap heap : heaps) {
				MethodGen methodGen = new MethodGen(method, clazz.getClassName(),
						new ConstantPoolGen(clazz.getConstantPool()));

				BaseMethodAnalyzer methodAnalyzer = new NoMutatorsAnalyzer(classContext, methodGen,
						new Heap(heap), cache, 0);
				bugs.addAll(methodAnalyzer.analyze().getBugs());
			}
		}
		return bugs.getCollection();
	}

	private Collection<BugInstance> stateUnmodifiable() {
		SortedBugCollection bugs = new SortedBugCollection();
		bugs.addAll(ctorArgsAreCopied());
		bugs.addAll(allReferenceFieldsPrivate());
		bugs.addAll(noMutators());
		bugs.addAll(fieldsAreNotPublished());
		return bugs.getCollection();
	}

	public synchronized Collection<BugInstance> isImmutable() {
		SortedBugCollection bugs = new SortedBugCollection();

		if (clazz.getClassName().equals("java.lang.Object"))
			return bugs.getCollection();

		ClassContext superContext;
		try {
			final JavaClass superClass = clazz.getSuperClass();
			logger.info("class: " + clazz.getClassName() + ", super class: "
					+ superClass.getClassName());

			superContext = mock(ClassContext.class);
			when(superContext.getJavaClass()).thenReturn(superClass);

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		ClassAnalyzer superAnalyzer = new ClassAnalyzer(superContext, new AnalysisCache());

		Collection<BugInstance> superBugs = superAnalyzer.isImmutable();
		if (!superBugs.isEmpty())
			bugs.add(Utils.createBug("IMMUTABILITY_BUG", Confidence.HIGH,
					"mutable superclass renders this class mutable, too.",
					classContext.getJavaClass()));

		bugs.addAll(allFieldsFinal());
		bugs.addAll(properlyConstructed());
		bugs.addAll(stateUnmodifiable());
		return bugs.getCollection();
	}
}
