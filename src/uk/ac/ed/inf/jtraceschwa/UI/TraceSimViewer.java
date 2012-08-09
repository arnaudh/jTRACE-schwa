package uk.ac.ed.inf.jtraceschwa.UI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.lf5.viewer.TrackingAdjustmentListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import uk.ac.ed.inf.jtraceschwa.Model.ConcreteTraceParam;
import uk.ac.ed.inf.jtraceschwa.Model.ConcreteTraceSim;
import uk.ac.ed.inf.jtraceschwa.UI.graph.GraphTools;
import uk.ac.ed.inf.jtraceschwa.UI.graph.MatrixViewer;
import uk.ac.ed.inf.jtraceschwa.compare.Chrono;
import uk.ac.ed.inf.jtraceschwa.compare.Evaluation;
import uk.ac.ed.inf.jtraceschwa.compare.Evaluation.Lexicon;
import uk.ac.ed.inf.jtraceschwa.compare.Evaluation.Model;
import uk.ac.ed.inf.jtraceschwa.compare.Evaluation.Phonemes;
import edu.uconn.psy.jtrace.Model.TraceSim;
import edu.uconn.psy.jtrace.Model.TraceSimAnalysis;
import edu.uconn.psy.jtrace.Model.TraceWord;
import edu.uconn.psy.jtrace.UI.GraphParameters;
import edu.uconn.psy.jtrace.UI.WordSimGraph;

/**
 * Simulator enabling to quickly compare the original and modified model
 * @author arnaudhenry
 *
 */
public class TraceSimViewer extends JFrame {

	//Simulations
	private List<Simulation> simulations;
	
	//
	public static int maxCycle = 100; // number of cycles for each simulation (input dependent)
	
	//analysis
	private int topNWords = 10;
	private TraceSimAnalysis wordAnalysis;
	private TraceSimAnalysis phonemeAnalysis;
	
