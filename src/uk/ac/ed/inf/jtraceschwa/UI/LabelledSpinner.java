package uk.ac.ed.inf.jtraceschwa.UI;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Just a spinner with a label
 * @author arnaudhenry
 *
 */
public class LabelledSpinner extends JPanel {
	
	private JLabel label;
	private JSpinner spinner;
	
	public LabelledSpinner(SpinnerNumberModel model, String label_) {
		label = new JLabel(label_);
		spinner = new JSpinner(model);
		
		//layout
		this.add(label);
		this.add(spinner);
	}
	
	public void addChangeListener( ChangeListener listener ){
		spinner.addChangeListener(listener);
	}
	
	public double getValue(){
		return (Double) spinner.getValue();
	}
	

}
