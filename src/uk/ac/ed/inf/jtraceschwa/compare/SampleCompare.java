package uk.ac.ed.inf.jtraceschwa.compare;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import uk.ac.ed.inf.jtraceschwa.Model.SchwaParam;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaSim;
import uk.ac.ed.inf.jtraceschwa.UI.TraceSimViewer;

public class SampleCompare {
	
	public static void main(String[] args) {
		SchwaParam param = new SchwaParam();
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
				sv.resetAndRun();
			}
		});
	}

	
	
	

}
