package uk.ac.ed.inf.jtraceschwa.Model.schwa;

/**
 * All components which listen to the schwa component must implement this interface
 * @author Arnaud Henry
 *
 */
public interface SchwaListener {

	public void schwaUpdated( Schwa schwa );
	public void reset( Schwa schwa );
	
}
