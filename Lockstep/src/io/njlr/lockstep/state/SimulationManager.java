package io.njlr.lockstep.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

import io.njlr.bytes.Bytes;
import io.njlr.bytes.BytesBuilder;
import io.njlr.bytes.BytesReader;
import io.njlr.lockstep.network.ChannelListener;
import io.njlr.lockstep.network.NetworkAddress;
import io.njlr.lockstep.network.channels.StopAndWaitChannel;
import io.njlr.lockstep.network.sequence.Sequence32;
import io.njlr.lockstep.network.session.ChannelBinding;
import io.njlr.lockstep.network.session.ChannelCodes;
import io.njlr.lockstep.network.session.NetworkSession;
import io.njlr.lockstep.network.session.SessionSettings;

/**
 * Maintains the synchronisation of a simulation across a network. 
 * 
 * This class handles: 
 *  - Connecting to peers
 *  - The relay of commands
 *  - The execution of ticks
 *
 * All methods are non-blocking. 
 *
 * @param <T> The sub-type of <code>Simulation</code>
 */
public final class SimulationManager<T extends Simulation> extends AbstractExecutionThreadService {
	
	private static final Logger logger = Logger.getLogger(SimulationManager.class.getName());
	
	public static final byte sequenceNumberUpdateMessageLeadingByte = (byte) 17;
	public static final byte sequencedActionMessageLeadingByte = (byte) 98; 
	
	private final Object lock = new Object();
	
	private final NetworkSession session;
	private final SessionSettings settings;
	private final ActionDecoder<T> decoder;
	private final T simulation;
	
	private final Map<NetworkAddress, ChannelBinding> peerBindings;
	private final Map<NetworkAddress, Integer> peerProgress;
	
	private final List<SequencedAction<T>> actions;
	
	private volatile boolean keepRunning;
	
	private volatile int sequenceNumber;
	
	/**
	 * Creates a new <code>SimulationManager</code> instance. 
	 * 
	 * @param session The network session to communicate via
	 * @param settings The settings to use
	 * @param decoder The decoder for received simulation actions
	 * @param simulation The simulation to manage 
	 */
	public SimulationManager(final NetworkSession session, final SessionSettings settings, final ActionDecoder<T> decoder, final T simulation) {
		
		super();
		
		this.session = session;
		this.settings = settings;
		this.decoder = decoder;
		this.simulation = simulation;
		
		peerBindings = new HashMap<>();
		peerProgress = new HashMap<>();
		
		for (final NetworkAddress peer : settings.peers()) {
			
			peerProgress.put(peer, 0);
		}
		
		actions = new ArrayList<>();
	}
	
	/**
	 * Submit an action to be performed on the simulation. 
	 * 
	 * The action will be automatically scheduled across peers. 
	 * 
	 * @param action The action to perform
	 */
	public void submitAction(final SimulationAction<T> action) {
		
		if (!keepRunning) {
			
			return;
		}
		
		// Which sequence number should this action be executed on? 
		final int actionSequenceNumber = actionSequenceNumber();
		
		logger.fine("@" + sequenceNumber + ": Submitting " + new SequencedAction<>(actionSequenceNumber, action));
		
		// Queue the action for local execution
		queue(new SequencedAction<>(actionSequenceNumber, action));
		
		// Notify all peers... 
		final Bytes message = new BytesBuilder()
				.append(sequencedActionMessageLeadingByte)
				.appendInt(actionSequenceNumber)
				.append(action.encode())
				.toBytes();
		
		for (final NetworkAddress peer : settings.peers()) {
			
			session.send(peerBindings.get(peer), message);
		}
	}
	
	@Override
	protected void startUp() throws Exception {
		
		super.startUp();
		
		synchronized (lock) {
		
			// Open a channel to each peer
			for (final NetworkAddress peer : settings.peers()) {
				
				final ChannelBinding binding = new ChannelBinding(peer, ChannelCodes.ReliableOrdered1);
				final ChannelListener listener = (final Bytes message) -> { onMessageReceived(binding, message); };
				
				session.connect(binding, StopAndWaitChannel::create, listener);
				
				peerBindings.put(peer, binding);
				peerProgress.put(peer, 0);
			}
			
			// Mark the service as running
			keepRunning = true;
		}
		
		logger.info("Started");
	}
	
