package uk.ac.ed.inf.jtraceschwa.Model2;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaParam;
import uk.ac.ed.inf.jtraceschwa.compare.Chrono;

public class LexicalStressComponent2 {

	private SchwaNet2 net;
	private HashMap<String, String> stressPatterns;
	private byte[][] stressPatterns2; //stress patterns converted to 0s and 1s
	
	// Constants
	private static byte WEAK = 0;
	private static byte STRONG = 1;
	
	// parameters
	double wordActivation    = 0.001;
	
	public LexicalStressComponent2(SchwaNet2 net) {
		super();
		this.net = net;
		loadStressPatterns();
	}

	public void loadStressPatterns() {
		
		stressPatterns = new HashMap<String, String>();
		// Load lexicon
		Pattern pattern = Pattern.compile("("+net.tp.getPhonology().getInputPattern()+")\\n([SW ]+)");
		Matcher matcher = pattern.matcher(IOTools.readFile(new File("tools/Lexicons/biglex901STRESS.txt")));
		while( matcher.find() ){
			stressPatterns.put(matcher.group(1), matcher.group(2));
		}
		// Load grammatical words
		pattern = Pattern.compile("("+net.tp.getPhonology().getInputPattern()+")");
		matcher = pattern.matcher(IOTools.readFile(new File("tools/Lexicons/grammatical.txt")));
		while( matcher.find() ){
			stressPatterns.put(matcher.group(1), "W");
		}

		stressPatterns2 = new byte[net.getParam().getLexicon().size()][10];
        for(int w=0; w<net.tp.getLexicon().size(); w++){     
            String word = net.tp.getLexicon().get(w).getPhon();
            char[] stress = null;
            if( stressPatterns.get(word) == null  ){
            	if( word.equals("-") ) continue;
            	System.err.println("No Stress pattern found for "+word);
            }else{
            	stress = stressPatterns.get(word).toCharArray();
            }
            byte currentStress = WEAK;
            for(int p=0; p < word.length(); p++){
            	if( p < stress.length ){
            		if( stress[p] == 'W' ){
            			currentStress = WEAK;
            		}else if( stress[p] == 'S' ){
            			currentStress = STRONG;
            		}
            	}
            	stressPatterns2[w][p] = currentStress;
            }
        }
		
	}
	
	/**
	 * 
	 */
	public void schwaUpdated( SchwaComponent2 schwa ){
		Chrono.tic();
        for(int w=0; w<net.tp.getLexicon().size(); w++){     
		
            String word = net.tp.getLexicon().get(w).getPhon();
            
            double[] activations = schwa.getSchwaActivations();
            
//            for(int pslice = 0; pslice < net.pSlices; pslice++){
            int pslice = net.inputSlice/3;
            	double activation = activations[pslice];
	            //for each of its phonemes
	            for(int p = 0, wslice = pslice; p < word.length() && wslice>=0; p++, wslice--){
	            	if( stressPatterns2[w][p]==WEAK && activation>0 ){
//	            		System.out.println("boosting '"+word+"'["+wslice+"] for activation["+pslice+"]="+activation);
	            		net.wordNet[w][wslice] += 0.002 * activation * ((SchwaParam)net.tp).stressWeight;
	            	}else{
//	            		net.wordNet[w][wslice] -= activation * ((SchwaParam)net.tp).stressWeight;
	            	}
	                  
	            }
            
//            }

        }
//        Chrono.toc("LexicalStress update");
	}
	
}
