package de.seerhein_lab.jic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void testFormatLoggingOutput0() {
		String indentation = Utils.formatLoggingOutput(0);
		assertEquals("", indentation);
	}

	@Test
	public void testFormatLoggingOutput1() {
		String indentation = Utils.formatLoggingOutput(1);
		assertEquals("\t", indentation);
	}

	@Test
	public void testFormatLoggingOutput2() {
		String indentation = Utils.formatLoggingOutput(2);
		assertEquals("\t\t", indentation);
	}
}
