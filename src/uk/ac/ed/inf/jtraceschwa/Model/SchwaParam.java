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
		lexicon.reset();
		lexiconWords = new LinkedHashSet<String>();
		loadFile(new File("tools/Lexicons/biglex901.txt"));
		loadFile(new File("tools/Lexicons/grammatical.txt"));
		
	}
	
	private void loadFile(File file){
		Pattern pattern = Pattern.compile(getPhonology().getInputPattern());
		Matcher matcher = pattern.matcher(IOTools.readFile(file));
		while( matcher.find() ){
			if( matcher.group().equals("^")) continue; //no word "a"
			if( lexiconWords.add(matcher.group()) ){ // if it indeed was added (no duplicate)
				lexicon.add(new TraceWord(matcher.group())); //add it to the lexicon
			}
		}
	}
	
}
