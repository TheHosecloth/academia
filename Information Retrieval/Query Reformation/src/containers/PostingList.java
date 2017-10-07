package containers;

import java.util.HashMap;

public class PostingList {
	private HashMap<String, Triple> map;	// K = DOCNO, V = Triple
	private double idf;
	
	public PostingList() {
		map = new HashMap<String, Triple>();
	}
	
	public HashMap<String, Triple> map() {
		return map;
	}
	
	public double idf() {
		return idf;
	}
	
	public void storeTriple(String DOCNO, Triple triple) {
		map.put(DOCNO, triple);
	}
	
	public void compute_idf(int n) {
		idf = Math.log10((n - map.size() + 0.5) / (map.size() + 0.5));
	}
	
	public double n_idf() {
		return map.size() * idf;
	}
}
