package fsm;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StateController {

    public interface Command {}

    enum CreateNodes implements Command {
        INSTANCE
    }

    enum IterationComplete implements Command  {
        INSTANCE
    }


    // this info will come from the main method

    public static class CreateParticles implements  Command {
        int total_Iterations;
        int total_particles;

        public CreateParticles(int total_Iterations, int total_particles) {
            this.total_Iterations = total_Iterations;
            this.total_particles = total_particles;
        }
    }

    private final ActorContext<Command> ctx;
    private final String name;
    int currentIteration;
    int totalIterations;

    public StateController(ActorContext<Command> ctx, String name) {
        this.ctx = ctx;
        this.name = name;
        this.currentIteration = 0;
    }

    // create

    public static Behavior<Command> create(String name) {
        return Behaviors.setup(ctx -> new StateController(ctx, name).spawnNodes(ctx));
    }

    private Behavior<Command> spawnNodes(ActorContext<Command> context) {

        // spawning particles

        List<ActorRef<NodeParticleWithStates.SimpleState>> nodes = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> context.spawn(NodeParticleWithStates.create("node" + i, context.getSelf(),
                        100, 100), "node" + i))
                .collect(Collectors.toList());


        // telling them to start receiving

        currentIteration ++;

        for (ActorRef<NodeParticleWithStates.SimpleState> node : nodes) {
            node.tell(NodeParticleWithStates.Receive.INSTANCE);
        }

        // we're going to receive the responses from the particles to say that they're done with the iteration

        return Behaviors.receive(Command.class)
                .onMessage(IterationComplete.class, msg -> {
                    context.getLog().info("node is complete with single iteration");

                    if(currentIteration < this.totalIterations) {

                        for (ActorRef<NodeParticleWithStates.SimpleState> node : nodes) {
                            node.tell(NodeParticleWithStates.Receive.INSTANCE);
                        }

                        currentIteration++;
                    }
                    return Behaviors.same();
                })
                .build();
    }
}
