package io.njlr.lockstep.network.sequence;

/**
 * A 32-bit circular sequence.
 *
 */
public final class Sequence32 {

	private Sequence32() {
		
		super();
	}
	
	/**
	 * Returns the successor int of a given int
	 * 
	 * @param n The int to continue from
	 * @return The successor int
	 */
	public static int next(final int n) {
		
		return n + 1;
	}
	
	/**
	 * Returns the sum of two ints, wrapping-around if necessary. 
	 * 
	 * @param x The first addend
	 * @param y The second addend
	 * @return The sum
	 */
	public static int add(final int x, final int y) {
		
		return x + y;
	}
	
	/** 
	 * Tests that x is more recent than y, taking wrap-around into account. 
	 * @param x The first sequence number
	 * @param y The second sequence number
	 * @return If x is more recent than y
	 */
	public static boolean isMoreRecent(final int x, final int y) {
		
		return ((x > y) && (x - y <= Integer.MAX_VALUE)) || ((y > x) && (y - x > Integer.MAX_VALUE));
	}
}
