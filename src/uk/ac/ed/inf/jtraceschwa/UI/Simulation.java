package uk.ac.ed.inf.jtraceschwa.UI;

import java.awt.Stroke;

import edu.uconn.psy.jtrace.Model.TraceSim;

/**
 * Just a container class to tie together a TraceSim, a name and a stroke for graphing
 * @author Arnaud Henry
 *
 */
public class Simulation {
	
	private TraceSim sim;
	private String name;
	private Stroke stroke;
	
	public Simulation(TraceSim sim, String name, Stroke stroke) {
		super();
		this.sim = sim;
		this.name = name;
		this.stroke = stroke;
	}
	public TraceSim getSim() {
		return sim;
	}
	public void setSim(TraceSim sim) {
		this.sim = sim;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Stroke getStroke() {
		return stroke;
	}
	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}
	
	

}
