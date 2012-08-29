package playground;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public class ObjectTest {

	/**
	 * @param args
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		JavaClass clazz = Repository.lookupClass("java.lang.Object");

		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.getName().equals("<init>")) {
				System.out.println(method);
				System.out.println(method.getCode());
			}

		}

	}

}
