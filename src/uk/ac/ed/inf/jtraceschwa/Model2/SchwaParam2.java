package uk.ac.ed.inf.jtraceschwa.Model2;

import edu.uconn.psy.jtrace.Model.TracePhones;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaParam;

public class SchwaParam2 extends SchwaParam {

	private String defaultLabels[] = {"p", "b", "t", "d", "k", "g", "s", "S", "r", "l", "a", "i", "u", "^", "-"
    		, "w", "U", "f", "6", "I", "A", "T", "n", "m", "D", "e", "z", "v", "Z", "j", "E", "h", "N", "O"};     
	private int[] schwaWeights = { 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 6, 4, 5, 8, 0, 5, 3, 0, 5, 3, 3, 0, 3, 3, 1, 3, 1, 2, 1, 4, 3, 1, 3, 3, };

	public SchwaParam2() {
		super();

		//modify phonology: add schwa feature
		double[][] _f = new double[phonology.DefaultPhonDefs.length][8*9];
		for( int i = 0; i < phonology.DefaultPhonDefs.length; i++){
			//copy the original 7 features
			for(int f = 0; f < 63; f++){
				_f[i][f] = phonology.DefaultPhonDefs[i][f];
			}
			//add the schwa feature
//			if( schwaWeights[i] > 0 ){
//				int index = 63 + 8 - schwaWeights[i];
//				_f[i][index] = 1;
//			}
			// just for schwa
			if( defaultLabels[i].equals("^")){
				_f[i][63] = 1;
			}
		}
		phonology = new TracePhones("Schwa language", phonology.defaultLabels , _f , phonology.DefaultDurationScalar );
		phonology.NCONTS = 8; // instead of 7
		// just adding one value for the schwa feature
		spread = new int[]{6, 6, 6, 6, 6, 6, 6, 6};
		spreadScale = new double[]{1, 1, 1, 1, 1, 1, 1, 1 };
	}
}
