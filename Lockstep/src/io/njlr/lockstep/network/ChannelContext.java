package io.njlr.lockstep.network;

import io.njlr.bytes.Bytes;

/**
 * A context object used by a Channel to interact with layers above and below it. 
 */
public interface ChannelContext {

	/**
	 * Passes a message to the layer below to send to the remote. 
	 * 
	 * @param message The message to send
	 */
	void sendMessage(final Bytes message);
	
	/**
	 * Passes a message to the layer above for processing. 
	 * 
	 * @param message The message to pass
	 */
	void takeMessage(final Bytes message);
}
