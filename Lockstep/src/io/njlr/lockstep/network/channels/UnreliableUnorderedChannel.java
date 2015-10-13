package io.njlr.lockstep.network.channels;

import com.google.common.base.Preconditions;

import io.njlr.bytes.Bytes;
import io.njlr.lockstep.network.Channel;
import io.njlr.lockstep.network.ChannelContext;

/**
 * A fire-and-forget <code>Channel</code> for unreliable, unordered communication. 
 *
 */
public final class UnreliableUnorderedChannel implements Channel {
	
	private final Object lock = new Object();

	private final ChannelContext context;
	
	private volatile boolean isClosed;
	
	public UnreliableUnorderedChannel(final ChannelContext context) {
		
		super();
		
		Preconditions.checkNotNull(context);
		
		this.context = context;
		
		isClosed = false;
	}

	@Override
	public void send(final Bytes message) {
		
		synchronized (lock) {
			
			if (isClosed) {
				
				return;
			}
			
			context.sendMessage(message);
		}
	}
	
	@Override
	public void handle(final Bytes content) {
		
		synchronized (lock) {
			
			if (isClosed) {
			
				return;
			}
				
			context.takeMessage(content);
		}
	}

	@Override
	public void close() {
		
		synchronized (lock) {
			
			isClosed = true;
		}
	}
	
	/**
	 * Static factory method to allow explicit referencing. 
	 * 
	 * @param context The context to create the <code>Channel</code> in 
	 * @return A new UnreliableUnorderedChannel instance
	 */
	public static UnreliableUnorderedChannel create(final ChannelContext context) {
		
		return new UnreliableUnorderedChannel(context);
	}
}
