package io.njlr.lockstep.network.channels;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

import io.njlr.bytes.Bytes;
import io.njlr.bytes.BytesBuilder;
import io.njlr.lockstep.network.ChannelContext;
import io.njlr.lockstep.network.sequence.Sequence8;

/**
 * The "sender" part of the <code>StopAndWaitChannel</code>
 *
 */
public final class StopAndWaitSender implements AutoCloseable {
	
	private final Object lock = new Object();
	
	private final ChannelContext context;
	private final int timeout;
	
	private final Queue<Bytes> messagesToSend;	
	private final ScheduledExecutorService scheduler;
	
	private volatile byte sequenceNumber;
	private volatile ScheduledFuture<?> future;
	
	public StopAndWaitSender(final ChannelContext context, final int timeout) {
		
		super();
		
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(timeout > 0);
		
		this.context = context;
		
		this.timeout = timeout; 
		
		messagesToSend = new LinkedList<>();
		
		scheduler = Executors.newSingleThreadScheduledExecutor();
	}

	public void handle(final Bytes message) {
		
		if (message.get(0) == StopAndWaitChannel.Ack) {
			
			synchronized (lock) {
				
				byte ackedSequenceNumber = message.get(1);
				
				if (ackedSequenceNumber == sequenceNumber) {
					
					sequenceNumber = Sequence8.next(sequenceNumber);
					
					future.cancel(false);
					
					future = null;
					
					messagesToSend.remove();
					
					if (!messagesToSend.isEmpty()) {
						
						sendCurrentPacket();
					}
				}
			}
		}
	}
	
	public void send(final Bytes message) {
		
		synchronized (lock) {
			
			final boolean wasEmpty = messagesToSend.isEmpty();
			
			messagesToSend.add(message);
			
			if (wasEmpty) {
				
				sendCurrentPacket();
			}
		}
	}
	
	public void close() {
		
		synchronized (lock) {
			
			if (future != null) {
				
				future.cancel(false);
				
				future = null;
			}
			
			scheduler.shutdown();
		}
	}
	
	private void sendCurrentPacket() {
		
		synchronized (lock) {
			
			final Bytes sequencedMessage = new BytesBuilder()
					.append(StopAndWaitChannel.Data)
					.append(sequenceNumber)
					.append(messagesToSend.peek())
					.toBytes();
			
			context.sendMessage(sequencedMessage);
			
			future = scheduler.schedule(this::onTimeout, timeout, TimeUnit.MILLISECONDS);
		}
	}
	
	private void onTimeout() {
		
		synchronized (lock) {
		
			sendCurrentPacket();
		}
	}
}
