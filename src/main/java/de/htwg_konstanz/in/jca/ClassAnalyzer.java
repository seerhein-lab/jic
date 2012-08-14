package de.htwg_konstanz.in.jca;

import java.util.List;
import java.util.Vector;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;


public class ClassAnalyzer {    
    private final JavaClass clazz;
        
    public ClassAnalyzer(JavaClass clazz) {
	this.clazz = clazz;	
    }
    
    private List<Method> getConstructors() {
	List<Method> ctors = new Vector<Method>();
	Method[] methods = clazz.getMethods();
	for ( Method method : methods ) 
	    if ( method.getName().equals("<init>") ) 
		ctors.add(method);
	return ctors;	
    }
    
    public Method getConstructor(Type[] types) {
	Method[] methods = clazz.getMethods();	
	
	for ( Method method : methods ) {  
	    Type[] methodTypes = method.getArgumentTypes();
	    if ( method.getName().equals("<init>") && methodTypes.length == types.length ) 
		for ( int i = 0; i < types.length; i++ ) 
		    if ( !types[i].equals(methodTypes[i])) 
			break;
		return method;
	}
	return null;
    }
    
    private boolean allFieldsFinal() {
	Field[] fields = clazz.getFields();
	for ( Field field : fields ) 
	    if ( !field.isFinal() ) 
		return false;
	return true;
    }
    
    public ThreeValueBoolean properlyConstructed() { 
	List<Method> ctors = getConstructors();
		
	ThreeValueBoolean thisReferenceEscapes = ThreeValueBoolean.no;
	
	for ( Method ctor : ctors ) {
	    CtorAnalyzer ctorAnalyzer = new CtorAnalyzer(ctor);
	    thisReferenceEscapes = thisReferenceEscapes.or(ctorAnalyzer.doesThisReferenceEscape());
	}
	return thisReferenceEscapes.not(); 
    }
    
    private ThreeValueBoolean stateUnmodifiable() { return ThreeValueBoolean.unknown; }
    
    public ThreeValueBoolean isImmutable() {
	return properlyConstructed().and(stateUnmodifiable()).and(allFieldsFinal());
    }
    
}
