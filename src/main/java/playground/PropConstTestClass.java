package playground;

import java.io.IOException;

public class PropConstTestClass {

	public PropConstTestClass(int j) {
		int i = 0;

		try {
			i = i / j;
			if (i > 3)
				throw new IOException();
			i++;
		} catch (IOException e) {
			j++;
		}
	}
}
