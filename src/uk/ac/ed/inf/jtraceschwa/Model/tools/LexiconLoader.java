package uk.ac.ed.inf.jtraceschwa.Model.tools;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import edu.uconn.psy.jtrace.Model.TraceLexicon;
import edu.uconn.psy.jtrace.Model.TracePhones;
import edu.uconn.psy.jtrace.Model.TraceWord;

public class LexiconLoader {

	private Set<String> lexiconWords; //just to make sure we don't have duplicates in the lexicon
	private TraceLexicon lexicon;
	private TracePhones tp;
	
	public LexiconLoader(TracePhones tp) {
		this.tp = tp;
		lexiconWords = new LinkedHashSet<String>();
		lexicon = new TraceLexicon();
	}
	
	public void loadFile(File file){
		Pattern pattern = Pattern.compile(tp.getInputPattern());
		Matcher matcher = pattern.matcher(IOTools.readFile(file));
		while( matcher.find() ){
//			if( matcher.group().equals("^")) continue; //no word "a"
			if( lexiconWords.add(matcher.group()) ){ // if it indeed was added (no duplicate)
				lexicon.add(new TraceWord(matcher.group())); //add it to the lexicon
			}
		}
	}
	
	public TraceLexicon getLexicon(){
		return lexicon;
	}
}
