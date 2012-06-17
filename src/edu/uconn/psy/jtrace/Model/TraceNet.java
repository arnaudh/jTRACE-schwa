/*
 * TraceNet.java
 *
 * Created on April 16, 2004, 5:18 PM
 */
 
package edu.uconn.psy.jtrace.Model;

import edu.uconn.psy.jtrace.UI.traceProperties;

/**
 * 
 *  The code implementing the TRACE model is located in this class.
 *  Many of the instance variables are stored in TraceParam.
 *  This class is responsible for computing one processing cycle at a time,
 *  wherein an entire simulation consists of multiple cycles. TraceSim
 *  is responsible for cycling this class and storing the data that results.
 *
 *  In order to quantitatively replicate the original TRACE model's performance,
 *  the original C code has been copied as closely as possible.  We have even 
 *  retained some bizarre variable names in case anyone wants to refer to the 
 *  original code.  If there are bugs in the C code, they have been replicated 
 *  here.  Comments attempt to clarify the flow of activation values.
 *  
 */
public class TraceNet {
    private int nwords; //number of words, calculated from the lexicon
    private int inputSlice = 0; //
    private double inputLayer[][], featLayer[][], phonLayer[][], wordLayer[][]; //current activation values
    private double featNet[][], phonNet[][], wordNet[][]; //used during processing to store intermediate states
    private int fSlices, pSlices, wSlices; //width of arrays (number of slices)
    double phonarr[][][]; //phoneme representations, fetched from TracePhones
    private TracePhones phonemeRepresentation= null; //phoneme representations
    private TraceParam parameters = null; //parameters for this net
    private TraceError terr = null;
    private double pww[][], wpw[][], pfw[][][], fpw[][][]; //represents the extent to which a unit can activate a non-overlapping unit on a different layer.
    private int __nreps;
            
    edu.uconn.psy.jtrace.IO.WTFileWriter diagnosticFileWriter; //tmp
    public String inputstring; //tmp    
    
    private edu.uconn.psy.jtrace.Model.GaussianDistr stochasticGauss; //gaussian noise applied to processing layers
    private edu.uconn.psy.jtrace.Model.GaussianDistr inputGauss; //gaussian noise applied to input representation
    
    private boolean length_normalization;
    private double length_normalization_scale;
    double globalPhonemeCompetitionIndex;
    double globalLexicalCompetitionIndex;
    /** Creates a new instance of TraceNet */
    public TraceNet(TraceParam tp) {
        terr = new TraceError();
        if(tp == null){
            terr.report("Fatal Error: TraceParam = null passed to TraceNet");
            return; //fatal error
        }
        this.parameters = tp;
        reset();
    } 
    
    /**
     * Resets the net to its initial state, using the existing parameters.
     */
    
