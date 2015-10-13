package io.njlr.lockstep.network.sequence;

/**
 * A 16-bit circular sequence.
 *
 */
public final class Sequence16 {

	private Sequence16() {
		
		super();
	}
	
	/**
	 * Returns the successor short of a given short
	 * 
	 * @param n The short to continue from
	 * @return The successor short
	 */
	public static short next(final short n) {
		
		if (n == Short.MAX_VALUE) {
			
			return Short.MIN_VALUE;
		} else {
			
			return (short) (n + 1);
		}
	}
	
	/**
	 * Returns the sum of two shorts, wrapping-around if necessary. 
	 * 
	 * @param x The first addend
	 * @param y The second addend
	 * @return The sum
	 */
	public static short add(final short x, final short y) {
		
		return (short) (x + y);
	}
	
	/** 
	 * Tests that x is more recent than y, taking wrap-around into account. 
	 * @param x The first sequence number
	 * @param y The second sequence number
	 * @return If x is more recent than y
	 */
	public static boolean isMoreRecent(final short x, final short y) {
		
		return ((x > y) && (x - y <= Short.MAX_VALUE)) || ((y > x) && (y - x > Short.MAX_VALUE));
	}
}
