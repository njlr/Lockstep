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

public final class TwoWayReliability {
	
	private TwoWayReliability() {
		
		super();
	}
	
	public static final void test(final ChannelFactory channelFactory, final float packetLossChance, final Bytes messageA, final Bytes messageB) throws Exception {
		
		final ExecutorService executorService = Executors.newFixedThreadPool(2);
		
		final NetworkAddress senderAddress = NetworkAddress.of(InetAddress.getLocalHost(), 1234);
		final NetworkAddress receiverAddress = NetworkAddress.of(InetAddress.getLocalHost(), 4567);
		
		final Task taskA = new Task(senderAddress.port(), receiverAddress, channelFactory, packetLossChance, messageA);
		final Task taskB = new Task(receiverAddress.port(), senderAddress, channelFactory, packetLossChance, messageB);
		
		executorService.submit(taskA);
		executorService.submit(taskB);
		
		final Bytes resultA = taskB.future().get(); // Calling get inside an assert does not block! 
		final Bytes resultB = taskB.future().get();
		
		assert(resultA.equals(messageB));
		assert(resultB.equals(messageA));
		
		// No need to tidy-up; JUnit does it for us
	}
	
	private static final class Task implements Runnable {
		
		private final int port;
		private final NetworkAddress remoteAddress;
		private final ChannelFactory channelFactory;
		private final float packetLossChance;
		
		private final Bytes message;
		
		private final SettableFuture<Bytes> future;
		
		public ListenableFuture<Bytes> future() {
			
			return this.future;
		}
		
		public Task(final int port, final NetworkAddress remoteAddress, final ChannelFactory channelFactory, final float packetLossChance, final Bytes message) {
			
			this.port = port;
			this.remoteAddress = remoteAddress;
			this.channelFactory = channelFactory;
			this.packetLossChance = packetLossChance;
			this.message = message;
			
			future = SettableFuture.create();
		}

		@Override
		public void run() {
			
			final NetworkSession networkSession = new NetworkSession(port);
			
			final ChannelBinding binding = new ChannelBinding(remoteAddress, ChannelCodes.ReliableOrdered1);
			
			networkSession.startAsync();
			
			final ChannelListener listener = new ChannelListener() {
				
				@Override
				public synchronized void handle(final Bytes message) {
					
					assert(!future.isDone());
					
					future.set(message);
				};
			};
			
			networkSession.connect(
					binding, 
					(final ChannelContext context) -> { return new PacketLossChannelDecorator(channelFactory.create(context), packetLossChance); }, 
					listener);
			
			try {
				
				Thread.sleep(new Random().nextInt(3000));
			} catch (final InterruptedException e) {
				
				e.printStackTrace();
			}
			
			networkSession.send(binding, message);
		}
	}
}
