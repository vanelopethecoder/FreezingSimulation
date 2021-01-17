package fsm;

import NodeModels.NodeParticle;
import NodeModels.ParticleProperties;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import okhttp3.MultipartBody;
import org.decimal4j.util.DoubleRounder;
import scala.concurrent.impl.FutureConvertersImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Map;

public class NodeParticleWithStates {

    public static final ServiceKey<SimpleState> nodeServiceKey =
            ServiceKey.create(SimpleState.class, "particle");

    public interface SimpleState {
    }

    // our 2 states

    int countingStateReceive = 0;
    int countingStateAdjust = 0;
    ParticleProperties particleProperties;
    int iteration;
    int numberOfOtherParticles = 0;
    // we need the total iteration to compare to current and change state constraints
    int threshHold;
    int totalIterations;
    BigDecimal fitness;
    ActorRef<SimpleState> pBest;


    // node properties

    String myName;

    private final ActorRef<Receptionist.Listing> listingResponseAdapter;

    public enum Receive implements SimpleState {
        INSTANCE
    }

    enum Adjust implements SimpleState {
        INSTANCE
    }

    public static class RequestCalculateFitness implements NodeParticleWithStates.SimpleState {

        ParticleProperties particleProperties;
        int iteration;
        ActorRef<NodeParticleWithStates.SimpleState> node;

        public RequestCalculateFitness(int iteration, ActorRef<NodeParticleWithStates.SimpleState> node,
                                       ParticleProperties particleProperties) {
            this.particleProperties = particleProperties;
            System.out.println("creating swarm particles");
            this.iteration = iteration;
            this.node = node;
        }
    }


    private final ActorContext<SimpleState> ctx;
    private final String name;
    ActorRef<StateController.Command> stateController;

    public NodeParticleWithStates(ActorContext<SimpleState> ctx, String name,
                                  ActorRef<StateController.Command> stateController, int xMax, int yMax,
                                  int totalIterations, int threshHold) throws IOException {

        this.particleProperties = new ParticleProperties();

        this.particleProperties.setVelocity(BigDecimal.valueOf(
                DoubleRounder.round(Math.random() * (8 - 0.1 + 1) + 0.1, 2)));

        this.fitness = BigDecimal.ZERO;
        this.myName = myName;
        this.particleProperties.setX((int) (Math.random() * ((xMax) + 1)));
        this.particleProperties.setY((int) (Math.random() * ((yMax) + 1)));
        this.totalIterations = totalIterations;
        this.fitness = BigDecimal.ZERO;
        this.iteration = 1;
        this.threshHold = threshHold;
        this.ctx = ctx;
        this.name = name;
        this.stateController = stateController;
        this.listingResponseAdapter =
                ctx.messageAdapter(Receptionist.Listing.class, ListingResponse::new);

        appendPropertiesToFile();

    }


    public static class ListingResponse implements SimpleState {
        final Receptionist.Listing listing;

        public ListingResponse(Receptionist.Listing listing) {
            System.out.println("listing constructor");
            this.listing = listing;
        }
    }


    public static Behavior<SimpleState> create(String name, ActorRef<StateController.Command> stateController,
                                               int xMax, int yMax, int totalIterations, int threshHold) {
        return Behaviors.withStash(
                100,
                stash ->
                        Behaviors.setup(
                                ctx -> {
                                    ctx
                                            .getSystem()
                                            .receptionist()
                                            .tell(Receptionist.register(nodeServiceKey, ctx.getSelf()));
                                    return new NodeParticleWithStates(ctx, name, stateController, xMax, yMax,
                                            totalIterations, threshHold)
                                            .waiting();
                                }
                        ));
    }


    /*
     *      state : waiting
     */
    private Behavior<SimpleState> waiting() {

        return Behaviors.receive(SimpleState.class)
                .onMessage(Receive.class, msg -> {
                    ctx.getLog().info("{} starts to receive", name);

                    //todo: this iteration counter will need to be changed
                    // this will do for now since a particle only goes into waiting when a new iteration is starting
                    this.iteration++;
                    this.numberOfOtherParticles = 0;
                    // then go into receiving state
                    return startReceiving(Duration.ofSeconds(5));
                })
                .build();
    }

    // This method is like the inbetween, just to schedule the message

    private Behavior<SimpleState> startReceiving(Duration duration) {
        ctx.scheduleOnce(duration, ctx.getSelf(), Adjust.INSTANCE);
        // Receive the properties of other particles
        broadcastProperties(ctx);

        return Behaviors.receive(NodeParticleWithStates.SimpleState.class)
                // for when you receive a listing of another node
                .onMessage(ListingResponse.class, response -> onlisting(response.listing, ctx))
                .onMessage(RequestCalculateFitness.class, response -> calculateFitness(response, ctx))
                .build();

    }

    private Behavior<SimpleState> calculateFitness(RequestCalculateFitness response, ActorContext<SimpleState> ctx) throws IOException {
        // calculating the fitness

        this.numberOfOtherParticles++;
        ctx.getLog().info(name + "has received this many particle properties : " + numberOfOtherParticles);

        compareToLocalBest(response.particleProperties, response);

        // check the number of particles against the threshold
        if(this.numberOfOtherParticles >= this.threshHold){
            ctx.getLog().info("The number of properties is >90");
            ctx.getSelf().tell(Adjust.INSTANCE);

            return startAdjusting();
        }

        return Behaviors.same();
    }

