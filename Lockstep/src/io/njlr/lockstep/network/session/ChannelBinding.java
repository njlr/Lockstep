package io.njlr.lockstep.network.session;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import io.njlr.lockstep.network.NetworkAddress;

/**
 * An identifier (and meta-data) for the binding of <code>Channel</code> to a session. 
 * 
 * This class is immutable. 
 *
 */
public final class ChannelBinding {

	private final NetworkAddress remoteAddress;
	private final byte channelCode;
	
	/**
	 * The remote address that the <code>Channel</code> is bound to
	 * 
	 * @return The remote address
	 */
	public NetworkAddress remoteAddress() {
		
		return remoteAddress;
	}
	
	/**
	 * A byte identifier to distinguish between other bindings with the same remote
	 * 
	 * @return The channel code
	 */
	public byte channelCode() {
		
		return channelCode;
	}
	
	public ChannelBinding(final NetworkAddress remoteAddress, final byte split) {
		
		super();
		
		Preconditions.checkNotNull(remoteAddress);
		
		this.remoteAddress = remoteAddress;
		this.channelCode = split;
	}
	
	@Override
	public int hashCode() {
		
		return remoteAddress.hashCode() * 11 + channelCode * 17;
	}
	
	@Override
	public boolean equals(final Object that) {
		
		if (this == that) {
			
			return true;
		}
		
		if (that instanceof ChannelBinding) {
			
			final ChannelBinding thatBinding = (ChannelBinding) that;
			
			return ((this.remoteAddress.equals(thatBinding.remoteAddress)) && 
					(this.channelCode == thatBinding.channelCode));
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		
		return MoreObjects.toStringHelper(this).add("remoteAddress", remoteAddress).add("channelCode", channelCode).toString();
	}
}