	//ui
	private JFreeChart wordChart;
	private JFreeChart bestWordsChart;
	private JFreeChart phonemeChart;
	public static Stroke dashedThin = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {6.0f, 6.0f}, 0.0f);
	public static Stroke dashedThick = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {6.0f, 10.0f}, 0.0f);
	public static Stroke thin = new BasicStroke(1f);
	public static Stroke thick = new BasicStroke(2f);
	private JPanel controls;
	private JPanel lexiconPanel;
	private JTextField inputField;
	private List<JLabel> recognitionPointLabels;
	
	private MatrixViewer matrixViewer;

	// launches a TraceSimViewer
	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				TraceSimViewer sv = new TraceSimViewer();
				sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				sv.setVisible(true);
				sv.resetAndRun(false);
			}
		});
	}
	
	public TraceSimViewer() {
        simulations = new ArrayList<Simulation>();
        simulations.add(new Simulation(Evaluation.createSim(Model.MODIFIED, Phonemes.ORIGINAL_PHON, Lexicon.BIG_LEX), "Modified", dashedThick));
        simulations.add(new Simulation(Evaluation.createSim(Model.ORIGINAL, Phonemes.ORIGINAL_PHON, Lexicon.BIG_LEX), "Original", thin));
//        simulations.add(new Simulation(Evaluation.createSim(Model.MODIFIED, Phonemes.EXTENDED_SET, Lexicon.BIG_LEX), null, dashedThick));
//        ConcreteTraceParam param3 = new ConcreteTraceParam();
//        param3.phonemeInhibition = 0.0022;
//        param3.wordActivation = 0.0008;
//        simulations.add(new Simulation(new ConcreteTraceSim(param3), null, thick));
        
        setModelInput("-Sil^-");
//        SchwaSim ssim = new SchwaSim(param, false);
//        ((SchwaNet)ssim.tn).schwa.prioritizeSchwa = true;
//        simulations.add(new Simulation(ssim, "Modified + prioritize schwa", dashedThick));
//        simulations.add(new Simulation(new SchwaSim(param, true), "Modified+stress", thick));
		
		//analysis
//		wordAnalysis = new TraceSimAnalysis(TraceSimAnalysis.WORDS, TraceSimAnalysis.WATCHSPECIFIED,
//				new java.util.Vector(), 0, TraceSimAnalysis.STATIC, 4,
//				TraceSimAnalysis.FORCED, 4);
//		wordAnalysis.setItemsToWatch(simulations.get(0).getSim().tp.getLexicon());
		
		phonemeAnalysis = new TraceSimAnalysis(TraceSimAnalysis.PHONEMES, TraceSimAnalysis.WATCHTOPN,
				new java.util.Vector(), topNWords, TraceSimAnalysis.STATIC, 4,
				TraceSimAnalysis.FORCED, 4);
		wordChart = GraphTools.createCycleActivationChart("Words", null);
		bestWordsChart = GraphTools.createCycleActivationChart("Best Words", null);
		phonemeChart = GraphTools.createCycleActivationChart("Phonemes", null);
		
		/////////////////////////// UI
		// Simulation controls
		initControlPanel();
		// Lexicon panel
		lexiconPanel = new LexiconEditor(simulations.get(0).getSim().getParameters());
		// Graph
		ChartPanel chartPanel = new ChartPanel(wordChart){
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				int x = 90;
				int y = 100;
				for(Simulation sim : simulations){
					g2.setStroke(sim.getStroke());
					g2.drawLine(x, y, x+30, y);
					g2.drawString(sim.getName(), x+35, y+5);
					y += 20;
				}
			}
		};
		//Matrix viewer
		matrixViewer = new MatrixViewer(simulations.get(0).getSim().tn.wordLayer, simulations.get(0).getSim());
		JScrollPane matrixScrollPane = new JScrollPane(matrixViewer);
		matrixScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		// Extra labels
		JPanel extraLabels = new JPanel(new GridLayout(0, 2));
		extraLabels.add(new JLabel("Recognition points"));
		extraLabels.add(new JLabel());
		recognitionPointLabels = new ArrayList<JLabel>();
		for(int i = 0; i < simulations.size(); i++){
			JLabel label = new JLabel("-1");
			recognitionPointLabels.add(label);
			extraLabels.add( new JLabel(simulations.get(i).getName()+":") );
			extraLabels.add(label);
		}
		
		//////////////////////////// Layout
		this.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		this.getContentPane().add(lexiconPanel, gbc);
		gbc.gridy+=1;
		gbc.gridheight = 1;
		gbc.weighty = 0;
		this.getContentPane().add(controls, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.weightx = 0.5;
		gbc.weighty = 1;
		gbc.gridy=0;
		gbc.gridx++;
//		this.getContentPane().add(matrixScrollPane, gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridheight = 1;
		this.getContentPane().add(chartPanel, gbc);
		gbc.gridwidth = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy++;
		this.getContentPane().add(extraLabels, gbc);
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy++;
		this.getContentPane().add(new ChartPanel(bestWordsChart), gbc);
		this.pack();
		this.setTitle("TraceSimViewer");
		this.setLocationRelativeTo(null);
		
	}
	
	private void setModelInput(String input){
		for( Simulation sim : simulations ){
			sim.getSim().tp.setModelInput(input);
			sim.getSim().reset();
		}
	}

	private void initControlPanel() {
		inputField = new JTextField(8);
		inputField.setText(simulations.get(0).getSim().getInputString());
		ActionListener resetAndRunListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					if( simulations.get(0).getSim().getParameters().getPhonology().validTraceWord(inputField.getText())){
						inputField.setBackground(Color.WHITE);
						setModelInput(inputField.getText());
						resetAndRun(false);
					}else{
						inputField.setBackground(Color.RED);
					}						
			}
		};
		inputField.addActionListener(resetAndRunListener);
		JButton runButton = new JButton("Run");
		runButton.addActionListener(resetAndRunListener);
		JButton runSlowButton = new JButton("Run slowly");
		runSlowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
//				JFrame frame = new JFrame("Net");
//				matrixViewer = new MatrixViewer(simulations.get(0).getSim().tn.wordLayer, simulations.get(0).getSim());
//				frame.getContentPane().add(matrixViewer);
//				frame.pack();
//				frame.setVisible(true);		
				setModelInput(inputField.getText());
				resetAndRun(true);
			}
		});
		JPanel simulationControls = new JPanel(new GridLayout(4, 1));
		simulationControls.add(new JLabel("Input: "));
		simulationControls.add(inputField);
		simulationControls.add(runButton);
		simulationControls.add(runSlowButton);
//		simulationControls.setMinimumSize(new Dimension(200, 0));
		
		//Layout
		controls = new JPanel(new BorderLayout());
		controls.add(simulationControls, BorderLayout.NORTH);
