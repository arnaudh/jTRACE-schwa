package uk.ac.ed.inf.jtraceschwa.Model;

import uk.ac.ed.inf.jtraceschwa.Model.schwa.Schwa;
import uk.ac.ed.inf.jtraceschwa.Model.schwa.SchwaLexiconActivator;
import uk.ac.ed.inf.jtraceschwa.UI.graph.MatrixViewer;
import edu.uconn.psy.jtrace.Model.TraceNet;
import edu.uconn.psy.jtrace.Model.TraceParam;

public class SchwaNet extends TraceNet {
	
	public int schwaIndex = 1; //index of the phoneme schwa in the phonLayer
	public Schwa schwa;

	public SchwaNet(TraceParam tp) {
		super(tp);
		schwa = new Schwa();
		schwa.addSchwaListener(new SchwaLexiconActivator(this));
	}
	
	//Override desired methods...

	
	@Override
	public double[][][] cycle() {

        act_features();
        
        featToPhon();
        phonToPhon();
        phonToWord();
        //update Schwa component
        phonToSchwa();
        
        wordToPhon();
        wordToWord();
        featUpdate();
        phonUpdate();
        wordUpdate();                   

        inputSlice += __nreps; //nrep steps in a cycle
        //array boundary check
        if(inputSlice >= fSlices) 
            inputSlice = fSlices-1;
		return null; //return value never used...
	}
	
	private void phonToSchwa(){
		double schwaVal = phonLayer[schwaIndex][inputSlice/3];
		schwa.setActivation(schwaVal);
	}
	
	@Override
	public void phonToPhon() {
        int pmax, pmin, halfdur;
        halfdur=1; 
        double[] ppi=new double[pSlices];
        //the ppi accumulates all of the inhibition at a particular phoneme slice.
        //this amount of inhibition is later applied equally to all phonemes.        
        for(int slice=0;slice<pSlices;slice++)
            for(int phon=0;phon<pd.NPHONS;phon++)
                //if the phon unit has activation, determine its extent (does it hit an edge?) ...                
                if(phonLayer[phon][slice]>0){
                    pmax=slice+halfdur;
                    if(pmax>=pSlices){
                        pmax = pSlices-1;                    
                        pmin=slice-halfdur; 
                    }
                    else{ 
                        pmin=slice-halfdur; 
                        if(pmin < 0)
                            pmin=0;
                    }
                    //then add its activation to ppi, scaled by gamma.
                    for(int i=pmin;i<pmax;i++)
                        ppi[i]+=phonLayer[phon][slice]*tp.getGamma().P;                    
                }
        //now, determine again the extent of each phoneme unit,
        //then apply inhibition equally to phons lying on the same phon slice.
        globalPhonemeCompetitionIndex=0;
        phonLoop:for(int phon = 0; phon < pd.NPHONS; phon++){ //loop over phonemes             
            for(int slice = 0; slice < pSlices; slice++)  //loop over phoneme slices (original configuration 33)                
            {
            	// SCHWA doesn't get inhibited
            	if( ((SchwaParam)tp).shcwaPhonemeLocked && phon==schwaIndex ) continue phonLoop;
            	
                pmax=slice+halfdur;
                if(pmax>=pSlices){
                    pmax = pSlices-1;                    
                    pmin=slice-halfdur; 
                }
                else{ 
                    pmin=slice-halfdur; 
                    if(pmin < 0)
                        pmin=0;
                }
                for(int i=pmin;i<pmax;i++){
                    //application of inhibition occurs here
                    if(ppi[i]>0){
                        phonNet[phon][slice]-=ppi[i];                        
                        globalPhonemeCompetitionIndex+=ppi[i];
                    }
                }
                //here, we make up for self-inhibition, reimbursing nodes for inhibition that 
                //originated from themselves.
                if((phonLayer[phon][slice]*tp.getGamma().P)>0&&ppi[slice]>0){
                    phonNet[phon][slice]+=((pmax-pmin)*phonLayer[phon][slice])*tp.getGamma().P;                    
                    globalPhonemeCompetitionIndex-=((pmax-pmin)*phonLayer[phon][slice])*tp.getGamma().P;                    
                }
                //here, we make up for allophone-inhibition, reimbursing nodes for inhibition
                //that originate from allophones of the target, as defined in the allophon matrix.
                //note that this is an experimental feature of jtrace, implemented by tjs, 07/19/2007.
                for(int allophone = 0; allophone < pd.NPHONS; allophone++){ //loop over phonemes             
                    if(tp.getPhonology().getAllophoneRelation(phon, allophone)){
                        phonNet[phon][slice]+=((pmax-pmin)*phonLayer[allophone][slice])*tp.getGamma().P;                    
                    }
                }
            }            
        }
    }
	
	@Override
	public void reset() {
		super.reset();
		if( schwa!=null) schwa.reset();
	}
	
}
