package io.njlr.lockstep.network.channels;

import io.njlr.bytes.Bytes;
import io.njlr.bytes.BytesBuilder;
import io.njlr.lockstep.network.ChannelContext;
import io.njlr.lockstep.network.sequence.Sequence8;

/**
 * The "receiver" part of the <code>StopAndWaitChannel</code>
 *
 */
public final class StopAndWaitReceiver {
	
	private final Object lock = new Object();

	private final ChannelContext context;
	
	private volatile byte sequenceNumber;
	
	public StopAndWaitReceiver(final ChannelContext context) {
		
		super();
		
		this.context = context;
	}
	
	public void handle(final Bytes message) {
		
		if (message.get(0) == StopAndWaitChannel.Data) {
			
			synchronized (lock) {
				
				final byte receivedSequenceNumber = message.get(1);
				
				if (receivedSequenceNumber == sequenceNumber) {
					
					sequenceNumber = Sequence8.next(sequenceNumber);
					
					final Bytes data = message.sub(2, message.length());
					
					context.takeMessage(data);
				}
				
				sendAck(receivedSequenceNumber);
			}
		}
	}
	
	private void sendAck(final byte receivedSequenceNumber) {
		
		final Bytes sequencedMessage = new BytesBuilder()
				.append(StopAndWaitChannel.Ack)
				.append(receivedSequenceNumber)
				.toBytes();
		
		context.sendMessage(sequencedMessage);
	}
}
