package io.njlr.lockstep.network.sequence;

/**
 * An 8-bit circular sequence. 
 *
 */
public final class Sequence8 {

	private Sequence8() {
		
		super();
	}
	
	/**
	 * Returns the successor byte of a given byte
	 * 
	 * @param n The byte to continue from
	 * @return The successor byte
	 */
	public static byte next(final byte n) {
		
		if (n == Byte.MAX_VALUE) {
			
			return Byte.MIN_VALUE;
		} else {
			
			return (byte) (n + 1);
		}
	}
	
	/**
	 * Returns the sum of two bytes, wrapping-around if necessary. 
	 * 
	 * @param x The first addend
	 * @param y The second addend
	 * @return The sum
	 */
	public static byte add(final byte x, final byte y) {
		
		return (byte) (x + y);
	}
	
	/** 
	 * Tests that x is more recent than y, taking wrap-around into account. 
	 * @param x The first sequence number
	 * @param y The second sequence number
	 * @return If x is more recent than y
	 */
	public static boolean isMoreRecent(final byte x, final byte y) {
		
		return ((x > y) && (x - y <= Byte.MAX_VALUE)) || ((y > x) && (y - x > Byte.MAX_VALUE));
	}
}
