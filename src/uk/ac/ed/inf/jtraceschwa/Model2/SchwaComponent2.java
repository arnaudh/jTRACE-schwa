package uk.ac.ed.inf.jtraceschwa.Model2;

public class SchwaComponent2 {

	
	private SchwaNet2 net;
	private LexicalStressComponent2 lexicalStressComponent;
	
	// activation values for the schwa component
	private double[] schwaActivations;
	
	// parameters
	double phonemeInhibition = 0.001;
	double wordActivation    = 0.001;
	
	public SchwaComponent2(SchwaNet2 net) {
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
		schwaActivations = new double [ net.pSlices ];
        for(int fslice=0;fslice<net.fSlices;fslice++){
        	int pslice = fslice/3;
        	//do a weighted sum of the 8 dimensions of the schwa features
        	for(int i=1; i<=8; i++){
        		if( net.featLayer[63+8-i][fslice]> 0 ){
        			schwaActivations[pslice] += i * net.featLayer[63+8-i][fslice];
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
        for(int phon=0;phon<net.pd.NPHONS;phon++){
          	if( phon==net.schwaIndex ) continue; //Exclude old schwa phoneme
          	int schwaWeight = ((SchwaParam2)net.tp).getSchwaWeightOf(net.pd.getLabels()[phon]);
          	for(int pslice = 0; pslice < net.pSlices; pslice++){
          		double weight = phonemeInhibition * (8-schwaWeight);
          		net.phonNet[phon][pslice] -= weight * schwaActivations[pslice];
          	}
        }
	}
	
	/**
	 * Sends activation to words containing schwa
	 */
	private void schwaToWord() {
    	// iterate over the lexicon 
        for(int word=0;word<net.tp.getLexicon().size();word++){         
            String str = net.tp.getLexicon().get(word).getPhon();
            int strlen = str.length();
            //for each letter in the current word
            for(int offset=0;offset<strlen;offset++){
                //if that letter corresponds to the schwa
                 if(str.charAt(offset)=='^'){
                	 for( int wslice = 0; wslice < net.wSlices-offset; wslice++){
                		 	net.wordNet[word][wslice] += wordActivation*schwaActivations[wslice+offset];
                	 }
                 }
            }
        }
    }

	
	
	// Getters and Setters
	
	public double[] getSchwaActivations() {
		return schwaActivations;
	}

	public void setLexicalStressComponent(LexicalStressComponent2 lexicalStressComponent) {
		this.lexicalStressComponent = lexicalStressComponent;
	}

}
