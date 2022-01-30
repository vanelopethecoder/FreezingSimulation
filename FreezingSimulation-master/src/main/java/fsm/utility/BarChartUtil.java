package fsm.utility;

import NodeModels.ParticleProperties;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BarChartUtil extends JFrame {

    private static final long serialVersionUID = 1L;

    private String [] columnKeys = new String [41];

    final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    JFreeChart bar;


    public BarChartUtil(String title) throws HeadlessException {
        super(title);

    }

    public void createBarDataSet(String titleRow, Map<String, ParticleProperties> particlePropertiesMap) {

        int countUnpaired = 0;

        List<ParticleProperties> unpaired = new ArrayList<>();

        List<ParticleProperties> paired = new ArrayList<>();

        particlePropertiesMap.forEach((x, y) ->
                {
                    if(y.getVelocity() != 0) {
                        unpaired.add(y);
                    } else {
                        paired.add(y);
                    }
                }
                );

        this.dataset.addValue(unpaired.size(), titleRow, "unbonded ");
        this.dataset.addValue(paired.size(), titleRow, "bonded");
    }

    public void updatePannel(String title, Map<String, ParticleProperties> particlePropertiesMap) {

        bar = ChartFactory.createBarChart(title, title + "Particles bond stats", "Count", this.dataset,
                PlotOrientation.VERTICAL, true, true, false);

        // Adding chart into a chart panel
        ChartPanel chartPanel = new ChartPanel(bar);

        // settind default size
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        // add to contentPane
        setContentPane(chartPanel);

    }

}
