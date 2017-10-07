package containers;

import java.util.HashMap;
import java.util.Hashtable;

import preprocessing.PorterStemmer;
import tools.Loader;

public class Document {
	private String DOCNO;
	private String TEXT;
	private Hashtable<String, Triple> tokens;
	
	public Document(String number, String text) {
		DOCNO = number;
		TEXT = text;
		tokens = new Hashtable<String, Triple>();
	}
	
	public String number() {
		return DOCNO;
	}
	
	public String text() {
		return TEXT;
	}
	
	public Hashtable<String, Triple> tokens() {
		return tokens;
	}
	
	public void update(String newText) {
		TEXT = newText;
	}
	
	public void saveToken(String token) {
		if (!tokens.contains(token)) {
			tokens.put(token, new Triple(token, DOCNO, 1));
		}
	}
	
	public void tokenize() {
		HashMap<String, String> stopWords = new Loader().buildDictionary("Resources/stopWords.txt", 1);
		PorterStemmer stemmer = new PorterStemmer();
		
		for (String term : TEXT.split("\\s+")) {
//			System.out.println(term);
			if (!stopWords.containsKey(term) && term.length() > 0) {
				saveToken(stemmer.stripAffixes(term));
			}
		}
	}
}