    public void reset()
    {
        if(parameters.getLengthNormalization()==0)
            length_normalization=false;
        else
            length_normalization=true;
        // a clever way to guess the optimal 'fulcrum' of length normalization
        length_normalization_scale=1/parameters.getLexicon().getMeanWordLength();
        inputSlice = 0;
        
        if(parameters.getNoiseSD()!=0)  inputGauss=new edu.uconn.psy.jtrace.Model.GaussianDistr(0.0, parameters.getNoiseSD());
        if(parameters.getStochasticitySD()!=0) stochasticGauss=new edu.uconn.psy.jtrace.Model.GaussianDistr(0.0, parameters.getStochasticitySD());
        
        phonemeRepresentation = parameters.getPhonology();
        phonemeRepresentation.compileAll();
        nwords = parameters.getNWords();
        fSlices = parameters.getFSlices();
        pSlices = fSlices / parameters.getSlicesPerPhon(); 
        wSlices = pSlices; //currently word slices and phoneme slices are aligned 1:1
        inputLayer = new double[phonemeRepresentation.NFEATS*phonemeRepresentation.NCONTS][fSlices];
        featLayer = new double[phonemeRepresentation.NFEATS*phonemeRepresentation.NCONTS][fSlices];
        featNet = new double[phonemeRepresentation.NFEATS*phonemeRepresentation.NCONTS][fSlices];        
        phonLayer = new double[phonemeRepresentation.NPHONS][pSlices];
        phonNet = new double[phonemeRepresentation.NPHONS][pSlices];
        wordLayer = new double[nwords][wSlices];
        wordNet = new double[nwords][wSlices];
        
        pww = new double[phonemeRepresentation.NPHONS][4];
        wpw = new double[phonemeRepresentation.NPHONS][4];        
        fpw = new double[phonemeRepresentation.NPHONS][phonemeRepresentation.NCONTS][];
        pfw = new double[phonemeRepresentation.NPHONS][phonemeRepresentation.NCONTS][];        
        
        for(int p=0;p<phonemeRepresentation.NPHONS;p++)
            for(int c=0;c<phonemeRepresentation.NCONTS;c++){        
                fpw[p][c]= new double[parameters.getSpread()[c]*2];
                pfw[p][c]= new double[parameters.getSpread()[c]*2];
            }        
        // if there is a phoneme continuum defined in the parameters, create it here.
        if(parameters.getContinuumSpec().trim().length()==3){
            int step=(new Integer(new Character(parameters.getContinuumSpec().trim().charAt(2)).toString())).intValue();
            if(step>1&&step<10)
                try
                {
                    phonemeRepresentation.makePhonemeContinuum(parameters.getContinuumSpec().trim().charAt(0),parameters.getContinuumSpec().trim().charAt(1),step);
                }
                catch (TraceException te)
                {
                    System.out.println("Problems in makePhonemeContinuum");
                }
        }
        
        try {
            phonemeRepresentation.spreadPhons(parameters.getSpread(), parameters.getSpreadScale(), parameters.getMin(), parameters.getMax());
        }
        catch(TraceException td) {
            report(td.getMessage());
            return;
        }
        
        //init feature layer to resting value
        double rest;
        rest = parameters.clipWeight(parameters.getRest().F);
        for(int fslice = 0 ; fslice < fSlices; fslice++)
            for(int feat = 0 ; feat < phonemeRepresentation.NCONTS * phonemeRepresentation.NFEATS ; feat++)
                featLayer[feat][fslice] = rest;
        
        //init phon layer to resting value
        rest = parameters.clipWeight(parameters.getRest().P);
        for(int slice = 0; slice < pSlices; slice++)
            for(int phon = 0; phon < phonemeRepresentation.NPHONS; phon++)
                phonLayer[phon][slice] = rest;          
        
        //init word layer to resting value
        //Original frequency implementation from cTRACE is being dropped: 
        //  wp->base = rest[W] + fscale*log(1. + wordfreq[i]);
        rest = parameters.clipWeight(parameters.getRest().W);
        for(int wslice = 0; wslice < wSlices; wslice++)
            for(int word = 0; word < nwords; word++){
                wordLayer[word][wslice] = rest;                                
            }
        //frequency applied to the resting level of lexical items
        if(parameters.getFreqNode().RDL_rest_s != 0){
            for(int wslice = 0; wslice < wSlices; wslice++)
                for(int word = 0; word < nwords; word++){
                    if(parameters.getLexicon().get(word).getFrequency()>0)
                        wordLayer[word][wslice]+= parameters.getFreqNode().applyRestFreqScaling(parameters.getLexicon().get(word));                                
                }
        }
        
        //priming applied to the resting level of lexical items
        if(parameters.getPrimeNode().RDL_rest_s != 0){
            for(int wslice = 0; wslice < wSlices; wslice++)
                for(int word = 0; word < nwords; word++){
                    if(parameters.getLexicon().get(word).getPrime()>0)
                        wordLayer[word][wslice]+= parameters.getPrimeNode().applyRestPrimeScaling(parameters.getLexicon().get(word));                                
                }
        }
                
        
        double denom=0;
        double ft;
        // from C code: tdur = (float)(PWIDTH + POVERLAP)*pp->wscale/FPP = (((6+6)*1)/3)=4
        double tdur = 4; 
        __nreps=parameters.getNReps();
        if(__nreps<=0) __nreps=1;
    
        //calculate the pww and wpw arrays.
        //how much can a phoneme at slice 4 activate a word at slice 5?
        //the pww array contains scalars stating how much to scale down per offset slices.
        //the wpw array is the same idea, except for w->p connections.
        for(int phon = 0; phon < phonemeRepresentation.NPHONS; phon++){
                denom = 0;
                for(int pslice = 0; pslice <= 4; pslice++){
                    ft =  ((tdur - Math.abs(2 - pslice))/ tdur);
                    denom += ft * ft;
                }        
                for(int pslice = 0; pslice < 4; pslice++){
                    ft =  ((tdur - Math.abs(2 - pslice))/ tdur);
                    pww[phon][pslice] = ft / denom;
                    wpw[phon][pslice] = (1 * ft) / denom;                     
                }                
        }
        // Basically (approximately, but both arrays have the same values) :
        // pww = [2, 3, 4, 3] for each phoneme
        // wpw = [2, 3, 4, 3] for each phoneme
        
        //calculate fpw, pfw 
        //how much can a feature influence a phoneme if there are mis-aligned.
        //these arrays state how much to scale down per offset slice.
        int spr, ispr;
        ft=0;
        for(int phon = 0; phon < phonemeRepresentation.NPHONS; phon++){            
            for(int cont = 0; cont < phonemeRepresentation.NCONTS; cont++){
                denom=0;
                spr=parameters.getSpread()[cont]*1; //1 is stand in for pp->wscale (?)
                ispr=spr;
                for(int fslice=0;fslice < 2*ispr;fslice++){
                    ft = (double)(((double)spr - Math.abs((double)ispr -(double)fslice))/(double)spr);
                    denom += ((double)ft * (double)ft);
                }
                for(int fslice=0;fslice < 2*ispr;fslice++){
                    pfw[phon][cont][fslice] = (double)(((double)spr - Math.abs((double)ispr - (double)fslice))/(double)spr);
                    fpw[phon][cont][fslice] = (double)pfw[phon][cont][fslice] * (double)(1/denom);                                        
                }                
            }            
        }    
        // Basically (although the spread ispr could depend on the cont)
        // pfw = [0, 0.16, 0.3, 0.5, 0.6, 0.8, 1, 0.8, 0.6, 0.6, 0.3, 0.16] for each [phon][cont]
        // fpw = [0, 0.04, 0.08, 0.12, 0.16, 0.20, 0.25, 0.20, 0.16, 0.12, 0.08, 0.04] for each [phon][cont]
        
    }
    
