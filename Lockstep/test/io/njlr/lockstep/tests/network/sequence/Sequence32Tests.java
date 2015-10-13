package io.njlr.lockstep.tests.network.sequence;

import org.junit.Test;

import io.njlr.lockstep.network.sequence.Sequence32;

public final class Sequence32Tests {
	
	@Test
	public void testIsMoreRecent() {
		
		assert(Sequence32.isMoreRecent(40, 27));
		assert(Sequence32.isMoreRecent(-40, -50));
		assert(Sequence32.isMoreRecent(Integer.MIN_VALUE + 20, Integer.MAX_VALUE - 10));
		assert(Sequence32.isMoreRecent(Integer.MIN_VALUE + 1, Integer.MAX_VALUE));
		assert(Sequence32.isMoreRecent(Integer.MAX_VALUE + 20, Integer.MAX_VALUE));
		
		assert(!Sequence32.isMoreRecent(40, 87));
	}
	
	@Test
	public void testNextMatchesAdd() {
		
		assert(Sequence32.next(40) == Sequence32.add(40, 1));
		assert(Sequence32.next(Integer.MAX_VALUE) == Sequence32.add(Integer.MAX_VALUE, 1));
	}
}
