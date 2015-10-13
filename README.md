# Lockstep
A Java framework for running interactive simulations across a network. 

## About

There are two approaches for running large simulations across a network: 

 1. Client-server: One participant runs the whole simulation and sends periodic state-updates to all clients. The server is the single-source-of-truth. 
 2. Peer-to-peer: Every participant runs the simulation deterministicly. Interactive commands must be coordinated to ensure everyone is in-sync. 

The problem with the former approach is that it scales poorly with simulation size. The problem with the latter approach is that it scales poorly with participant count. The best trade-off will be application specific. 

This is a framework for the peer-to-peer approach. 

## Overview

 * Peers report their progress to each other
 * Each peer only runs `n` ticks ahead of the slowest peer, waiting if necessary
 * Actions are scheduled `>n` ticks ahead of the slowest peer, ensuring they are always executed at the right time
 
The framework takes care of all of this. 

The programmer must ensure: 

 * The simulation is deterministic
 * Actions can be encoded and decoded properly


## Future Work

 * Use [sliding window protocol](https://en.wikipedia.org/wiki/Sliding_window_protocol) as the reliable transport mechanism for greater throughput 
 * Implement channel layers e.g. for fragmenting large packets
 * Add a channel-timeout mechanism 
 * Allow for the coordinated dropping of peers (e.g. participant crashes, quits)
 * Provide automatic state hash checking e.g. by including the hash with sequence number updates
 * Build a more interesting example simulation

## See Also

 * [Wikipedia](https://en.wikipedia.org/wiki/Lockstep_protocol)
 * [1500 Archers on a 288 Network](http://www.gamasutra.com/view/feature/131503/1500_archers_on_a_288_network_.php)



