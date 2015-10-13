package io.njlr.lockstep.tests.network.channels;

import org.junit.Test;

import com.google.common.base.Charsets;

import io.njlr.bytes.Bytes;
import io.njlr.lockstep.network.channels.StopAndWaitChannel;

public final class StopAndWaitTests {
	
	@Test
	public void testOneWayReliability() throws Exception {
		
		final Bytes[] sequence = new Bytes[] { 
				encode("This"), 
				encode("is"), 
				encode("a"), 
				encode("test"), 
				encode("message"),
				encode("message"),
				encode("message"),
				encode("1"),
				encode("2"),
				encode("3"), 
				encode("testing... "), 
				encode("Fin")
			};
		
		OneWayReliability.test(StopAndWaitChannel::create, 0.6f, sequence);
	}
	
	@Test
	public void testTwoWayReliability() throws Exception {
		
		final Bytes messageA = encode("Hello");
		final Bytes messageB = encode("Howdy");
		
		TwoWayReliability.test(StopAndWaitChannel::create, 0.9f, messageA, messageB);
	}
	
	@Test
	public void testTwoWayReliability2() throws Exception {
		
		TwoWayReliability2.test(StopAndWaitChannel::create, 0.1f, 100);
	}
	
	private static Bytes encode(final String message) {
		
		return new Bytes(Charsets.US_ASCII.encode(message).array());
	}
}
