package io.njlr.lockstep.state;

import java.util.Optional;

import io.njlr.bytes.Bytes;

/**
 * A function that can decode actions - or at least attempt to! 
 *
 * @param <T>
 */
@FunctionalInterface
public interface ActionDecoder<T extends Simulation> {

	Optional<SimulationAction<T>> tryDecode(final Bytes content);
}
