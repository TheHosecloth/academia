package scheduling;

import java.util.LinkedList;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.IOException;

// all main does is load processes and bootstrap the scheduling algorithm

public class Master { 
	static LinkedList<Process> processes;
	static Boolean verboseMode;

	public static void main (String[] args) throws SchedulingAlgorithmArgumentException {
		if (args[0].equals("RR")) {
			Integer filesStartingIndex = filesArgumentStartingIndex(args[0], args[2]);
			processes = loadProcesses(args, filesStartingIndex);
			RoundRobin roundRobin = new RoundRobin(processes, Integer.parseInt(args[1]), verboseMode);
			roundRobin.schedule();
		}
		else if (args[0].equals("SJF")) {
			Integer filesStartingIndex = filesArgumentStartingIndex(args[0], args[1]);
			processes = loadProcesses(args, filesStartingIndex);
			ShortestJobFirst shortestJobFirst = new ShortestJobFirst(processes, verboseMode);
			shortestJobFirst.schedule();
		}
		else if (args[0].equals("SJR")) {
			Integer filesStartingIndex = filesArgumentStartingIndex(args[0], args[1]);
			processes = loadProcesses(args, filesStartingIndex);
			ShortestJobRemaining shortestJobRemaining = new ShortestJobRemaining(processes, verboseMode);
			shortestJobRemaining.schedule();
		}
		else {
			throw new SchedulingAlgorithmArgumentException("Invalid Schduling Algorithm Argument: " + args[0]);
		}
	}
	
	private static Integer filesArgumentStartingIndex (String algorithm, String verboseOption) {
		Integer startingIndex = -1;
		
		if (algorithm.equals("RR")) {
			if (verboseOption.equals("verbose")) {
				startingIndex = 3;
				verboseMode = true;
			}
			else {
				startingIndex = 2;
				verboseMode = false;
			}
		}
		else {	// SFJ or SJR
			if (verboseOption.equals("verbose")) {
				startingIndex = 2;
				verboseMode = true;
				
			}
			else {
				startingIndex = 1;
				verboseMode = false;
			}
		}
		
		return startingIndex;
	}
	
	private static LinkedList<Process> loadProcesses (String[] args, Integer startingIndex) {
		LinkedList<Process> processes = new LinkedList<Process>();
		Integer currentProcessNumber = 1;
		
		for (int i = startingIndex; i < args.length; i++) {
			Path path = Paths.get("/Users/Eidolon/Developer/Operating Systems/Scheduling Algorithms/src/scheduling/" + args[i]);
			Charset charset = Charset.forName("US-ASCII");
			
			Process currentProcess = null;
			
			try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			    String line = null;
			    while ((line = reader.readLine()) != null) {
			        String[] splitLine = line.split("\\s+");
			        
			        if (splitLine[0].equals("start")) {
			        	currentProcess = new Process(currentProcessNumber++, Integer.parseInt(splitLine[1]));
			        }
			        else if (splitLine[0].equals("B") || splitLine[0].equals("I")) {
			        	Subprocess newSubprocess = new Subprocess(splitLine[0], Integer.parseInt(splitLine[1]));
			        	currentProcess.addSubprocess(newSubprocess);
			        	
			        }
			        else {	// "end"
			        	break;
			        }
			    }
			} catch (IOException e) {
			    e.printStackTrace();;
			}
			processes.add(currentProcess);
		}
		
		return processes;
	}
}