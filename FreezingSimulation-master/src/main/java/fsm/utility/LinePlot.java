package fsm.utility;

import NodeModels.ParticleProperties;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class LinePlot extends JFrame {

    private static final long serialVersionUID = 1L;

    final XYSeriesCollection dataset = new XYSeriesCollection();

    public LinePlot(String title) throws HeadlessException {
        super(title);

    }

    public void updatePanel(String title,  XYSeries xySeriesMap) {

        JFreeChart lineChart = ChartFactory.createXYLineChart(title, "Category", "Score",
                this.dataset, PlotOrientation.VERTICAL, true, true, false);

        // Adding chart into a chart panel
        ChartPanel chartPanel = new ChartPanel(lineChart);

        // settind default size
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        // add to contentPane
        setContentPane(chartPanel);

    }

    public XYSeries createDataSetForThoseThatNotYetPaired(String title, Map<String, ParticleProperties> particlePropertiesMap) {

        XYSeries unpaired = new XYSeries(title);

        particlePropertiesMap.forEach((x , y) ->
                {
                    if(y.getVelocity() == 0 ) {
                     unpaired.add(y.getX(), y.getY());
                    }
                }
                );

        this.dataset.addSeries(unpaired);

        return unpaired;

    }

}
