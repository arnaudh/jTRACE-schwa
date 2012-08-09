package uk.ac.ed.inf.jtraceschwa.UI.graph;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ed.inf.jtraceschwa.Model.ConcreteTraceNet;
import uk.ac.ed.inf.jtraceschwa.Model.ConcreteTraceSim;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaComponent;
import uk.ac.ed.inf.jtraceschwa.UI.TraceSimViewer;
import edu.uconn.psy.jtrace.UI.GraphParameters;

public class SchwaGraph extends ChartPanel {

	private ConcreteTraceSim sim;
	
	public SchwaGraph(ConcreteTraceSim sim_) {
		super(null);
		this.sim = sim_;
		
		
        JFreeChart chart = GraphTools.createCycleActivationChart("Schwa", new XYSeriesCollection());
        
        
        setChart(chart);
	}
	
	public void schwaUpdated(SchwaComponent schwa) {
		XYSeries series = new XYSeries(""+sim.tn.inputSlice);
		((XYSeriesCollection) getChart().getXYPlot().getDataset()).addSeries(series);
		int max = Math.min(schwa.getActivations().length, TraceSimViewer.maxCycle/3);
		for( int i = 0; i < max; i++){
			double val = schwa.getActivations()[i];
			series.add(i*3, val);
		}
		
		edu.uconn.psy.jtrace.UI.GraphPanel.annotateJTRACEChart(getChart(), new GraphParameters(), sim.getParameters());
		getChart().clearSubtitles();
//        getChart().getSubtitle(0).setPosition(RectangleEdge.RIGHT);
		repaint();
	}

	public void reset() {
		((XYSeriesCollection)getChart().getXYPlot().getDataset()).removeAllSeries();
	}

}
