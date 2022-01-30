package com.akka.pso;

import akka.actor.typed.ActorSystem;
import kamon.Kamon;

import java.util.Arrays;
import java.util.List;


public class ApplicationMain {

//  using a square lattice for now

    public static void main(String [] args) {

        Kamon.init();
        List<Integer> latticeProperties = Arrays.asList(100, 100);
        Integer numberOfParticles = 100;

        // The types in the guardian need to be changed

        final ActorSystem<Guardian.Command> system =
                ActorSystem.create(Guardian.create(latticeProperties, numberOfParticles), "guardian");

        system.tell(new Guardian.InitialiseNodes());

        system.terminate();

    }
}
