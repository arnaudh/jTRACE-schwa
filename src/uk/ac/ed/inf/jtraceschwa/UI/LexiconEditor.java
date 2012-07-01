package uk.ac.ed.inf.jtraceschwa.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import sun.nio.cs.ext.GBK;

import edu.uconn.psy.jtrace.Model.TraceLexicon;
import edu.uconn.psy.jtrace.Model.TraceParam;
import edu.uconn.psy.jtrace.Model.TraceWord;

public class LexiconEditor extends JPanel {
	
	private TraceParam tp;
	
	//ui
	private JTextComponent textcomp;
	
	public LexiconEditor(TraceParam tp) {
		this.tp = tp;
		
		//ui
		textcomp = new JTextArea(){ //no preferred or minimum size so that it adapts to teh LayoutManager
			public Dimension getPreferredSize() {
				return new Dimension();
			};
			public Dimension getMinimumSize() {
				return new Dimension();
			};
		};
		updateEditorFromLexicon();
		addUndoRedo();
		textcomp.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateLexiconFromEditor();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateLexiconFromEditor();		
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateLexiconFromEditor();		
			}
		});
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textcomp);
		
		//layout
//		this.setLayout(new BorderLayout());
//		this.add(new JLabel("Lexicon"), BorderLayout.NORTH);
//		this.add(scrollPane, BorderLayout.CENTER);
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 0;
		gbc.weighty = 0;
		this.add(new JLabel("Lexicon"), gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.weighty = 1;
		this.add(scrollPane, gbc);
	}

	private void updateEditorFromLexicon(){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<tp.getLexicon().size(); i++){
			sb.append(tp.getLexicon().get(i).getPhon());
			sb.append('\n');
		}
		textcomp.setText(sb.toString());
	}
	
	private void updateLexiconFromEditor(){
		tp.getLexicon().reset();
		Pattern pattern = Pattern.compile(tp.getPhonology().getInputPattern());
		Matcher matcher = pattern.matcher(textcomp.getText());
		while( matcher.find() ){
			tp.getLexicon().add(new TraceWord(matcher.group()));
		}
	}

	private void addUndoRedo(){
		final UndoManager undo = new UndoManager();
		Document doc = textcomp.getDocument();

		// Listen for undo and redo events
		doc.addUndoableEditListener(new UndoableEditListener() {
		    public void undoableEditHappened(UndoableEditEvent evt) {
		        undo.addEdit(evt.getEdit());
		    }
		});

		// Create an undo action and add it to the text component
		textcomp.getActionMap().put("Undo",
		    new AbstractAction("Undo") {
		        public void actionPerformed(ActionEvent evt) {
		            try {
		                if (undo.canUndo()) {
		                    undo.undo();
		                }
		            } catch (CannotUndoException e) {
		            }
		        }
		   });

		// Bind the undo action to ctl-Z
		textcomp.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

		// Create a redo action and add it to the text component
		textcomp.getActionMap().put("Redo",
		    new AbstractAction("Redo") {
		        public void actionPerformed(ActionEvent evt) {
		            try {
		                if (undo.canRedo()) {
		                    undo.redo();
		                }
		            } catch (CannotRedoException e) {
		            }
		        }
		    });

		// Bind the redo action to ctl-Y
		textcomp.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
	}
}

