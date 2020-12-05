package NodeModels;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import org.decimal4j.util.DoubleRounder;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

public class NodeParticle extends AbstractBehavior<NodeParticle.Request> {

    // define MessageType receivers

    public static final ServiceKey<Request> nodeServiceKey =
            ServiceKey.create(Request.class, "particle");

    String myName;
    BigDecimal fitness;
    ActorRef<Request> pBest;
    NodeParticle gBest;
    ParticleProperties particleProperties;
    private final StashBuffer<Request> buffer;
    int x;
    int y;
    int particlesSent;
    int iteration;
    int totalIterations;

    private final ActorRef<Receptionist.Listing> listingResponseAdapter;

    public NodeParticle(ActorContext<Request> context, StashBuffer<Request> buffer , String myName,
                         int xMax, int yMax, int totalIterations) {
        super(context);
        this.buffer = buffer;
        this.particleProperties = new ParticleProperties();
        this.particleProperties.setVelocity(BigDecimal.valueOf(
                DoubleRounder.round(Math.random() * (8 - 0.1 + 1) + 0.1, 2)));

        this.fitness = BigDecimal.ZERO;
        this.myName = myName;
        this.x = (int) (Math.random() * ((xMax) + 1));
        this.y = (int) (Math.random() * ((yMax) + 1));

        this.particleProperties.setX(this.x);
        this.particleProperties.setY(this.y);
        this.totalIterations = totalIterations;
        this.iteration = 1;
        this.listingResponseAdapter =
                context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);

