package com.akka.pso;

import akka.actor.typed.ActorSystem;
import kamon.Kamon;

import java.util.Arrays;
import java.util.List;


public class ApplicationMain {

//  using a square lattice for now

    public static void main(String [] args) {

        Kamon.init();
        List<Integer> latticeProperties = Arrays.asList(80, 100);
        Integer numberOfParticles = 80;

        final ActorSystem<Guardian> system =
                ActorSystem.create(Guardian.create(latticeProperties, numberOfParticles), "guardian");

        system.terminate();

    }
}
