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

public class StaticVectorSpaceModel {
	private HashMap<String, PostingList> index;
	private HashMap<String, Query> queries;
	private Set<String> documents;
	
	private int N;	// number of documents in index
	private HashMap<String, Double> d_squares;
	private HashMap<String, Double> w_squares;
	private HashMap<String, Double> weights;
	private String resultsPath;
	
	public StaticVectorSpaceModel(HashMap<String, PostingList> _index, HashMap<String, Query> _queries, Set<String> docs, String outputPath) {
		index = _index;
		queries = _queries;
		documents = docs;
		N = documents.size();
		d_squares = new HashMap<String, Double>();
		w_squares = new HashMap<String, Double>();
		weights = new HashMap<String, Double>();
		resultsPath = outputPath;
	}
	
	public void cosine() {
		compute_idfs();
		compute_d_squares();
		compute_w_squares();
		compute_weights();
		generateCosineMeasureOutput();
	}
	
	public void compute_idfs() {
		for (String term : index.keySet()) {
			PostingList postingList = index.get(term);
			postingList.compute_probabilistic_model_idf(N);
			index.replace(term, postingList);
		}
	}
	
	public void compute_d_squares() {
		for (String term: index.keySet()) {
			PostingList postingList = index.get(term);	// grab posting list for current term
			
			for (String docNo : documents) {
				if (postingList.map().containsKey(docNo)) {
					if (!d_squares.containsKey(docNo)) {
						d_squares.put(docNo, Math.pow(postingList.map().get(docNo).tf(), 2));
					} else {
						d_squares.replace(docNo, d_squares.get(docNo) + Math.pow(postingList.map().get(docNo).tf(), 2));
					}
				}
			}
		}
	}
	
	public void compute_w_squares() {
		for (String queryNum : queries.keySet()) {
			Query query = queries.get(queryNum);
			
			double w_square_sum = 0;
			for (String token : query.tokens().keySet()) {
				if (!index.containsKey(token)) {
					continue;
				}
				
				w_square_sum += Math.pow(Math.log10((query.tokens().get(token).tf()) + 1) * index.get(token).idf(), 2);
			}
			w_squares.put(queryNum, w_square_sum);
		}
	}
	
	public void compute_weights() {
		for (String queryNum : queries.keySet()) {
			Query query = queries.get(queryNum);
			
			double w_square_sum = 0;
			for (String token : query.tokens().keySet()) {
				if (!index.containsKey(token)) {
					continue;
				}
				
				w_square_sum += Math.pow(query.tokens().get(token).tf() * index.get(token).idf(), 2);
			}
			w_squares.put(queryNum, w_square_sum);
		}
	}
	
	public void generateCosineMeasureOutput() {
		String output = "";
		
		for (String queryNum : queries.keySet()) {
			Query query = queries.get(queryNum);
			
			Score[] scores = new Score[N];
			int i = 0;
			
			for (String docNo : documents) {
				double dotProduct = 0;
				
				for (String token : query.tokens().keySet()) {
					if (!index.containsKey(token)) {
						continue;
					}
					
					PostingList postingList = index.get(token);	// grab posting list for current token
					
					if (postingList.map().containsKey(docNo)) {	// posting list for token has a mapping for the document in question
						double w_numerator = Math.log10((query.tokens().get(token).tf()) + 1) * index.get(token).idf();
						double w_denominator = w_squares.get(query.number());
						double d = postingList.map().get(docNo).tf();
						
						dotProduct += (w_numerator / w_denominator) * d;
					}
				}	
				double score = dotProduct / (Math.sqrt(d_squares.get(docNo) * w_squares.get(queryNum)));
				
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
				output += scores[j].queryNumber() + " 0 " + scores[j].documentNumber() + " " + (j+1) + " " + scores[j].score() + " COSINE\n";
			}
		}
		
		// NOW WRITE THAT SHIT :D
		
		Path path = Paths.get(resultsPath + "staticCosine.txt");
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
