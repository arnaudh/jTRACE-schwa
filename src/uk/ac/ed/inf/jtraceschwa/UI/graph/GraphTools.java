package uk.ac.ed.inf.jtraceschwa.UI.graph;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

public class GraphTools {
	
	public static JFreeChart createCycleActivationChart(String title, XYDataset dataset){
		JFreeChart graph = ChartFactory.createXYLineChart(title,
                "Cycle", "Activation",
                dataset, PlotOrientation.VERTICAL,
                true,       // legend
                true,       // tooltips
                false);     // URLs
        graph.setBackgroundPaint(Color.WHITE);
        graph.getPlot().setBackgroundPaint(Color.WHITE);
        graph.getPlot().setBackgroundAlpha(0.25f);
        ((XYPlot) graph.getPlot()).setDomainGridlinePaint(Color.GRAY);
        ((XYPlot) graph.getPlot()).setRangeGridlinePaint(Color.GRAY);
        // set axis settings
        NumberAxis domain = (NumberAxis)((XYPlot) graph.getPlot()).getDomainAxis();
        domain.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        domain.setAutoRange(true);
        // legend to the right
        for(int i = 0; i < graph.getSubtitleCount(); i++){
        	graph.getSubtitle(i).setPosition(RectangleEdge.RIGHT);
        }
//        graph.removeLegend();
        return graph;
	}

	
}
