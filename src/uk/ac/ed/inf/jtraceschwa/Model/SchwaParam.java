package uk.ac.ed.inf.jtraceschwa.Model;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.Model.TraceWord;

public class SchwaParam extends TraceParam {

	private Set<String> lexiconWords; //just to make sure we don't have duplicates in the lexicon
	
	@Override
	public void loadDefaultlexicon() {
		lexiconWords = new LinkedHashSet<String>();
		loadFile(new File("tools/Lexicons/default.txt"));
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
}
