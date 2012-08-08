package uk.ac.ed.inf.jtraceschwa.UI;

import java.awt.Stroke;

import uk.ac.ed.inf.jtraceschwa.compare.Evaluation;

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
		setName( name );
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
		if(name == null ){
			name = Evaluation.getNameFor(sim);
		}
		this.name = name;
	}
	public Stroke getStroke() {
		return stroke;
	}
	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}
	
	

}
