package uk.ac.ed.inf.jtraceschwa.Model;

public class SchwaComponent {

	
	private ConcreteTraceNet net;
	private LexicalStressComponent lexicalStressComponent;
	
	// activation values for the schwa component
	private double[] activations;
	
	public SchwaComponent(ConcreteTraceNet net) {
		super();
		this.net = net;
	}
	
	/**
	 * Execute one cycle of the schwa's role in the model
	 */
	public void cycle(){
		featToSchwa();
        schwaToPhon();
        schwaToWord();
        if(lexicalStressComponent!=null) lexicalStressComponent.schwaUpdated(this);
	}

	/**
	 * updates the schwa activation from the feature layer
	 */
	private void featToSchwa() {
		activations = new double [ net.pSlices ];
        for(int fslice=0;fslice<net.fSlices;fslice++){
        	int pslice = fslice/3;
        	//do a weighted sum of the 8 dimensions of the schwa features
        	for(int i=1; i<=8; i++){
        		if( net.featLayer[63+8-i][fslice]> 0 ){
        			activations[pslice] += i * net.featLayer[63+8-i][fslice];
        		}
        	}
        }
//        for(int pslice = 0; pslice<net.pSlices; pslice++){
//        	System.out.println("["+pslice+"] sum="+sums[pslice]); //sum goes from 0 to 10 (rough idea)
//        }
	}

	/**
	 * Inhibition from schwa to the phonemes
	 */
	private void schwaToPhon(){
		double inhibition = ((ConcreteTraceParam)net.tp).phonemeInhibition;
        for(int phon=0;phon<net.pd.NPHONS;phon++){
          	if( phon==net.schwaIndex ) continue; //Exclude old schwa phoneme
          	int schwaWeight = ((ConcreteTraceParam)net.tp).getSchwaWeightOf(net.pd.getLabels()[phon]);
          	for(int pslice = 0; pslice < net.pSlices; pslice++){
          		double weight = inhibition * (8-schwaWeight);
          		net.phonNet[phon][pslice] -= weight * activations[pslice];
          	}
        }
	}
	
	/**
	 * Sends activation to words containing schwa
	 */
	private void schwaToWord() {
		double activation = ((ConcreteTraceParam)net.tp).wordActivation;
    	// iterate over the lexicon 
        for(int word=0;word<net.tp.getLexicon().size();word++){         
            String str = net.tp.getLexicon().get(word).getPhon();
            int strlen = str.length();
            //for each letter in the current word
            for(int offset=0;offset<strlen;offset++){
                //if that letter corresponds to the schwa
                 if(str.charAt(offset)=='^'){
                	 for( int wslice = 0; wslice < net.wSlices-offset; wslice++){
                		 	net.wordNet[word][wslice] += activation *activations[wslice+offset];
                	 }
                 }
            }
        }
    }

	
	
	// Getters and Setters
	
	public double[] getActivations() {
		return activations;
	}

	public void setLexicalStressComponent(LexicalStressComponent lexicalStressComponent) {
		this.lexicalStressComponent = lexicalStressComponent;
	}

}
