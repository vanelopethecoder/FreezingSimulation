package fsm;

import NodeModels.NodeParticle;
import NodeModels.ParticleProperties;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

import java.time.Duration;
import java.util.Map;

public class NodeParticleWithStates {


    public interface SimpleState {
    }

    // our 2 states

    int countingStateReceive = 0;
    int countingStateAdjust = 0;

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

    public NodeParticleWithStates(ActorContext<SimpleState> ctx, String name) {
        this.ctx = ctx;
        this.name = name;
    }

    public static Behavior<SimpleState> create(String name) {
        return Behaviors.setup(ctx -> new NodeParticleWithStates(ctx, name).waiting());
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

    private Behavior<SimpleState> startAdjusting() {

        this.countingStateAdjust = this.countingStateAdjust ++;

        System.out.println(name + "  adjust");
        ctx.scheduleOnce(Duration.ofMillis(1), ctx.getSelf(), Receive.INSTANCE);

        if (this.countingStateAdjust == 2) {

        }

        return Behaviors.receive(SimpleState.class)
                .onMessage(Receive.class, msg -> {
                    ctx.getLog().info("{} starts to receive", name);
                    return receiving("second time");
                })
                .build();

    }
}
