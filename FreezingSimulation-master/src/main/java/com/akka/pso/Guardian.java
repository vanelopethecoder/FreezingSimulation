package com.akka.pso;

import NodeModels.NodeParticle;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;

import java.math.BigDecimal;
import java.util.*;

public class Guardian extends AbstractBehavior<Guardian.StartFitness> {

    public static final ServiceKey<Guardian.StartFitness> guardianServiceKey =
            ServiceKey.create(Guardian.StartFitness.class, "guardian");

    List<Integer> latticeProperties;
    int numberParticles;

    public Guardian(ActorContext<StartFitness> context) {
        super(context);
    }

    public Guardian(ActorContext<StartFitness> cxt, List<Integer> latticeProperties, int numberParticles) {
        super(cxt);
        this.latticeProperties = latticeProperties;
        this.numberParticles = numberParticles;
    }

    static class StartFitness {
        Boolean start = true;

        public StartFitness() {
        }
    }

    public static Behavior<StartFitness> create(List<Integer> latticeProperties, int numberParticles) {

        return Behaviors.setup(
                cxt -> {
                    cxt.getSystem()
                            .receptionist()
                            .tell(Receptionist.register(guardianServiceKey, cxt.getSelf()));
                    return new Guardian(cxt, latticeProperties, numberParticles).behavior(cxt);
                }
        );
    }

    private Behavior<StartFitness> behavior(ActorContext<StartFitness> cxt) {
        // todo: we need to spawn the nodes here

        Map<ActorRef<NodeParticle.RequestCalculateFitness>, List<Double>> nodes = new HashMap<>();

        Random feature = new Random();

        return Behaviors.receive(StartFitness.class).onMessage(
                StartFitness.class, this::onSpawning).build();
    }

    private Behavior<StartFitness> onSpawning(StartFitness a) {
        return this;
    }

    @Override
    public Receive<Guardian.StartFitness> createReceive() {
        return null;
    }
}
