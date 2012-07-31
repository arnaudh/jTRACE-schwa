package uk.ac.ed.inf.jtraceschwa.Model;

import edu.uconn.psy.jtrace.Model.TraceNet;
import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.Model.TraceSim;

public class SchwaSim extends TraceSim {

	public SchwaSim(TraceParam _tp, boolean useLexicalStress) {
        tp = _tp;
        tn = new SchwaNet(tp, useLexicalStress);
        
        paramUpdateCt = tp.getUpdateCt();
        
        reset();
	}
	

}
