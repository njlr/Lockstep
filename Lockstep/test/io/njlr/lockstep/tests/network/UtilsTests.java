package io.njlr.lockstep.tests.network;

import org.junit.Test;

import io.njlr.lockstep.network.Utils;

public final class UtilsTests {

	public UtilsTests() {
		
		super();
	}
	
	@Test
	public void testStringEncodeDecode() {
		
		final String messageIn = "Hello, how is everyone today? ";
		final String messageOut = Utils.decode(Utils.encode(messageIn));
		
		assert(messageIn.equals(messageOut));
	}
}
