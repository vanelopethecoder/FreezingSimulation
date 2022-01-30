package fsm;

import NodeModels.ParticleProperties;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import fsm.utility.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StateController {

    private final ActorContext<Command> ctx;
    private final String name;

    public static int xMax = 500;
    public static int yMax = 500;
    public static int numberParticles = 200;
    public static int threshold = 25;

    // this info will come from the main method
    int currentIteration;
    int totalIterations;

    public StateController(ActorContext<Command> ctx, String name) {
        this.ctx = ctx;
        this.name = name;
        this.currentIteration = 1;
        this.totalIterations = 50;
    }

    public static Behavior<Command> create(String name) {
        return Behaviors.setup(ctx -> new StateController(ctx, name).spawnNodes(ctx));
    }

    private Behavior<Command> spawnNodes(ActorContext<Command> context) {

        // spawning particles
        HashMap<ActorRef<NodeParticleWithStates.SimpleState>, ParticleProperties> particlePropertiesMap1 = new HashMap<>();

        List<ActorRef<NodeParticleWithStates.SimpleState>> nodes = IntStream.rangeClosed(1, numberParticles)
                .mapToObj(i ->
                        {
                            ParticleProperties properties  = new ParticleProperties();
                            int x = (int) (Math.random() * ((xMax) + 1));
                            int y = (int) (Math.random() * ((yMax) + 1));
                            properties.setX(x);
                            properties.setY(y);

                            ActorRef<NodeParticleWithStates.SimpleState> node = context.spawn(NodeParticleWithStates.create("node" + i, context.getSelf(),
                                    x, y, this.totalIterations, threshold), "node" + i);

                            particlePropertiesMap1.put(node, properties);

                            return node;
                        }
                        )
                .collect(Collectors.toList());

        GraphPlottingUtil graphPlottingUtil_1 = new GraphPlottingUtil("Particle initialisation", particlePropertiesMap1);
        graphPlottingUtil_1.pack();
        graphPlottingUtil_1.setVisible(true);

        // telling them to start receiving

        this.currentIteration++;

        for (ActorRef<NodeParticleWithStates.SimpleState> node : nodes) {
            node.tell(NodeParticleWithStates.Receive.INSTANCE);
        }

        Map<String, ParticleProperties> particlePropertiesMap = new HashMap<>();
        GraphPlottingUtil graph = new GraphPlottingUtil(1 + "", particlePropertiesMap);
        BubblePlotUtil bubble = new BubblePlotUtil(currentIteration + "", null);
        // we're going to receive the responses from the particles to say that they're done with the iteration

        return Behaviors.receive(Command.class)
                .onMessage(IterationComplete.class, msg -> {
                    context.getLog().info("node is complete with single iteration");

                    particlePropertiesMap.put(msg.particleName, msg.particleProperties);

                    if (particlePropertiesMap.size() >= numberParticles) {

                        if(currentIteration == 1) {
                            plotBarChart(particlePropertiesMap, msg);
                        }
                        if(currentIteration == 3) {
                            plotBarChart(particlePropertiesMap, msg);
                            PieChartUtil pieChartUtil = new PieChartUtil(currentIteration + "", particlePropertiesMap, threshold, numberParticles);
                            pieChartUtil.pack();
                            pieChartUtil.setVisible(true);
                        }

                        if (currentIteration == 5) {
                            plotBarChart(particlePropertiesMap, msg);
                        }

                        if ((currentIteration == totalIterations / 2)) {

                            // scatter plot
                            // plot the particles and their properties
                            //graph.createDataSet(currentIteration + "", particlePropertiesMap);
                            graph.updatePannel(currentIteration + "", particlePropertiesMap);

                            graph.pack();
                            graph.setVisible(true);

//                             bubble plot
//
//                            bubble.updatePannel(currentIteration + "" + msg.particleName, particlePropertiesMap);
//                            bubble.pack();
//                            bubble.setVisible(true);


                            LinePlot line = new LinePlot(currentIteration + " " + msg.particleName);
                            line.updatePanel(currentIteration + " " + msg.particleName, line.createDataSetForThoseThatNotYetPaired(
                                    currentIteration + " " + msg.particleName, particlePropertiesMap
                            ));
                            line.pack();
                            line.setVisible(true);

                            plotBarChart(particlePropertiesMap, msg);


                        } else if (currentIteration == totalIterations) {
                            GraphPlottingUtil graph2 = new GraphPlottingUtil(currentIteration + "", particlePropertiesMap);

//                          graph2.createDataSet(currentIteration + "", particlePropertiesMap);
                            graph2.updatePannel(currentIteration + " " /*+ msg.particleName*/ , particlePropertiesMap);
                            graph2.pack();
                            graph2.setVisible(true);

                            plotBarChart(particlePropertiesMap, msg);

                            PieChartUtil pieChartUtil2 = new PieChartUtil("Iteration " + currentIteration + "",
                                    particlePropertiesMap, threshold , numberParticles);
                            pieChartUtil2.pack();
                            pieChartUtil2.setVisible(true);

//                            BubblePlotUtil graphbub = new BubblePlotUtil(currentIteration + "", particlePropertiesMap);
//
////                          graph2.createDataSet(currentIteration + "", particlePropertiesMap);
//                            graphbub.updatePannel(currentIteration + " " + msg.particleName , particlePropertiesMap);
//                            graphbub.pack();
//                            graphbub.setVisible(true);

                        }
                        particlePropertiesMap.clear();
                        if (currentIteration < this.totalIterations) {
                            for (ActorRef<NodeParticleWithStates.SimpleState> node : nodes) {
                                node.tell(NodeParticleWithStates.Receive.INSTANCE);
                            }
                            currentIteration++;
                        }

                    }

                    return Behaviors.same();
                })
                .build();
    }

    private void plotBarChart(Map<String, ParticleProperties> particlePropertiesMap, IterationComplete msg) {
        BarChartUtil barChartUtil = new BarChartUtil("Iteration " + currentIteration);
        barChartUtil.createBarDataSet("Iteration " + currentIteration, particlePropertiesMap);
        barChartUtil.updatePannel("", particlePropertiesMap);
        barChartUtil.pack();
        barChartUtil.setVisible(true);
    }

    enum CreateNodes implements Command {
        INSTANCE
    }

    public interface Command {
    }

    // create

    public static class CreateParticles implements Command {
        int total_Iterations;
        int total_particles;

        public CreateParticles(int total_Iterations, int total_particles) {
            this.total_Iterations = total_Iterations;
            this.total_particles = total_particles;
        }
    }

    public static class IterationComplete implements Command {
        String particleName;
        ParticleProperties particleProperties;

        public IterationComplete(String particleName, ParticleProperties particleProperties) {
            this.particleName = particleName;
            this.particleProperties = particleProperties;
        }
    }
}
