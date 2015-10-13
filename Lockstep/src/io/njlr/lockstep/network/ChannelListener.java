package io.njlr.lockstep.network;

import io.njlr.bytes.Bytes;

/**
 * A function that accepts messages from a <code>Channel</code>
 *
 */
@FunctionalInterface
public interface ChannelListener {

	void handle(final Bytes message);
}
