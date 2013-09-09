package playground;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

import de.seerhein_lab.jca.Utils;
import de.seerhein_lab.jca.analyzer.ClassAnalyzer;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.ba.ClassContext;

public class ProperlyConstructedTestDriver {
	private static final String LOGFILEPATH = "log.txt";

	public static void main(String[] args) throws ClassNotFoundException,
			SecurityException, IOException {

		Logger logger = Utils.setUpLogger("ProperlyConstructedTestDriver",
				LOGFILEPATH);

		JavaClass clazz = Repository
				.lookupClass("playground.PropConstTestClass");

		ClassContext classContextMock = mock(ClassContext.class);

		when(classContextMock.getJavaClass()).thenReturn(clazz);

		SortedBugCollection bugs = new SortedBugCollection();
		bugs.addAll(new ClassAnalyzer(classContextMock).properlyConstructed());

		logger.log(Level.SEVERE, "bugs: ");
		for (BugInstance bug : bugs) {
			logger.log(Level.SEVERE,
					" " + bug.getType() + " (" + bug.getPriorityString() + ")");
		}

		logger.log(Level.SEVERE, "end bugs");

	}

}
