package io.njlr.lockstep.tests.network.channels;

import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import io.njlr.bytes.Bytes;
import io.njlr.lockstep.network.ChannelContext;
import io.njlr.lockstep.network.ChannelFactory;
import io.njlr.lockstep.network.ChannelListener;
import io.njlr.lockstep.network.NetworkAddress;
import io.njlr.lockstep.network.channels.PacketLossChannelDecorator;
import io.njlr.lockstep.network.session.ChannelBinding;
import io.njlr.lockstep.network.session.ChannelCodes;
import io.njlr.lockstep.network.session.NetworkSession;

public final class OneWayReliability {
	
	private OneWayReliability() {
		
		super();
	}
	
	public static final void test(final ChannelFactory channelFactory, final float packetLossChance, final Bytes[] sequence) throws Exception {
		
		final ExecutorService executorService = Executors.newFixedThreadPool(2);
		
		final NetworkAddress senderAddress = NetworkAddress.of(InetAddress.getLocalHost(), 1234);
		final NetworkAddress receiverAddress = NetworkAddress.of(InetAddress.getLocalHost(), 4567);
		
		final SenderTask senderTask = new SenderTask(senderAddress.port(), receiverAddress, channelFactory, packetLossChance, sequence);
		final ReceiverTask receiverTask = new ReceiverTask(receiverAddress.port(), senderAddress, channelFactory, packetLossChance, sequence);
		
		executorService.submit(senderTask);
		executorService.submit(receiverTask);
		
		final Object result = receiverTask.future().get(); // Calling get inside an assert does not block! 
		
		assert(result == null);
		
		// No need to tidy-up; JUnit does it for us
	}
	
	public static final class SenderTask implements Runnable {
		
		private final int port;
		private final NetworkAddress remoteAddress;
		private final ChannelFactory channelFactory;
		private final float packetLossChance;
		private final Bytes[] sequence;
		
		public SenderTask(final int port, final NetworkAddress remoteAddress, final ChannelFactory channelFactory, final float packetLossChance, final Bytes[] sequence) {
			
			this.port = port;
			this.remoteAddress = remoteAddress;
			this.channelFactory = channelFactory;
			this.packetLossChance = packetLossChance;
			this.sequence = sequence;
		}

		@Override
		public void run() {
			
			final NetworkSession networkSession = new NetworkSession(port);
			
			networkSession.startAsync();
			
			final ChannelBinding binding = new ChannelBinding(remoteAddress, ChannelCodes.ReliableOrdered1);
			
			networkSession.connect(
					binding, 
					(final ChannelContext context) -> { return new PacketLossChannelDecorator(channelFactory.create(context), packetLossChance); }, 
					(final Bytes message) -> { });
			
			final Random random = new Random();
			
			for (int i = 0; i < sequence.length; i++) {
				
				try {
					
					Thread.sleep(random.nextInt(15000));
				} catch (final InterruptedException e) {
					
					e.printStackTrace();
				}
				
				networkSession.send(binding, sequence[i]);
			}
		}
	}
	
	private static final class ReceiverTask implements Runnable {
		
		private final int port;
		private final NetworkAddress remoteAddress;
		private final ChannelFactory channelFactory;
		private final float packetLossChance;
		private final Bytes[] expectedSequence;
		
		private volatile int index;
		private final SettableFuture<?> future;
		
		public ListenableFuture<?> future() {
			
			return this.future;
		}
		
		public ReceiverTask(final int port, final NetworkAddress remoteAddress, final ChannelFactory channelFactory, final float packetLossChance, final Bytes[] expectedSequence) {
			
			this.port = port;
			this.remoteAddress = remoteAddress;
			this.channelFactory = channelFactory;
			this.packetLossChance = packetLossChance;
			this.expectedSequence = expectedSequence;
			
			future = SettableFuture.create();
		}

		@Override
		public void run() {
			
			index = 0;
			
			final NetworkSession networkSession = new NetworkSession(port);
			
			final ChannelBinding binding = new ChannelBinding(remoteAddress, ChannelCodes.ReliableOrdered1);
			
			networkSession.startAsync();
			
			final ChannelListener listener = new ChannelListener() {
				
				@Override
				public synchronized void handle(final Bytes message) {
					
					assert(expectedSequence[index].equals(message));
					
					index++;
					
					if (index == expectedSequence.length) {
						
						future.set(null);
					}
				};
			};
			
			networkSession.connect(
					binding, 
					(final ChannelContext context) -> { return new PacketLossChannelDecorator(channelFactory.create(context), packetLossChance); }, 
					listener);
		}
	}
}
