package uk.ac.ed.inf.jtraceschwa.Model;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.Model.TracePhones;
import edu.uconn.psy.jtrace.Model.TraceWord;

public class ConcreteTraceParam extends TraceParam {

//	private String defaultLabels[] = {"p", "b", "t", "d", "k", "g", "s", "S", "r", "l", "a", "i", "u", "^", "-"
//   		, "w", "U", "f", "6", "I", "A", "T", "n", "m", "D", "e", "z", "v", "Z", "j", "E", "h", "N", "O"};     
	public int[] schwaWeights      = { 0,   0,   0,   0,   0,   0,   0,   0,   3,   3,   6,   4,   5,   8,   0, 
			   5,   3,   0,   5,   3,   3,   0,   3,   3,   1,   3,   1,   2,   1,   4,   3,   1,   3,   3, };

	public boolean lexicalStressActivated = false;
	public double phonemeInhibition = 0.001;
	public double wordActivation    = 0.001;
	
	public ConcreteTraceParam() {
		super();
		updatePhonology();
	}
	
	@Override
	public void setPhonology(TracePhones _phonology) {
		super.setPhonology(_phonology);
		updatePhonology();
	}
	
	private void updatePhonology(){
		//modify phonology: add schwa feature
		double[][] _f = new double[phonology.getPhonDefs().length][8*9];
		for( int i = 0; i < phonology.getPhonDefs().length; i++){
			//copy the original 7 features
			for(int f = 0; f < 63; f++){
				_f[i][f] = phonology.getPhonDefs()[i][f];
			}
//			//add the schwa feature
//			if( schwaWeights[i] > 0 ){
//				int index = 63 + 8 - schwaWeights[i];
//				_f[i][index] = 1;
//			}
			// just for schwa
			if( phonology.getLabels()[i].equals("^")){
				_f[i][63] = 1;
			}
		}
		phonology = new TracePhones("Schwa language", phonology.getLabels() , _f , phonology.getDurationScalar() );
		phonology.NCONTS = 8; // instead of 7
		// just adding one value for the schwa feature
		spread = new int[]{6, 6, 6, 6, 6, 6, 6, 6};
		spreadScale = new double[]{1, 1, 1, 1, 1, 1, 1, 1 };
	}

	public int getSchwaWeightOf(String phoneme){
		for(int i = 0; i < phonology.defaultLabels.length; i++){
			if( phonology.defaultLabels[i].equals(phoneme) ){
				return schwaWeights[i];
			}
		}
		return 0;
	}
	
	
	
	

	public double stressWeight = 0.3;

	private Set<String> lexiconWords; //just to make sure we don't have duplicates in the lexicon
	
	@Override
	public void loadDefaultlexicon() {
		lexicon.reset();
		lexiconWords = new LinkedHashSet<String>();
		loadFile(new File("tools/Lexicons/biglex901.txt"));
		loadFile(new File("tools/Lexicons/grammatical.txt"));
		
	}
	
	private void loadFile(File file){
		Pattern pattern = Pattern.compile(getPhonology().getInputPattern());
		Matcher matcher = pattern.matcher(IOTools.readFile(file));
		while( matcher.find() ){
			if( matcher.group().equals("^")) continue; //no word "a"
			if( lexiconWords.add(matcher.group()) ){ // if it indeed was added (no duplicate)
				lexicon.add(new TraceWord(matcher.group())); //add it to the lexicon
			}
		}
	}
	
	
}
