package uk.ac.ed.inf.jtraceschwa.Model;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import uk.ac.ed.inf.jtraceschwa.UI.LabelledSpinner;
import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.Model.TraceWord;

public class SchwaParam extends TraceParam {
	
	public double stressWeight = 0.3;

	private Set<String> lexiconWords; //just to make sure we don't have duplicates in the lexicon
	
	@Override
	public void loadDefaultlexicon() {
		lexiconWords = new LinkedHashSet<String>();
		loadFile(new File("tools/Lexicons/biglex901.txt"));
		loadFile(new File("tools/Lexicons/grammatical.txt"));
		
		lexicon.reset();
		for(String word : lexiconWords){
			lexicon.add(new TraceWord(word));
		}
	}
	
	private void loadFile(File file){
		Pattern pattern = Pattern.compile(getPhonology().getInputPattern());
		Matcher matcher = pattern.matcher(IOTools.readFile(file));
		while( matcher.find() ){
			lexiconWords.add(matcher.group());
		}
	}
	
	public JPanel createControlPanel(){
		JPanel panel = new JPanel();
		
		final LabelledSpinner spin = new LabelledSpinner(new SpinnerNumberModel(stressWeight, 0, 0.9, 0.05), "Stress");
		spin.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				stressWeight = spin.getValue();
			}
		});
		
		//Layout
		panel.add(spin);
		
		return panel;
	}
}
