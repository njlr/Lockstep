package io.njlr.lockstep.sample;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import io.njlr.bytes.Bytes;
import io.njlr.lockstep.state.SimulationAction;

/**
 * A decoder implementation for <code>SimulationAction<StrangeSimulation></code> instances. 
 *
 */
public final class StrangeSimulationDecoder {
	
	private StrangeSimulationDecoder() {
		
		super();
	}
	
	/**
	 * Mapping of <code>byte</code> identifiers to decoding functions. 
	 * 
	 * All action types should be listed here. 
	 * 
	 */
	private static final Map<Byte, Function<Bytes, SimulationAction<StrangeSimulation>>> decoders = ImmutableMap.of(
			JumpAction.leadingByte, JumpAction::decode, 
			FlipAction.leadingByte, FlipAction::decode);

	public static Optional<SimulationAction<StrangeSimulation>> tryDecode(final Bytes content) {
		
		final Function<Bytes, SimulationAction<StrangeSimulation>> decoder = decoders.get(content.get(0));
		
		if (decoder != null) {
			
			return Optional.of(decoder.apply(content));
		} else {
			
			return Optional.empty();
		}
	}
}
