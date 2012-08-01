package uk.ac.ed.inf.jtraceschwa.Model;

import uk.ac.ed.inf.jtraceschwa.IO.IOTools;
import uk.ac.ed.inf.jtraceschwa.Model.schwa.LexicalStressComponent;
import uk.ac.ed.inf.jtraceschwa.Model.schwa.Schwa;
import edu.uconn.psy.jtrace.Model.TraceNet;
import edu.uconn.psy.jtrace.Model.TraceParam;

/**
 * Modified version of the trace net.
 * Methods of TraceNet are overriden to take into account the fact that schwa doesn't receive inhibition from other phonemes,
 * @author arnaudhenry
 *
 */
public class SchwaNet extends TraceNet {
	
	public int schwaIndex = 1; //index of the phoneme schwa in the phonLayer
	public Schwa schwa;
	
	public LexicalStressComponent lexicalStressComponent;

	public SchwaNet(TraceParam tp, boolean useLexicalStress) {
		super(tp);
		schwa = new Schwa(this);
		//Components related to schwa
		if( useLexicalStress ){
			lexicalStressComponent = new LexicalStressComponent(this);
			schwa.addSchwaListener(lexicalStressComponent);
		}
		
		// get the schwa index 
        for(int phon=0;phon<pd.NPHONS;phon++){
        	if( pd.toChar(phon) == '^' ){
        		schwaIndex = phon;
        		break;
        	}
        }
		
	}
	
