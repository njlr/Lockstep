package io.njlr.lockstep.network.session;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

import io.njlr.bytes.Bytes;
import io.njlr.bytes.BytesBuilder;
import io.njlr.lockstep.network.Channel;
import io.njlr.lockstep.network.ChannelContext;
import io.njlr.lockstep.network.ChannelFactory;
import io.njlr.lockstep.network.ChannelListener;
import io.njlr.lockstep.network.NetworkAddress;

/**
 * NetworkSession is a layer on top of NetworkService that handles the concepts of peers and channels. 
 * 
 * Each channel is assigned a byte identifier to allow different concurrent transports with a peer. 
 *
 */
public final class NetworkSession extends AbstractExecutionThreadService {
	
	private static final int maximumPacketSize = 1400;
	
	private static final Logger logger = Logger.getLogger(NetworkSession.class.getName());
	
	private final Object lock = new Object();
	
	private final int port;
	
	private final Map<ChannelBinding, Channel> bindings;
	private final ExecutorService executorService;
	
	private volatile DatagramSocket socket;
	
	private volatile boolean keepRunning;
	
	/**
	 * Gets a snapshot of the currently connected peers. 
	 * 
	 * @return The currently connected peers
	 */
	public Set<NetworkAddress> peers() {
		
		synchronized (lock) {
			
			return bindings.keySet().stream().map(x -> x.remoteAddress()).collect(Collectors.toSet());
		}
	}
	
	/**
	 * Gets a snapshot of the current bindings. 
	 * 
	 * @return The current bindings
	 */
	public Set<ChannelBinding> bindings() {
		
		synchronized (lock) {

			return ImmutableSet.copyOf(bindings.keySet());
		}
	}
	
	/**
	 * Gets a snapshot of the current channels. 
	 * 
	 * @return The current channels
	 */
	public Set<Channel> channels() {
		
		synchronized (lock) {

			return ImmutableSet.copyOf(bindings.values());
		}
	}
	
	public NetworkSession(final int port) {
		
		super();
		
		Preconditions.checkArgument(port >= 0);
		Preconditions.checkArgument(port <= 65535);
		
		this.port = port;
		
		bindings = new HashMap<>();
		executorService = Executors.newCachedThreadPool(); // TODO: Param?
	}
	
	@Override
	protected void startUp() throws Exception {
		
		super.startUp();
		
		logger.info("Starting... ");
		
		synchronized (lock) {
			
			keepRunning = true;
			
			socket = new DatagramSocket(port);
		}
	}
	
	/**
	 * Opens a new connection on the given binding.  
	 * 
	 * @param binding The binding to use; must not be already taken
	 * @param channelFactory The factory to use in creating the <code>Channel</code>
	 * @param listener Message callback for receiving messages. Must be thread-safe. 
	 */
	public void connect(final ChannelBinding binding, final ChannelFactory channelFactory, final ChannelListener listener) {
		
		Preconditions.checkNotNull(binding);
		Preconditions.checkNotNull(channelFactory);
		Preconditions.checkNotNull(listener);
		
		final Channel channel = channelFactory.create(
				new ChannelContext() {
					
					@Override
					public void sendMessage(final Bytes content) {
						
						final Bytes message = new BytesBuilder(content.length() + 1).append(binding.channelCode()).append(content).toBytes();
						
						try {
							
							socket.send(new DatagramPacket(message.array(), message.length(), binding.remoteAddress().host(), binding.remoteAddress().port()));
						} catch (final IOException e) {
							
							logger.warning(e.getMessage());
						}
					}
					
					@Override
					public void takeMessage(final Bytes message) {
						
						executorService.submit(() -> { listener.handle(message); });
					}
				});
		
		synchronized (lock) {
			
			if (!keepRunning) {
				
				return;
			}
			
			// Prevent "re-bindings" from occurring
			Preconditions.checkState(!bindings.containsKey(binding));
			
			bindings.put(binding, channel);
		}
	}
	
	/**
	 * Sends a message on the <code>Channel</code> with the given binding. 
	 * 
	 * The delivery guarantees will depend on the <code>Channel</code>. 
	 * 
	 * @param binding The binding to send on
	 * @param message The message to send
	 */
	public void send(final ChannelBinding binding, final Bytes message) {
		
		Preconditions.checkNotNull(binding);
		Preconditions.checkNotNull(message);
		
		synchronized (lock) {
			
			bindings.get(binding).send(message);
		}
	}
	
	@Override
	protected void run() throws Exception {
		
		final DatagramPacket packet = new DatagramPacket(new byte[maximumPacketSize], maximumPacketSize);
		
		while (keepRunning) { 
			
			socket.receive(packet);
			
			logger.finest("Received " + packet);
			
			synchronized (lock) {
				
				// Find out which binding the message is for
				Channel channel = null;
				
				for (final Entry<ChannelBinding, Channel> i : bindings.entrySet()) {
					
					boolean isMatch = (
							(i.getKey().remoteAddress().host().equals(packet.getAddress())) && 
							(i.getKey().remoteAddress().port() == packet.getPort()) && 
							(i.getKey().channelCode() == packet.getData()[0]));
					
					if (isMatch) {
						
						channel = i.getValue();
						
						break;
					}
				}
				
				if (channel == null) {
					
					logger.warning("No binding found for " + packet);
				} else {
					
					// Strip off the "split" byte; downstream handlers should be unaware of it
					channel.handle(Bytes.of(packet.getData()).sub(1));
				}
			}
		}
	}
	
	@Override
	protected void triggerShutdown() {
		
		super.triggerShutdown();
		
		synchronized (lock) {
		
			keepRunning = false;
		}
	}
	
	@Override
	protected void shutDown() throws Exception {
		
		super.shutDown();
		
		synchronized (lock) {
			
			socket.close();
			
			executorService.shutdown();
			
			for (final Channel channel : bindings.values()) {
				
				channel.close();
			}
			
			bindings.clear();
		}
	}
}
