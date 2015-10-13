package io.njlr.lockstep.network;

/**
 * A function that creates a <code>Channel</code> for the given context. 
 *
 */
@FunctionalInterface
public interface ChannelFactory {

	Channel create(final ChannelContext context);
}
