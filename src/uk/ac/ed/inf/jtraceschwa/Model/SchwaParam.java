package uk.ac.ed.inf.jtraceschwa.Model;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;

import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.Model.TraceWord;

public class SchwaParam extends TraceParam {

	public boolean shcwaPhonemeLocked = false;
	
	@Override
	public void loadDefaultlexicon() {
		lexicon.reset();
		Pattern pattern = Pattern.compile(getPhonology().getInputPattern());
		Matcher matcher = pattern.matcher(IOTools.readFile(new File("tools/lexicons_txt/default.txt")));
		while( matcher.find() ){
			lexicon.add(new TraceWord(matcher.group()));
		}
	}
}
