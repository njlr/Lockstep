package io.njlr.lockstep.tests.network.channels;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import io.njlr.bytes.Bytes;
import io.njlr.bytes.BytesBuilder;
import io.njlr.lockstep.network.ChannelContext;
import io.njlr.lockstep.network.ChannelFactory;
import io.njlr.lockstep.network.ChannelListener;
import io.njlr.lockstep.network.NetworkAddress;
import io.njlr.lockstep.network.channels.PacketLossChannelDecorator;
import io.njlr.lockstep.network.session.ChannelBinding;
import io.njlr.lockstep.network.session.ChannelCodes;
import io.njlr.lockstep.network.session.NetworkSession;

public final class TwoWayReliability2 {
	
	private TwoWayReliability2() {
		
		super();
	}
	
	public static void test(final ChannelFactory channelFactory, final float packetLossChance, final int limit) throws Exception {
		
		Logger.getLogger("").setLevel(Level.OFF);
		
		final ExecutorService executorService = Executors.newFixedThreadPool(2);
		
		final NetworkAddress senderAddress = NetworkAddress.of(InetAddress.getLocalHost(), 1234);
		final NetworkAddress receiverAddress = NetworkAddress.of(InetAddress.getLocalHost(), 4567);
		
		final Task taskA = new Task(senderAddress.port(), receiverAddress, channelFactory, packetLossChance, limit);
		final Task taskB = new Task(receiverAddress.port(), senderAddress, channelFactory, packetLossChance, limit);
		
		executorService.submit(taskA);
		executorService.submit(taskB);
		
		taskA.future().get();
		taskB.future().get();
		
		// No need to tidy-up; JUnit does it for us
	}
	
	private static final class Task implements Runnable {
		
		private final int port;
		private final NetworkAddress remoteAddress;
		private final ChannelFactory channelFactory;
		private final float packetLossChance;
		private final int limit;
		
		private volatile int i;
		
		private final SettableFuture<?> future;
		
		public ListenableFuture<?> future() {
			
			return future;
		}
		
		public Task(final int port, final NetworkAddress remoteAddress, final ChannelFactory channelFactory, final float packetLossChance, final int limit) {
			
			Preconditions.checkArgument(limit > 0);
			
			this.port = port;
			this.remoteAddress = remoteAddress;
			this.channelFactory = channelFactory;
			this.packetLossChance = packetLossChance;
			this.limit = limit;
			
			future = SettableFuture.create();
		}

		@Override
		public void run() {
			
			i = 0;
			
			final NetworkSession networkSession = new NetworkSession(port);
			
			final ChannelBinding binding = new ChannelBinding(remoteAddress, ChannelCodes.ReliableOrdered1);
			
			networkSession.startAsync();
			
			final ChannelListener listener = new ChannelListener() {
				
				@Override
				public synchronized void handle(final Bytes message) {
					
					assert(message.read().readInt() == i);
					
					i++;
					
					System.out.println(port + ": " + i);
					
					if (i == limit) {
						
						future.set(null);
					} else {
					
						networkSession.send(binding, new BytesBuilder(4).appendInt(i).toBytes());
					}
				};
			};
			
			networkSession.connect(
					binding, 
					(final ChannelContext context) -> { return new PacketLossChannelDecorator(channelFactory.create(context), packetLossChance); }, 
					listener);
			
			networkSession.send(binding, new BytesBuilder(4).appendInt(i).toBytes());
		}
	}
}
