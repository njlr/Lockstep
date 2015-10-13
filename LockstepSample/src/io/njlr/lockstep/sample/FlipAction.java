package io.njlr.lockstep.sample;

import io.njlr.bytes.Bytes;
import io.njlr.lockstep.state.SimulationAction;

/**
 * Action that flips the counter direction. 
 *
 */
public final class FlipAction implements SimulationAction<StrangeSimulation> {
	
	public static final byte leadingByte = (byte)102;
	
	public FlipAction() {
		
		super();
	}

	public void execute(final StrangeSimulation simulation) {
		
		simulation.flip();
	}
	
	@Override
	public Bytes encode() {
		
		return Bytes.of(leadingByte);
	}
	
	@Override
	public int hashCode() {
		
        return 61;
	}
	
	@Override
	public boolean equals(final Object that) {
		
		return (this == that) || (that instanceof FlipAction);
	}

	@Override
	public String toString() {
		
		return "FlipAction{}";
	}
	
	public static FlipAction decode(final Bytes bytes) {
		
		return new FlipAction();
	}
}
