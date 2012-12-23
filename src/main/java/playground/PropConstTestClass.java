package playground;

import java.io.IOException;

public class PropConstTestClass {

	private void thrower(int i) throws Exception {
		try {
			if (i != 0)
				throw new IOException();
			else
				i /= 20;
		} catch (IOException e) {
			i--;
		} catch (Exception e) {
			i++;
		} finally {
			i *= 16;
		}
		i /= 5;
	}

	public PropConstTestClass(int i) throws IOException {
		try {
			thrower(i);
		} catch (IOException e) {
			i--;
		} catch (Exception e) {
			i++;
		} finally {
			i *= 16;
		}
		i /= 5;
	}
}
