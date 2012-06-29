package uk.ac.ed.inf.jtraceschwa.UI;

import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.UI.ParametersPanel;

public class SchwaParametersPanel extends ParametersPanel {

	public SchwaParametersPanel(TraceParam p) {
		super(p);
	}

	@Override
	//just so that NullPointerException doesn't happen (there is no Hint Manager)
	public void initHints() {
	}
}
