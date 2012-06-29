package uk.ac.ed.inf.jtraceschwa.UI.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

import edu.uconn.psy.jtrace.Model.TraceSim;
import edu.uconn.psy.jtrace.Model.TraceSimAnalysis;
import edu.uconn.psy.jtrace.UI.GraphParameters;

public class TraceGraph extends ChartPanel {

	private TraceSim sim;
	private int _domain;

	private TraceSimAnalysis analysis;

	public TraceGraph(TraceSim sim_, int _domain_) {
		super(null);
		sim = sim_;
		this._domain = _domain_;

		// create an analysis object
		analysis = new TraceSimAnalysis(_domain, TraceSimAnalysis.WATCHTOPN,
				new java.util.Vector(), 15, TraceSimAnalysis.STATIC, 4,
				TraceSimAnalysis.FORCED, 4);
		// analyze the current run
		XYSeriesCollection dataset = analysis.doAnalysis(sim);
        GraphParameters graphParams = new GraphParameters();

        JFreeChart graph = edu.uconn.psy.jtrace.UI.GraphPanel.createJTRACEChart(dataset, graphParams);
        String title = "";
        if( _domain == TraceSimAnalysis.PHONEMES ){
        	title+="Phonemes";
        }else if( _domain == TraceSimAnalysis.WORDS ){
        	title+="Words";
        }
        graph.setTitle(title);

		this.setChart(graph);

	}

	public void updateGraph() {
		XYPlot plot = (XYPlot) getChart().getPlot();
		XYSeriesCollection dataset = analysis.doAnalysis(sim);
		plot.setDataset(dataset);
		//annotate
		edu.uconn.psy.jtrace.UI.GraphPanel.annotateJTRACEChart(getChart(), new GraphParameters(), sim.getParameters());
	}

	public void updateGraphUsingColors(TraceGraph original) {
		updateGraph();
		
		XYPlot originalPlot = ((XYPlot) original.getChart().getPlot());
		XYPlot plot = ((XYPlot) getChart().getPlot());
		for(int i = 0; i < originalPlot.getDataset().getSeriesCount(); i++){
			String name = originalPlot.getDataset().getSeriesName(i);
			for(int j = 0; j < plot.getDataset().getSeriesCount(); j++){
				if( plot.getDataset().getSeriesName(j).equals(name) ){
					//copy the color used in the original for that series
					plot.getRenderer().setSeriesPaint(j, originalPlot.getRenderer().getSeriesPaint(i));
					break;
				}
			}
		}
		
	}
}
