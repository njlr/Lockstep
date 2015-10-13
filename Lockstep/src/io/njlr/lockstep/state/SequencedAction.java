package io.njlr.lockstep.state;

import java.util.Optional;

import com.google.common.base.MoreObjects;

import io.njlr.bytes.Bytes;
import io.njlr.bytes.BytesBuilder;

/**
 * An action to be executed at a specific sequence number. 
 *
 * @param <T>
 */
public final class SequencedAction<T extends Simulation> {

	private final int sequenceNumber;
	
	private final SimulationAction<T> action;
	
	/**
	 * The sequence number at which the action should be performed
	 * 
	 * @return The sequence number
	 */
	public int sequenceNumber() {
		
		return sequenceNumber;
	}
	
	/**
	 * The action to be performed
	 * 
	 * @return The action
	 */
	public SimulationAction<T> action() {
		
		return action;
	}
	
	public SequencedAction(final int sequenceNumber, final SimulationAction<T> action) {
		
		super();
		
		this.sequenceNumber = sequenceNumber;
		this.action = action;
	}
	
	public Bytes encode() {
		
		return new BytesBuilder().appendInt(sequenceNumber).append(action.encode()).toBytes();
	}
	
	@Override
	public int hashCode() {
		
		return sequenceNumber * 11 + action.hashCode() * 17;
	}
	
	@Override
	public boolean equals(final Object that) {
		
		if (this == that) {
			
			return true;
		}
		
		if (that instanceof SequencedAction) {
			
			final SequencedAction<?> thatSequencedAction = (SequencedAction<?>) that;
			
			return ((this.action.equals(thatSequencedAction.action)) && 
					(this.sequenceNumber == thatSequencedAction.sequenceNumber));
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		
		return MoreObjects.toStringHelper(this).addValue(sequenceNumber).addValue(action).toString();
	}
	
	public static <T extends Simulation> SequencedAction<T> of(final int sequenceNumber, final SimulationAction<T> action) {
		
		return new SequencedAction<>(sequenceNumber, action);
	}
	
	public static <T extends Simulation> Optional<SequencedAction<T>> decode(final Bytes data, final ActionDecoder<T> decoder) {
		
		final Optional<SimulationAction<T>> action = decoder.tryDecode(data);
		
		if (action.isPresent()) {
			
			final int sequenceNumber = data.read().readInt();
			
			return Optional.of(new SequencedAction<T>(sequenceNumber, action.get()));
		} else {
			
			return Optional.empty();
		}
	}
}
