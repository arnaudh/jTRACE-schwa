package uk.ac.ed.inf.jtraceschwa.Model;

import edu.uconn.psy.jtrace.Model.TraceSim;

public class ConcreteTraceSim extends TraceSim {

	public ConcreteTraceSim(ConcreteTraceParam _tp) {
        tp = _tp;
        tn = new ConcreteTraceNet(_tp);
        
        paramUpdateCt = tp.getUpdateCt();
        reset();
	}
	
}
