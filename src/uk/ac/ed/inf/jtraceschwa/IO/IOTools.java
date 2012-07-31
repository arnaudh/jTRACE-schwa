package uk.ac.ed.inf.jtraceschwa.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility class with methods to read and write to files
 * @author arnaudhenry
 *
 */
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

	public static void writeToFile(File file, String str) {
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(str);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Some utility methods to print arrays
	 * @param array
	 */
	public static void printArray(double[] array){
		printArray(array, -1);
	}
	public static void printArray(double[] array, int specialIndex){
    	for(int i = 0; i < array.length; i++){
    		if(i==specialIndex) System.out.print("***");
    		System.out.print(array[i]+" ");
    	}
    	System.out.println();
	}

}
