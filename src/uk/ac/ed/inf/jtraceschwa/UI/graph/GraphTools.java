package uk.ac.ed.inf.jtraceschwa.UI.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import uk.ac.ed.inf.jtraceschwa.Model.SchwaSim;
import edu.uconn.psy.jtrace.Model.TraceSim;
import edu.uconn.psy.jtrace.Model.TraceSimAnalysis;
import edu.uconn.psy.jtrace.UI.GraphParameters;

public class GraphTools {
	
	public static JFreeChart createCycleActivationChart(String title, XYDataset dataset){
		JFreeChart graph = ChartFactory.createXYLineChart(title,
                "Cycle", "Activation",
                dataset, PlotOrientation.VERTICAL,
                true,       // legend
                true,       // tooltips
                false);     // URLs
        graph.setBackgroundPaint(null);
        // set axis settings
        NumberAxis domain = (NumberAxis)((XYPlot) graph.getPlot()).getDomainAxis();
        domain.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        domain.setAutoRange(true);
        // legend to the right
        for(int i = 0; i < graph.getSubtitleCount(); i++){
        	graph.getSubtitle(i).setPosition(RectangleEdge.RIGHT);
        }
        return graph;
	}

	
}
