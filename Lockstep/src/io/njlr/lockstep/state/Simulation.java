package io.njlr.lockstep.state;

/**
 * A stateful representation of the shared-simulation. 
 * 
 * The implementation must be deterministic, but it does not have to be thread-safe. 
 *
 */
public interface Simulation {

	/**
	 * Moves the state of the simulation forward by one tick. 
	 * 
	 * This operation should block until the tick is complete. 
	 * 
	 * All ticks should be deterministic. 
	 */
	void tick();
	
	/**
	 * The hash of all state in the simulation. 
	 * 
	 * This is useful for debugging. 
	 * 
	 * @return The current state hash 
	 */
	int stateHash();
}
