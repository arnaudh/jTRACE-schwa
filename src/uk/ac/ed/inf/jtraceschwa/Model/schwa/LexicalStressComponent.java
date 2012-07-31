package uk.ac.ed.inf.jtraceschwa.Model.schwa;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uconn.psy.jtrace.Model.TraceWord;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaNet;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaParam;

public class LexicalStressComponent implements SchwaListener{
	
	private SchwaNet net;
	private HashMap<String, String> stressPatterns;
	
	//static
	private static int WEAK = 0;
	private static int STRONG = 1;
	
	public LexicalStressComponent(SchwaNet schwaNet) {
		net = schwaNet;
		
		//Load stress patterns
		stressPatterns = new HashMap<String, String>();
		Pattern p = Pattern.compile("("+net.tp.getPhonology().getInputPattern()+")\\n([SW ]+)");
		Matcher matcher = p.matcher(IOTools.readFile(new File("tools/Lexicons/biglex901STRESS.txt")));
		while( matcher.find() ){
			stressPatterns.put(matcher.group(1), matcher.group(2));
		}
	}

	@Override
	public void schwaUpdated(Schwa schwa) {
		// the higher the activation, the more likely the syllable is weak (W)
		// do it SIMPLY
		
		// for each word with a weak syllable which spans over the current time slice
		// send activation proportionally to the schwa activation at the time slice
		
		//send activation to strong syllables too ? (inversionnally proportional to the schwa activation)

		int w = 573; //pr^dusr
        for(w=0; w<net.tp.getLexicon().size(); w++){     
		
            String word = net.tp.getLexicon().get(w).getPhon();
//            System.out.println("word="+word+" (net.inputSlice="+net.inputSlice+")");
            
            char[] stress;
            if( stressPatterns.get(word) == null  ){
            	if( word.equals("-") ) continue;
            	//grammatical word
            	stress = new char[]{'W'};
            }else{
            	stress = stressPatterns.get(word).toCharArray();
            }
            
            
//            int slice = net.inputSlice;
            double[] activations = schwa.getActivations();
            
            for(int slice = 0; slice < net.pSlices; slice++){
	
	//    		IOTools.printArray(activations, net.inputSlice/3);
	            
	            //send activation to a "word" if one of its weak syllables overlaps with the slice
	
	            
	            int currentStress = WEAK;
	            //for each of its phonemes
	            for(int p = 0, wslice = slice; p < word.length() && wslice>=0; p++, wslice--){
	        		double activation = activations[slice];
	            	if( p < stress.length ){
	            		if( stress[p] == 'W' ){
	            			currentStress = WEAK;
	            		}else if( stress[p] == 'S' ){
	            			currentStress = STRONG;
	            		}
	            	}
	
	            	if( currentStress==WEAK ){
	            		net.wordNet[w][wslice] += activation * ((SchwaParam)net.tp).stressWeight;
	            	}else{
//	            		net.wordNet[w][wslice] -= activation * ((SchwaParam)net.tp).stressWeight;
	            	}
	                  
	            }
            
            }

        }
		
		
	}

	@Override
	public void reset(Schwa schwa) {
		// nothing to do
	}

}
