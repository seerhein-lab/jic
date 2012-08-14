package de.htwg_konstanz.in.jca;

import static org.junit.Assert.*;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.junit.Test;

public class TestClassAnalyzer {
    
    @Test
    public void testProperlyConstructedDefaultCtorNoInitializers() {
	JavaClass clazz = null;
	try {
	    clazz = Repository.lookupClass("de.htwg_konstanz.in.jca.testclasses.DefaultCtorNoInitializers");
	}
	catch ( ClassNotFoundException e) {
	    fail("class not found");
	}
	assertEquals(new ClassAnalyzer(clazz).properlyConstructed(), ThreeValueBoolean.yes);
    }
//    
//    @Test
//    public void testProperlyConstructedDefaultCtorInitializers() {
//	JavaClass clazz = null;
//	try {
//	    clazz = Repository.lookupClass("de.htwg_konstanz.in.jca.testclasses.DefaultCtorInitializers");
//	}
//	catch ( ClassNotFoundException e) {
//	    fail("class not found");
//	}
//	assertEquals(new ClassAnalyzer(clazz).properlyConstructed(), ThreeValueBoolean.yes);
//    }
    
//	ClassPath cp = new ClassPath("/Users/haase/Documents/workspace/utils/target/classes");
//	SyntheticRepository repo = SyntheticRepository.getInstance(cp);	
//	Repository.setRepository(repo);
//	JavaClass clazz = Repository.lookupClass("de.htwg_konstanz.util.GraphicIO");
        
}
   