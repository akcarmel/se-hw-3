package edu.cmu.lti.f14.hw3.hw3_alokkoth.casconsumers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_alokkoth.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_alokkoth.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_alokkoth.utils.Utils;


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** declaring query and text relevant values **/
	public ArrayList<Integer> relList;
  public ArrayList<Map<String, Integer>> fsList;
	public ArrayList<String> docList;
	public ArrayList<Double> CosineScores;
	public ArrayList<Integer> Ranks;
	public ArrayList<Double> MaxCosine;
	public ArrayList<String> BestDoc;
  
	
  

		
	public void initialize() throws ResourceInitializationException {
  /** Initializing the relevant values  **/
		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();
		
		docList = new ArrayList<String>();
		fsList = new ArrayList<Map<String, Integer>>();
		CosineScores = new ArrayList<Double>();
		Ranks = new ArrayList<Integer>();
		MaxCosine = new ArrayList<Double>();
		BestDoc = new ArrayList<String>();
	}

	/**
	 * Populates the relevant lists by reading from the CAS.
	 */
	@Override
	
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
	
		if (it.hasNext()) {
			Document doc = (Document) it.next();
       
			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			//ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);

			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			
			docList.add(doc.getText());
			ArrayList<Token> queryToken = Utils.fromFSListToCollection(doc.getTokenList(), Token.class);
      Map<String, Integer> temp = CollectionToMap(queryToken);
      fsList.add(temp);
    //  Map<String, Double> queryMap = L1_normalize(temp);
     // fsList.add(temp);
			//Do something useful here

		}

	}

	/**
	 * Going from MAP to collection, inorder that it will be easier to use the Util functions involving FSLists.
	 * @param queryTokens
	 * @return termMap
	 */
	private Map<String, Integer> CollectionToMap(Collection<Token> queryTokens)
	{
	  Map<String, Integer> termMap = new HashMap<String, Integer>();
	  for(Token t: queryTokens)
	  {
	    termMap.put(t.getText(), t.getFrequency());
	  }
	  return termMap;
	}
	
	/*
	 * returns Computes all the similarity scores and ranks them, then computes MRR.
	 * @see org.apache.uima.collection.CasConsumer_ImplBase#collectionProcessComplete(org.apache.uima.util.ProcessTrace)
	 */
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);
    
  

    int i = 0;
    while(i < qIdList.size()) {
    
    Map<String, Integer> queryMap = fsList.get(i);
      CosineScores.add(1.0);
    int j=i+1;
    System.out.println("###############");
    System.out.println(docList.get(i));
      while(j < qIdList.size() && qIdList.get(i) == qIdList.get(j)) 
      {
        Map<String, Integer> docMap = fsList.get(j);
       // System.out.println(j);
        //System.out.println(docMap);
       
        /*
         * System.out.println("Question ID");
         * System.out.println(qIdList.get(i));
        
         */
         System.out.println("Question ID");
         System.out.println(qIdList.get(i));
         System.out.println("Document ID");
         System.out.println(j-i);
         System.out.println(docList.get(j));
         System.out.println(queryMap);
         System.out.println(docMap);
        /** SIMILARITY SCORES **/
        System.out.println(computeCosineSimilarity(queryMap, docMap));
       
         CosineScores.add(computeCosineSimilarity(queryMap, docMap)); // S0
        
        //CosineScores.add(computeJaccardSimilarity(queryMap, docMap)); // S1
        //CosineScores.add(computeDiceSimilarity(queryMap, docMap)); // S2
        j++;
      }
      i = j;
    }
	  
	   int t = 0;
	    while( t < qIdList.size()) {
	      ArrayList<Integer> temp = new ArrayList<Integer>();
	      //int p = t + 1;
	    int p = t+1;
        
	      while(p < qIdList.size() && qIdList.get(t) == qIdList.get(p)) {
	        temp.add(p);
	        p++;
	      }
	      /** Comparator between scores **/
	      Collections.sort(temp,  new Comparator<Integer>() {
	        public int compare(Integer o1, Integer o2) {
	          return CosineScores.get(o1).compareTo(CosineScores.get(o2));
	        }
	      });
	      Collections.reverse(temp);
	      for(int n = 0; n < temp.size(); n++) {
	        if(relList.get(temp.get(n)) == 1) {
	          
	          Ranks.add(n + 1);
	          MaxCosine.add(CosineScores.get(temp.get(n)));
	          BestDoc.add(docList.get(temp.get(n)));
	          //System.out.println(docList.get(temp.get(n)));
	          break;
	        }
	      }
	      t = p;
	    }

	     double metric_mrr = compute_mrr();
	      System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	      write_out_results(MaxCosine,Ranks,BestDoc, metric_mrr);
	      
	   }
	
	/*
	 * Inputs the maximum cosine score and the corresponding best document for that score.
	 *  Writes out the report.txt. Also the formatting is managed here (4-significant digits).
	 * @param  MaxCosine, BestDoc
	 * Inputs: MaxCosine, BestDoc
	 */
		
	private void write_out_results( ArrayList<Double> MaxCosine,ArrayList<Integer> Ranks, ArrayList<String> BestDoc, Double metric_mrr )
	{
	  int i = 0;
	  int j=0;
	  PrintWriter pw = null;
    try {
      pw = new PrintWriter(new BufferedWriter(new FileWriter((String) getConfigParameterValue("outputfilename"))));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
	  while(i < Ranks.size())
	  {
	  /**System.out.println(i);
	  System.out.println(Ranks.get(i));
	  System.out.println(MaxCosine.get(i));
	  System.out.println(BestDoc.get(i));**/
    j=i+1;
	  pw.write("cosine=" + String.format("%.4f",MaxCosine.get(i))+ "\t"+"rank=" + Ranks.get(i)+ "\t"+"qid="+(j)+ "\t"+"rel=1"+"\t"+BestDoc.get(i) + '\n' ); 
	  i++;
	  }
	  pw.write("MRR="+String.format("%.4f",metric_mrr));
	  pw.close();
	  }
		
	
	
	

	/**
	 * Computes the cosine similarity between a queryvector and a documentvector.
	 * The function does L1,L2 normalization inside itself.
	 * @param queryVector
	 * @param docVector
	 * input: queryVector, docVector
	 * output: cosinescore
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity=0.0;
		double qf = 0.0;
		double mag_query = 0.0;
		double mag_doc = 0.0;
		
		Map<String, Double> queryVector_L1 = L1_normalize(queryVector);
		Map<String, Double> docVector_L1 = L1_normalize(docVector);
		Map<String, Double> queryVector_L2 = L2_normalize(queryVector_L1);
    Map<String, Double> docVector_L2 = L2_normalize(docVector_L1);
    
    for(String s: queryVector_L2.keySet())
    {
      qf = queryVector_L2.get(s);
      if(docVector.containsKey(s))
      {
        Double df =  docVector_L2.get(s);
        cosine_similarity  = cosine_similarity + df*qf;
      }
    }
		
   
		return cosine_similarity;
	}
	/**
	 * Computes the Jaccard Similarity between queryVector and docVector. 
	 * Note the similarity is computed on the two Sets -- keys of queryVector and keys of docVector
	 * This set similarity is irrespective of frequencies of each key.
	 * @param queryVector
	 * @param docVector
	 * input: queryVector, docVector
   * output: jaccardscore
	 * @return jaccard_similarity
	 */
	 private double computeJaccardSimilarity(Map<String, Integer> queryVector,
	         Map<String, Integer> docVector) {
	       double jaccard_similarity=0.0;
	   
	      
	       Set union = new HashSet<String>();
	       Set inter = new HashSet<String>(docVector.keySet());
	       
	      // Map<String, Double> queryVector_L1 = L1_normalize(queryVector);
	       //Map<String, Double> docVector_L1 = L1_normalize(docVector);
	      // Map<String, Double> queryVector_L2 = L2_normalize(queryVector_L1);
	      // Map<String, Double> docVector_L2 = L2_normalize(docVector_L1);
	       union.addAll(docVector.keySet());
	       union.addAll(queryVector.keySet());
         inter.retainAll(queryVector.keySet());
	       int SizeOfUnion = union.size();
	       int SizeOfInter = inter.size();
	       System.out.println("Union");
  
	       System.out.println(union);
	       System.out.println(SizeOfUnion);
         System.out.println("Intersection");
         System.out.println(SizeOfInter);
         System.out.println(inter);
         double denom = (double) SizeOfUnion;
         double numer = (double) SizeOfInter;
	      jaccard_similarity =   (numer)/(denom);
	      System.out.println("Similarity");
	      System.out.println(jaccard_similarity);
	      System.out.println(numer);
	      System.out.println(denom);
	       return jaccard_similarity;
	     }
	  /**
	   * Computes the Dice Similarity between queryVector and docvector. 
	   *  Note the similarity is computed on the two Sets -- keys of queryVector and keys of docVector
     * This set similarity is irrespective of frequencies of each key.
	   * Treats the vector as a set of
	   * @param queryVector
	   * @param docVector
	   * input: queryVector, docVector
	   * output: jaccardscore
	   * @return jaccard_similarity
	   */
	 private double computeDiceSimilarity(Map<String, Integer> queryVector,
           Map<String, Integer> docVector) {
         double dice_similarity=0.0;
     
        
         Set union = new HashSet<String>();
         Set inter = new HashSet<String>(docVector.keySet());
         
        // Map<String, Double> queryVector_L1 = L1_normalize(queryVector);
         //Map<String, Double> docVector_L1 = L1_normalize(docVector);
        // Map<String, Double> queryVector_L2 = L2_normalize(queryVector_L1);
        // Map<String, Double> docVector_L2 = L2_normalize(docVector_L1);
       
         int SizeOfUnion = queryVector.size() + docVector.size();
         int SizeOfInter = inter.size();
         System.out.println("Union");
  
       
         System.out.println(SizeOfUnion);
         System.out.println("Intersection");
         System.out.println(SizeOfInter);
         System.out.println(inter);
         double denom = (double) SizeOfUnion;
         double numer = (double) SizeOfInter;
        dice_similarity =   (2*numer)/(denom);
        System.out.println("Similarity");
        System.out.println(dice_similarity);
        System.out.println(numer);
        System.out.println(denom);
         return dice_similarity;
       }

	 
	 private double Double(int i) {
    // TODO Auto-generated method stub
    return 0;
  }

	 /**
	  * Implements L2 Normalization on the given vector.
	  * @param TERMVector
	  * @return termVector
	  */
  private Map<String, Double> L2_normalize(Map<String, Double> TERMVector)
	  {
	   double total = 0.0;
	   double freq = 0;
	   Map<String, Double> termVector = new HashMap<String, Double>();
	   for(String s: TERMVector.keySet())
	   {
	     total += TERMVector.get(s)*TERMVector.get(s);
	   }
	   total = Math.sqrt(total);
	   for(String s: TERMVector.keySet())
	   { 
	     freq = TERMVector.get(s);
	     termVector.put(s, freq/total);
	   }
	   return termVector;
	  }
  /**
   * Implements L1 Normalization on the given vector.
   * @param TERMVector
   * @return termVector
   */
	private Map<String, Double> L1_normalize(Map<String, Integer> queryVector)
	{
	 double total = 0.0;
	 int freq = 0;
	 Map<String, Double> termVector = new HashMap<String, Double>();
	 for(String s: queryVector.keySet())
	 {
	   total += queryVector.get(s);
	 }
	 for(String s: queryVector.keySet())
	 { 
	   freq = queryVector.get(s);
	   termVector.put(s, freq/total);
	 }
	 return termVector;
	}

	/**
	 * Computes the Mean Reciprocal Rank of the given ranking.
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr=0.0;

    double  TotalRank = 0.0;
    System.out.println(Ranks.size() + " " + Ranks);
    for(int i = 0; i < Ranks.size(); i++) {
     TotalRank += 1.0/Ranks.get(i);
    }
    metric_mrr = (1.0/Ranks.size()) * TotalRank;
   
    
    
		
		return metric_mrr;
	}

}
