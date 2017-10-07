package dynamic_ranking;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import containers.PositionalPostingList;
import containers.PostingList;
import containers.Query;
import containers.Score;
import tools.ScoreComparator;

public class DynamicStandardBM25 {
	private HashMap<String, PostingList> index;
	private HashMap<String, Query> queries;
	private Set<String> documents;
	
	private int N;	// number of documents in index
	private HashMap<String, Integer> docLengths;
	private double avgDocLength;
	
	public DynamicStandardBM25(HashMap<String, PostingList> _index, Set<String> docs) {
		index = _index;
		documents = docs;
		N = documents.size();
		docLengths = new HashMap<String, Integer>();
		
		compute_docLengths_and_avgDocLength();
		compute_idfs();
	}
	
	public void compute_idfs() {
		for (String term : index.keySet()) {
			PostingList postingList = index.get(term);
			postingList.compute_probabilistic_model_idf(N);;
			index.replace(term, postingList);
		}
	}
	
	public void compute_docLengths_and_avgDocLength() {
		for (String term: index.keySet()) {
			PostingList postingList = index.get(term);
			
			for (String docNo : postingList.map().keySet()) {
				if (!docLengths.containsKey(docNo)) {
					docLengths.put(docNo, postingList.map().get(docNo).tf());
				} else {
					docLengths.replace(docNo, docLengths.get(docNo) + postingList.map().get(docNo).tf());
				}
			}
		}
		int docLengthSum = 0;
		
		for (String docNo : docLengths.keySet()) {
			docLengthSum += docLengths.get(docNo);
		}
		avgDocLength = docLengthSum / docLengths.size();
	}
	
	public String generateBM25Output(Query query, double k_1, int k_2, double b) {
		String output = "";
		
		Score[] scores = new Score[N];
		int i = 0;
		
		for (String docNo : documents) {
			double score = 0;
			
			for (String token : query.tokens().keySet()) {
//					System.out.println(token);
				
				if (!index.containsKey(token)) {
					continue;
				}
				
				PostingList postingList = index.get(token);	// grab posting list for current query token
				
				if (postingList.map().containsKey(docNo)) {	// posting list for token has a mapping for the document in question
					double w = postingList.idf();
					
					double term2_numerator = (k_1 + 1) * postingList.map().get(docNo).tf();
					double term2_denominator = (postingList.map().get(docNo).tf() + 
							(k_1 * (1 - b + (b * (docLengths.get(docNo) / avgDocLength)))));
					
					double term3 = ((k_2 + 1) * query.tokens().get(token).tf()) / (k_2 + query.tokens().get(token).tf());
					
					score += w * (term2_numerator / term2_denominator) * term3;
				}
			}	
			
			if (String.valueOf(score)!= "NaN" && score != 0) {
				scores[i] = new Score(score, query.number(), docNo);
				i++;
			}
		}
	    List<Score> list = new ArrayList<Score>(Arrays.asList(scores));
	    list.removeAll(Collections.singleton(null));
	    scores = list.toArray(new Score[list.size()]);
		
		Arrays.sort(scores, new ScoreComparator());
		
		int indexUpperLimit;
		
		if (scores.length > 100) {
			indexUpperLimit = 100;
		} else {
			indexUpperLimit = scores.length;
		}
		
		for (int j = 0; j < indexUpperLimit; j++) {
			output += scores[j].queryNumber() + " 0 " + scores[j].documentNumber() + " " + (j+1) + " " + scores[j].score() + " BM25\n";
		}
		
		return output;
	}
}