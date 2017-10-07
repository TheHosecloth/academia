package containers;

public class Triple {
	private String term;
	private String number;
	private int frequency;
	
	public Triple(String token, String identifier, int freq) {
		term = token;
		number = identifier;
		frequency = freq;
	}
	
	public String term() {
		return term;
	}
	
	public String number() {
		return number;
	}
	
	public int tf() {
		return frequency;
	}
	
	public void increment_tf() {
		frequency++;
	}
}