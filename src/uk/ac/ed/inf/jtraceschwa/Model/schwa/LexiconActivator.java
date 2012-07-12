package uk.ac.ed.inf.jtraceschwa.Model.schwa;

import uk.ac.ed.inf.jtraceschwa.Model.SchwaNet;

/**
 * 
 * Send extra activation to the words which contain schwa
 * @author Arnaud Henry
 *
 */
public class LexiconActivator implements SchwaListener {
	
	private SchwaNet net;
	
	

	public LexiconActivator(SchwaNet net_) {
		super();
		this.net = net_;
	}



	/**
	 * Send activation to the words which contain schwa
	 * Inspired from TraceNet.phonToWord(). Differences :
	 *  - no word frequency / word prime effects
	 */
	@Override
	public void schwaUpdated(Schwa schwa) {
		
		int startSlice = 0;
		int finishSlice = net.pSlices;
		
		for(int pslice = startSlice; pslice <finishSlice; pslice++ ){
			edu.uconn.psy.jtrace.Model.TraceLexicon dict = net.tp.getLexicon();
	        String str;     
	        double t;
	        int wpeak, wmin, winstart, wmax, pdur, strlen;
	        pdur=2; // see "hack" in phonToWord()
	
	        //iterate over each word in the dictionary
	        words: for(int word=0;word<dict.size();word++){                    
	            str = dict.get(word).getPhon();
	            strlen = str.length();
	            //for each letter in the current word
	            for(int offset=0;offset<strlen;offset++){
	                //if that letter corresponds to the phoneme we're now considering...
	                 if(str.charAt(offset)=='^'){
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
	                         if(wmax > net.pSlices - 1)
	                             wmax = net.pSlices - 1;
	                         winstart = 1; 
	                     }
	                     
	                     if( pslice == 33 ){
	                    	 System.out.println("hollaaa");
	                     }
	                     
	                     //determine the raw amount of activation that is sent to the word units
	                     t = 2 * net.phonLayer[net.schwaIndex][pslice] * net.tp.getAlpha().PW; //cTRACE: the 2 stands for word->scale
                         
	                     
	                     double _t;
	                     //now iterate over the temporal range determined about 15 lines above
	                     for(int wslice = wmin; wslice<wmax && wslice<net.wSlices; wslice++, winstart++)
	                         if(winstart>=0 && winstart<4){
	                             //scale activation by pww; this determines how temporal offset should affect excitation 
	                             net.wordNet[word][wslice] += net.pww[net.schwaIndex][winstart] * t;                                                                          
	                         }
	                 }
	            }
	        }
		}
	}



	@Override
	public void reset(Schwa schwa) {
		//nothing to do
	}

}
