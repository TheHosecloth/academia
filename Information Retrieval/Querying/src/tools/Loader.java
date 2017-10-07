package tools;
/*
 * Takes a file of text terms and generates 
 */

import java.io.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import containers.PositionalPostingList;
import containers.PositionalTriple;
import containers.PostingList;
import containers.Query;
import containers.Triple;
import exceptions.IndexConfigException;

public class Loader {
	
	public HashMap<String, PostingList> loadStandardIndex(String configuration, String indexPath) throws IndexConfigException {
		HashMap<String, PostingList> index = new HashMap<String, PostingList>();
		
		String textFile = null;
		if (configuration.equals("single")) {
			textFile = indexPath + "single_termIndex.txt";
		} else if (configuration.equals("stem")) {
			textFile = indexPath + "stemIndex.txt";
		} else if (configuration.equals("phrase")) {
			textFile = indexPath + "phraseIndex.txt";
		} else {
			throw(new IndexConfigException("Invalid index configuration: " + configuration));
		}
		
		Path path = Paths.get(textFile);
		Charset charset = Charset.forName("US-ASCII");
		
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			String line;
			
			while ((line = reader.readLine()) != null) {
				PostingList postingList = new PostingList();
				
				String[] splitLine = line.split(" --> ");
				String term = splitLine[0];
			
				for (String rawTriple : splitLine[1].split(" \\| ")) {
//					System.out.println(configuration + ": " + term);
					String[] tripleComponents = rawTriple.split(" ");
					Triple newTriple = new Triple(tripleComponents[0], tripleComponents[1], 
							Integer.parseInt(tripleComponents[2]));
					postingList.storeTriple(tripleComponents[1], newTriple);
				}
				index.put(term, postingList);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return index;
	}
	
	public HashMap<String, PositionalPostingList> loadPositionalIndex(String configuration, String indexPath) throws IndexConfigException {
		HashMap<String, PositionalPostingList> positionalIndex = new HashMap<String, PositionalPostingList>();
		
		String textFile = null;
		if (configuration.equals("proximity")) {
			textFile = indexPath + "positionalIndex.txt";
		} else {
			throw(new IndexConfigException("Invalid index configuration for proximity index: " + configuration));
		}
		
		Path path = Paths.get(textFile);
		Charset charset = Charset.forName("US-ASCII");
		
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			String line;
			
			while ((line = reader.readLine()) != null) {
			
				PositionalPostingList positionalPostingList = new PositionalPostingList();
				
				String[] splitLine = line.split(" --> ");
				String term = splitLine[0];

				for (String rawTriple : splitLine[1].split(" \\| ")) {
					String[] tripleComponents = rawTriple.split(" ");
					
					PositionalTriple newTriple = new PositionalTriple(tripleComponents[0], tripleComponents[1], 
							Integer.parseInt(tripleComponents[2]));
					
					String positions = tripleComponents[3].replaceAll("\\{|\\}", "").trim();
					for (String position : positions.split(",")) {
						newTriple.addPosition(Integer.parseInt(position));
					}
					
					positionalPostingList.storeTriple(tripleComponents[1], newTriple);
					positionalIndex.put(term, positionalPostingList);
					} 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return positionalIndex;
	}
	
	public HashMap<String, Query> loadQueries(String textFile) {
		HashMap<String, Query> queries = new HashMap<String, Query>();
		
		Path path = Paths.get(textFile);
		Charset charset = Charset.forName("US-ASCII");
		
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			String queryNumber = null;
			String queryString = null;
			
			Pattern numberPattern = Pattern.compile("(<num> Number: )([0-9]{3})( )?");
			Pattern topicPattern = Pattern.compile("(<title> Topic: +)(.+)");
			
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				Matcher numberMatcher = numberPattern.matcher(line);
				Matcher topicMatcher = topicPattern.matcher(line);
				
				if (numberMatcher.matches()) {
					queryNumber = numberMatcher.group(2);
					continue;
				}
				if (topicMatcher.matches()) {
					queryString = topicMatcher.group(2);
				}
				if (line.equals("</top>")) {
					queries.put(queryNumber, new Query(queryNumber, queryString));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return queries;
	}
	
	
	public String[] populateArray(String[] array, String textFile) {
		Path path = Paths.get(textFile);
		Charset charset = Charset.forName("US-ASCII");
		
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			String line = null;
			
			for (int i = 0; i < array.length; i++) {
				line = reader.readLine();
				array[i] = line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return array;
	}
	
	
	public HashMap<String, String> buildDictionary(String textFile, int fields) {
		HashMap<String, String> dictionary = new HashMap<String, String>();
		
		Path path = Paths.get(textFile);
		Charset charset = Charset.forName("US-ASCII");
		
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				if (fields == 1) {
					dictionary.put(line, line);
				}
				if (fields == 2) {
					String[] keyValuePair = line.split(" : ");
					dictionary.put(keyValuePair[0], keyValuePair[1]);	
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return dictionary;
	}
}
