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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ed.inf.jtraceschwa.Model.SchwaNet;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaParam;
import uk.ac.ed.inf.jtraceschwa.Model.SchwaSim;
import uk.ac.ed.inf.jtraceschwa.UI.graph.GraphTools;
import uk.ac.ed.inf.jtraceschwa.UI.graph.SchwaGraph;
import uk.ac.ed.inf.jtraceschwa.compare.Evaluation;
import edu.uconn.psy.jtrace.Model.TraceSim;
import edu.uconn.psy.jtrace.Model.TraceSimAnalysis;
import edu.uconn.psy.jtrace.UI.GraphParameters;

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
	
	//ui
	private JFreeChart wordChart;
	public static Stroke dashedThin = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {6.0f, 6.0f}, 0.0f);
	public static Stroke dashedThick = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {6.0f, 6.0f}, 0.0f);
	public static Stroke thin = new BasicStroke(1f);
	public static Stroke thick = new BasicStroke(2f);
	private JPanel controls;
	private JPanel lexiconPanel;
	private JTextField inputField;
	private List<JLabel> recognitionPointLabels;

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

        simulations = new ArrayList<Simulation>();
        simulations.add(new Simulation(new TraceSim(param), "Original", thin));
        simulations.add(new Simulation(new SchwaSim(param, false), "Modified", dashedThick));
//        simulations.add(new Simulation(new SchwaSim(param, true), "Modified+stress", thick));
		
		//analysis
		wordAnalysis = new TraceSimAnalysis(TraceSimAnalysis.WORDS, TraceSimAnalysis.WATCHTOPN,
				new java.util.Vector(), topNWords, TraceSimAnalysis.STATIC, 4,
				TraceSimAnalysis.FORCED, 4);
		wordChart = GraphTools.createCycleActivationChart("Words", null);
		
		/////////////////////////// UI
		// Simulation controls
		initControlPanel();
		// Lexicon panel
		lexiconPanel = new LexiconEditor(param);
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
		this.getContentPane().add(controls, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1;
		gbc.gridy=0;
		gbc.gridx++;
		this.getContentPane().add(chartPanel, gbc);
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy++;
		this.getContentPane().add(extraLabels, gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy++;
//		this.getContentPane().add(new SchwaGraph((SchwaSim) simulations.get(2).getSim()), gbc);
		this.pack();
		this.setTitle("TraceSimViewer");
		this.setLocationRelativeTo(null);
	}

	private void initControlPanel() {
		inputField = new JTextField(8);
		inputField.setText(simulations.get(0).getSim().getInputString());
		ActionListener resetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( simulations.get(0).getSim().getParameters().getPhonology().validTraceWord(inputField.getText())){
					inputField.setBackground(Color.WHITE);
					simulations.get(0).getSim().tp.setModelInput(inputField.getText());
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
//		controls.add(((SchwaSim)simulations.get(2).getSim()).createControlPanel(), BorderLayout.CENTER);
//		controls.add(new TraceParamPanel(sim.tp), BorderLayout.CENTER);
	}
	
	// Runs the simulations
	public void resetAndRun(){
		maxCycle = cyclesForInput(inputField.getText()); //run the simulation for a "sufficient" amount of time

		for(Simulation sim : simulations){
			sim.getSim().reset();
			// reload stress patterns
			if(sim.getSim() instanceof SchwaSim && ((SchwaNet)sim.getSim().tn).lexicalStressComponent!=null ){
				((SchwaNet)sim.getSim().tn).lexicalStressComponent.loadStressPatterns();
			}
			// run the simulation
			sim.getSim().cycle(maxCycle);
		}
		
		updateGraphs();
	}
	
	public static int cyclesForInput( String input ){
		return input.length() * 7 + 20;
	}
	
	private void updateGraphs(){
		XYPlot plot = (XYPlot) wordChart.getPlot();
		
		for(int i = 0; i<simulations.size(); i++){
			TraceSim sim = simulations.get(i).getSim();
			plot.setDataset(i, wordAnalysis.doAnalysis(sim));
			plot.setRenderer(i, new XYLineAndShapeRenderer(true, false));
			plot.getRenderer(i).setStroke(simulations.get(i).getStroke());
			//recognition point
			int recogition = Evaluation.timeOfRecognition((XYSeriesCollection) plot.getDataset(i), inputField.getText().substring(1, inputField.getText().length()-1));
			recognitionPointLabels.get(i).setText(""+recogition);
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
		
		//annotate
		edu.uconn.psy.jtrace.UI.GraphPanel.annotateJTRACEChart(wordChart, new GraphParameters(), simulations.get(0).getSim().getParameters());
	}


}
