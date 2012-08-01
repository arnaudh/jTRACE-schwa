package uk.ac.ed.inf.jtraceschwa.Model;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartPanel;

import uk.ac.ed.inf.jtraceschwa.UI.LabelledSpinner;

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

	public JPanel createControlPanel(){
		JPanel panel = new JPanel();
		
		final LabelledSpinner spin = new LabelledSpinner(new SpinnerNumberModel(((SchwaParam)tp).stressWeight, 0, 0.9, 0.05), "Stress");
		spin.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				((SchwaParam)tp).stressWeight = spin.getValue();
			}
		});
		
		//Layout
		panel.add(spin);
		
		return panel;
	}
	

}
