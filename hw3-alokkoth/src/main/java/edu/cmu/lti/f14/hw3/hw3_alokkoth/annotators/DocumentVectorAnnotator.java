package edu.cmu.lti.f14.hw3.hw3_alokkoth.annotators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_alokkoth.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_alokkoth.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_alokkoth.utils.StanfordLemmatizer;
import edu.cmu.lti.f14.hw3.hw3_alokkoth.utils.Utils;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

  HashSet<String> StopWords;
  public void initialize(UimaContext aContext) {
    // TODO Auto-generated method stub
    StopWords = new HashSet<String>();
    File stopfile = new File("src/main/resources/stopwords.txt");
    FileReader fileReader = null;
    try {
      fileReader = new FileReader(stopfile);
    } catch (FileNotFoundException e) {
     
      e.printStackTrace();
    }
    BufferedReader bufferedReader = new BufferedReader(fileReader);
    StringBuffer stringBuffer = new StringBuffer();
    String line;
    try {
      while ((line = bufferedReader.readLine()) != null) {
        StopWords.add(line);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      fileReader.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
	
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
	 * One more basic tokenizer which splits the punctuation too.  (It increases performance).
	 * @param doc input text
	 * @return a list of tokens
	 */
	 List<String> MyTokenizer1(String doc) {
	    List<String> res = new ArrayList<String>();
	    
	    for (String s: doc.split("[\\p{Punct}\\s]+"))
	      res.add(s);
	    return res;
	  }
	  /**
	   * One more basic tokenizer which splits the punctuation too AND lowercases the strings  (It increases performance).
	   * @param doc input text
	   * @return a list of tokens
	   */
	   List<String> MyTokenizer2(String doc) {
	      List<String> res = new ArrayList<String>();
	      
	      for (String s: doc.split("[\\p{Punct}\\s]+"))
	        res.add(s.toLowerCase());
	      return res;
	    }
	   List<String> MyTokenizer3(String doc) {
       List<String> res = new ArrayList<String>();
       
       for (String s: doc.split("[\\p{Punct}\\s]+"))
         
         if(!StopWords.contains(s) && s.length() > 1)
         {
         res.add(s.toLowerCase());}
       return res;
     }
	 
	  /**
	   * tokenizer which uses the stanford lemmatizer, 
	   *  Also lowercasing and removing punctuation.
	   * @param doc input text
	   * @return a list of tokens
	   */
	  List<String> MyStanfordStemmerTokenizer(String doc) {
	    List<String> res = new ArrayList<String>();
	    
	    StanfordLemmatizer stanfordlemmatizer = new StanfordLemmatizer();
	    for (String s: doc.split("[\\p{Punct}\\s]+")) {
	      s = s.toLowerCase();
	      s = stanfordlemmatizer.stemWord(s);
	    
	        res.add(s);
	    }
	    
	    return res;
	  }
	  /**
     * tokenizer which uses the stanford lemmatizer, 
     *  Also lowercasing and removing punctuation. In this case it also removes stopwords.
     * @param doc input text
     * @return a list of tokens
     */
    List<String> MyStanfordStemmerTokenizer2(String doc) {
      List<String> res = new ArrayList<String>();
      
      StanfordLemmatizer stanfordlemmatizer = new StanfordLemmatizer();
      for (String s: doc.split("[\\p{Punct}\\s]+")) {
        s = s.toLowerCase();
        s = stanfordlemmatizer.stemWord(s);
        if(!StopWords.contains(s))
          {res.add(s);}
      }
      
      return res;
    }

	/**
	 * Creates the term frequency vector. 
	 * Uses the tokens supplied by the tokenizer to construct the termfrequency Map. 
	 * i.e. frequency of the terms as value, and the term as a map.
	 * Uploads the Map to JCas.
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();
		List<String> tokenized = tokenize0(docText); //Given Tokenizer
		//List<String> tokenized = MyTokenizer(docText); //Given Tokenizer
		//List<String> tokenized = MyStanfordStemmerTokenizer(docText);
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
		    t.setFrequency((int) tokens.get(k));
		    t.setText(k);

	      tokenlist.add(t);
	        
		  }
		  doc.setTokenList(Utils.fromCollectionToFSList(jcas, tokenlist));
      
      
		}
	
		

	}

}
