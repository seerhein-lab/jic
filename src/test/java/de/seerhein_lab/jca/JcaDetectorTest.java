package de.seerhein_lab.jca;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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

@RunWith(Parameterized.class)
public class JcaDetectorTest {

	private JcaDetector jcaDetector;
	private JavaClass javaClass;
	private boolean isSupposedlyImmutable;

	public JcaDetectorTest(Object javaClass, Object isSupposedlyImmutable) {
		this.javaClass = (JavaClass) javaClass;
		this.isSupposedlyImmutable = (Boolean) isSupposedlyImmutable;
	}

	@Parameters
	public static List<Object[]> data() throws ClassNotFoundException {
		Class<?>[] classes = de.seerhein_lab.jca.JcaDetectorTest.class
				.getClasses();
		List<Object[]> list = new ArrayList<Object[]>();
		for (int i = 0; i < classes.length; i++) {
			list.add(new Object[] { Repository.lookupClass(classes[i]),
					isSupposedlyImmutableArray[i] });
		}
		return list;
	}

	@Before
	public void setUp() throws Exception {
		jcaDetector = new JcaDetector(null);
	}

	@Test
	public void testSupposedlyImmutable() throws ClassNotFoundException {

		assertEquals(isSupposedlyImmutable,
				jcaDetector.supposedlyImmutable(javaClass));
	}

	// use alphabetical order
	private final static boolean isSupposedlyImmutableArray[] = { true, true,
			false, false, false, false };

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
