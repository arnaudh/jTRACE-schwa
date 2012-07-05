package uk.ac.ed.inf.jtraceschwa.UI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ed.inf.jtraceschwa.Model.SchwaSim;
import uk.ac.ed.inf.jtraceschwa.UI.graph.GraphTools;
import uk.ac.ed.inf.jtraceschwa.UI.graph.MatrixViewer;
import uk.ac.ed.inf.jtraceschwa.UI.graph.SchwaGraph;
import edu.uconn.psy.jtrace.Model.TraceSim;
import edu.uconn.psy.jtrace.Model.TraceSimAnalysis;
import edu.uconn.psy.jtrace.UI.GraphParameters;

public class TraceSimViewer extends JFrame {

	//trace
	private SchwaSim sim;
	private TraceSim originalSim;
	
	//analysis
	private int topNWords = 10;
	private TraceSimAnalysis wordAnalysis;
	
	//ui
	private MatrixViewer mv1;
	private MatrixViewer mv2;
	private MatrixViewer mv3;
	private JFreeChart wordChart;
	//ui - controls
	private JPanel controls;
	private JPanel lexiconPanel;
	
	
	public TraceSimViewer(SchwaSim sim_, final String title) {
		this.sim = sim_;
		this.originalSim = new TraceSim(sim.tp);
		//analysis
		wordAnalysis = new TraceSimAnalysis(TraceSimAnalysis.WORDS, TraceSimAnalysis.WATCHTOPN,
				new java.util.Vector(), topNWords, TraceSimAnalysis.STATIC, 4,
				TraceSimAnalysis.FORCED, 4);
		wordChart = GraphTools.createCycleActivationChart("Words", null);
		
		// Net representation
//		mv1 = new MatrixViewer(sim.tn.featLayer, sim);
//		mv2 = new MatrixViewer(sim.tn.phonLayer, sim);
//		mv3 = new MatrixViewer(sim.tn.wordLayer, sim);
//		JScrollPane sc1 = new JScrollPane(mv1);
//		JScrollPane sc2 = new JScrollPane(mv2);
//		JScrollPane sc3 = new JScrollPane(mv3);
		
		// Simulation controls
		initControlPanel();
		// Lexicon panel
		lexiconPanel = new LexiconEditor(sim.tp);
		
		// Layout
		this.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
//		gbc.insets = new Insets(0, 5, 0, 5);
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
//		this.getContentPane().add(originalChartPanelPhonemes, gbc);
//		gbc.gridy++;
//		this.getContentPane().add(chartPanelPhonemes, gbc);
//		gbc.gridx++;
//		gbc.gridy--;
//		this.getContentPane().add(originalChartPanelWords, gbc);
//		gbc.gridy++;
		this.getContentPane().add(new ChartPanel(wordChart), gbc);
		gbc.gridy++;
		this.getContentPane().add(new SchwaGraph(sim), gbc);
		
		
		
		this.pack();
		this.setTitle(title);
		this.setLocationRelativeTo(null);
	}

	
	private void initControlPanel() {
		final JTextField inputField = new JTextField(8);
		inputField.setText(sim.getInputString());
		ActionListener resetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( sim.getParameters().getPhonology().validTraceWord(inputField.getText())){
					inputField.setBackground(Color.WHITE);
					sim.tp.setModelInput(inputField.getText());
					originalSim.tp.setModelInput(inputField.getText());
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
		controls.add(simulationControls, BorderLayout.CENTER);
//		controls.add(new TraceParamPanel(sim.tp), BorderLayout.CENTER);
	}
	
	
	
	private void resetAndRun(){
		sim.reset();
		originalSim.reset();
		int cycle = sim.inputString.length() * 7 + 6;
		sim.cycle(cycle);
		originalSim.cycle(cycle);
		updateGraphs();
	}
	
	private void updateGraphs(){

		XYPlot plot = (XYPlot) wordChart.getPlot();
		XYSeriesCollection originalDataset = wordAnalysis.doAnalysis(originalSim);
		XYSeriesCollection dataset = wordAnalysis.doAnalysis(sim);
		plot.setDataset(0, originalDataset);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, new XYLineAndShapeRenderer(true, false));
		plot.setRenderer(1, new XYLineAndShapeRenderer(true, false));
		
		// make colors concur
		for(int i = 0; i < originalDataset.getSeriesCount(); i++){
			String name = originalDataset.getSeriesName(i);
			for(int j = 0; j < plot.getDataset().getSeriesCount(); j++){
				if( dataset.getSeriesName(j).equals(name) ){
					//copy the color used in the original for that series
					plot.getRenderer(1).setSeriesPaint(j, plot.getRenderer(0).getSeriesPaint(i));
					break;
				}
			}
		}
		plot.getRenderer(1).setStroke(new BasicStroke(
		        1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
		        1.0f, new float[] {6.0f, 6.0f}, 0.0f
		    ));
		plot.getRenderer(0).setStroke(new BasicStroke(2f));
		//TODO remove duplicates from legend
		
		
		//annotate
		edu.uconn.psy.jtrace.UI.GraphPanel.annotateJTRACEChart(wordChart, new GraphParameters(), originalSim.getParameters());
	}


}
