package io.njlr.lockstep.state;

import io.njlr.bytes.Bytes;

/**
 * An action that can be performed on a simulation of type T. 
 * 
 * It is important that implementers be immutable and implement hashCode. 
 * 
 * Implementations should be immutable and thread-safe. 
 *
 * @param <T>
 */
public interface SimulationAction<T extends Simulation> {
	
	/**
	 * Performs this action on the given simulation. 
	 * 
	 * @param simultion The simulation to act on
	 */
	void execute(final T simultion);

	/**
	 * Encodes this action as a sequence of bytes
	 * 
	 * @return The encoded action
	 */
	Bytes encode();
}
