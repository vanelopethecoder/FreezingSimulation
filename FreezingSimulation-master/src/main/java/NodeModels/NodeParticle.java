package NodeModels;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NodeParticle extends AbstractBehavior<NodeParticle.Request> {

    // define MessageType receivers

    public static final ServiceKey<Request> nodeServiceKey =
            ServiceKey.create(Request.class, "testnode");

    String myName;
    BigDecimal fitness;
    NodeParticle pBest;
    NodeParticle gBest;
    ParticleProperties particleProperties;

    public NodeParticle(ActorContext<Request> context, String myName, BigDecimal fitness, NodeParticle pBest, ParticleProperties particleProperties) {
        super(context);
        this.myName = myName;
        this.fitness = fitness;
        this.pBest = pBest;
        this.particleProperties = particleProperties;
    }

    public Receive<Request> createReceive() {
        return null;
    }

    interface Request {
    }
    public static class RequestCalculateFitness implements Request {

        Map<NodeParticle, ParticleProperties> swarmParticles;

        public RequestCalculateFitness(Map<NodeParticle, ParticleProperties> swarmParticles) {
            this.swarmParticles = swarmParticles;
        }
    }

    public static Behavior<Request> create(String myName, BigDecimal fitness, NodeParticle pBest, ParticleProperties particleProperties) {
        System.out.println("Hi there I am creating a typed Node");
        return Behaviors.setup(
                context -> {
                    context
                            .getSystem()
                            .receptionist()
                            .tell(Receptionist.register(nodeServiceKey, context.getSelf()));
                    return new NodeParticle(context, myName, fitness, pBest, 
                            particleProperties).behavior(context);
                }
        );
    }

    private Behavior<Request> behavior(ActorContext<Request> context) {
        return
                Behaviors.receive(NodeParticle.Request.class).onMessage(
                        NodeParticle.Request.class, this::onMessageReceived).build();
    }

    private Behavior<NodeParticle.Request> onMessageReceived(Request r) {
        System.out.println("bleh");




        return Behaviors.same();
    }
}