        System.out.println("My name: " + myName + " velocity: " + this.particleProperties.getVelocity());

    }

    @Override
    public Receive<Request> createReceive() {

        // try to investigate why the messages are not coming here

        return null;
    }

    enum UpdateMyVelocity implements Request {
        INSTANCE
    }

    enum CalculateFA_again implements Request {
        INSTANCE
    }


    public interface Request {
    }

    public static class RequestCalculateFitness implements Request {

        Map<NodeParticle, ParticleProperties> swarmParticles;
        int iteration;

        public RequestCalculateFitness(int iteration) {
            this.swarmParticles = swarmParticles;
            System.out.println("creating swarm particles");
            this.iteration = iteration;
        }
    }

    public static class CalculateForceOfAttraction implements Request {

        ParticleProperties particleProperties;
        int x;
        int y;
        ActorRef<Request> nodeParticleActor;

        public CalculateForceOfAttraction(ParticleProperties particleProperties, int x, int y,
                                          ActorRef<Request> nodeParticleActorRef) {
            this.particleProperties = particleProperties;
            this.x = x;
            this.y = y;
            this.nodeParticleActor = nodeParticleActorRef;
        }
    }

    public static class UpdateVelocity implements Request {

        Boolean localBest;
        Boolean globalBest;
        int iteration;
        int totalIterations;

        public UpdateVelocity(Boolean localBest, Boolean globalBest, int iteration, int totalIterations) {
            this.localBest = localBest;
            this.globalBest = globalBest;
            this.iteration = this.iteration;
            this.totalIterations = totalIterations;
        }
    }

    public static class ListingResponse implements Request {
        final Receptionist.Listing listing;

        public ListingResponse(Receptionist.Listing listing) {
            System.out.println("listing constructor");
            this.listing = listing;
        }
    }

    public static Behavior<Request> create(String myName,
                                           int xmax, int ymax, int totalIterations) {
        System.out.println("Hi there I am creating a typed Node");
        return Behaviors.withStash(
            100,
            stash ->
        Behaviors.setup(
                context -> {
                    context
                            .getSystem()
                            .receptionist()
                            .tell(Receptionist.register(nodeServiceKey, context.getSelf()));
                    return new NodeParticle(context, stash, myName,
                            xmax, ymax, totalIterations).behavior(context);
                }
        ));

    }

    private Behavior<Request> behavior(ActorContext<Request> context) {
        return
                Behaviors.receive(NodeParticle.Request.class)
                        // .onMessage(NodeParticle.Request.class, this::onMessageReceived)
                        .onMessage(RequestCalculateFitness.class, response -> broadcastProperties(response, context))
                        .onMessage(ListingResponse.class, response -> onListing(response.listing, context))
                        .onMessage(CalculateForceOfAttraction.class, response -> onCalculateFA(response, context, Duration.ofMillis(5)))
                        //.onMessage(UpdateVelocity.class, response -> updateParticleProperties(response, context))
                        .build();
    }

    private Behavior<Request> updateParticleProperties(/*UpdateVelocity response,*/ ActorContext<Request> context, Duration duration) {

        //  if (response.localBest && !response.globalBest) {

        System.out.println("PLEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASSSSSSSSSSSSSSSSSSSSSEEEE");

        this.getContext().scheduleOnce(duration, context.getSelf(), CalculateFA_again.INSTANCE);

        // make sure that calculatFA requests get stashed

        BigDecimal vel = this.particleProperties.getVelocity();

        // should i store the localBest values?
        // or should i ask for them afterwards?
        // im leaning towards storing their props

        BigDecimal newVel = vel.subtract(BigDecimal.valueOf(/*this.iteration / this.totalIterations*/ 1 *
                Math.pow(this.particleProperties.p_best_properties.x - this.x, 2)
                + Math.pow(this.particleProperties.p_best_properties.y - this.y, 2)));

        this.particleProperties.setVelocity(newVel);

        System.out.println("My name: " + myName + " updated velocity: " + this.particleProperties.getVelocity()  +
                "  p_best_x = " + this.particleProperties.p_best_properties.x  + "  p_best_y = "  + this.particleProperties.p_best_properties.y
                + "  p_best_velocity = " + this.particleProperties.p_best_properties.getVelocity()
                );

        //  }


        // you could make a generic method to make it listen again

//               buffer.stash(Request message);
//

//        Behaviors.receive(Request.class)
//                .onMessageEquals(CalculateFA_again.INSTANCE, () -> onCalculateFA(this.getContext(), Duration.ofMillis(3)))
//                .build();

      return   Behaviors.same();

    }

    private Behavior<Request> onCalculateFA(CalculateForceOfAttraction calculateForceOfAttraction,
                                            ActorContext<Request> context, Duration duration) {

        // ctx.scheduleOnce(duration, ctx.getSelf(), Receive.INSTANCE);
        this.getContext().scheduleOnce(duration, context.getSelf(), UpdateMyVelocity.INSTANCE);

        // exception handling

        System.out.println("CONFIRMING THE CALCULATE FA PORTION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        BigDecimal sumVelocity = calculateForceOfAttraction.particleProperties.getVelocity()
                .add(this.particleProperties.getVelocity());

        int euclideanDistance = (calculateForceOfAttraction.x - this.x) * (calculateForceOfAttraction.x - this.x) +
                (calculateForceOfAttraction.y - this.y) * (calculateForceOfAttraction.y - this.y);

        BigDecimal fa = BigDecimal.valueOf(euclideanDistance).subtract(sumVelocity);

        // euclidean distance - (sum of velocities)

        if ((this.fitness.compareTo(BigDecimal.ZERO) == 0) || (fa.compareTo(this.fitness) >= 0)) {
            this.fitness = fa;
            //   this.pBest = actor red, you need the actor reference.
            this.pBest = calculateForceOfAttraction.nodeParticleActor;
            this.particleProperties.p_best_properties = calculateForceOfAttraction.particleProperties;

        }

//
//        return Behaviors.withTimers(
//                timers -> {
//                    // State timeouts done with withTimers
//                    timers.startSingleTimer("Timeout", UpdateMyVelocity.INSTANCE, Duration.ofSeconds(1));
//                    return Behaviors.receive(Request.class)
//                            .onMessage(UpdateMyVelocity.class, message -> activeOnFlushOrTimeout(data))
//                            .build();
//                });

        return Behaviors.receive(Request.class)
                .onMessageEquals(UpdateMyVelocity.INSTANCE, () -> updateParticleProperties(this.getContext(), Duration.ofMillis(5)))
                .build();


    }

    private Behavior<Request> onListing(Receptionist.Listing msg, ActorContext<Request> context) {

        // what happens when it receives a listing

        System.out.println("found a listing  " + msg.getAllServiceInstances(nodeServiceKey));

        msg.getAllServiceInstances(nodeServiceKey)
                .forEach(nodeParticle ->
                        nodeParticle.tell(
                                (new CalculateForceOfAttraction(this.particleProperties, this.x, this.y,
                                        getContext().getSelf()))));

        // what return on behaviors should i put here
        // i think this is the problem
        return Behaviors.same();
    }

    private Behavior<Request> broadcastProperties(RequestCalculateFitness a, ActorContext<Request> c) {
        this.iteration = a.iteration;
        c
                .getSystem()
                .receptionist()
                .tell(Receptionist.find(NodeParticle.nodeServiceKey, listingResponseAdapter));
        return Behaviors.same();
    }

    private Behavior<NodeParticle.Request> onMessageReceived(Request r) {
        System.out.println("bleh");

        return Behaviors.same();
    }
}
