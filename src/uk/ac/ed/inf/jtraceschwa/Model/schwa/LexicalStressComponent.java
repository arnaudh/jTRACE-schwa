package uk.ac.ed.inf.jtraceschwa.Model.schwa;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uconn.psy.jtrace.Model.TraceWord;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaNet;

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
//        for(int w=0; w<net.tp.getLexicon().size(); w++){     
		
            String word = net.tp.getLexicon().get(w).getPhon();
            System.out.println("word="+word);
            
            char[] stress = stressPatterns.get(word).toCharArray();
            if( stress==null ){ //grammatical word
            	stress = new char[]{'W'};
            }
            
            
            int slice = net.inputSlice;
            double activation = schwa.getActivation();
            
            
            //send activation to a "word" if one of its weak syllables overlaps with the slice
            
            
            int currentStress = WEAK;
            //for each of its phonemes
            for(int p = 0; p < word.length(); p++){
            	if( p < stress.length ){
            		if( stress[p] == 'W' ){
            			currentStress = WEAK;
            		}else if( stress[p] == 'S' ){
            			currentStress = STRONG;
            		}
            	}
            	System.out.println("Stress for "+p+" : "+currentStress);
            	
//            	for( int wslice = 0; wslice< )

//                  net.wordNet[w][wslice]
            }
            
            

//        }
		
		
	}

	@Override
	public void reset(Schwa schwa) {
		// nothing to do
	}

}
