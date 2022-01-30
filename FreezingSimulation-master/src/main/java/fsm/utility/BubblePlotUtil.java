package fsm.utility;

import NodeModels.ParticleProperties;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.MatrixSeriesCollection;
import org.jfree.data.xy.NormalizedMatrixSeries;
import org.jfree.data.xy.DefaultXYZDataset;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class BubblePlotUtil extends JFrame {

   final MatrixSeriesCollection matrixSeriesCollection = new MatrixSeriesCollection();

    JFreeChart bubble;

    public BubblePlotUtil(String title, Map<String, ParticleProperties> particlePropertiesMap) throws HeadlessException {
        super(title);
//
//        bubble = ChartFactory.createBubbleChart("Particle Properties" , "x-position", "y-psoition",
//                createMatrixDataSet(title, particlePropertiesMap), PlotOrientation.VERTICAL, true, true, false);
//
//        XYPlot bubblePlot = bubble.getXYPlot();
//
//        bubblePlot.setForegroundAlpha(0.6f);
//        NumberAxis domainAxis = (NumberAxis) bubblePlot.getDomainAxis();
//
//        domainAxis.setLowerBound(-150);
//        domainAxis.setUpperBound(150);
//
//        ChartPanel chartPanelBubble = new ChartPanel(bubble);
//
//        chartPanelBubble.setPreferredSize(new java.awt.Dimension(500, 270));
//
//        setContentPane(chartPanelBubble);
    }

    public void createMatrixDataSet(String title, Map<String, ParticleProperties> particlePropertiesMap) {

        // consider sending in the total number of properties
        NormalizedMatrixSeries series = new NormalizedMatrixSeries("Grid 1", 200, 200);

        particlePropertiesMap.forEach((x , y) ->
                {
                    int particle_x = y.getX();
                    int particle_y = y.getY();
                    int particle_vel = 10;

//                    int particle_x = 10;
//                    int particle_y = 50;
//                    int particle_vel = 10;


                    series.update(particle_x, particle_y, particle_vel);
                }
        );

        this.matrixSeriesCollection.addSeries(series);
    }


    public void updatePannel(String title, Map<String, ParticleProperties> particlePropertiesMap) {

        createMatrixDataSet(title, particlePropertiesMap);

        bubble = ChartFactory.createBubbleChart(title, "X-Axis", "Y-Axis", this.matrixSeriesCollection,
                PlotOrientation.VERTICAL, true, true, false);


        // Changes background color
        XYPlot plot = (XYPlot) bubble.getPlot();

        plot.setForegroundAlpha(0.6f);
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();

        domainAxis.setLowerBound(-10);
        domainAxis.setUpperBound(150);

        plot.setBackgroundPaint(new Color(255, 228, 196));

        // Adding chart into a chart panel
        ChartPanel chartPanel = new ChartPanel(bubble);

        // settind default size
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        // add to contentPane
        setContentPane(chartPanel);

    }
}
