package uk.ac.ed.inf.jtraceschwa.compare;

/**
 * A simple utility class to count execution time
 * @author arnaudhenry
 *
 */
public class Chrono {

	static long lastTic;
	
	public static void tic(){
		lastTic = System.currentTimeMillis();
	}
	
	public static void toc(){
		toc("");
	}
	
	public static void toc(String str){
		long newTic = System.currentTimeMillis();
		double time = (newTic - lastTic)/(double)1000;
		System.out.println(" == toc("+str+") == "+time+" s");
		lastTic = newTic;
	}
}
