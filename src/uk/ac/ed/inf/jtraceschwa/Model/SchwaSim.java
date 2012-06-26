package uk.ac.ed.inf.jtraceschwa.Model;

import edu.uconn.psy.jtrace.Model.TraceNet;
import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.Model.TraceSim;

public class SchwaSim extends TraceSim {

	public SchwaSim(TraceParam _tp) {
        // initialize param and net objects
        tp = _tp;
        tn = new SchwaNet(tp);
        
        paramUpdateCt = tp.getUpdateCt();
        
        reset();
	}
	
	
	
	//Override desired methods...
	

}
