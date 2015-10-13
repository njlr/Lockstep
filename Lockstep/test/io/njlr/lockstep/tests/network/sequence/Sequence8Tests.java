package io.njlr.lockstep.tests.network.sequence;

import org.junit.Test;

import io.njlr.lockstep.network.sequence.Sequence8;

public final class Sequence8Tests {
	
	@Test
	public void testIsMoreRecent() {
		
		assert(Sequence8.isMoreRecent((byte)40, (byte)27));
		assert(Sequence8.isMoreRecent((byte)-40, (byte)-50));
		assert(Sequence8.isMoreRecent((byte)(Byte.MIN_VALUE + 20), (byte) (Byte.MAX_VALUE - 10)));
		assert(Sequence8.isMoreRecent((byte)(Byte.MIN_VALUE + 1), Byte.MAX_VALUE));
		assert(Sequence8.isMoreRecent((byte)(Byte.MAX_VALUE + 20), Byte.MAX_VALUE));
		
		assert(!Sequence8.isMoreRecent((byte)98, (byte)98));
		assert(!Sequence8.isMoreRecent((byte)12, (byte)27));
		assert(!Sequence8.isMoreRecent(Byte.MAX_VALUE, (byte)(Byte.MAX_VALUE + 20)));
	}
	
	@Test
	public void testNextMatchesAdd() {
		
		assert(Sequence8.next((byte)40) == Sequence8.add((byte)40, (byte)1));
		assert(Sequence8.next(Byte.MAX_VALUE) == Sequence8.add(Byte.MAX_VALUE, (byte)1));
	}
}
