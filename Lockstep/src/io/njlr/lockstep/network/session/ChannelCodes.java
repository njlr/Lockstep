package io.njlr.lockstep.network.session;

/**
 * An (optional) set of channel code conventions. 
 *
 */
public final class ChannelCodes {

	private ChannelCodes() {
		
		super();
	}
	
	public static final byte UnreliableUnordered = 0;
	
	public static final byte ReliableUnordered = 1;
	
	public static final byte UnreliableOrdered1 = 2;
	public static final byte UnreliableOrdered2 = 3;
	public static final byte UnreliableOrdered3 = 4;
	public static final byte UnreliableOrdered4 = 5;
	
	public static final byte ReliableOrdered1 = 6;
	public static final byte ReliableOrdered2 = 7;
	public static final byte ReliableOrdered3 = 8;
	public static final byte ReliableOrdered4 = 9;
}
