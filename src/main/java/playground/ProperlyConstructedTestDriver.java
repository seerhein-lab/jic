package playground;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

import de.htwg_konstanz.in.jca.PropConClassAnalyzer;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;

public class ProperlyConstructedTestDriver {

	public static void main(String[] args) throws ClassNotFoundException {
		JavaClass clazz = Repository
				.lookupClass("playground.PropConstTestClass");

		BugCollection bugs = new PropConClassAnalyzer(clazz)
				.properlyConstructed();
		System.out.println("bugs: ");
		for (BugInstance bug : bugs) {
			System.out.println("  " + bug);
		}
		System.out.println("end bugs");

	}

}
