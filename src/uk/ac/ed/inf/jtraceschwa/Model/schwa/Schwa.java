package uk.ac.ed.inf.jtraceschwa.Model.schwa;

import java.util.ArrayList;
import java.util.List;

/**
 * Schwa component, central to the modified version of Trace that we propose
 * @author Arnaud Henry
 *
 */
public class Schwa {

	private double activation;
	private List<SchwaListener> listeners;
	
	public Schwa() {
		listeners = new ArrayList<SchwaListener>();
	}
	
	public void addSchwaListener(SchwaListener lis){
		listeners.add(lis);
	}
	
	/**
	 * Called when any parameter of the component is updated : sends a message to all listeners
	 */
	private void updated(){
		for(SchwaListener lis : listeners){
			lis.schwaUpdated(this);
		}
	}
	

	public void setActivation(double activation) {
		if( activation < this.activation ) activation = this.activation;
		System.out.println("Schwa.setActivation("+activation+")");
		this.activation = activation;
		updated();
	}

	public double getActivation() {
		return activation;
	}
	
	public void reset(){
		activation = 0;
	}
}
