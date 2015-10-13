package io.njlr.lockstep.network.session;

import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import io.njlr.lockstep.network.NetworkAddress;

/**
 * Various settings relevant for a <code>SimulationManager</code>. 
 * 
 * This class is immutable. 
 *
 */
public final class SessionSettings {

	private final int sequenceRunAhead;
	private final int actionScheduleOffset;
	
	private final Set<NetworkAddress> peers;
	
	/**
	 * How many ticks the simulation may run ahead of the slowest peer
	 * 
	 * @return The number of ticks ahead
	 */
	public int sequenceRunAhead() {
		
		return this.sequenceRunAhead;
	}
	
	/**
	 * How many ticks ahead of the slowest peer actions must be scheduled for. 
	 * 
	 * @return The number of ticks ahead
	 */
	public int actionScheduleOffset() {
		
		return this.actionScheduleOffset;
	}
	
	/**
	 * The addresses of all peers in the session (except this one!) 
	 * 
	 * @return The set of peers
	 */
	public Set<NetworkAddress> peers() {
		
		return peers;
	}
	
	public SessionSettings(final int sequenceRunAhead, final int actionScheduleOffset, final Set<NetworkAddress> peers) {
		
		super();
		
		Preconditions.checkArgument(sequenceRunAhead > 0);
		Preconditions.checkArgument(actionScheduleOffset > sequenceRunAhead);
		
		Preconditions.checkNotNull(peers);
		
		this.sequenceRunAhead = sequenceRunAhead;
		this.actionScheduleOffset = actionScheduleOffset;
		
		this.peers = ImmutableSet.copyOf(peers);
	}
	
	@Override
	public int hashCode() {
		
		return sequenceRunAhead * 11 + actionScheduleOffset * 31 + peers.hashCode() * 17;
	}
	
	@Override
	public boolean equals(final Object that) {
		
		if (this == that) {
			
			return true;
		}
		
		if (that instanceof SessionSettings) {
			
			final SessionSettings thatSessionSettings = (SessionSettings) that;
			
			return ((this.sequenceRunAhead == thatSessionSettings.sequenceRunAhead) && 
					(this.actionScheduleOffset == thatSessionSettings.actionScheduleOffset) && 
					(this.peers.equals(thatSessionSettings.peers)));
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		
		return MoreObjects.toStringHelper(this)
				.add("sequenceRunAhead", sequenceRunAhead)
				.add("actionScheduleOffset", actionScheduleOffset)
				.add("peers", peers).toString();
	}
}
