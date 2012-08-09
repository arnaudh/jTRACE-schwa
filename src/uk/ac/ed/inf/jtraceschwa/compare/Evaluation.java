package uk.ac.ed.inf.jtraceschwa.compare;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.sound.midi.SysexMessage;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.sun.org.apache.bcel.internal.generic.NEW;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import uk.ac.ed.inf.jtraceschwa.Model.ConcreteTraceParam;
import uk.ac.ed.inf.jtraceschwa.Model.ConcreteTraceSim;
import uk.ac.ed.inf.jtraceschwa.Model.ExtendedTracePhones;
import uk.ac.ed.inf.jtraceschwa.Model.tools.LexiconLoader;
import edu.uconn.psy.jtrace.Model.TraceLexicon;
import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.Model.TraceSim;
import edu.uconn.psy.jtrace.Model.TraceSimAnalysis;
import edu.uconn.psy.jtrace.UI.WordSimGraph;

/**
 * Evaluation of the modified model's performance vs the original's
 * @author arnaudhenry
 *
 */
public class Evaluation {
	
	public static void main(String[] args) {
		Model model = Model.ORIGINAL;
		Phonemes phonemes = Phonemes.EXTENDED_PHON;
		Lexicon lexicon = Lexicon.BIG_LEX;
		Input input = Input.SINGLE_WORDS;
		
		TraceSim sim = createSim(model, phonemes, lexicon);
		evaluate(sim, input, "results/"+model+"+"+phonemes+"+"+lexicon+"+"+input+".txt");
	}
	
