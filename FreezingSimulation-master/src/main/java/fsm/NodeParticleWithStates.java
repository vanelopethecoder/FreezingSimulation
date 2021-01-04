package fsm;

import NodeModels.NodeParticle;
import NodeModels.ParticleProperties;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.receptionist.ServiceKey;
import org.decimal4j.util.DoubleRounder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
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
    ActorRef<SimpleState> pBest;
    ParticleProperties particleProperties;

    int iteration;

    // we need the total iteration to compare to current and change state constraints

    int totalIterations;

    // node properties

    String myName;
    BigDecimal fitness;

    public enum Receive implements SimpleState {
        INSTANCE
    }

    enum Adjust implements SimpleState {
        INSTANCE
    }

    public static class RequestCalculateFitness implements NodeParticleWithStates.SimpleState {

        Map<NodeParticle, ParticleProperties> swarmParticles;
        int iteration;

        public RequestCalculateFitness(int iteration) {
            this.swarmParticles = swarmParticles;
            System.out.println("creating swarm particles");
            this.iteration = iteration;
        }
    }

    private final ActorContext<SimpleState> ctx;
    private final String name;
    ActorRef<StateController.Command> stateController;

    public NodeParticleWithStates( ActorContext<SimpleState> ctx, String name,
                                  ActorRef<StateController.Command> stateController, int xMax, int yMax) throws IOException {

        this.particleProperties = new ParticleProperties();

        this.particleProperties.setVelocity(BigDecimal.valueOf(
                DoubleRounder.round(Math.random() * (8 - 0.1 + 1) + 0.1, 2)));

        this.fitness = BigDecimal.ZERO;
        this.myName = myName;
        this.particleProperties.setX((int) (Math.random() * ((xMax) + 1)));
        this.particleProperties.setY((int) (Math.random() * ((yMax) + 1)));
        this.totalIterations = totalIterations;
        this.iteration = 1;

        this.ctx = ctx;
        this.name = name;
        this.stateController = stateController;

        writeNodePropertiesToFile(iteration);

    }

    public static Behavior<SimpleState> create(String name, ActorRef<StateController.Command> stateController,
                                               int xMax, int yMax) {
        return Behaviors.setup(ctx -> new NodeParticleWithStates( ctx, name, stateController, xMax, yMax).waiting());
    }

    private Behavior<SimpleState> waiting() {
        return Behaviors.receive(SimpleState.class)
                .onMessage(Receive.class, msg -> {
                    ctx.getLog().info("{} starts to receive", name);
                    return startReceiving(Duration.ofSeconds(5));
                })
                .build();
    }

    // This method is like the inbetween, just to schedule the message
    private Behavior<SimpleState> startReceiving(Duration duration) {
        ctx.scheduleOnce(duration, ctx.getSelf(), Adjust.INSTANCE);
        return receiving("first time");
    }

    // you can't do the actual logic in here, the behavior is in here
    private Behavior<SimpleState> receiving(String msg) {

        this.countingStateReceive = this.countingStateReceive ++;
        return Behaviors.receive(SimpleState.class)

                .onMessageEquals(Adjust.INSTANCE, this::startAdjusting)
                .onMessage(RequestCalculateFitness.class, param -> {

                    System.out.println(name + "  receiving properties" + msg);
                    return Behaviors.same();
                })
                .build();
    }

    private Behavior<SimpleState> startAdjusting() throws IOException {

        this.countingStateAdjust = this.countingStateAdjust ++;

        System.out.println(name + "  adjust");
        ctx.scheduleOnce(Duration.ofMillis(1), ctx.getSelf(), Receive.INSTANCE);

        if (this.countingStateAdjust == 2) {

            // you, unfortunately, have to tell an actor that you're done, cause the main method doesn't have an address
            this.stateController.tell(StateController.IterationComplete.INSTANCE);

        }

        writeNodePropertiesToFile(this.iteration);

        return Behaviors.receive(SimpleState.class)
                .onMessage(Receive.class, msg -> {
                    ctx.getLog().info("{} starts to receive", name);
                    return receiving("second time");
                })
                .build();

    }

    public void writeNodePropertiesToFile(int iteration) throws IOException {

        RandomAccessFile writer = new RandomAccessFile("X:/Masters projects/FreezingSimulation-master/FreezingSimulation-master/nodes/" + this.name, "rw");

        writer.writeChars("\n");
        writer.writeChars("Iteration: " + iteration);
        writer.writeChars("\n");

        writer.writeChars(String.format("%15s, %15s, %15s, %15s, %15s ", "name", "iteration",
                "velocity" , "x-value", "y-value"));

        writer.writeChars("\n" + toString());

        writer.close();

    }

    public String toString() {

        return String.format("%15s, %15d, %03f, %15d, %15d ", name, this.iteration,
                this.particleProperties.getVelocity(), this.particleProperties.getX(), this.particleProperties.getY());

    }

}
