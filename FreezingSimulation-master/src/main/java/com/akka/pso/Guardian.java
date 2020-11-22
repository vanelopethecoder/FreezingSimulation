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

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class Guardian {

    public static final ServiceKey<InitialiseNodes> guardianServiceKey =
            ServiceKey.create(InitialiseNodes.class, "guardian");

    private List<Integer> latticeProperties;
    private int numberParticles;
    int xMax = 100;
    int yMax = 100;
    int iteration;
    public  static  final int  TOTAL_TOTAL_ITERATIONS = 100;

    interface Command {
    }

    ;

    static class InitialiseNodes implements Command {
        Boolean start = true;

        public InitialiseNodes() {
        }
    }

    private static class ListingResponse implements Command {
        final Receptionist.Listing listing;

        private ListingResponse(Receptionist.Listing listing) {
            this.listing = listing;
        }
    }

    private final ActorRef<Receptionist.Listing> listingResponseAdapter;
    private final ActorContext<Command> context;


    public Guardian(ActorContext<Command> cxt, List<Integer> latticeProperties, int numberParticles) {
        this.context = cxt;
        this.latticeProperties = latticeProperties;
        this.numberParticles = numberParticles;
        this.listingResponseAdapter =
                cxt.messageAdapter(Receptionist.Listing.class, ListingResponse::new);
    }

    public static Behavior<Command> create(List<Integer> latticeProperties, int numberParticles) {

        return Behaviors.setup(cxt -> new Guardian(cxt, latticeProperties, numberParticles).behavior(cxt));
    }

    private Behavior<Command> behavior(ActorContext<Command> cxt) {
        // todo: we need to spawn the nodes here

        List<ActorRef<NodeParticle.Request>> nodes = new ArrayList<>();
        Random feature = new Random();

        LocalTime now = LocalTime.now();

        this.iteration = 1;

        for (int i = 0; i < this.numberParticles; i++) {
            ActorRef<NodeParticle.Request> nodeParticle =
                    cxt.spawn(NodeParticle.create("Particle" + i, xMax, yMax, TOTAL_TOTAL_ITERATIONS), "name" + i);
            nodes.add(nodeParticle);
        //    nodeParticle.tell(new NodeParticle.RequestCalculateFitness());
        }

        // attempting to wait for all nodes to be created

        if ((nodes.size() >= this.numberParticles - 10) ||
                (LocalTime.now()
                        .minus(Duration.ofSeconds(now.getSecond()))
                        .equals(LocalTime
                                .of(0,0,5)))) {

            nodes.forEach( node  ->
                    node.tell(new NodeParticle.RequestCalculateFitness(iteration))
                    );
        }

        cxt
                .getSystem()
                .receptionist()
                .tell(Receptionist.find(NodeParticle.nodeServiceKey, listingResponseAdapter));

        return Behaviors.receive(Command.class).onMessage(
                InitialiseNodes.class, respnse -> onSpawning(cxt))
                .onMessage(ListingResponse.class, respnse -> onListing(cxt))
                .build();
    }

    private Behavior<Command> onListing(ActorContext<Command> cxt) {

        // tell the particles to broadcast their properties

        return null;
    }


    private Behavior<Command> onSpawning(ActorContext<Command> cxt) {
        return Behaviors.same();
    }

//    @Override
//    public Receive<Command> createReceive() {
//        System.err.println("Helllloooooo");
//     return    newReceiveBuilder()
//                .onMessage(InitialiseNodes.class, this::onSpawning)
//                .build();
//    }
}
