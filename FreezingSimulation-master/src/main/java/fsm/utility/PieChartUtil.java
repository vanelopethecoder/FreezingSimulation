package fsm.utility;

import NodeModels.ParticleProperties;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PieChartUtil extends JFrame {

    private static final long serialVersionUID = 6294689542092367723L;


    public PieChartUtil(String titleRow, Map<String, ParticleProperties> particlePropertiesMap, int threshHold, int totalParticles) throws HeadlessException {
        super(titleRow);

        PieDataset dataset = createDataSet(titleRow, particlePropertiesMap, threshHold, totalParticles);

        // Create chart
        JFreeChart chart = ChartFactory.createPieChart(
                titleRow,
                dataset,
                true,
                true,
                false);

        //Format Label
        PieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
                " {0} : ({2})", new DecimalFormat("0"), new DecimalFormat("0%"));
        ((PiePlot) chart.getPlot()).setLabelGenerator(labelGenerator);

        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);

    }

    public PieDataset createDataSet(String titleRow, Map<String, ParticleProperties> particlePropertiesMap, int threshHold, int totalParticles) {

        DefaultPieDataset dataset = new DefaultPieDataset();

        AtomicInteger countbonded = new AtomicInteger();
        AtomicInteger countUnbonded = new AtomicInteger();

        particlePropertiesMap.forEach((x, y) -> {

            if (y.getVelocity() == 0) {

                countbonded.getAndIncrement();

            } else if (y.getVelocity() != 0) {
                countUnbonded.getAndIncrement();
            }
        });

        // int notReturned = totalParticles - threshHold;

        dataset.setValue("count bonded " + countbonded, countbonded);
        dataset.setValue("count unbonded " + countUnbonded, countUnbonded);
        // dataset.setValue("Threshold leftover " + notReturned, notReturned);

        return dataset;

    }

}
