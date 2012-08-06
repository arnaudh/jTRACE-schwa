package uk.ac.ed.inf.jtraceschwa.compare;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import uk.ac.ed.inf.jtraceschwa.Model2.SchwaParam2;
import uk.ac.ed.inf.jtraceschwa.Model2.SchwaSim2;
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
		String outputFile = "results/fullModified.txt";
		
		SchwaParam2 param = new SchwaParam2();
		TraceSim sim = new SchwaSim2(param);
//		TraceSim sim = new SchwaSim(param, false);
//		TraceSim sim = new TraceSim(param);
		

		TraceSimAnalysis wordAnalysis = new TraceSimAnalysis(TraceSimAnalysis.WORDS, TraceSimAnalysis.WATCHTOPN,
				new java.util.Vector(), 10, TraceSimAnalysis.STATIC, 4,
				TraceSimAnalysis.FORCED, 4);
		
		// For each word in the lexicon
		for(int w = 0; w < param.getLexicon().size(); w++){
			// Run the model for that input
			String word = param.getLexicon().get(w).getPhon();
			
			if(!word.contains("^")) continue;
			
			System.out.println("*** "+word+" ("+w+"/"+param.getLexicon().size()+") ");
			param.setModelInput("-"+word+"-");

			sim.reset();
			int cycle = TraceSimViewer.cyclesForInput("-"+word+"-");
			sim.cycle(cycle);
			
			// Analyse the results
			XYSeriesCollection dataset = wordAnalysis.doAnalysis(sim);
			
			int	recognition = timeOfRecognition(dataset, word);
			System.out.println("recognition of "+word+" : "+recognition);
			
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
				out.println(IOTools.lengthenWithBlanks(word, 12)+""+recognition);
			    out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

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
