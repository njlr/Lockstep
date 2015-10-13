package io.njlr.lockstep.sample;

import java.util.Random;

import io.njlr.lockstep.state.Simulation;

/**
 * A simple simulation for easy debugging. 
 * 
 * A counter ticks up or down depending on a toggle. 
 * The counter can be "jumped" by any participant. 
 *
 */
public final class StrangeSimulation implements Simulation {
	
	private final Random random = new Random();

	private final String tag;
	
	private boolean toggle;
	
	private int tick;
	private int counter;
	
	public StrangeSimulation(final String tag) {
		
		super();
		
		this.tag = tag;
		
		toggle = false;
		
		tick = 0;
		counter = 0;
	}

	@Override
	public void tick() {
		
		if (toggle) {
			
			counter--;
		} else {
			
			counter++;
		}
		
		// Sleep to simulate a processor-intensive task
		try {
			
			Thread.sleep(random.nextInt(3000));
		} catch (final InterruptedException e) {
			
			e.printStackTrace();
		}
		
		System.out.println(tag + "@" + tick + ": Counter: " + counter);
		
		tick++;
	}

	@Override
	public int stateHash() {
		
		return counter ^ 31 * (toggle ? 17 : 11);
	}
	
	public void jump(final int jump) {
		
		System.out.println(tag + "@" + tick + ": Jump: " + jump);
		
		if (toggle) {
		
			counter += jump;
		} else {
			
			counter -= jump;
		}
	}
	
	public void flip() {
		
		toggle = !toggle;
		
		System.out.println(tag + "@" + tick + ": Toggle: " + toggle);
	}
}
