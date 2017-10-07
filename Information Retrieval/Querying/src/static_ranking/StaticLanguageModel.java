package static_ranking;

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

import containers.PostingList;
import containers.Query;
import containers.Score;
import tools.ScoreComparator;

public class StaticLanguageModel {
	private HashMap<String, PostingList> index;
	private HashMap<String, Query> queries;
	private Set<String> documents;
	
	private int N;	// number of documents in index
	private int C;
	private HashMap<String, Integer> docLengths;
	private double avgDocLength;
	private String resultsPath;
	
	public StaticLanguageModel(HashMap<String, PostingList> _index, HashMap<String, Query> _queries, Set<String> docs, String outputPath) {
		index = _index;
		queries = _queries;
		documents = docs;
		N = documents.size();
		docLengths = new HashMap<String, Integer>();
		resultsPath = outputPath;
	}
	
	public void Dirichlet() {
		compute_C_docLengths_and_avgDocLength();
		generateDirichletOutput();
	}
	
	public void compute_C_docLengths_and_avgDocLength() {
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
			C += docLengths.get(docNo);
			docLengthSum += docLengths.get(docNo);
		}
		avgDocLength = docLengthSum / docLengths.size();
	}
	
	public void generateDirichletOutput() {
		String output = "";
		
		for (String queryNum : queries.keySet()) {
			Query query = queries.get(queryNum);
			
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
					
					double tf_q_D = 0;
					if (postingList.map().containsKey(docNo)) {	// posting list for token has a mapping for the document in question
						tf_q_D = query.tokens().get(token).tf();
					}
					
					double tf_q_C = 0;
					for (String element : postingList.map().keySet()) {
						tf_q_C += postingList.map().get(element).tf();
					}
					
					double mu = 2000;
					
					double numerator = tf_q_D + (mu * (tf_q_C / C));
					double denominator = docLengths.get(docNo) + mu;
					
					score += Math.log10(numerator / denominator);
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
				output += scores[j].queryNumber() + " 0 " + scores[j].documentNumber() + " " + (j+1) + " " + scores[j].score() + " LM\n";
			}
		}
		
		// NOW WRITE THAT SHIT :D
		
		Path path = Paths.get(resultsPath + "staticLM.txt");
		try {
			BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE);
			bw.write(output);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
