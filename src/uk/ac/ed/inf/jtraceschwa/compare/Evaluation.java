package uk.ac.ed.inf.jtraceschwa.compare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaParam;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaSim;
import edu.uconn.psy.jtrace.Model.TraceSim;
import edu.uconn.psy.jtrace.Model.TraceSimAnalysis;

/**
 * Evaluation of the modified model's performance vs the original's
 * @author arnaudhenry
 *
 */
public class Evaluation {

	public static void main(String[] args) {
		
		// Results output
		File outputFile = new File("results.txt");
		StringBuilder output = new StringBuilder();
		
		
		SchwaParam param = new SchwaParam();
		SchwaSim sim = new SchwaSim(param, true);
		TraceSim originalSim = new SchwaSim(param, false);
		

		TraceSimAnalysis wordAnalysis = new TraceSimAnalysis(TraceSimAnalysis.WORDS, TraceSimAnalysis.WATCHTOPN,
				new java.util.Vector(), 5, TraceSimAnalysis.STATIC, 4,
				TraceSimAnalysis.FORCED, 4);
		
		Chrono.tic();
		
		// For each word in the lexicon
		for(int w = 0; w < param.getLexicon().size(); w++){
			// Run the 2 models for that input
			String word = param.getLexicon().get(w).getPhon();
			System.out.println("*** "+word+" ("+w+"/"+param.getLexicon().size()+") ");
			param.setModelInput("-"+word+"-");

			Chrono.tic();
			sim.reset();
			originalSim.reset();
			int cycle = sim.inputString.length() * 7 + 6;
			sim.cycle(cycle);
			originalSim.cycle(cycle);
			Chrono.toc("cycle");
			
			// Analyse the results
			XYSeriesCollection originalDataset = wordAnalysis.doAnalysis(originalSim);
			XYSeriesCollection dataset = wordAnalysis.doAnalysis(sim);
			Chrono.toc("analysis");
			
			String originalWinner = originalDataset.getSeriesName(0);
			String winner = dataset.getSeriesName(0);
			
			int recognitionOriginal = -1;
			int recognition = -1;
			if( originalWinner.equals(word) ){ //the original guessed it right
				  recognitionOriginal = timeOfRecognition(originalDataset);
			}
			if( winner.equals(word) ){ //the modified guessed it right
				recognition = timeOfRecognition(dataset);
			}
			
			System.out.println("winner= "+winner+" ("+recognition+") // originalWinner= "+originalWinner+" ("+recognitionOriginal+")");
			
			output.append(word);
			for(int i=word.length(); i<10; i++) output.append(' ');
			for(int i=String.valueOf(recognitionOriginal).length(); i<4; i++) output.append(' ');
			output.append(recognitionOriginal);
			for(int i=String.valueOf(recognition).length(); i<4; i++) output.append(' ');
			output.append(recognition);
			String result = "";
			if( recognitionOriginal == recognition ){
				result = "SAME";
			}else if( recognitionOriginal==-1 ){ 
				result = "YEAAAH";
			}else if( recognition == -1 ){
				result = "NOOOOO";
			}else if( recognitionOriginal > recognition ){
				result = "BETTER";
			}else{
				result = "WORSE";
			}
			for(int i=String.valueOf(result).length(); i<10; i++) output.append(' ');
			output.append(result);
			output.append('\n');
			IOTools.writeToFile(outputFile, output.toString());

		}
		
	}
	
	/**
	 * Evaluate the slice at which the (best) word is recognised, i.e. when its activation is greater than all the others
	 * @param dataset : analysis dataset
	 * @return slice
	 */
	public static int timeOfRecognition(XYSeriesCollection dataset){
		XYSeries best = dataset.getSeries(0);
		
		for(int i = best.getItemCount()-1; i > 0; i --){ //start from the end
			for(int s = 1; s < dataset.getSeriesCount(); s++){
				if( best.getY(i).floatValue() < dataset.getSeries(s).getY(i).floatValue() ){ //see where the best is less activated than the others
					return best.getX(i++).intValue();
				}
			}
		}
		return 0;
	}
}
