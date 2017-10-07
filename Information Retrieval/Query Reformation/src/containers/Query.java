package containers;

import java.util.HashMap;

public class Query {
	private String queryNumber;
	private String queryString;
	private HashMap<String, Triple> tokens;
	
	public Query(String num, String str) {
		queryNumber = num;
		queryString = str;
		tokens = new HashMap<String, Triple>();
	}
	
	public String number() {
		return queryNumber;
	}
	
	public String string() {
		return queryString;
	}
	
	public HashMap<String, Triple> tokens() {
		return tokens;
	}
	
	public void updateString(String str) {
		queryString = str;
	}
	
	public void saveToken(String tokenString) {
		tokens.put(tokenString, new Triple(tokenString, queryNumber, 1));
	}
	
	public void incrementTokenFreq(String tokenString) {
		Triple tokenTriple = tokens.get(tokenString);
		tokenTriple.increment_tf();
		tokens.replace(tokenString, tokenTriple);
	}
}
