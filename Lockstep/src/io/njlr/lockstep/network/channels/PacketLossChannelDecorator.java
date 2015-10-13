package io.njlr.lockstep.network.channels;

import java.util.Random;

import com.google.common.base.Preconditions;

import io.njlr.bytes.Bytes;
import io.njlr.lockstep.network.Channel;

/**
 * A decorator for a Channel used to simulate dropped packets at a framework level. 
 * 
 * It fails to receive any packet with a given probability. 
 *
 */
public final class PacketLossChannelDecorator implements Channel {
	
	private final Channel channel;
	
	private final float packetLossChance;
	
	private final Random random;
	
	/**
	 * Creates a new <code>PacketLossChannelDecorator</code> instance. 
	 * 
	 * @param channel The <code>Channel</code> to wrap
	 * @param packetLossChance The probability 0..1 of a packet being dropped
	 */
	public PacketLossChannelDecorator(final Channel channel, final float packetLossChance) {
		
		super();
		
		Preconditions.checkNotNull(channel);
		
		Preconditions.checkArgument(packetLossChance >= 0f);
		Preconditions.checkArgument(packetLossChance <= 1f);
		
		this.channel = channel;
		this.packetLossChance = packetLossChance;
		
		random = new Random();
	}

	@Override
	public void send(final Bytes content) {
		
		channel.send(content);
	}

	@Override
	public void handle(final Bytes content) {
		
		if (random.nextFloat() > packetLossChance || packetLossChance == 0f) {
			
			channel.handle(content);
		}
	}

	@Override
	public void close() {
		
		channel.close();
	}
}
