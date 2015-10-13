package io.njlr.lockstep.sample;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;

import io.njlr.lockstep.network.NetworkAddress;
import io.njlr.lockstep.network.session.NetworkSession;
import io.njlr.lockstep.network.session.SessionSettings;
import io.njlr.lockstep.state.SimulationManager;

/**
 * Example of how everything fits together. 
 *
 */
public final class Main {
	
	private static final Logger logger = Logger.getLogger("");

	public static void main(final String[] args) throws Exception {
		
		Logger.getLogger("").setLevel(Level.OFF);
		
		final int port = Integer.parseInt(args[0]);
		final int theirPort = Integer.parseInt(args[1]);
		
		logger.info("port: " + port);
		logger.info("remote port: " + theirPort);
		
		final NetworkSession networkSession = new NetworkSession(port);
		
		final SessionSettings settings = new SessionSettings(1, 3, ImmutableSet.of(new NetworkAddress(InetAddress.getLocalHost(), theirPort)));
		
		final SimulationManager<StrangeSimulation> simulationManager = new SimulationManager<StrangeSimulation>(
				networkSession, settings, StrangeSimulationDecoder::tryDecode, new StrangeSimulation("A"));
		
		networkSession.startAsync();
		simulationManager.startAsync();
	}
}
