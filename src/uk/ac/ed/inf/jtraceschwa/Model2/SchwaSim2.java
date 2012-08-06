package uk.ac.ed.inf.jtraceschwa.Model2;

import uk.ac.ed.inf.jtraceschwa.Model.SchwaNet;
import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.Model.TraceSim;

public class SchwaSim2 extends TraceSim {

	public SchwaSim2(SchwaParam2 _tp) {
        tp = _tp;
        tn = new SchwaNet2(_tp);
        
        paramUpdateCt = tp.getUpdateCt();
        reset();
	}
	
}
