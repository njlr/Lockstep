package io.njlr.lockstep.network;

import java.net.InetAddress;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * An immutable representation of an <code>InetAddress</code> and a port.  
 *
 */
public final class NetworkAddress {
	
	private final InetAddress host;
	private final int port;
	
	/**
	 * The host portion of the address
	 * 
	 * @return The host
	 */
	public InetAddress host() {
		
		return this.host;
	}
	
	/**
	 * The port portion of the address
	 * 
	 * @return The port
	 */
	public int port() {
		
		return this.port;
	}
	
	public NetworkAddress(final InetAddress host, final int port) {
		
		super();
		
		Preconditions.checkNotNull(host);
		
		this.host = host;
		this.port = port;
	}
	
	@Override
	public int hashCode() {
		
		return host.hashCode() * port;
	}
	
	@Override
	public boolean equals(final Object that) {
		
		if (this == that) {
			
			return true;
		}
		
		if (that instanceof NetworkAddress) {
			
			final NetworkAddress thatNetworkAddress = (NetworkAddress) that;
			
			return this.port == thatNetworkAddress.port && this.host.equals(thatNetworkAddress.host);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		
		return MoreObjects.toStringHelper(this).addValue(host).addValue(port).toString();
	}
	
	public static NetworkAddress of(final InetAddress host, final int port) {
		
		return new NetworkAddress(host, port);
	}
}
