package io.njlr.lockstep.sample;

import io.njlr.bytes.Bytes;
import io.njlr.bytes.BytesBuilder;
import io.njlr.lockstep.state.SimulationAction;

/**
 * Action that jumps the counter by the specified amount
 *
 */
public final class JumpAction implements SimulationAction<StrangeSimulation> {
	
	public static final byte leadingByte = (byte)101;

	private final int count;
	
	public int getCount() {
		
		return count;
	}
	
	public JumpAction(final int count) {
		
		super();
		
		this.count = count;
	}
	
	public void execute(final StrangeSimulation simulation) {
		
		simulation.jump(count);
	}
	
	public Bytes encode() {
		
		return new BytesBuilder(5).append(leadingByte).appendInt(count).toBytes();
	}
	
	@Override
	public int hashCode() {
		
        return 37 * count;
	}
	
	@Override
	public boolean equals(final Object that) {
		
		if (this == that) {
			
			return true;
		} 
		
		if (that instanceof JumpAction) {
			
			return this.count == ((JumpAction) that).count;
		}
		
		return false;
	}

	@Override
	public String toString() {
		
		return new StringBuilder().append("JumpAction{").append(count).append("}").toString();
	}
	
	public static JumpAction decode(final Bytes bytes) {
		
		return new JumpAction(bytes.read().skip(1).readInt());
	}
}
