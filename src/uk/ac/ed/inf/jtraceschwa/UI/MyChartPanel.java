package uk.ac.ed.inf.jtraceschwa.UI;

import java.awt.Graphics;
import java.awt.Graphics2D;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * Just a ChartPanel overlaying a small legend indicating which curves are for the original and which are for the modified model
 * @author arnaudhenry
 *
 */
public class MyChartPanel extends ChartPanel {

	public MyChartPanel(JFreeChart chart) {
		super(chart);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
		int x = 90;
		int y = 100;
		g2.setStroke(TraceSimViewer.dashedThin);
		g2.drawLine(x, y, x+30, y);
		g2.drawString("Original", x+35, y+5);
		y += 20;
		g2.setStroke(TraceSimViewer.dashedThick);
		g2.drawLine(x, y, x+30, y);
		g2.drawString("Modified", x+35, y+5);
	}

}
