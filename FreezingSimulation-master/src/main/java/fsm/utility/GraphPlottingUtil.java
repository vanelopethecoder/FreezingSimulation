package fsm.utility;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import NodeModels.ParticleProperties;
import akka.actor.typed.ActorRef;
import fsm.NodeParticleWithStates;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.*;
import org.jfree.data.xy.DefaultXYZDataset;


public class GraphPlottingUtil extends JFrame {

    final XYSeriesCollection dataset = new XYSeriesCollection(); // series xy collection

    JFreeChart chart;

    // you also need to pass in all the properties
    public GraphPlottingUtil(String title, Map<String, ParticleProperties> particlePropertiesMap) throws HeadlessException {
        super(title);

//         chart = ChartFactory.createScatterPlot(title, "X-Axis", "Y-Axis", createDataSet(title, particlePropertiesMap),
//                PlotOrientation.VERTICAL, true, true, true);
//
//        // Changes background color
//        XYPlot plot = (XYPlot) chart.getPlot();
//        plot.setBackgroundPaint(new Color(255, 228, 196));
//
//        // Adding chart into a chart panel
//        ChartPanel chartPanel = new ChartPanel(chart);
//
//        // settind default size
//        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
//
//        // add to contentPane
//        setContentPane(chartPanel);

    }

    public GraphPlottingUtil(String title,  HashMap<ActorRef<NodeParticleWithStates.SimpleState>, ParticleProperties> particlePropertiesMap1) throws HeadlessException {
        super("Particle: Node within a two dimensional constraint space");

        createDataSet("Particle: Node within a two dimensional constraint space", particlePropertiesMap1);
        this.chart = ChartFactory.createScatterPlot(title, "Constraint A", "Constraint B", this.dataset ,
                PlotOrientation.VERTICAL, true, true, true);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 228, 196));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        setContentPane(chartPanel);

    }

    public void createDataSet(String title, HashMap<ActorRef<NodeParticleWithStates.SimpleState>, ParticleProperties> particlePropertiesMap1) {

        XYSeries particlePositions = new XYSeries(title);

        particlePropertiesMap1
                .forEach((x, y) ->
                        particlePositions.add(y.getX(), y.getY())
                );

        this.dataset.addSeries(particlePositions);

    }


    public void createDataSet(String title, Map<String, ParticleProperties> particlePropertiesMap) {

        XYSeries particlePositions = new XYSeries("Particle: Node within a two dimensional constraint space");

        particlePropertiesMap
                .forEach((x, y) ->
                        particlePositions.add(y.getX(), y.getY())
                );

        this.dataset.addSeries(particlePositions);
    }


     public void updatePannel(String title, Map<String, ParticleProperties> particlePropertiesMap) {

        createDataSet(title, particlePropertiesMap);

         chart = ChartFactory.createScatterPlot("Iteration " + title, "Constraint A", "Constraint B", this.dataset,
                 PlotOrientation.VERTICAL, true, true, true);

        // Changes background color
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 228, 196));

        // Adding chart into a chart panel
        ChartPanel chartPanel = new ChartPanel(chart);

        // settind default size
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        // add to contentPane
        setContentPane(chartPanel);

    }
}
