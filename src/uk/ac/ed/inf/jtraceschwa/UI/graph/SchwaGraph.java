package uk.ac.ed.inf.jtraceschwa.UI.graph;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import uk.ac.ed.inf.jtraceschwa.Model.SchwaNet;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaSim;
import uk.ac.ed.inf.jtraceschwa.Model.schwa.Schwa;
import uk.ac.ed.inf.jtraceschwa.Model.schwa.SchwaListener;
import edu.uconn.psy.jtrace.UI.GraphParameters;

public class SchwaGraph extends ChartPanel implements SchwaListener {

	private SchwaSim sim;
	
	public SchwaGraph(SchwaSim sim_) {
		super(null);
		this.sim = sim_;
		
		
        JFreeChart chart = GraphTools.createCycleActivationChart("Schwa", new XYSeriesCollection());
        
        
        setChart(chart);
        
        ((SchwaNet)sim.tn).schwa.addSchwaListener(this);
	}
	
	@Override
	public void schwaUpdated(Schwa schwa) {
		if( getChart().getXYPlot().getDataset().getSeriesCount() == 0){
			((XYSeriesCollection) getChart().getXYPlot().getDataset()).addSeries(new XYSeries("schwa"));
		}
		((XYSeriesCollection)getChart().getXYPlot().getDataset()).getSeries(0).add(sim.tn.inputSlice, schwa.getActivation());
		edu.uconn.psy.jtrace.UI.GraphPanel.annotateJTRACEChart(getChart(), new GraphParameters(), sim.getParameters());
        getChart().getSubtitle(0).setPosition(RectangleEdge.RIGHT);
		repaint();
	}

	@Override
	public void reset(Schwa schwa) {
		((XYSeriesCollection)getChart().getXYPlot().getDataset()).removeAllSeries();
	}

}