	//Override desired methods...

	
	@Override
	public double[][][] cycle() {

        act_features();
        
        featToPhon();
        phonToPhon(); //excludes schwa
        phonToWord(); //excludes schwa
        
        phonToSchwa(); //update Schwa component
        
        wordToPhon(); //excludes schwa
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
		schwa.setActivations(phonNet[schwaIndex]);
	}

    
	@Override
	public void phonToPhon() {
        int pmax, pmin, halfdur;
        halfdur=1; 
        double[] ppi=new double[pSlices];
        //the ppi accumulates all of the inhibition at a particular phoneme slice.
        //this amount of inhibition is later applied equally to all phonemes.        
        for(int slice=0;slice<pSlices;slice++)
            for(int phon=0;phon<pd.NPHONS;phon++){
            	if( phon==schwaIndex ) continue; //Exclude schwa
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
            }
        //now, determine again the extent of each phoneme unit,
        //then apply inhibition equally to phons lying on the same phon slice.
        globalPhonemeCompetitionIndex=0;
        phonLoop:for(int phon = 0; phon < pd.NPHONS; phon++){ //loop over phonemes   
        	if( phon==schwaIndex ) continue; //Exclude schwa
            for(int slice = 0; slice < pSlices; slice++)  //loop over phoneme slices (original configuration 33)                
            {
            	
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
	//This implementation actually depends on pdur being 2, re: pww dynamics.
    public void phonToWord() {
        double excitation = 0;
        edu.uconn.psy.jtrace.Model.TraceLexicon dict = tp.getLexicon();
        String str;     
        double t;
        int wpeak, wmin, winstart, wmax, pdur, strlen;
        //for each phoneme
        for(int phon=0;phon<pd.NPHONS;phon++){
        	if( phon==schwaIndex ) continue; //Exclude schwa
            pdur=2;
            
            //hack
            if(tp.getDeltaInput()!=6||tp.getSlicesPerPhon()!=3)
                pdur=(int)Math.floor(tp.getDeltaInput()/tp.getSlicesPerPhon());
            //end hack
            
            //and for each phoneme slice
            for(int pslice=0;pslice<pSlices;pslice++){
                //if the current unit is below zero, skip it.
                if(phonLayer[phon][pslice]<=0) continue;
                //iterate over each word in the dictionary
                words: for(int word=0;word<dict.size();word++){                    
                    str = dict.get(word).getPhon();
                    strlen = str.length();
                    //for each letter in the current word
                    for(int offset=0;offset<strlen;offset++){
                        //if that letter corresponds to the phoneme we're now considering...
                         if(str.charAt(offset)==pd.toChar(phon)){
                             //then determine the temporal range of word units for which it
                             //makes sense that the current phoneme should send activation to it.
                             wpeak = pslice - (pdur * offset);
                             if(wpeak< -pdur) continue words;
                             wmin = 1 + wpeak - pdur; 
                             if(wmin < 0){
                                winstart = 1 - wmin;
                                wmin = 0;
                                wmax = wpeak + pdur;                                 
                             }
                             else{
                                 wmax = wpeak + pdur; 
                                 if(wmax > pSlices - 1)
                                     wmax = pSlices - 1;
                                 winstart = 1; 
                             }
                             //determine the raw amount of activation that is sent to the word units
                             t = 2 * phonLayer[phon][pslice] * tp.getAlpha().PW; //cTRACE: the 2 stands for word->scale
                             
                             double wfrq=0;
                             if(tp.getFreqNode().RDL_wt_s != 0&&dict.get(word).getFrequency()>0){
                               wfrq =  tp.getFreqNode().RDL_wt_s  * ((double)Math.log((double)dict.get(word).getFrequency()) * 0.434294482);
                             }
                             double wprm=0;
                             if(tp.getPrimeNode().RDL_wt_s != 0&&dict.get(word).getPrime()>0){
                               wprm =  tp.getPrimeNode().RDL_wt_s  * ((double)Math.log((double)dict.get(word).getPrime()) * 0.434294482);
                               //t = tp.getPrimeNode().applyWeightPrimeScaling(tp.getLexicon().get(word), t);                                
                             }
                             
                             double _t;
                             //now iterate over the temporal range determined about 15 lines above
                             for(int wslice = wmin; wslice<wmax && wslice<wSlices; wslice++, winstart++)
                                 if(winstart>=0 && winstart<4){
                                     //scale activation by pww; this determines how temporal offset should affect excitation                                        
                                     wordNet[word][wslice] += (1+wfrq+wprm) * pww[phon][winstart] * t;                                                                          
                                 }
                         }
                    }
                }
            }
        }        
    }    
	

	@Override
    //lexical to phoneme feedback.
    public void wordToPhon() {
        //temporary monitoring variables
        int monitortarget=-1;
        int monitorslice=4;
        double[][][] monitorfdbk;        
        //initialize variables
        double excitation = 0;
        edu.uconn.psy.jtrace.Model.TraceLexicon dict = tp.getLexicon();
        int strlen, phon;
        String str;
        int wslot, t_o_p, pmin, pwin, pmax, currChar; 
        char t_c_p;
        //for every word in the lexicon
        for(int word = 0 ; word < dict.size(); word++){
            //for each word slice
            for(int wslice = 0; wslice < wSlices; wslice++){
                //if the word has activation above zero
                if(wordLayer[word][wslice]<=0) continue;
                //determine what range of slices (for that word unit) can be
                //fed back to the phoneme layer.
                str = dict.get(word).getPhon();                
                for(int wstart=0; wstart < str.length(); wstart++){
                    t_c_p = str.charAt(wstart);
                    currChar = pd.mapPhon(t_c_p);
                    if( currChar==schwaIndex ) continue; //Exclude schwa
                    wslot = wslice + (wstart*2);
                    pmin = wslot - 1; //??
                    if(pmin >= pSlices) break;
                    if(pmin < 0){
                        pwin = 1 - pmin;
                        pmin=0;
                        pmax = wslot + 2;  //from +2                        
                    }
                    else{
                        pmax = wslot + 2;  //from +2
                        if ( pmax > pSlices - 1)
			    pmax = pSlices - 1;                                                
                        pwin = 1;                        
                    }
                    
                    //now that we know the range to iterator over, iterate over the appropriate phoneme slices
                    for(int pslice = pmin ;pslice < pmax && pslice < pSlices && pwin<4; pslice++, pwin++){
                        //this check makes sure that ambiguous phonemes do not feedback
                        if(currChar > pd.NPHONS && currChar < 0){ 
                            int contIdx;
                            try{
                                contIdx=(new Integer(tp.getContinuumSpec().toCharArray()[2])).intValue();
                            } catch(Exception e){ e.printStackTrace(); contIdx=-1; }
                            if(contIdx==-1){ 
                                //there is something wrong with the input or the continuum.
                                //feedback will not be calculated for this character.
                                break;
                            }       
                            else{
                               if(currChar==50){ //this is the bottom of the continuum.
                                   currChar = pd.mapPhon(tp.getContinuumSpec().toCharArray()[0]);                                   
                               }
                               else if(currChar==(50+contIdx-1)){ //this is the top of the continuum
                                   currChar = pd.mapPhon(tp.getContinuumSpec().toCharArray()[2]);                                   
                               }
                               else{ //in the middle of the continuum
                                   //feedback will not be accumulated for any ambiguous phonemes representations
                                   break;
                               }
                            }
                        }
                        //if the current word activation is above zero
                        if(wordLayer[word][wslice] > 0){
                                //if lexical frequency is in effect.                            
                                //if(tp.getFreqNode().RDL_wt_s!=0&&dict.get(word).getFrequency()>0){
                            
                            double wfrq=0;
                            if(dict.get(word).getFrequency()>0&&tp.getFreqNode().RDL_wt_s>0)
                                wfrq = tp.getFreqNode().RDL_wt_s  * (Math.log(0 + dict.get(word).getFrequency()) * 0.434294482);                                            
                            double wprim=0;
                            if(dict.get(word).getFrequency()>0&&tp.getPrimeNode().RDL_wt_s>0)
                                wprim = tp.getPrimeNode().RDL_wt_s  * (Math.log(0 + dict.get(word).getPrime()) * 0.434294482);                
                            
                            //scale the activation by alpha and wpw
                            phonNet[currChar][pslice] += (1 + wfrq + wprim) * wordLayer[word][wslice] * tp.getAlpha().WP * wpw[currChar][pwin]; 
                                                        
                        }
                    }
                }                
            }
        }
        //output monitored feedback        
        /*String contents;
        double notzero;
        for(int h=0;h<monitorfdbk.length;h++){
            contents="";
            for(int i=0;i<monitorfdbk[0].length;i++){
                notzero=0;
                for(int j=0;j<monitorfdbk[0][0].length;j++)
                    if(monitorfdbk[h][i][j]>notzero) notzero=monitorfdbk[h][i][j];
                if(notzero==0) continue;
                contents+="\\N\t"; 
                contents+=tp.getAlpha().WP+"\t"; // alpha
                contents+=inputstring+"\t"; // input string  
                contents+=tp.getLexicon().get(h).getPhon()+"\t"; // feedbackER
                contents+=monitorslice+"\t"; //left edge of targ word
                contents+=(monitorslice+(tp.getLexicon().get(monitortarget).getPhon().length()*2)-1)+"\t"; //right edge of targ word        
                contents+=inputSlice+"\t"; //input cycle
                contents+=pd.toChar(i)+"\t"; // feedbackEE        
                for(int j=0;j<monitorfdbk[0][0].length;j++)
                    contents+=monitorfdbk[h][i][j]+"\t";
                contents+="\n";
            }
            //diagnosticFileWriter.write(contents);                        
        }    */    
    }
	
	@Override
	public void reset() {
		super.reset();
		if( schwa!=null) schwa.reset();
	}
	
}
