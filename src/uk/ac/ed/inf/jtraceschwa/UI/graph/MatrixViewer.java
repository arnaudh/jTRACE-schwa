package uk.ac.ed.inf.jtraceschwa.UI.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.uconn.psy.jtrace.Model.TraceSim;

public class MatrixViewer extends JPanel {
	
//	public static void main(String[] args) {
//		double[][] m = new double[15][24];
//		for (int i = 0; i < m.length; i++) {
//			for (int j = 0; j < m[i].length; j++) {
//				m[i][j] = i/(float)m.length + j/(float)m[0].length +Math.random()*0.5;
//			}
//		}
//		showMatrix(m);
//	}
	
	
	// The data to be viewed
	private double[][] matrix;
	private double min = 0;
	private double max = 1;
	// trace parameters (to show labels and info)
	private TraceSim sim;
	
	// ui
	private int squareSize = 7;
	private int betweenSquares = 1;
	private int leftSpace = 32; //space for labelling the rows
	private String[] dimensions = {"POW", "VOC", "DIF", "ACU", "GRD", "VOI", "BUR"}; 
	private String[] phonemes = {"-", "^", "a", "b", "d", "g", "i", "k", "l", "p", "r", "s", "S", "t", "u" }; 
	private int hoveredI = -1; // vertical index under the cursor
	private int hoveredJ = -1; // horizontal index under the cursor
	
	//greyscale
	private Color[] greyscale;
	
	public MatrixViewer(double[][] matrix_, TraceSim sim_) {
		this.sim = sim_;
		setMatrix(matrix_);
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				hoveredI = (e.getY()-betweenSquares) / (squareSize + betweenSquares);
				hoveredJ = (e.getX()-betweenSquares -leftSpace) / (squareSize + betweenSquares);
				if( hoveredI > matrix.length-1 ) hoveredI = -1;
				if( hoveredJ > matrix[0].length-1 ) hoveredJ = -1;
				repaint();
			}
		});
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				hoveredI=-1;hoveredJ=-1; repaint();
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
		int y = betweenSquares;
		for (int i = 0; i < matrix.length; i++) {
			int x = leftSpace + betweenSquares;
			if( matrix.length==sim.tn.pd.NCONTS*sim.tn.pd.NFEATS && i%9==0 ){ //if continua matrix, separate dimensions
				g2.setColor(Color.BLACK);
				g2.drawLine(0, y-betweenSquares/2, getWidth(), y-betweenSquares/2);
				g2.drawString(dimensions[i/9], 2, (int)(9 * (i/9+0.6) * (squareSize+betweenSquares)));
			} 
			if( matrix.length==sim.tn.pd.NPHONS ){ //phoneme matrix
				g2.setColor(Color.BLACK);
				g2.drawString(phonemes[i], 20, (int)((i+0.7) * (squareSize+betweenSquares) ) );
			}
			if( matrix.length==sim.tn.nwords ){ //lexicon matrix
				g2.setColor(Color.BLACK);
				g2.drawString(sim.tp.lexicon.get(i).getPhon(), 2, (int)((i+0.7) * (squareSize+betweenSquares) ) );
			}
			for (int j = 0; j < matrix[i].length; j++) { //actual drawing of the matrix
				g2.setColor(colorForData(matrix[i][j]));
				g2.fillRect(x, y, squareSize, squareSize);
				if( i==hoveredI && j==hoveredJ ){ //outline the hovered element
					g2.setColor(Color.RED);
					g2.drawRect(x-1, y-1, squareSize+1, squareSize+1);
				}
				x+= squareSize + betweenSquares;
			}
			y+= squareSize + betweenSquares;
		}
		if( hoveredI>-1 && hoveredJ>-1 ){ //draw info of the hovered element
			String infoTxt = "valuet at ["+hoveredI+"]["+hoveredJ+"] = "+String.format("%.2f", matrix[hoveredI][hoveredJ]);
			this.setToolTipText(infoTxt);
		}
		
	}
	
	private void updateMinMax(){
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				double value = matrix[i][j];
				if( value < min ) min = value;
				if( value > max ) max = value;
			}
		}
	}

	private Color colorForData(double value){
		if( greyscale == null ){ //lazy initialization of greyscale
			greyscale = new Color[256];
			for (int i = 0; i < greyscale.length; i++) {
				float strength = i/(float)greyscale.length;
				greyscale[i] = new Color(strength, strength, strength);
			}
		}
		int index = (int) ( (1-(value-min)/(max-min)) * greyscale.length);
		if(index>=greyscale.length) index = greyscale.length-1;
		return greyscale[index];
	}
	
	public void setMatrix(double[][] matrix) {
		this.matrix = matrix;
		updateMinMax();
		if( matrix.length==sim.tn.pd.NPHONS || matrix.length==sim.tn.nwords ){ //phoneme or lexicon matrix
			squareSize = 16;
			betweenSquares = 3;
			leftSpace = 50;
		}
		setPreferredSize(new Dimension(matrix[0].length*(squareSize+betweenSquares)+betweenSquares+leftSpace, 
				matrix.length*(squareSize+betweenSquares)+betweenSquares));
		repaint();
	}
	
	/**
	 * Brings up a frame containing a matrix viewer for the matrix passed as a parameter
	 * @param matrix
	 */
	public static void showMatrix(double[][] matrix){
		showMatrix(matrix, "");
	}
	public static void showMatrix(double[][] matrix, String title){
		MatrixViewer mv = new MatrixViewer(matrix, null);
		
		JScrollPane scrollPane = new JScrollPane(mv);
		
		final JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(scrollPane, BorderLayout.CENTER);
		f.pack();
		f.setTitle(title);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {}
			@Override
			public void focusGained(FocusEvent arg0) {
				f.repaint();
			}
		});
	}
	
	
}