    private void compareToLocalBest(ParticleProperties particleProperties, RequestCalculateFitness requestCalculateFitness ) {

        // adding the 2 velocities
        BigDecimal sumVelocity = particleProperties.getVelocity()
                .add(this.particleProperties.getVelocity());


        int euclideanDistance = (particleProperties.getX() - this.particleProperties.getX()) * (particleProperties.getX() - this.particleProperties.getX()) +
                (particleProperties.getY() - particleProperties.getY()) * (particleProperties.getY() - particleProperties.getY());

        BigDecimal fa = BigDecimal.valueOf(euclideanDistance).subtract(sumVelocity);

        if ((this.fitness.compareTo(BigDecimal.ZERO) == 0) || (fa.compareTo(this.fitness) >= 0)) {
            this.fitness = fa;
            //   this.pBest = actor red, you need the actor reference.
            this.pBest = requestCalculateFitness.node;
            this.particleProperties.setP_best_properties(particleProperties);

        }
    }

    private Behavior<SimpleState> onlisting(Receptionist.Listing msg, ActorContext<SimpleState> context) {

        msg.getAllServiceInstances(nodeServiceKey)
                .forEach(node ->
                        node.tell(
                                (new RequestCalculateFitness(this.iteration, context.getSelf(), this.particleProperties))
                        ));

        System.out.println("found a listing");

        // this should return back to start receiving
        return Behaviors.same();
    }

    private void broadcastProperties(ActorContext<SimpleState> ctx) {

        // tell the receptionist to find other particles
        ctx.getLog().info("broad casting props");

        ctx.getSystem()
                .receptionist()
                .tell(Receptionist.find(NodeParticleWithStates.nodeServiceKey, listingResponseAdapter));
    }


    // you can't do the actual logic in here, the behavior is in here
    private Behavior<SimpleState> receiving(String msg) {

        this.countingStateReceive = this.countingStateReceive++;

        return Behaviors.receive(SimpleState.class)

                .onMessageEquals(Adjust.INSTANCE, this::startAdjusting)
                .onMessage(RequestCalculateFitness.class, param -> {

                    // calculate fitness here and find your personal local best

                    System.out.println(name + "  receiving properties" + msg);
                    return Behaviors.same();
                })
                .build();
    }

    private Behavior<SimpleState> startAdjusting() throws IOException {

        //this.countingStateAdjust++;

        //System.out.println(name + "  adjust");
//      ctx.scheduleOnce(Duration.ofMillis(1), ctx.getSelf(), Receive.INSTANCE);

       // ctx.getLog().info("counting adjust state : " + countingStateAdjust);

        appendPropertiesToFile();

        // is this just changing the velocity?
        // the position also needs to be updated though
        // figure out how the iteration affects it

        BigDecimal newVel = this.particleProperties.getVelocity().subtract(BigDecimal.valueOf(/*this.iteration / this.totalIterations*/ 1 *
                Math.sqrt(Math.pow(this.particleProperties.getP_best_properties().getX() - this.particleProperties.getX(), 2)
                + Math.pow(this.particleProperties.getP_best_properties().getY() - this.particleProperties.getY(), 2))));

        this.particleProperties.setVelocity(newVel);

        // tell state controller that you're done
        this.stateController.tell(StateController.IterationComplete.INSTANCE);



//        if (this.countingStateAdjust >= 2) {
//            this.countingStateAdjust = 0;
//
//            System.out.println("STATE CONTROLLER COMPLETE");
//.
//            // you, unfortunately, have to tell an actor that you're done, cause the main method doesn't have an address
//            this.stateController.tell(StateController.IterationComplete.INSTANCE);
//
//        } else {
//
//            // change the receiving block that it switches to
//            return Behaviors.receive(SimpleState.class)
//                    .onMessage(Receive.class, msg -> {
//                        ctx.getLog().info("{} starts to receive", name);
//                        return startReceiving(Duration.ofSeconds(5));
//                    })
//                    .build();
//        }
//
//        return Behaviors.receive(SimpleState.class)
//                .onMessage(Receive.class, msg -> waiting()).build();

        // go back to waiting
        return waiting();
    }

    private void appendPropertiesToFile() throws IOException {

        Path path = Paths.get("X:/Masters projects/FreezingSimulation-master/FreezingSimulation-master/nodes/"
                + this.name + ".txt");

        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        Files.write(path, "\n".getBytes(), StandardOpenOption.APPEND);
        Files.write(path, toString().getBytes(), StandardOpenOption.APPEND);
        Files.write(path, "\n".getBytes(), StandardOpenOption.APPEND);
        Files.write(path, "appending".getBytes(), StandardOpenOption.APPEND);
        Files.write(path, "\n".getBytes(), StandardOpenOption.APPEND);
    }

    public String toString() {

        return String.format("%15s, %15d, %03f, %15d, %15d ", name, this.iteration,
                this.particleProperties.getVelocity(), this.particleProperties.getX(), this.particleProperties.getY());

    }

}