//		controls.add(((SchwaSim)simulations.get(2).getSim()).createControlPanel(), BorderLayout.CENTER);
//		controls.add(new TraceParamPanel(sim.tp), BorderLayout.CENTER);
	}
	
	// Runs the simulations
	public void resetAndRun(final boolean slowly){

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				maxCycle = Evaluation.cyclesForInput(inputField.getText()); //run the simulation for a "sufficient" amount of time

				for(Simulation sim : simulations){
					sim.getSim().reset();
					// run the simulation
					for(int cycle = 0; cycle < maxCycle; cycle++){
						sim.getSim().cycle(1);
						int[] topN = WordSimGraph.topN(sim.getSim().tn.wordLayer, topNWords);
//						String [] labels = new String[topN.length];
//						for(int i =0; i<topN.length; i++) labels[i] = sim.getSim().getParameters().getLexicon().get(topN[i]).getPhon();
//						System.out.print("["+cycle+"]  ");
//						IOTools.printArray(labels);

						if(slowly && cycle%5==0){
							updateGraphs();
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//pause or not necessary ?
						}
					}
				}

				updateGraphs();
			}
		});
		thread.start();
	}
	
	private void updateGraphs(){
		wordAnalysis = new TraceSimAnalysis(TraceSimAnalysis.WORDS, TraceSimAnalysis.WATCHTOPN,
				new java.util.Vector(), topNWords, TraceSimAnalysis.MAX_POSTHOC, 4,
				TraceSimAnalysis.NORMAL, 4);

		
		// BEST WORDS
		TraceSimAnalysis bestWordsAnalysis = new TraceSimAnalysis(TraceSimAnalysis.WORDS, TraceSimAnalysis.WATCHTOPN,
				new java.util.Vector(), topNWords, TraceSimAnalysis.MAX_POSTHOC, 4,
				TraceSimAnalysis.FORCED, 4);
		
		
		XYPlot plot = (XYPlot) wordChart.getPlot();
		XYPlot plotPhoneme = (XYPlot) phonemeChart.getPlot();
		XYPlot plotBest = (XYPlot) bestWordsChart.getPlot();

		showAnalysis(wordAnalysis, plot);
		showAnalysis(phonemeAnalysis, plotPhoneme);
		showAnalysis(bestWordsAnalysis, plotBest);
		
		//annotate
		edu.uconn.psy.jtrace.UI.GraphPanel.annotateJTRACEChart(wordChart, new GraphParameters(), simulations.get(0).getSim().getParameters());
		edu.uconn.psy.jtrace.UI.GraphPanel.annotateJTRACEChart(phonemeChart, new GraphParameters(), simulations.get(0).getSim().getParameters());
		edu.uconn.psy.jtrace.UI.GraphPanel.annotateJTRACEChart(bestWordsChart, new GraphParameters(), simulations.get(0).getSim().getParameters());

		//update TraceNet viewer
		matrixViewer.setMatrix(simulations.get(0).getSim().tn.wordLayer);
	}

	private void showAnalysis( TraceSimAnalysis analysis, XYPlot plot){
		for(int i = 0; i<simulations.size(); i++){
			TraceSim sim = simulations.get(i).getSim();
			plot.setDataset(i, analysis.doAnalysis(sim));
			plot.setRenderer(i, new XYLineAndShapeRenderer(true, false));
			plot.getRenderer(i).setStroke(simulations.get(i).getStroke());
			// recognition points
			if( analysis == wordAnalysis ){
				int recogition = Evaluation.timeOfRecognition((XYSeriesCollection) plot.getDataset(i), inputField.getText().substring(1, inputField.getText().length()-1));
				recognitionPointLabels.get(i).setText(""+recogition);
			}
		}
		//Make colors match between the different simulations
		for(int i = 0; i < plot.getDataset(0).getSeriesCount(); i++){
			String name = plot.getDataset(0).getSeriesName(i);
			for(int sim = 1; sim < simulations.size(); sim++){
				for(int j = 0; j < plot.getDataset(sim).getSeriesCount(); j++){
					if( plot.getDataset(sim).getSeriesName(j).equals(name) ){
						//copy the color used in the original for that series
						plot.getRenderer(sim).setSeriesPaint(j, plot.getRenderer(0).getSeriesPaint(i));
					}
				}
			}
		}
	}


}
