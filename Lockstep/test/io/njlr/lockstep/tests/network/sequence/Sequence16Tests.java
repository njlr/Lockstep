package io.njlr.lockstep.tests.network.sequence;

import org.junit.Test;

import io.njlr.lockstep.network.sequence.Sequence16;

public final class Sequence16Tests {
	
	@Test
	public void testIsMoreRecent() {
		
		assert(Sequence16.isMoreRecent((short)40, (short)27));
		assert(Sequence16.isMoreRecent((short)-40, (short)-50));
		assert(Sequence16.isMoreRecent((short)(Short.MIN_VALUE + 20), (short) (Short.MAX_VALUE - 10)));
		assert(Sequence16.isMoreRecent((short)(Short.MIN_VALUE + 1), Short.MAX_VALUE));
		assert(Sequence16.isMoreRecent((short)(Short.MAX_VALUE + 20), Short.MAX_VALUE));
	}
	
	@Test
	public void testNextMatchesAdd() {
		
		assert(Sequence16.next((short)40) == Sequence16.add((short)40, (short)1));
		assert(Sequence16.next(Short.MAX_VALUE) == Sequence16.add(Short.MAX_VALUE, (short)1));
	}
}