    public void resetNoise(){
        if(parameters.getNoiseSD()!=0)  inputGauss=new edu.uconn.psy.jtrace.Model.GaussianDistr(0.0, parameters.getNoiseSD());
        if(parameters.getStochasticitySD()!=0) stochasticGauss=new edu.uconn.psy.jtrace.Model.GaussianDistr(0.0, parameters.getStochasticitySD());        
    }    
    
    /** Create the input layer
     *  loop through all the phonemes, and copy the corresponding features to it.
     *  the offset for the phoneme should be used inorder to center
     **/
    
    /** Variables which have been left out from original trace, M&E did not use them:
     *  WEIGHTp(i),c,fs   STRENGTHp(i)   PEAKp(i)   SUSp(i)   RATEp(i)
     **/
    //converts the model input into a pseudo-spectral input representation, store in inputLayer
    public void createInput(String phons)
    {
        if(traceProperties.startupOptions!=null&&
                traceProperties.startupOptions.equals("-cmp"))
            System.out.println("input= "+phons);
        if(phons==null||phons.length()==0) phons="";
        phons=phons.trim();
        System.out.println(phons);
        
        //store the target:
        if(phons.equals("-")) inputstring=phons;
        else{ 
            try{
                java.util.StringTokenizer tk = new java.util.StringTokenizer(phons.trim(),"-");
                if(tk.hasMoreTokens())
                    inputstring=tk.nextToken();
                else
                    inputstring="---";
            }
            catch(java.util.NoSuchElementException nsee){
                nsee.printStackTrace();
                inputstring="---";
            }
        }
        //create the input layer.
        int phon, slice = 0, inputOffset, phonOffset;
        int i,t,cont, syntactic_incr;
        int deltaInput = parameters.getDeltaInput();
        slice += deltaInput; //attempt to fix something.
        double phonSpread[][][] = phonemeRepresentation.getPhonSpread(); //fetch phoneme representations, may contain ambiguous phonemes.
        double durationScalar[][] = phonemeRepresentation.getDurationScalar(); //fetch phoneme duration scalars
        double ambigDurScalar[][] = phonemeRepresentation.getAmbiguousDurScalars(); //ambig phoneme duration scalars
        // loop over phoneme input. go to next phoneme and step 6 slices. until the end of the input is reached or 
        // FSLICES is reached
        for(i = 0, syntactic_incr=0; (i < parameters.inputLength()) && (slice < fSlices); i++) //next bit moved to for-body: , slice += deltaInput) 
        {
            // if we encounter a 'splice' phone, proceed accordingly
            if(phons.charAt(i+syntactic_incr)=='{'){
                //System.out.println("Splice phone: "+phons.substring(i,i+5));
                int p1 = phonemeRepresentation.mapPhon(phons.charAt(1+i+syntactic_incr)); 
                int splicePoint = Character.getNumericValue(phons.charAt(2+i+syntactic_incr)); 
                int p2 = phonemeRepresentation.mapPhon(phons.charAt(3+i+syntactic_incr)); 
                
                // first half of the spliced phoneme.
                inputOffset = slice - phonemeRepresentation.getSpreadOffset()[p1];
                for(t=inputOffset, phonOffset = 0; t < inputOffset + splicePoint; t++, phonOffset++)
                    for(cont = 0; cont < phonemeRepresentation.NFEATS * phonemeRepresentation.NCONTS; cont++)
                        if(t >= 0 && t < fSlices){
                            //System.out.println("tn:"+cont+","+t+","+phon+","+phonOffset);
                            inputLayer[cont][t] += phonSpread[p1][cont][phonOffset];                         
                        }
                // second half of the spliced phoneme.
                //inputOffset = slice - pd.getSpreadOffset()[p2];
                for(t=inputOffset+splicePoint, phonOffset = splicePoint; t < inputOffset + phonemeRepresentation.getSpreadOffset()[p2]*2; t++, phonOffset++)
                    for(cont = 0; cont < phonemeRepresentation.NFEATS * phonemeRepresentation.NCONTS; cont++)
                        if(t >= 0 && t < fSlices){
                            //System.out.println("tn283: inputLayer["+cont+"]["+t+"] += phonSpread["+p2+"]["+cont+"]["+phonOffset+"];");
                            inputLayer[cont][t] += phonSpread[p2][cont][phonOffset];                         
                        }
                //change this int to make sure that iteration through the input string works right.
                syntactic_incr+=4;
                slice += deltaInput;
            }
            // otherwise, we are dealing with a normal, or ambiguous phoneme input.
            else{ // normal phoneme
                phon = phonemeRepresentation.mapPhon(phons.charAt(i+syntactic_incr));
                //System.out.println("phon->char "+phons.charAt(i+syntactic_incr)+"->"+phon);
                if(phon<50){
                    inputOffset = slice - Math.round((float)(phonemeRepresentation.getSpreadOffset()[phon]));
                    //copy the spread phonemes onto the input layer (aligned correctly)
                    for(t=inputOffset, phonOffset = 0; t < inputOffset + (Math.round((float)phonemeRepresentation.getSpreadOffset()[phon]*2)); t++, phonOffset++)
                        for(cont = 0; cont < phonemeRepresentation.NFEATS * phonemeRepresentation.NCONTS; cont++)
                            if(t >= 0 && t < fSlices){
                                //durationScalar[phon][(int)(cont/pd.NFEATS)];
                                inputLayer[cont][t] += phonSpread[phon][cont][phonOffset];                         
                            }
                    // duration scaling!
                    slice += (int)Math.round((float)(deltaInput * durationScalar[phon][0]));
                }
                else{ //ambiguous phoneme
                    inputOffset = slice - Math.round((float)(phonemeRepresentation.getAmbigSpreadOffset()[phon-50]));
                    //copy the spread phonemes onto the input layer (aligned correctly)
                    for(t=inputOffset, phonOffset = 0; t < inputOffset + (Math.round((float)(phonemeRepresentation.getAmbigSpreadOffset()[phon-50]*2))); t++, phonOffset++) //&& phonOffset < phonSpread[phon][0].length
                        for(cont = 0; cont < phonemeRepresentation.NFEATS * phonemeRepresentation.NCONTS; cont++)
                            if(t >= 0 && t < fSlices){
                                //System.out.println("inputLayer["+cont+"]["+t+"] += phonSpread["+phon+"]["+cont+"]["+phonOffset+"];");
                                inputLayer[cont][t] += phonSpread[phon][cont][phonOffset];                         
                            }
                    // duration scaling!
                    slice += (int)Math.round((float)(deltaInput * ambigDurScalar[phon-50][0]));
                }
            }
            
        }
        //apply input noise here.
        if(parameters.getNoiseSD()!=0d){ 
            for(int feat = 0; feat < phonemeRepresentation.NCONTS*phonemeRepresentation.NFEATS; feat++)
                for(int islice = 0; islice < fSlices; islice++){
                    inputLayer[feat][islice] += inputGauss.nextGauss();
                }
        }
        //make sure the input did not go out of bounds.
        for(int feat = 0; feat < phonemeRepresentation.NCONTS*phonemeRepresentation.NFEATS; feat++)
                for(int islice = 0; islice < fSlices; islice++){
                    inputLayer[feat][islice] = parameters.clipWeight(inputLayer[feat][islice]);
                }
        //the next line copies one column of data, forcing the _feature layer_ to undergo one cycle immediately. 
        //this compensates for a discrepency between jTrace and cTrace; keeps things lined up.                
        initialSlice(); 
    }
    
