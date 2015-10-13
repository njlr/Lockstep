package io.njlr.lockstep.network.channels;

import io.njlr.bytes.Bytes;
import io.njlr.lockstep.network.Channel;
import io.njlr.lockstep.network.ChannelContext;

/**
 * A reliable-ordered <code>Channel</code> implementation using Stop-and-Wait ARQ. 
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Stop-and-wait_ARQ">https://en.wikipedia.org/wiki/Stop-and-wait_ARQ</a>
 *
 */
public final class StopAndWaitChannel implements Channel {
	
	public static final byte Ack = 17;
	public static final byte Data = 61;
	public static final byte Close = 97;
	
	private final Object lock = new Object();

	private final ChannelContext context;
	
	private final StopAndWaitSender sender;
	private final StopAndWaitReceiver receiver;
	
	private volatile boolean isClosed;
	
	public StopAndWaitChannel(final ChannelContext context) {
		
		super();
		
		this.context = context;
		
		sender = new StopAndWaitSender(this.context, 1000);
		receiver = new StopAndWaitReceiver(this.context);
		
		isClosed = false;
	}

	@Override
	public void send(final Bytes message) {
		
		synchronized (lock) {
			
			if (isClosed) {
				
				return;
			}
			
			sender.send(message);
		}
	}

	@Override
	public void handle(final Bytes content) {
		
		synchronized (lock) {
			
			if (isClosed) {
				
				return;
			}
			
			if (content.get(0) == StopAndWaitChannel.Close) {
				
				close();
			} else {
			
				sender.handle(content);
				receiver.handle(content);
			}
		}
	}

	@Override
	public void close() {
		
		synchronized (lock) {
			
			if (!isClosed) {
			
				isClosed = true;
			
				sender.send(Bytes.of(Close));
				sender.close();
			}
		}
	}
	
	/**
	 * Static factory method to allow explicit referencing. 
	 * 
	 * @param context The context to create the <code>Channel</code> in 
	 * @return A new StopAndWaitChannel instance
	 */
	public static StopAndWaitChannel create(final ChannelContext context) {
		
		return new StopAndWaitChannel(context);
	}
}
