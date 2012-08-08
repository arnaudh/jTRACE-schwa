package uk.ac.ed.inf.jtraceschwa.compare;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import uk.ac.ed.inf.jtraceschwa.Model2.SchwaParam2;
import uk.ac.ed.inf.jtraceschwa.Model2.SchwaSim2;
import uk.ac.ed.inf.jtraceschwa.UI.TraceSimViewer;
import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.Model.TraceSim;
import edu.uconn.psy.jtrace.Model.TraceSimAnalysis;

/**
 * Evaluation of the modified model's performance vs the original's
 * @author arnaudhenry
 *
 */
public class Evaluation {

	public static void main(String[] args) {
		
		for(double wA = 0.0004; wA <= 0.0009; wA+=0.0002){
			for(double pI = 0.0018; pI <= 0.0023; pI+=0.0002){
				if (equal(wA, 0.0008) && equal(pI, 0.0018)) {
					//just skip this one we've already done
					continue;
				}
				SchwaParam2 param = new SchwaParam2();
				param.lexicalStressActivated = false;
				param.wordActivation = wA;
				param.phonemeInhibition = pI;
				final TraceSim sim = new SchwaSim2(param);
				
				Thread th = new Thread(new Runnable() {
					@Override
					public void run() {
						evaluate(sim);
					}
				});
				th.start();
			}
		}

		
	}
	
	public static void evaluate(TraceSim sim){

		String outputFile = getNameFor(sim)+".txt";
		System.out.println("Output file: "+outputFile);
		

		TraceSimAnalysis wordAnalysis = new TraceSimAnalysis(TraceSimAnalysis.WORDS, TraceSimAnalysis.WATCHTOPN,
				new java.util.Vector(), 10, TraceSimAnalysis.STATIC, 4,
				TraceSimAnalysis.FORCED, 4);
		
		// For each word in the lexicon
		for(int w = 0; w < sim.tp.getLexicon().size(); w++){
			// Run the model for that input
			String word = sim.tp.getLexicon().get(w).getPhon();

			if(!word.contains("^")) continue;
//			if(!word.equals("k^n")) continue;
			
			sim.tp.setModelInput("-"+word+"-");

			sim.reset();
			int cycle = TraceSimViewer.cyclesForInput("-"+word+"-");
			sim.cycle(cycle);
			
			// Analyse the results
			XYSeriesCollection dataset = wordAnalysis.doAnalysis(sim);
			
			int	recognition = timeOfRecognition(dataset, word);
			System.out.println("["+outputFile+"] recognition of "+word+" ("+w+"/"+sim.tp.getLexicon().size()+") : "+recognition);
			
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
				out.println(IOTools.lengthenWithBlanks(word, 12)+""+recognition);
			    out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
	}
	
	public static String getNameFor(TraceSim sim){
		// Results output
		String name = "results/";
		if( sim instanceof SchwaSim2 ){
			name += "modified";
			if( ((SchwaParam2)sim.tp).lexicalStressActivated ) name += "+stress";
			name += "+phoneme="+String.format("%.4f", ((SchwaParam2)sim.tp).phonemeInhibition);
			name += "+word="   +String.format("%.4f", ((SchwaParam2)sim.tp).wordActivation);
		}else{
			name += "reference";
		}
		return name;
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
}
