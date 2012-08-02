package uk.ac.ed.inf.jtraceschwa.compare;

import java.io.File;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaParam;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaSim;
import uk.ac.ed.inf.jtraceschwa.UI.TraceSimViewer;
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
		File outputFile = new File("results/modified-Word2SchwaActivated.txt");
		StringBuilder output = new StringBuilder();
		
		
		SchwaParam param = new SchwaParam();
		TraceSim sim = new SchwaSim(param, false);
//		TraceSim sim = new TraceSim(param);
		

		TraceSimAnalysis wordAnalysis = new TraceSimAnalysis(TraceSimAnalysis.WORDS, TraceSimAnalysis.WATCHTOPN,
				new java.util.Vector(), 10, TraceSimAnalysis.STATIC, 4,
				TraceSimAnalysis.FORCED, 4);
		
		// For each word in the lexicon
		for(int w = 0; w < param.getLexicon().size(); w++){
			// Run the 2 models for that input
			String word = param.getLexicon().get(w).getPhon();
			System.out.println("*** "+word+" ("+w+"/"+param.getLexicon().size()+") ");
			param.setModelInput("-"+word+"-");

			sim.reset();
			int cycle = TraceSimViewer.cyclesForInput("-"+word+"-");
			sim.cycle(cycle);
			
			// Analyse the results
			XYSeriesCollection dataset = wordAnalysis.doAnalysis(sim);
			
			int	recognition = timeOfRecognition(dataset, word);
			System.out.println("recognition of "+word+" : "+recognition);
			
			output.append(word);
			for(int i=word.length()+String.valueOf(recognition).length(); i<14; i++) output.append(' ');
			output.append(recognition);
			output.append('\n');
			IOTools.writeToFile(outputFile, output.toString());

		}
		
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
}