	public static void evaluate(TraceSim sim, Input input, String outputFile) {
		
		// RUN THE EVALUATION
		System.out.println("Output file: "+outputFile);
		
		if( new File(outputFile).exists() ){
			System.err.println("file "+outputFile+" already exists");
			System.exit(0);
		}
		
		switch( input ){
		case SINGLE_WORDS:
			for(int w = 0; w < sim.tp.getLexicon().size(); w++){
				// Run the model for that input
				String word = sim.tp.getLexicon().get(w).getPhon();
				String modelInput = "-"+word+"-";

//				if(!word.contains("^")) continue;
				
				sim.tp.setModelInput(modelInput);
				sim.reset();
				int maxCycle = Evaluation.cyclesForInput(modelInput);
				int [][] tops = new int[maxCycle][];
				for(int cycle = 0; cycle < maxCycle; cycle++){
					sim.cycle(1);
					tops[cycle] = WordSimGraph.topN(sim.tn.wordLayer, 10);
				}
				
				// Analyse the results
				int	recognition = timeOfRecognition(tops, w);
				System.out.println("["+outputFile+"] recognition of "+word+" ("+(w+1)+"/"+sim.tp.getLexicon().size()+") : "+recognition);
				
				try {
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
					out.println(IOTools.lengthenWithBlanks(word, 12)+""+recognition);
				    out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
			break;
		case MULTIWORD_STRINGS:
			// slex pairs
			LexiconLoader loader = new LexiconLoader(sim.tp.getPhonology());
			loader.loadFile(new File("tools/Lexicons/slex_pairs.txt"));
			TraceLexicon slex_pairs = loader.getLexicon();
			// slex
			LexiconLoader loader2 = new LexiconLoader(sim.tp.getPhonology());
			loader2.loadFile(new File("tools/Lexicons/slex.txt"));
			TraceLexicon slex = loader2.getLexicon();
			// Iterate over the pairs
			for(int w=0; w < slex_pairs.size(); w++){
				String pair = slex_pairs.get(w).getPhon();
				String firstWord = slex.get(w).getPhon();
				String secondWord = pair.substring(firstWord.length());

				String modelInput = "-"+pair+"-";
				sim.tp.setModelInput(modelInput);
				sim.reset();
				int maxCycle = Evaluation.cyclesForInput(modelInput);
				int [][] tops = new int[maxCycle][];
				// get indices for first word, second word, and silence (for analysis)
				int first = -1; int second = -1; int silence = -1;
				for(int i = 0; i < sim.tp.getLexicon().size(); i++){
					if( sim.tp.getLexicon().get(i).getPhon().equals(firstWord)) first=i;
					if( sim.tp.getLexicon().get(i).getPhon().equals(secondWord)) second=i;
					if( sim.tp.getLexicon().get(i).getPhon().equals("-")) silence=i;
				}
				// Run simulation
				for(int cycle = 0; cycle < maxCycle; cycle++){
					sim.cycle(1);
					tops[cycle] = WordSimGraph.topN(sim.tn.wordLayer, 10);
//					String [] labels = new String[tops[cycle].length];
//					for(int i =0; i<tops[cycle].length; i++) labels[i] = sim.getParameters().getLexicon().get(tops[cycle][i]).getPhon();
//					System.out.print("["+cycle+"]  ");
//					IOTools.printArray(labels);
					
					// remove silence from top 2
					if( tops[cycle][0] == silence){
						tops[cycle][0] = tops[cycle][1];
						tops[cycle][1] = tops[cycle][2];
					}else if( tops[cycle][1] == silence){
						tops[cycle][1] = tops[cycle][2];
					}
				}
				
				//analyse results
				int recognitionFirst = timeOfRecognitionInTop2(first, tops);
				int recognitionSecond = timeOfRecognitionInTop2(second, tops);
				System.out.println("["+outputFile+"] recognition of '"+modelInput+"' ("+(w+1)+"/"+slex_pairs.size()+") :  "+firstWord+"="+recognitionFirst+",  "+secondWord+"="+recognitionSecond);
				
				try {
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
					out.println(IOTools.lengthenWithBlanks(firstWord, 12)+""+IOTools.lengthenWithBlanks(""+recognitionFirst, 10)
							+ IOTools.lengthenWithBlanks(secondWord, 12)+""+recognitionSecond);
				    out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
			break;
		}
		System.out.println("Output file: "+outputFile);
		
	}
	

	public static int timeOfRecognitionInTop2(int first, int[][] tops) {
		for(int cycle = tops.length-1; cycle >= 0; cycle--){
			if( tops[cycle][0] != first && tops[cycle][1] != first ){
				if( cycle == tops.length -1 ) return -1; // wasn't even in the top2 at the end
				return cycle + 1; //was in the top2 at cycle+1
			}
		}
		return -1;
	}

	public static TraceSim createSim( Model model, Phonemes phonemes, Lexicon lexicon ){
		TraceSim sim = null;
		TraceParam param = null;
		
		// MODEL
		switch( model ){
		case ORIGINAL:
			param = new TraceParam();
			sim = new TraceSim(param); 
			break;
		case MODIFIED:
			param = new ConcreteTraceParam();
			sim = new ConcreteTraceSim((ConcreteTraceParam) param);
			break;
		}
		
		// PHONEME SET
		switch( phonemes ){
		case EXTENDED_PHON:
			param.setPhonology(new ExtendedTracePhones());
			break;
		}

		// LEXICON
		LexiconLoader loader = new LexiconLoader(param.getPhonology());
		switch (lexicon) {
		case SMALL_LEX:
			loader.loadFile(new File("tools/Lexicons/slex.txt"));
			break;
		case BIG_LEX:
			loader.loadFile(new File("tools/Lexicons/biglex901.txt"));
			loader.loadFile(new File("tools/Lexicons/grammatical.txt"));
			break;
		}
		param.setLexicon(loader.getLexicon());
		
		return sim;
	}
	
	
	public static String getNameFor(TraceSim sim){
		// Results output
		String name = "";
		if( sim instanceof ConcreteTraceSim ){
			name += "modified";
			if( ((ConcreteTraceParam)sim.tp).lexicalStressActivated ) name += "+stress";
			name += "+phoneme="+String.format("%.4f", ((ConcreteTraceParam)sim.tp).phonemeInhibition);
			name += "+word="   +String.format("%.4f", ((ConcreteTraceParam)sim.tp).wordActivation);
		}else{
			name += "reference";
		}
		return name;
	}

	/**
	 * Evaluate the slice at which the (best) word is recognised, i.e. when it is at the top of the topN
	 * @param dataset : analysis dataset
	 * @return slice
	 */
	private static int timeOfRecognition(int[][] tops, int word) {
		for(int cycle = tops.length-1; cycle>=0; cycle--){
			if( tops[cycle][0] != word ){
				if( cycle == tops.length-1 ) return -1; //the word isn't the top at the last cycle
				return cycle+1; //the word got to the top at that cycle+1
			}
		}
		return -1;
	}
	
	/**
	 * Evaluate the slice at which the (best) word is recognised, i.e. when its activation is greater than all the others
	 * @param dataset : analysis dataset
	 * @return slice
	 */
	public static int timeOfRecognition(XYSeriesCollection dataset, String inputWord){
		XYSeries best = dataset.getSeries(0);
		
		if( !best.getName().equals(inputWord) ){
			return -1;
		}
		
		for(int i = best.getItemCount()-1; i > 0; i --){ //start from the end
			for(int s = 1; s < dataset.getSeriesCount(); s++){
				float val1 = best.getY(i).floatValue();
				float val2 = dataset.getSeries(s).getY(i).floatValue();

//				System.out.println("diff="+(val1-val2)+" ("+best.getName()+"="+val1+", "+dataset.getSeries(s).getName()+"="+val2+")");
				if( lessOrEqual(val1, val2) ){ //see where the best is less activated than the others
					return best.getX(i++).intValue();
				}
			}
		}
		return 0;
	}

	public static boolean lessOrEqual(float val1, float val2){
		return val1<val2 || (val1-val2)<0.0001;
	}
	
	public static boolean equal(double val, double val2 ){
		return Math.abs(val-val2) < 0.000001;
	}

	public static int cyclesForInput( String input ){
		return input.length() * 7 + 20;
	}

	public enum Model    { ORIGINAL, MODIFIED }
	public enum Phonemes { ORIGINAL_PHON, EXTENDED_PHON }
	public enum Lexicon  { SMALL_LEX, BIG_LEX }
	public enum Input    { SINGLE_WORDS, MULTIWORD_STRINGS }
}
