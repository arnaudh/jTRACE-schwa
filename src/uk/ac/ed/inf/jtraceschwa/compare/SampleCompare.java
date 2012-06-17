package uk.ac.ed.inf.jtraceschwa.compare;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ed.inf.jtraceschwa.Model.SchwaSim;

import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.Model.TraceSim;
import edu.uconn.psy.jtrace.Model.TraceSimAnalysis;
import edu.uconn.psy.jtrace.UI.GraphParameters;

public class SampleCompare {
	
	public static void main(String[] args) {
		TraceParam param = new TraceParam();
		TraceSim sim = new TraceSim(param);

        param.setModelInput("-gradu^l-");
        runSim(sim, "Original");
        
        SchwaSim sim2 = new SchwaSim(param);
        runSim(sim2, "Schwa");
        
	}

	/**
	 * @param sim
	 */
	private static void runSim(TraceSim sim, String title) {
		sim.cycle(60);

        // create an analysis object
        TraceSimAnalysis an = new TraceSimAnalysis(TraceSimAnalysis.WORDS,
                TraceSimAnalysis.WATCHTOPN, new java.util.Vector(), 10, 
                TraceSimAnalysis.STATIC, 4, TraceSimAnalysis.FORCED, 4);
        // analyze the current run
        XYSeriesCollection analysis = an.doAnalysis(sim);

        GraphParameters graphParams = new GraphParameters();
        graphParams.setXLabel("X");
        graphParams.setYLabel("Y");
        graphParams.setGraphTitle(title);
        JFreeChart graph = edu.uconn.psy.jtrace.UI.GraphPanel.createJTRACEChart(analysis, graphParams);
        graph = edu.uconn.psy.jtrace.UI.GraphPanel.annotateJTRACEChart(graph, graphParams, sim.getParameters());
        File saveGraphFile = new File("noguitest-"+title+".png");
        try {
			ChartUtilities.saveChartAsPNG(saveGraphFile, graph, 800, 500);
	        Runtime.getRuntime().exec("open "+saveGraphFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	

}
