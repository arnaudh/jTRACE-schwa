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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ed.inf.jtraceschwa.Model.SchwaSim;
import uk.ac.ed.inf.jtraceschwa.UI.graph.MatrixViewer;
import uk.ac.ed.inf.jtraceschwa.UI.graph.TraceGraph;

import edu.uconn.psy.jtrace.IO.WTFileReader;
import edu.uconn.psy.jtrace.IO.XMLFileFilter;
import edu.uconn.psy.jtrace.Model.TraceSim;
import edu.uconn.psy.jtrace.Model.TraceSimAnalysis;
import edu.uconn.psy.jtrace.UI.FeatureSimGraph;
import edu.uconn.psy.jtrace.UI.GraphParameters;
import edu.uconn.psy.jtrace.UI.ParametersPanel;
import edu.uconn.psy.jtrace.UI.PhonemeSimGraph;
import edu.uconn.psy.jtrace.UI.WordSimGraph;
import edu.uconn.psy.jtrace.UI.traceProperties;

public class TraceSimViewer extends JFrame {

	//trace
	private TraceSim sim;
	private TraceSim originalSim;
	
	//ui
	private MatrixViewer mv1;
	private MatrixViewer mv2;
	private MatrixViewer mv3;
	private TraceGraph chartPanelPhonemes;
	private TraceGraph chartPanelWords;
	private TraceGraph originalChartPanelPhonemes;
	private TraceGraph originalChartPanelWords;
	//ui - controls
	private JPanel controls;
	private LabelledSpinner[] spinners;
	private JPanel lexiconPanel;
	
	
	public TraceSimViewer(TraceSim sim_, final String title) {
		this.sim = sim_;
		this.originalSim = new TraceSim(sim.tp);
		// Net representation
		mv1 = new MatrixViewer(sim.tn.featLayer, sim);
		mv2 = new MatrixViewer(sim.tn.phonLayer, sim);
		mv3 = new MatrixViewer(sim.tn.wordLayer, sim);
		JScrollPane sc1 = new JScrollPane(mv1);
		JScrollPane sc2 = new JScrollPane(mv2);
		JScrollPane sc3 = new JScrollPane(mv3);
		Dimension size = mv2.getPreferredSize();
		size.height+=20; size.width = 0;
		sc2.setMinimumSize(size);
		chartPanelPhonemes = new TraceGraph(sim, TraceSimAnalysis.PHONEMES);
		chartPanelWords = new TraceGraph(sim, TraceSimAnalysis.WORDS);
		originalChartPanelPhonemes = new TraceGraph(originalSim, TraceSimAnalysis.PHONEMES);
		originalChartPanelWords = new TraceGraph(originalSim, TraceSimAnalysis.WORDS);
		
		originalChartPanelPhonemes.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
		originalChartPanelWords.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		chartPanelPhonemes.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
		updateGraphs();

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
		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.fill = GridBagConstraints.BOTH;
		this.getContentPane().add(lexiconPanel, gbc);
		gbc.gridy++;
		this.getContentPane().add(controls, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.weightx = 1;
		gbc.gridy=0;
		gbc.gridx++;
		gbc.gridheight = 1;
		this.getContentPane().add(originalChartPanelPhonemes, gbc);
		gbc.gridy++;
		this.getContentPane().add(chartPanelPhonemes, gbc);
		gbc.gridx++;
		gbc.gridy--;
		this.getContentPane().add(originalChartPanelWords, gbc);
		gbc.gridy++;
		this.getContentPane().add(chartPanelWords, gbc);
		
		
		
		this.pack();
		this.setTitle(title);
		this.setLocationRelativeTo(null);
	}

	
	private void initControlPanel() {
		final JTextField inputField = new JTextField(sim.getInputString());
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
		simulationControls.add(inputField);
		simulationControls.add(runButton);
		
		//Parameter controls
		spinners = new LabelledSpinner[14];
		//decay
		spinners[0] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.decay.F, 0, 0.5, 0.005), "F");
		spinners[1] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.decay.P, 0, 0.5, 0.005), "P");
		spinners[2] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.decay.W, 0, 0.5, 0.005), "W");
		//rest
		spinners[3] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.rest.F, -.5, 0.5, 0.005), "F");
		spinners[4] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.rest.P, -.5, 0.5, 0.005), "P");
		spinners[5] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.rest.W, -.5, 0.5, 0.005), "W");
		//alpha
		spinners[6] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.alpha.IF, -.1, 1, 0.01), "I->F"); //-.1 for the right width...
		spinners[7] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.alpha.FP, -.1, 1, 0.01), "F->P");
		spinners[8] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.alpha.PW, -.1, 1, 0.01), "P->W");
		spinners[9] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.alpha.WP, -.1, 1, 0.01), "W->P");
		spinners[10] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.alpha.PF, -.1, 1, 0.01), "P->F");
		//gamma
		spinners[11] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.gamma.F, 0, 0.5, 0.005), "F->F");
		spinners[12] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.gamma.P, 0, 0.5, 0.005), "P->P");
		spinners[13] = new LabelledSpinner(new SpinnerNumberModel(sim.tp.gamma.W, 0, 0.5, 0.005), "W->W");
		//layout
		JPanel parametersPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx=0; gbc.gridy=0;
		gbc.fill = GridBagConstraints.BOTH;
		for( int i = 0; i < spinners.length; i++ ){
			final int idx = i;
			spinners[i].addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					double val = (Double) spinners[idx].getValue();
					switch (idx) {
					case 0: sim.tp.decay.F = val; break;
					case 1: sim.tp.decay.P = val; break;
					case 2: sim.tp.decay.W = val; break;
					case 3: sim.tp.rest.F = val; break;
					case 4: sim.tp.rest.P = val; break;
					case 5: sim.tp.rest.W = val; break;
					case 6: sim.tp.alpha.IF = val; break;
					case 7: sim.tp.alpha.FP = val; break;
					case 8: sim.tp.alpha.PW = val; break;
					case 9: sim.tp.alpha.WP = val; break;
					case 10: sim.tp.alpha.PF = val; break;
					case 11: sim.tp.gamma.F = val; break;
					case 12: sim.tp.gamma.P = val; break;
					case 13: sim.tp.gamma.W = val; break;
					default: System.err.println("Unknown spinner");
						break;
					}
				}
			});
			if( i==0 || i==3 || i==6 || i==11 ){
				gbc.gridx++;
				gbc.gridy=0;
				switch( i ){
				case 0: parametersPanel.add(new JLabel("Decay"), gbc); break;
				case 3: parametersPanel.add(new JLabel("Rest"), gbc); break;
				case 6: parametersPanel.add(new JLabel("Alpha (excitation)"), gbc); break;
				case 11: parametersPanel.add(new JLabel("Gamma (inhibition)"), gbc); break;
				}
			}
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.gridy++;
			spinners[i].setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.BLACK));
			parametersPanel.add(spinners[i], gbc);
		}
		
		//Layout
		controls = new JPanel(new BorderLayout());
		controls.add(simulationControls, BorderLayout.NORTH);
		controls.add(parametersPanel, BorderLayout.CENTER);
		
		
	}
	
	private void updateGraphs(){
		originalChartPanelPhonemes.updateGraph();
		chartPanelPhonemes.updateGraphUsingColors(originalChartPanelPhonemes);
		originalChartPanelWords.updateGraph();
		chartPanelWords.updateGraphUsingColors(originalChartPanelWords);
	}
	
	private void resetAndRun(){
		sim.reset();
		originalSim.reset();

		sim.cycle(99);
		originalSim.cycle(99);
		updateGraphs();
	}
	

    private void loadLexicon() {
        java.io.File lexFile;
        javax.swing.JFileChooser lexFileChooser = new javax.swing.JFileChooser(traceProperties.rootPath.getAbsolutePath());
            
        lexFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        lexFileChooser.addChoosableFileFilter(new XMLFileFilter());
        lexFileChooser.setCurrentDirectory(traceProperties.workingPath);            
        
        // show dialog
        int returnVal = lexFileChooser.showOpenDialog(this);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // we got a file...
            lexFile = lexFileChooser.getSelectedFile();
            traceProperties.workingPath = lexFile.getParentFile();    
        
            // try to read it
            WTFileReader fileReader = new WTFileReader(lexFile);
            if (!fileReader.validateLexiconFile()){             
                javax.swing.JOptionPane.showMessageDialog(null, "Invalid lexicon file.", "Error", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // LOAD LEXICON
            sim.tp.setLexicon(fileReader.loadJTLexicon());
            originalSim.tp.setLexicon(fileReader.loadJTLexicon());
            resetAndRun();
            
            return;
        }
        else{ //if(returnVal == javax.swing.JFileChooser.CANCEL_OPTION){
            return;
        }
    }


}