	@Override
	protected void run() throws Exception {
		
		while (keepRunning) {
			
			while (Sequence32.isMoreRecent(targetSequenceNumber(), sequenceNumber) && keepRunning) {
				
				synchronized (lock) {
					
					final List<SimulationAction<T>> tickActions = new ArrayList<>();
					
					for (int i = 0; i < actions.size(); i++) {
						
						final SequencedAction<T> action = actions.get(i);
						
						if (action.sequenceNumber() == sequenceNumber) {
							
							tickActions.add(action.action());
							
							actions.remove(i);
							
							i--;
						} else {
							
							logger.finest("@" + sequenceNumber + ": Skipping" + action);
						}
					}
					
					
					// Sort by hashCode so that they are in uniform order between peers
					tickActions.sort((x, y) -> { return x.hashCode() - y.hashCode(); });
					
					// Execute each sequentially
					for (final SimulationAction<T> action : tickActions) {
					
						logger.fine("@" + sequenceNumber + ": Executing" + action);
						
						action.execute(simulation);
					}
				}
				
				// Tick the simulation
				simulation.tick();
				
				synchronized (lock) {
					
					// Update our sequence number
					sequenceNumber = Sequence32.next(sequenceNumber);
					
					logger.finer("@" + sequenceNumber + ": Updated sequence number");
				}
				
				// Notify all peers of our progress
				final Bytes message = new BytesBuilder(5)
						.append(sequenceNumberUpdateMessageLeadingByte)
						.appendInt(sequenceNumber)
						.toBytes();
				
				for (final NetworkAddress peer : settings.peers()) {
				
					session.send(peerBindings.get(peer), message);
				}
			}
			
			if (keepRunning) {
			
				synchronized (lock) {
					
					lock.wait();
				}
			}
		}
	}
	
	@Override
	protected void triggerShutdown() {
		
		super.triggerShutdown();
		
		logger.info("Shutdown triggered");
		
		synchronized (lock) {
			
			keepRunning = false;
			
			lock.notifyAll();
		}
	}
	
	@Override
	protected void shutDown() throws Exception {
		
		super.shutDown();
		
		synchronized (lock) {
			
			actions.clear();
		}
	}
	
	private void onMessageReceived(final ChannelBinding binding, final Bytes content) {
		
		if (!keepRunning) {
			
			return;
		}
		
		final BytesReader reader = content.read();
		
		final byte leadingByte = reader.readByte();
		
		if (leadingByte == sequenceNumberUpdateMessageLeadingByte) {
			
			reportPeerProgress(binding.remoteAddress(), reader.readInt());
		} else if (leadingByte == sequencedActionMessageLeadingByte) {
			
			final int actionSequenceNumber = reader.readInt();
			
			if (!Sequence32.isMoreRecent(actionSequenceNumber, sequenceNumber)) {
				
				logger.warning("@" + sequenceNumber + ": Received an action for " + actionSequenceNumber);
			}
			
			final Bytes actionData = reader.readRemaining();
			
			final Optional<SimulationAction<T>> action = decoder.tryDecode(actionData);
			
			if (action.isPresent()) {
				
				logger.finer("@" + sequenceNumber + ": Received " + new SequencedAction<>(actionSequenceNumber, action.get()));
				
				queue(new SequencedAction<>(actionSequenceNumber, action.get()));
			} else {
				
				logger.warning("@" + sequenceNumber + ": Could not decode action. ");
			}
		} else {
			
			logger.warning("@" + sequenceNumber + ": Could not parse data. ");
		}
	}
	
	/**
	 * Updates the known progress of a peer
	 * 
	 * @param peer The peer to update
	 * @param reportedSequenceNumber The peer's reported sequence number
	 */
	private void reportPeerProgress(final NetworkAddress peer, final int reportedSequenceNumber) {
		
		Preconditions.checkNotNull(peer);
		Preconditions.checkArgument(settings.peers().contains(peer));
		
		synchronized (lock) {
			
			logger.finer("@" + sequenceNumber + ": " + peer.toString() + " has reportedly reached " + reportedSequenceNumber);
			
			peerProgress.put(peer, reportedSequenceNumber);
			
			lock.notifyAll();
		}
	}
	
	/**
	 * Queues an action for later execution. 
	 * 
	 * @param action The action to queue
	 */
	private void queue(final SequencedAction<T> action) {
		
		Preconditions.checkNotNull(action);
		
		synchronized (lock) {
			
			logger.finer("@" + sequenceNumber + ": Adding " + action + " to the action queue");
		
			Preconditions.checkArgument(!Sequence32.isMoreRecent(targetSequenceNumber(), action.sequenceNumber()));
			
			actions.add(action);
		}
	}
	
	/**
	 * The lowest sequence across all peers. 
	 * 
	 * This is used to determine how many ticks we can run. 
	 * 
	 * @return The lowest sequence number
	 */
	private int lowestSequenceNumber() {
		
		synchronized (lock) {
			
			int lowestSequenceNumber = this.sequenceNumber;
			
			for (final int i : peerProgress.values()) {
				
				if (Sequence32.isMoreRecent(lowestSequenceNumber, i)) {
					
					lowestSequenceNumber = i;
				}
			}
			
			return lowestSequenceNumber;
		}
	}
	
	/**
	 * The sequence number that we can safely tick up to. 
	 * 
	 * @return The target sequence number
	 */
	private int targetSequenceNumber() {
		
		return Sequence32.add(lowestSequenceNumber(), settings.sequenceRunAhead());
	} 
	
	/**
	 * The sequence number that actions should be scheduled for. 
	 * 
	 * @return The sequence number to schedule to
	 */
	private int actionSequenceNumber() {
		
		return Sequence32.add(lowestSequenceNumber(), settings.actionScheduleOffset());
	}
}
