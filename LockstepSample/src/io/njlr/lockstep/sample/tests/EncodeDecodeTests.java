package io.njlr.lockstep.sample.tests;

import org.junit.Test;

import io.njlr.lockstep.sample.FlipAction;
import io.njlr.lockstep.sample.JumpAction;
import io.njlr.lockstep.sample.StrangeSimulation;
import io.njlr.lockstep.sample.StrangeSimulationDecoder;
import io.njlr.lockstep.state.SimulationAction;

public final class EncodeDecodeTests {

	public EncodeDecodeTests() {
		
		super();
	}
	
	@Test
	public void testJumpActionEncodeDecode() {
		
		JumpAction actionIn = new JumpAction(3);
		SimulationAction<StrangeSimulation> actionOut = StrangeSimulationDecoder.tryDecode(actionIn.encode()).get();
		
		assert(actionIn.equals(actionOut));
	}
	
	@Test
	public void testFlipActionEncodeDecode() {
		
		FlipAction actionIn = new FlipAction();
		SimulationAction<StrangeSimulation> actionOut = StrangeSimulationDecoder.tryDecode(actionIn.encode()).get();
		
		assert(actionIn.equals(actionOut));
	}
}
