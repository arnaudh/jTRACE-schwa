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
	private double slice = 0;
	
	public SchwaGraph(SchwaSim sim_) {
		super(null);
		this.sim = sim_;
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(new XYSeries("schwa"));
		
        JFreeChart chart = ChartFactory.createXYLineChart("Schwa",
            "Cycle", "activation",
            dataset, PlotOrientation.VERTICAL,
            true,       // legend
            true,       // tooltips
            false);     // URLs
        
        setChart(chart);
        
        ((SchwaNet)sim.tn).schwa.addSchwaListener(this);
	}
	
	@Override
	public void schwaUpdated(Schwa schwa) {
		((XYSeriesCollection)getChart().getXYPlot().getDataset()).getSeries(0).add(slice, schwa.getActivation());
		edu.uconn.psy.jtrace.UI.GraphPanel.annotateJTRACEChart(getChart(), new GraphParameters(), sim.getParameters());
        getChart().getSubtitle(0).setPosition(RectangleEdge.RIGHT);
		slice++;
		repaint();
	}

	@Override
	public void reset(Schwa schwa) {
		slice = 0;
		((XYSeriesCollection)getChart().getXYPlot().getDataset()).getSeries(0).clear();
	}

}
