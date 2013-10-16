package de.seerhein_lab.jic;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

import net.jcip.annotations.Immutable;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.seerhein_lab.jic.JicDetector;

@RunWith(Parameterized.class)
public class JicDetectorTest {

	private JicDetector jicDetector;
	private JavaClass javaClass;
	private static final String PACKAGE = "de.seerhein_lab.jic.JicDetectorTest$";
	private final static Map<String, Boolean> isSupposedlyImmutable = new HashMap<String, Boolean>();

	public JicDetectorTest(Object javaClass) {
		this.javaClass = (JavaClass) javaClass;
	}

	@Parameters
	public static List<Object[]> data() throws ClassNotFoundException {
		Class<?>[] classes = de.seerhein_lab.jic.JicDetectorTest.class.getClasses();
		List<Object[]> list = new ArrayList<Object[]>();
		for (int i = 0; i < classes.length; i++) {
			list.add(new Object[] { Repository.lookupClass(classes[i]) });
		}
		return list;
	}

	@Before
	public void setUp() throws Exception {
		jicDetector = new JicDetector(null);
		isSupposedlyImmutable.put(PACKAGE + "ImmutableAnnotation", true);
		isSupposedlyImmutable.put(PACKAGE + "immutableAndOtherAnnotations", true);
		isSupposedlyImmutable.put(PACKAGE + "noAnnotation", false);
		isSupposedlyImmutable.put(PACKAGE + "otherAnnotation", false);
		isSupposedlyImmutable.put(PACKAGE + "otherImmutableAnnotation", false);
		isSupposedlyImmutable.put(PACKAGE + "otherJcipAnnotation", false);
	}

	@Test
	public void testSupposedlyImmutable() throws ClassNotFoundException {

		assertEquals(isSupposedlyImmutable.get(javaClass.getClassName()),
				jicDetector.supposedlyImmutable(javaClass));
	}

	@Immutable
	public static class ImmutableAnnotation {
	}

	@CheckForNull
	@Immutable
	@Ignore
	public static class immutableAndOtherAnnotations {
	}

	public static class noAnnotation {
	}

	@Deprecated
	public static class otherAnnotation {
	}

	@javax.annotation.concurrent.Immutable
	public static class otherImmutableAnnotation {
	}

	@net.jcip.annotations.ThreadSafe
	public static class otherJcipAnnotation {
	}

}
