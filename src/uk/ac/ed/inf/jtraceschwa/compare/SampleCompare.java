package uk.ac.ed.inf.jtraceschwa.compare;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ed.inf.jtraceschwa.Model.SchwaSim;
import uk.ac.ed.inf.jtraceschwa.UI.TraceSimViewer;

import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.Model.TraceParam.Decay;
import edu.uconn.psy.jtrace.Model.TraceParam.Gamma;
import edu.uconn.psy.jtrace.Model.TraceSim;
import edu.uconn.psy.jtrace.Model.TraceSimAnalysis;
import edu.uconn.psy.jtrace.UI.GraphParameters;

public class SampleCompare {
	
	public static void main(String[] args) {
		TraceParam param = new TraceParam();
        param.setModelInput("-art^st-");
        
//		TraceSim sim = new TraceSim(param);
//        runSim(sim, "Original");
        
        final SchwaSim sim2 = new SchwaSim(param);
        SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				TraceSimViewer sv = new TraceSimViewer(sim2, "Schwa");
				sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				sv.setVisible(true);
			}
		});
	}

	
	
	

}
