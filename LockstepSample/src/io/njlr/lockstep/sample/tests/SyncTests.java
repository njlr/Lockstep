package io.njlr.lockstep.sample.tests;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;

import io.njlr.lockstep.network.NetworkAddress;
import io.njlr.lockstep.network.session.NetworkSession;
import io.njlr.lockstep.network.session.SessionSettings;
import io.njlr.lockstep.sample.FlipAction;
import io.njlr.lockstep.sample.JumpAction;
import io.njlr.lockstep.sample.StrangeSimulation;
import io.njlr.lockstep.sample.StrangeSimulationDecoder;
import io.njlr.lockstep.state.SimulationManager;

public final class SyncTests {
	
	private static final Logger logger = Logger.getLogger("");
	
	public static void main(final String[] args) throws UnknownHostException {
		
		logger.setLevel(Level.OFF);
		
		new SyncTests().testSynchronization();
	}

	public void testSynchronization() throws UnknownHostException {
		
		final int portA = 1234;
		final int portB = 4567;
		
		final ExecutorService executorService = Executors.newFixedThreadPool(2);
		
		executorService.submit(new Runner("A", portA, new NetworkAddress(InetAddress.getLocalHost(), portB)));
		executorService.submit(new Runner("                                                                         B", portB, new NetworkAddress(InetAddress.getLocalHost(), portA)));
	}
	
	private final class Runner implements Runnable {

		private final String tag;
		private final int port;
		private final NetworkAddress remoteAddress;
		
		public Runner(final String tag, final int port, final NetworkAddress remoteAddress) {
			
			super();
			
			this.tag = tag;
			this.port = port;
			this.remoteAddress = remoteAddress;
		}
		
		@Override
		public void run() {
			
			final NetworkSession networkSession = new NetworkSession(port);
			
			final SessionSettings settings = new SessionSettings(1, 3, ImmutableSet.of(remoteAddress));
			
			final SimulationManager<StrangeSimulation> simulationManager = new SimulationManager<StrangeSimulation>(
					networkSession, settings, StrangeSimulationDecoder::tryDecode, new StrangeSimulation(tag));
			
			networkSession.startAsync();
			simulationManager.startAsync();
			
			final Random random = new Random();
			
			while (true && tag == "A") {
				
				try {
					
					Thread.sleep(random.nextInt(7000));
				} catch (final InterruptedException e) {
					
					e.printStackTrace();
				}
				
				if (random.nextBoolean()) {
					
					simulationManager.submitAction(new JumpAction(3));
				} else {
					
					simulationManager.submitAction(new FlipAction());
				}
			}
		}
	}
}
