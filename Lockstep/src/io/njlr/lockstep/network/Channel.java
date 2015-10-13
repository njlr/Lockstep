package io.njlr.lockstep.network;

import io.njlr.bytes.Bytes;

/**
 * Channel encapsulates the concept of a two-way communication link over a network. 
 * 
 * Each channel has exactly one remote end-point.
 * 
 * Channels may be stateful. 
 * 
 * Different channels have different guarantees for reliability and the ordering of packets.  
 * 
 */
public interface Channel extends AutoCloseable {
	
	/**
	 * Sends a message to the remote end-point of this channel. 
	 * 
	 * The message may arrive out-of-order or not at all, depending 
	 * on the channels guarantees. 
	 * 
	 * @param content The message to send
	 */
	void send(final Bytes content);
	
	/**
	 * Gives the channel a message from the remote end-point to process. 
	 * 
	 * An example of processing might be sending back an ACK message. 
	 * 
	 * @param content The message from the remote end-point
	 */
	void handle(final Bytes content);
	
	/* (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	void close();
}