    public double[][][] cycle() {                
        //int __nreps=tp.getNReps();
        //if(__nreps<=0) __nreps=1;
    
        //order of operation is critical here
        act_features();
        int cycles=parameters.getNReps();
        if(cycles<0) cycles=Math.abs(cycles);
        else cycles=1;
        for(int j=0;j<cycles;j++){
            featToPhon();
            phonToPhon();
            //phonToFeat();  //not yet implemented correctly; no one has ever been interested in this aspect.
            phonToWord();
            wordToPhon();
            wordToWord();
            if(traceProperties.startupOptions!=null&&
                traceProperties.startupOptions.equals("-cmp"))
               System.out.println(globalPhonemeCompetitionIndex+"\t"+globalLexicalCompetitionIndex);
            featUpdate();
            phonUpdate();
            wordUpdate();                   
            //featurePhonemeWeightUpdate();
        }
        inputSlice += __nreps; //nrep steps in a cycle
        //array boundary check
        if(inputSlice >= fSlices) 
            inputSlice = fSlices-1;
        double D[][][]={wordLayer,featLayer,phonLayer,inputLayer};         
        return D;
    }
    
    //this method compensates for a small difference between jTrace and cTrace.
    //it is called during initialization
    public void initialSlice(){
        for(int c = 0;c < phonemeRepresentation.NCONTS; c++)
            for(int f = 0;f < phonemeRepresentation.NFEATS; f++)
                featNet[(c*phonemeRepresentation.NFEATS)+f][0]+=inputLayer[(c*phonemeRepresentation.NFEATS)+f][0];                   
        featUpdate();
        
    }
    
