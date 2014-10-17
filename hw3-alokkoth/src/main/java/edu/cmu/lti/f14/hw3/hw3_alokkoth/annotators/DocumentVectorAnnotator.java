package edu.cmu.lti.f14.hw3.hw3_alokkoth.annotators;

import java.util.*;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_alokkoth.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_alokkoth.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_alokkoth.utils.Utils;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
		}

	}

	/**
   * A basic white-space tokenizer, it deliberately does not split on punctuation!
   *
	 * @param doc input text
	 * @return    a list of tokens.
	 */

	List<String> tokenize0(String doc) {
	  List<String> res = new ArrayList<String>();
	  
	  for (String s: doc.split("\\s+"))
	    res.add(s);
	  return res;
	}

	/**
	 * 
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();
		List<String> tokenized = tokenize0(docText);
		Map<String, Integer> tokens = new HashMap<String, Integer>();
		for(String s: tokenized)
		{
		  
		  if(!tokens.containsKey(s)){
		    tokens.put(s, (int) 1.0);
		  }
		  else{
		    Integer freq = tokens.get(s);
		    Integer new_freq =  freq + 1;
		    tokens.put(s, new_freq);
		            
		  }
		 Collection<Token> tokenlist = new ArrayList<Token>();  
		  for(String k:tokens.keySet())
		  {
		    
		    Token t = new Token(jcas);
		    t.setFrequency((int) tokens.get(s));
		    t.setText(s);

	      tokenlist.add(t);
	        
		  }
		  doc.setTokenList(Utils.fromCollectionToFSList(jcas, tokenlist));
      
      
		}
		//TO DO: construct a vector of tokens and update the tokenList in CAS
    //TO DO: use tokenize0 from above 
		

	}

}
