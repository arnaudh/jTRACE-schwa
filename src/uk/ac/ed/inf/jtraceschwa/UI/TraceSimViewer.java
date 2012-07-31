package uk.ac.ed.inf.jtraceschwa.UI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ed.inf.jtraceschwa.Model.SchwaParam;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaSim;
import uk.ac.ed.inf.jtraceschwa.UI.graph.GraphTools;
import edu.uconn.psy.jtrace.Model.TraceSim;
import edu.uconn.psy.jtrace.Model.TraceSimAnalysis;
import edu.uconn.psy.jtrace.UI.GraphParameters;

/**
 * Simulator enabling to quickly compare the original and modified model
 * @author arnaudhenry
 *
 */
public class TraceSimViewer extends JFrame {

	//trace
//	private SchwaSim sim;
//	private SchwaSim simLex;
//	private TraceSim originalSim;
	private List<TraceSim> simulations;
	
	//
	public static int maxCycle = 100; // number of cycles for each simulation (input dependent)
	
	//analysis
	private int topNWords = 10;
	private TraceSimAnalysis wordAnalysis;
	
	//ui
	private JFreeChart wordChart;
	public static Stroke dashedThin = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {6.0f, 6.0f}, 0.0f);
	public static Stroke dashedThick = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {6.0f, 6.0f}, 0.0f);
	public static Stroke thin = new BasicStroke(1f);
	public static Stroke thick = new BasicStroke(2f);
	private List<Stroke> strokes;
	private JPanel controls;
	private JPanel lexiconPanel;

	// launches a TraceSimViewer
	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				TraceSimViewer sv = new TraceSimViewer();
				sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				sv.setVisible(true);
				sv.resetAndRun();
			}
		});
	}
	
	public TraceSimViewer() {
		SchwaParam param = new SchwaParam();
        param.setModelInput("-pits^-");

        simulations = new ArrayList<TraceSim>();
//        this.sim = new SchwaSim(param, false);
//        this.simLex = new SchwaSim(param, true);
//		this.originalSim = new TraceSim(param);
        simulations.add(new TraceSim(param));
        simulations.add(new SchwaSim(param, false));
        simulations.add(new SchwaSim(param, true));
        
        strokes = new ArrayList<Stroke>();
        strokes.add(thin);
        strokes.add(dashedThick);
        strokes.add(thick);
		
		//analysis
		wordAnalysis = new TraceSimAnalysis(TraceSimAnalysis.WORDS, TraceSimAnalysis.WATCHTOPN,
				new java.util.Vector(), topNWords, TraceSimAnalysis.STATIC, 4,
				TraceSimAnalysis.FORCED, 4);
		wordChart = GraphTools.createCycleActivationChart("Words", null);
		
		// Simulation controls
		initControlPanel();
		// Lexicon panel
		lexiconPanel = new LexiconEditor(param);
		
		// Layout
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
		controls.setBackground(Color.red);
		this.getContentPane().add(controls, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.weightx = 1;
		gbc.gridy=0;
		gbc.gridx++;
		this.getContentPane().add(new MyChartPanel(wordChart), gbc);
		gbc.gridy++;
//		this.getContentPane().add(new SchwaGraph((SchwaSim) simulations.get(0)), gbc);
		this.pack();
		this.setTitle("TraceSimViewer");
		this.setLocationRelativeTo(null);
	}

	private void initControlPanel() {
		final JTextField inputField = new JTextField(8);
		inputField.setText(simulations.get(0).getInputString());
		ActionListener resetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( simulations.get(0).getParameters().getPhonology().validTraceWord(inputField.getText())){
					inputField.setBackground(Color.WHITE);
					simulations.get(0).tp.setModelInput(inputField.getText());
					resetAndRun();
				}else{
					inputField.setBackground(Color.RED);
				}
			}
		};
		inputField.addActionListener(resetListener);
		JButton runButton = new JButton("Run");
		runButton.addActionListener(resetListener);
		JPanel simulationControls = new JPanel();
		simulationControls.add(new JLabel("Input"));
		simulationControls.add(inputField);
		simulationControls.add(runButton);
		simulationControls.setMinimumSize(new Dimension(200, 0));
		
		//Layout
		controls = new JPanel(new BorderLayout());
		controls.add(simulationControls, BorderLayout.NORTH);
		controls.add(((SchwaParam)simulations.get(0).tp).createControlPanel(), BorderLayout.CENTER);
//		controls.add(new TraceParamPanel(sim.tp), BorderLayout.CENTER);
	}
	
	// Runs the two simulations
	public void resetAndRun(){
//		sim.reset();
//		simLex.reset();
//		originalSim.reset();
		
		maxCycle = simulations.get(0).inputString.length() * 7 + 20; //run the simulation for a "sufficient" amount of time

		for(TraceSim sim : simulations){
			sim.reset();
			sim.cycle(maxCycle);
		}
		
//		sim.cycle(maxCycle);
//		simLex.cycle(maxCycle);
//		originalSim.cycle(maxCycle);
		
		updateGraphs();
	}
	
	private void updateGraphs(){
		XYPlot plot = (XYPlot) wordChart.getPlot();
		
		for(int i = 0; i<simulations.size(); i++){
			TraceSim sim = simulations.get(i);
			plot.setDataset(i, wordAnalysis.doAnalysis(sim));
			plot.setRenderer(i, new XYLineAndShapeRenderer(true, false));
			plot.getRenderer(i).setStroke(strokes.get(i));
		}
		//make the curves match in color
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
		
		
//		XYSeriesCollection originalDataset = wordAnalysis.doAnalysis(originalSim);
//		XYSeriesCollection dataset = wordAnalysis.doAnalysis(sim);
//		XYSeriesCollection datasetLex = wordAnalysis.doAnalysis(simLex);
//		plot.setDataset(0, originalDataset);
//		plot.setDataset(1, dataset);
//		plot.setDataset(2, datasetLex);
//		plot.setRenderer(0, new XYLineAndShapeRenderer(true, false));
//		plot.setRenderer(1, new XYLineAndShapeRenderer(true, false));
//		plot.setRenderer(2, new XYLineAndShapeRenderer(true, false));
//		
//		// make colors concur
//		for(int i = 0; i < originalDataset.getSeriesCount(); i++){
//			String name = originalDataset.getSeriesName(i);
//			for(int j = 0; j < plot.getDataset().getSeriesCount(); j++){
//				if( dataset.getSeriesName(j).equals(name) ){
//					//copy the color used in the original for that series
//					plot.getRenderer(1).setSeriesPaint(j, plot.getRenderer(0).getSeriesPaint(i));
//				}
//				if( datasetLex.getSeriesName(j).equals(name) ){
//					//copy the color used in the original for that series
//					plot.getRenderer(2).setSeriesPaint(j, plot.getRenderer(0).getSeriesPaint(i));
//				}
//			}
//		}
//		plot.getRenderer(0).setStroke(originalStroke);
//		plot.getRenderer(1).setStroke(modifiedStroke);
//		plot.getRenderer(2).setStroke(modifiedLexStroke);
		//TODO remove duplicates from legend
		
		//annotate
		edu.uconn.psy.jtrace.UI.GraphPanel.annotateJTRACEChart(wordChart, new GraphParameters(), simulations.get(0).getParameters());
	}


}