    //variable names taken from cTRACE.
    //input-to-feature activation, AND feature-to-feature inhibition.
    public void act_features(){
        double[][] fsum=new double[phonemeRepresentation.NCONTS][fSlices]; //sum of prev slice's positive activations summed over each continuum at each fslice        
        double[][] ffi=new double[phonemeRepresentation.NCONTS][fSlices]; //ff=[c][i]=fsum[c][i]*Gamma.F
        //computes total inhibition coming from a continuum to each node at that time slice
        for(int c = 0;c < phonemeRepresentation.NCONTS; c++)  
            for(int f = 0;f < phonemeRepresentation.NFEATS; f++)
                for(int fslice= -1;fslice < fSlices-1; fslice++)
                    if(featLayer[(c*phonemeRepresentation.NFEATS)+f][fslice+1] > 0)
                        fsum[c][fslice+1] += featLayer[(c*phonemeRepresentation.NFEATS)+f][fslice+1];
        //this block scales down the fsum value by Gamma.F
        for(int c = 0;c < phonemeRepresentation.NCONTS; c++) 
            for(int fslice= -1;fslice < fSlices-1; fslice++)
                   ffi[c][fslice+1] = fsum[c][fslice+1] * parameters.getGamma().F;        
        //this block copies input activations to the feature layer
        if(inputSlice < fSlices){ 
            for(int fIndex = 0;fIndex < phonemeRepresentation.NCONTS*phonemeRepresentation.NFEATS; fIndex++)
                for(int fslice = inputSlice; (fslice < fSlices-1) && (fslice < inputSlice + __nreps); fslice++){ //small variation from original
                    featNet[fIndex][fslice+1] += parameters.clipWeight(parameters.getAlpha().IF * inputLayer[fIndex][fslice+1]); //input->feature activation
                }
        }
        //this block applies ffi inhibition to each node in the featue layer, and compensates for self-inhibition
        for(int c = 0;c < phonemeRepresentation.NCONTS; c++) 
            for(int f = 0;f < phonemeRepresentation.NFEATS; f++)
                for(int fslice= -1;fslice < fSlices-1; fslice++)
                    if((ffi[c][fslice+1] - (featLayer[(c*phonemeRepresentation.NFEATS)+f][fslice+1]*parameters.getGamma().F)) > 0) 
                        featNet[(c*phonemeRepresentation.NFEATS)+f][fslice+1] -= (ffi[c][fslice+1] - Math.max(0,(featLayer[(c*phonemeRepresentation.NFEATS)+f][fslice+1]*parameters.getGamma().F)));                            
    }    
    //feature to phoneme activations
    public void featToPhon(){
        int fspr, fmax, pstart, pend, winstart, c;
        double t;
        int FPP = parameters.getSlicesPerPhon();
        //for every feature at every slice, if the units activation is above zero,
        //then send activation to phonNet from the featLayer scaled by PhonDefs, 
        //spread, fwp and alpha.
        for(int featIndex=0;featIndex<phonemeRepresentation.NCONTS*phonemeRepresentation.NFEATS;featIndex++){
            for(int fslice=0;fslice<fSlices;fslice++){
                if(featLayer[featIndex][fslice]>0){
                    //for all phonemes affected by the current feature.
                    //C code appears to ignore the first phoneme affected by feat (why?)
                    for(int phon=0;phon<phonemeRepresentation.NPHONS;phon++){
                        //if the phoneme definition is blank here continue.
                        if(phonemeRepresentation.PhonDefs[phon][featIndex]==0) continue;
                        //determine, based on current slice and spread, what range of
                        //phoneme units to send activation to.
                        fspr = parameters.getSpread()[featIndex/phonemeRepresentation.NFEATS];
                        fmax = fSlices - fspr;
                        if(fslice < fspr){
                            pstart = 0;
                            pend = (fslice + fspr - 1)/FPP;                            
                        }
                        else{
                            if(fslice > fmax) pend = pSlices - 1;
                            else pend = (fslice + fspr - 1)/FPP;
                            pstart = ((fslice - fspr)/FPP) + 1;
                        }
                        winstart = fspr - (fslice - (FPP*pstart));
                        
                        //include only positive acoustic evidence
                        if(featLayer[featIndex][fslice] > 0) 
                            t = phonemeRepresentation.PhonDefs[phon][featIndex] * featLayer[featIndex][fslice] * parameters.getAlpha().FP;
                        else 
                            t = 0;
                        
                        c = featIndex / phonemeRepresentation.NFEATS;
                        for(int pslice=pstart;pslice<(pend+1)&&pslice<pSlices;pslice++){                            
                            //System.out.println(phon+"\t"+pslice+"\t"+phon+"\t"+c+"\t"+winstart);
                            phonNet[phon][pslice] += fpw[phon][c][winstart] * t; //crash here when FPP=1 (java.lang.ArrayIndexOutOfBoundsException: 14)
                            //winstart+=3; //changing this hard-coded line...
                            winstart+=FPP; //to this.  (seems to work 04/19/2007)
                        }
                    }
                }
            }
        }        
    }    
    /** calculate inhibitions in phoneme layer **/
    public void phonToPhon() {
        int pmax, pmin, halfdur;
        halfdur=1; 
        double[] ppi=new double[pSlices];
        //the ppi accumulates all of the inhibition at a particular phoneme slice.
        //this amount of inhibition is later applied equally to all phonemes.        
        for(int slice=0;slice<pSlices;slice++)
            for(int phon=0;phon<phonemeRepresentation.NPHONS;phon++)
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
                        ppi[i]+=phonLayer[phon][slice]*parameters.getGamma().P;                    
                }
        //now, determine again the extent of each phoneme unit,
        //then apply inhibition equally to phons lying on the same phon slice.
        globalPhonemeCompetitionIndex=0;
        for(int phon = 0; phon < phonemeRepresentation.NPHONS; phon++){ //loop over phonemes             
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
                if((phonLayer[phon][slice]*parameters.getGamma().P)>0&&ppi[slice]>0){
                    phonNet[phon][slice]+=((pmax-pmin)*phonLayer[phon][slice])*parameters.getGamma().P;                    
                    globalPhonemeCompetitionIndex-=((pmax-pmin)*phonLayer[phon][slice])*parameters.getGamma().P;                    
                }
                //here, we make up for allophone-inhibition, reimbursing nodes for inhibition
                //that originate from allophones of the target, as defined in the allophon matrix.
                //note that this is an experimental feature of jtrace, implemented by tjs, 07/19/2007.
                for(int allophone = 0; allophone < phonemeRepresentation.NPHONS; allophone++){ //loop over phonemes             
                    if(parameters.getPhonology().getAllophoneRelation(phon, allophone)){
                        phonNet[phon][slice]+=((pmax-pmin)*phonLayer[allophone][slice])*parameters.getGamma().P;                    
                    }
                }
            }            
        }
    }
    
    // feedback from phoneme to features - <PHONEXc,f,fs>
    // This is always off in the original trace.
    //TODO: this is not yet implemented correctly because it is never used.
    public void phonToFeat() {
        double activation;          
        int d, fpp = parameters.getSlicesPerPhon();
        
            for(int fslice = 0; fslice < parameters.getFSlices(); fslice++)
                for(int cont = 0; cont < phonemeRepresentation.NCONTS; cont++ )  //loop over all continua (7)
                    for(int feat = 0 ; feat < phonemeRepresentation.NFEATS; feat++)
                    {                
                        activation = 0;  //activation is basically <PFEXp,ps,c,f,fs>
                        for(int phon = 0; phon < phonemeRepresentation.NPHONS; phon++) //loop over phonemes 
                            for(int pslice = 0; pslice < parameters.getPSlices(); pslice++)  //loop over phoneme slices (original configuration 33)
                            {
	                        d = (int)Math.abs(pslice * fpp - fslice);
        	                if(d >= fSlices) d = fSlices - 1;
                                    if(phonLayer[phon][pslice] > 0) //aLPHA connections=only excitatory
                                        activation += pfw[phon][cont][d] * phonLayer[phon][pslice] * phonemeRepresentation.PhonDefs[phon][cont * phonemeRepresentation.NFEATS + feat];
                            }
                        featNet[cont * phonemeRepresentation.NFEATS + feat][fslice] += parameters.getAlpha().PF * activation;                    
                    }
    }     
    //This implementation actually depends on pdur being 2, re: pww dynamics.
    public void phonToWord() {
        TraceLexicon dict = parameters.getLexicon();
        String str;     
        double t;
        int wpeak, wmin, winstart, wmax, pdur, strlen;
        //for each phoneme
        for(int phon=0;phon<phonemeRepresentation.NPHONS;phon++){
            pdur=2;
            
            //hack
            if(parameters.getDeltaInput()!=6||parameters.getSlicesPerPhon()!=3)
                pdur=(int)Math.floor(parameters.getDeltaInput()/parameters.getSlicesPerPhon());
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
                         if(str.charAt(offset)==phonemeRepresentation.toChar(phon)){
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
                             t = 2 * phonLayer[phon][pslice] * parameters.getAlpha().PW; //cTRACE: the 2 stands for word->scale
                             
                             double wfrq=0;
                             if(parameters.getFreqNode().RDL_wt_s != 0&&dict.get(word).getFrequency()>0){
                               wfrq =  parameters.getFreqNode().RDL_wt_s  * ((double)Math.log((double)dict.get(word).getFrequency()) * 0.434294482);
                             }
                             double wprm=0;
                             if(parameters.getPrimeNode().RDL_wt_s != 0&&dict.get(word).getPrime()>0){
                               wprm =  parameters.getPrimeNode().RDL_wt_s  * ((double)Math.log((double)dict.get(word).getPrime()) * 0.434294482);
                               //t = tp.getPrimeNode().applyWeightPrimeScaling(tp.getLexicon().get(word), t);                                
                             }
                             
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
    //word to word inhibition: operates the same as phoneme inhibition -- calculate
    //the total amount of inhibition at each slice and apply that equally to all words
    //that overlap with that slice somewhere.  this means that word length increases
    //the amount of lexical inhibition linearly.  
    public void wordToWord() {
        double[] wwi=new double[pSlices];
        double[] wisum=new double[pSlices];
        TraceLexicon dict=parameters.getLexicon();   
        //for all word slices
        for(int wstart=0;wstart<pSlices;wstart++){
            //for all words
            for(int word=0;word<nwords;word++){
                //determine how many slices the current word lies on
                int wmin=wstart; //wstart - (1/2 phone width))
                if(wmin<0) wmin=0;
                int wmax=wstart+(dict.get(word).getPhon().length()*2); //!! wstart + (wlength*phone width) + (1/2 phone width)
                if(wmax>pSlices) wmax=pSlices-1;
                for(int l=wmin;l<wmax;l++){
                    //then add that word unit's activation to the wisum array,                    
                    if(wordLayer[word][wstart]>0){
                        wisum[l]+=wordLayer[word][wstart]*wordLayer[word][wstart];                                     
                    }
                }
            }       
        }
        //next, scale the wisum array by gamma, and it is now called the wwi array.
        //there is also a built-in ceiling here, preventing inhibition over 3.0d.
        for(int wstart=0;wstart<pSlices;wstart++){
            if(wisum[wstart] > 3.0d) wisum[wstart]=3.0d; 
            wwi[wstart]= wisum[wstart]*parameters.getGamma().W;            
        }
        //now, repeat the looping over words and slices and apply the inhibition
        //accumulated at each slice to every word unit that overlaps with that slice.
        globalLexicalCompetitionIndex=0;
        for(int wstart=0;wstart<pSlices;wstart++){
            for(int word=0;word<nwords;word++){
                int wmin=wstart; //wstart - (1/2 phone width))
                if(wmin<0) wmin=0;
                int wmax=wstart+(dict.get(word).getPhon().length()*2); //!! wstart + (wlength*phone width) + (1/2 phone width)
                if(wmax>pSlices) wmax=pSlices-1;                                
                
                //length_normalization_scale = 1/(14  -dict.get(word).getPhon().length());
                //inhibition applied in this loop.
                for(int l=wmin;l<wmax;l++){
                    //EXTENSION
                    if(length_normalization){
                        double compensation_factor = 1 / (dict.get(word).getPhon().length() * length_normalization_scale);
                        if(compensation_factor>1) compensation_factor=1;
                                //double compensation_factor = (((dict.get(word).getPhon().length() / length_normalization_fulcrum ) - 1) * length_normalization_scale) + 1;
                                //if(compensation_factor<0) compensation_factor=1;
                        wordNet[word][wstart]-=wwi[l] * compensation_factor; //if(wwi[l]>0) //inhibition applied here                                                            
                        globalLexicalCompetitionIndex += wwi[l] * compensation_factor;
                    }
                    //END EXTENSION                    
                    else{
                        wordNet[word][wstart]-=wwi[l]; //if(wwi[l]>0) //inhibition applied here                                                            
                        globalLexicalCompetitionIndex += wwi[l];
                    }
                }
                //re-imbursement of self-inhibition occurs here.
                if(wordLayer[word][wstart]>0){  //self-inhibitiopn prevented here.
                    //EXTENSION                    
                    if(length_normalization){
                        double compensation_factor = 1 / (dict.get(word).getPhon().length() * length_normalization_scale);
                        if(compensation_factor>1) compensation_factor=1;
                        wordNet[word][wstart]+=((wmax-wmin)*(wordLayer[word][wstart]*wordLayer[word][wstart]*parameters.getGamma().W)) * compensation_factor;                                                                          
                        globalLexicalCompetitionIndex -= ((wmax-wmin)*(wordLayer[word][wstart]*wordLayer[word][wstart]*parameters.getGamma().W)) * compensation_factor;                                                                          
                    }
                    //END EXTENSION                    
                    else{
                        wordNet[word][wstart]+=((wmax-wmin)*(wordLayer[word][wstart]*wordLayer[word][wstart]*parameters.getGamma().W));                                                                          
                        globalLexicalCompetitionIndex -= ((wmax-wmin)*(wordLayer[word][wstart]*wordLayer[word][wstart]*parameters.getGamma().W));                                                                          
                    }
                }
            }
        }          
    }
    //lexical to phoneme feedback.
    public void wordToPhon() {
        edu.uconn.psy.jtrace.Model.TraceLexicon dict = parameters.getLexicon();
        String str;
        int wslot, pmin, pwin, pmax, currChar; 
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
                    currChar = phonemeRepresentation.mapPhon(t_c_p);
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
                        if(currChar > phonemeRepresentation.NPHONS && currChar < 0){ 
                            int contIdx;
                            try{
                                contIdx=(new Integer(parameters.getContinuumSpec().toCharArray()[2])).intValue();
                            } catch(Exception e){ e.printStackTrace(); contIdx=-1; }
                            if(contIdx==-1){ 
                                //there is something wrong with the input or the continuum.
                                //feedback will not be calculated for this character.
                                break;
                            }       
                            else{
                               if(currChar==50){ //this is the bottom of the continuum.
                                   currChar = phonemeRepresentation.mapPhon(parameters.getContinuumSpec().toCharArray()[0]);                                   
                               }
                               else if(currChar==(50+contIdx-1)){ //this is the top of the continuum
                                   currChar = phonemeRepresentation.mapPhon(parameters.getContinuumSpec().toCharArray()[2]);                                   
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
                            if(dict.get(word).getFrequency()>0&&parameters.getFreqNode().RDL_wt_s>0)
                                wfrq = parameters.getFreqNode().RDL_wt_s  * (Math.log(0 + dict.get(word).getFrequency()) * 0.434294482);                                            
                            double wprim=0;
                            if(dict.get(word).getFrequency()>0&&parameters.getPrimeNode().RDL_wt_s>0)
                                wprim = parameters.getPrimeNode().RDL_wt_s  * (Math.log(0 + dict.get(word).getPrime()) * 0.434294482);                
                            
                            //scale the activation by alpha and wpw
                            phonNet[currChar][pslice] += (1 + wfrq + wprim) * wordLayer[word][wslice] * parameters.getAlpha().WP * wpw[currChar][pwin]; 
                                                        
                        }
                    }
                }                
            }
        }
    }
    
    //final processing of feature units incorporates stochasticity (if on) and
    //implements decay to resting level behavior.
    public void featUpdate() {
        double t, tt;        
        double min=parameters.getMin();
        double max= parameters.getMax();
        for(int slice = 0; slice < fSlices; slice++){
            for(int feat = 0; feat < phonemeRepresentation.NFEATS*phonemeRepresentation.NCONTS; feat++ )
            {   
                if(parameters.getStochasticitySD()!=0d){ //apply gaussian noise here
                    featNet[feat][slice] += stochasticGauss.nextGauss(); //this adds the noise                    
                }
        
                t = featLayer[feat][slice];
                if(featNet[feat][slice] > 0)
                    t+= (max-t)*featNet[feat][slice];                
                else if(featNet[feat][slice] < 0)
                    t += (t-min)*featNet[feat][slice];                    
                tt = featLayer[feat][slice] - parameters.getRest().F;
                //if(t!=0)
                t -= parameters.getDecay().F * tt;
                if(t > max) t = max;
                if(t < min) t = min;
                //final update for feature layer
                featLayer[feat][slice] = t;                
            }
        }
        featNet = new double[phonemeRepresentation.NFEATS*phonemeRepresentation.NCONTS][fSlices];                        
    }    
    
    //final processing of phoneme units incorporates stochasticity (if on) and
    //implements decay to resting level behavior.    
    public void phonUpdate() {
        double diff, rest;        
        for(int pslice = 0; pslice < pSlices; pslice++)
            for(int phon = 0; phon < phonemeRepresentation.NPHONS; phon++ )
            {
                if(parameters.getStochasticitySD()!=0d){ //apply gaussian noise here
                    phonNet[phon][pslice] += stochasticGauss.nextGauss(); //this adds the noise                    
                }
                
                if(phonNet[phon][pslice] >= 0)
                    diff = parameters.getMax() - phonLayer[phon][pslice];
                else
                    diff =  phonLayer[phon][pslice] - parameters.getMin();
                
                rest = phonLayer[phon][pslice] - parameters.getRest().P;

                //final update for phoneme layer 
                phonLayer[phon][pslice] += (diff * phonNet[phon][pslice]) - (parameters.getDecay().P * rest);
                phonLayer[phon][pslice] = parameters.clipWeight(phonLayer[phon][pslice]);                            
            }
        phonNet = new double[phonemeRepresentation.NPHONS][pSlices];        
    }
    
    
     //final processing of word units incorporates stochasticity (if on) and
    //implements decay to resting level behavior.    
    public void wordUpdate(){            
        double t, tt, max, min;
        min=parameters.getMin();
        max= parameters.getMax();
        for(int word = 0; word < nwords; word++ )
            for(int slice = 0; slice < wSlices; slice++){
                
                //apply attention modulation (cf. Mirman et al., 2005)
                if(parameters.getAtten()!=1d)
                    wordNet[word][slice] *= parameters.getAtten();
                if(parameters.getBias()!=0d)
                    wordNet[word][slice] -= parameters.getBias();
                    
                if(parameters.getStochasticitySD()!=0d){ //apply gaussian noise here
                    wordNet[word][slice] += stochasticGauss.nextGauss(); //this adds the noise                    
                }
                                
                t = wordLayer[word][slice];
                if( wordNet[word][slice] > 0)
                    t += (max - t) * wordNet[word][slice];
                else if( wordNet[word][slice] < 0)
                    t += (t - min) * wordNet[word][slice];
                //resting prime & resting freq effects
                if(parameters.getFreqNode().RDL_rest&&parameters.getLexicon().get(word).getFrequency()>0&&parameters.getPrimeNode().RDL_rest&&parameters.getLexicon().get(word).getPrime()>0)
                    tt = wordLayer[word][slice] - ((parameters.getRest().W + parameters.getFreqNode().applyRestFreqScaling(parameters.getLexicon().get(word))) + (parameters.getRest().W + parameters.getPrimeNode().applyRestPrimeScaling(parameters.getLexicon().get(word))));                
                //resting freq effects
                else if(parameters.getFreqNode().RDL_rest&&parameters.getLexicon().get(word).getFrequency()>0)
                    tt = wordLayer[word][slice] - (parameters.getRest().W + parameters.getFreqNode().applyRestFreqScaling(parameters.getLexicon().get(word)));                
                //resting prime 
                else if(parameters.getPrimeNode().RDL_rest&&parameters.getLexicon().get(word).getPrime()>0)
                    tt = wordLayer[word][slice] - (parameters.getRest().W + parameters.getPrimeNode().applyRestPrimeScaling(parameters.getLexicon().get(word)));                
                //no resting prime or resting freq effects
                else
                    tt = wordLayer[word][slice] - parameters.getRest().W;
                //if(tt != 0)
                t -= parameters.getDecay().W * tt;                                
                
                if(t > max) t = max;
                if(t < min) t = min;                
                wordLayer[word][slice] = t;
            }
        wordNet = new double[nwords][wSlices];        
    }    
    
    //Error Handling code
    public void report(String report) {
        terr.report(report);
    }
    public TraceParam getParameters(){return parameters;}
    public int getInputSlice(){return inputSlice;}    
    public void setInputSlice(int i){inputSlice=0;}
    public String toString(){return "this is an object of type TraceNet.";}
    public boolean isReady(){return true;}
    public String isReadyVerbose(){return " ready ... ";}    
    public double[][] getInputLayer() {return inputLayer;}
    public void setInputLayer(double[][] _l){inputLayer=_l;}
    public double[][] getFeatureLayer() {return featLayer;}
    public void setFeatureLayer(double[][] _l){featLayer=_l;}
    public double[][] getPhonemeLayer() {return phonLayer;}
    public void setPhonemeLayer(double[][] _l){phonLayer=_l;}
    public double[][] getWordLayer() {return wordLayer;}
    public void setWordLayer(double[][] _l){wordLayer=_l;}
    public TracePhones getPhonDefs() {return phonemeRepresentation;}
    public double[][] getFeatLayer() {return featLayer;}
    public double[][] getPhonLayer() {return phonLayer;}
    public TraceParam getParam() {return parameters;}
    public double getGlobalLexicalCompetition(){return globalLexicalCompetitionIndex;}
    public double getGlobalPhonemeCompetition(){return globalPhonemeCompetitionIndex;}
    public double[][] clearArray(double[][] d){
        for(int i=0;i<d.length;i++)
            for(int j=0;j<d[0].length;j++)
                d[i][j]=0d;
        return d;
    }
    
}