package fsm;

import akka.actor.typed.javadsl.ActorContext;

public class StateController {


    public interface Command {}

    enum CreateNodes implements Command {
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

    public StateController(ActorContext<Command> ctx, String name) {
        this.ctx = ctx;
        this.name = name;
    }



}
