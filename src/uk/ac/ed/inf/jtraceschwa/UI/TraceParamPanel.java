package uk.ac.ed.inf.jtraceschwa.UI;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.ed.inf.jtraceschwa.Model.SchwaParam;

import edu.uconn.psy.jtrace.Model.TraceParam;

/**
 * Panels with controls to modify the value of the trace's parameters : decay, rest, alpha, gamma
 * @author arnaudhenry
 *
 */
public class TraceParamPanel extends JPanel {
	
	private TraceParam param;

	private LabelledSpinner[] spinners;
	
	public TraceParamPanel(TraceParam tp) {
		param = tp;
		//Parameter controls
		spinners = new LabelledSpinner[14];
		//decay
		spinners[0] = new LabelledSpinner(new SpinnerNumberModel(param.decay.F, 0, 0.5, 0.005), "F");
		spinners[1] = new LabelledSpinner(new SpinnerNumberModel(param.decay.P, 0, 0.5, 0.005), "P");
		spinners[2] = new LabelledSpinner(new SpinnerNumberModel(param.decay.W, 0, 0.5, 0.005), "W");
		//rest
		spinners[3] = new LabelledSpinner(new SpinnerNumberModel(param.rest.F, -.5, 0.5, 0.005), "F");
		spinners[4] = new LabelledSpinner(new SpinnerNumberModel(param.rest.P, -.5, 0.5, 0.005), "P");
		spinners[5] = new LabelledSpinner(new SpinnerNumberModel(param.rest.W, -.5, 0.5, 0.005), "W");
		//alpha
		spinners[6] = new LabelledSpinner(new SpinnerNumberModel(param.alpha.IF, -.1, 1, 0.01), "I->F");
		spinners[7] = new LabelledSpinner(new SpinnerNumberModel(param.alpha.FP, -.1, 1, 0.01), "F->P");
		spinners[8] = new LabelledSpinner(new SpinnerNumberModel(param.alpha.PW, -.1, 1, 0.01), "P->W");
		spinners[9] = new LabelledSpinner(new SpinnerNumberModel(param.alpha.WP, -.1, 1, 0.01), "W->P");
		spinners[10] = new LabelledSpinner(new SpinnerNumberModel(param.alpha.PF, -.1, 1, 0.01), "P->F");
		//gamma
		spinners[11] = new LabelledSpinner(new SpinnerNumberModel(param.gamma.F, 0, 0.5, 0.005), "F->F");
		spinners[12] = new LabelledSpinner(new SpinnerNumberModel(param.gamma.P, 0, 0.5, 0.005), "P->P");
		spinners[13] = new LabelledSpinner(new SpinnerNumberModel(param.gamma.W, 0, 0.5, 0.005), "W->W");
		
		//layout
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx=0; gbc.gridy=0;
		gbc.fill = GridBagConstraints.BOTH;
		for( int i = 0; i < spinners.length; i++ ){
			final int idx = i;
			spinners[i].addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					double val = (Double) spinners[idx].getValue();
					switch (idx) {
					case 0: param.decay.F = val; break;
					case 1: param.decay.P = val; break;
					case 2: param.decay.W = val; break;
					case 3: param.rest.F = val; break;
					case 4: param.rest.P = val; break;
					case 5: param.rest.W = val; break;
					case 6: param.alpha.IF = val; break;
					case 7: param.alpha.FP = val; break;
					case 8: param.alpha.PW = val; break;
					case 9: param.alpha.WP = val; break;
					case 10: param.alpha.PF = val; break;
					case 11: param.gamma.F = val; break;
					case 12: param.gamma.P = val; break;
					case 13: param.gamma.W = val; break;
					default: System.err.println("Unknown spinner");
						break;
					}
				}
			});
			if( i==0 || i==3 || i==6 || i==11 ){
				gbc.gridx++;
				gbc.gridy=0;
				switch( i ){
				case 0: this.add(new JLabel("Decay"), gbc); break;
				case 3: this.add(new JLabel("Rest"), gbc); break;
				case 6: this.add(new JLabel("Alpha (excitation)"), gbc); break;
				case 11: this.add(new JLabel("Gamma (inhibition)"), gbc); break;
				}
			}
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.gridy++;
			spinners[i].setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.BLACK));
			this.add(spinners[i], gbc);
		}
		
	}

}
