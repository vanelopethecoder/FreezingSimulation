package fsm;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ApplicationTestFSM {

    public ApplicationTestFSM(ActorContext<NotUsed> context) {
        this.context = context;
    }

    public static void main(String[] args) {

        ActorSystem.create(ApplicationTestFSM.create(), "ParticlesWithStates");

    }

    private final ActorContext<NotUsed> context;

    private static Behavior<NotUsed> create() {
        return Behaviors.setup(context -> new ApplicationTestFSM(context).behavior());
    }

    // find out what the behavior "Not Used" is and does

    private Behavior<NotUsed> behavior() {

        // creating the particles

        List<ActorRef<NodeParticleWithStates.SimpleState>> nodes = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> context.spawn(NodeParticleWithStates.create("node" + i), "node" + i))
                .collect(Collectors.toList());

        // tell them to start receiving now

        for (ActorRef<NodeParticleWithStates.SimpleState> node : nodes) {
            node.tell(NodeParticleWithStates.Receive.INSTANCE);
        }
        return Behaviors.empty();

    }
}
