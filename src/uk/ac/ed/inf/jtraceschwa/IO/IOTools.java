package uk.ac.ed.inf.jtraceschwa.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class IOTools {
	
	
	public static String readFile(File file){
		StringBuilder sb = new StringBuilder();
		try {
		    BufferedReader in = new BufferedReader(new FileReader(file));
		    String str;
		    while ((str = in.readLine()) != null) {
		    	if(sb.length()!=0) sb.append('\n');
		        sb.append(str);
		    }
		    in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

}
