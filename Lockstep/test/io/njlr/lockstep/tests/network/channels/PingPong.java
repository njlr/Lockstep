package io.njlr.lockstep.tests.network.channels;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.njlr.bytes.Bytes;
import io.njlr.lockstep.network.ChannelContext;
import io.njlr.lockstep.network.ChannelFactory;
import io.njlr.lockstep.network.ChannelListener;
import io.njlr.lockstep.network.NetworkAddress;
import io.njlr.lockstep.network.Utils;
import io.njlr.lockstep.network.channels.PacketLossChannelDecorator;
import io.njlr.lockstep.network.channels.StopAndWaitChannel;
import io.njlr.lockstep.network.session.ChannelBinding;
import io.njlr.lockstep.network.session.ChannelCodes;
import io.njlr.lockstep.network.session.NetworkSession;

public final class PingPong {

	private PingPong() {
		
		super();
	}
	
	public static void main(final String[] args) throws UnknownHostException {
		
		Logger.getLogger("").setLevel(Level.OFF);
		
		final ExecutorService executorService = Executors.newFixedThreadPool(2);
		
		final NetworkAddress addressA = NetworkAddress.of(InetAddress.getLocalHost(), 1234);
		final NetworkAddress addressB = NetworkAddress.of(InetAddress.getLocalHost(), 4567);
		
		executorService.submit(new PingPonger("A", addressA.port(), addressB, StopAndWaitChannel::create, true));
		executorService.submit(new PingPonger("B", addressB.port(), addressA, StopAndWaitChannel::create, false));
	}
	
	private static final class PingPonger implements Runnable {

		private final String tag;		
		private final int port;
		private final NetworkAddress remoteAddress;
		private final ChannelFactory channelFactory;
		private final boolean start;
		
		public PingPonger(final String tag, final int port, final NetworkAddress remoteAddress, final ChannelFactory channelFactory, final boolean start) {
			
			super();
			
			this.tag = tag;
			this.port = port;
			this.remoteAddress = remoteAddress;
			this.channelFactory = channelFactory;
			this.start = start;
		}
		
		@Override
		public void run() {
			
			final NetworkSession networkSession = new NetworkSession(port);
			
			networkSession.startAsync();
			
			final ChannelBinding binding = new ChannelBinding(remoteAddress, ChannelCodes.ReliableOrdered1);
			final ChannelListener listener = new ChannelListener() {
				
				@Override
				public void handle(final Bytes data) {
					
					final String message = Utils.decode(data);
					
					if (message.equals("ping")) {
						
						System.out.println(tag + ": ping");
						
						networkSession.send(binding, Utils.encode("pong"));
					} 
					
					if (message.equals("pong")) {
						
						System.out.println(tag + ": pong");
						
						networkSession.send(binding, Utils.encode("ping"));
					}
					
					networkSession.send(binding, Utils.encode("garbage"));
				}
			};
			
			networkSession.connect(
					binding, 
					(final ChannelContext context) -> { return new PacketLossChannelDecorator(channelFactory.create(context), 0.2f); }, 
					listener);
			
			if (start) {
			
				networkSession.send(binding, Utils.encode("ping"));
			}
		}
	}
}
