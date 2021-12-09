package Extra;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import Analysis.NetlistAnalyzer;
import Analysis.ReadWrite;

public class ModuleInterConnect {
	private ArrayList<String> netlist = new ArrayList<String>();
	private ArrayList<Integer> numberOfGatesInModules = new ArrayList<Integer>();
	private ArrayList<String> moduleNames = new ArrayList<String>();
	private HashMap<String, ArrayList<String>> gatesInModule = new HashMap<String, ArrayList<String>>();
	private ArrayList<Integer> moduleTracker = new ArrayList<Integer>();
	private ArrayList<Integer> endmoduleTracker = new ArrayList<Integer>();
	


	public ModuleInterConnect () {
	}
	
	public ModuleInterConnect(String inputFileName, String firstModule, String secondModule, String key) {
		insertMUX(inputFileName, firstModule, secondModule, key);
		
	}

	public void insertMUX (String inputFileName, String firstModule, String secondModule, String key){
		// devide key in half. one for each module.
		// choose 1 node from one module. as first input 
		// choose another node from the same module as output. make sure first node is not in the fanout of second node
		// choose 1 node from other module as second input.
		// insert the MUX at prior module. 
		// do the same for other module
		ReadWrite rw = new ReadWrite();
		netlist = rw.fileReader(inputFileName);
		NetlistAnalyzer na = new NetlistAnalyzer(netlist);
		numberOfGatesInModules = na.numberOfGatesInModules;
		moduleNames = na.moduleNames;
		gatesInModule = na.gatesInModule;
		moduleTracker = na.moduleTracker;
		endmoduleTracker = na.endmoduleTracker;
		
		int	noOfMuxInFirstModule = key.length()/2;
		int noOfMuxInSecondModule = key.length() - noOfMuxInFirstModule;
		
		// MUXes in first module
		ArrayList<String> something1 = randomNodeSelector(noOfMuxInFirstModule, firstModule);
		ArrayList<String> something2 = randomNodeSelector(noOfMuxInFirstModule, secondModule);
		
	}
	
	private ArrayList<String> randomGatePositionSelector(int noOfKeyGate, String moduleName){
		ArrayList<String> selectedPositions = new ArrayList<String>();
		
		for (int i = 0; i < moduleNames.size(); i++) {
			if (moduleNames.get(i).equals(moduleName)) {
				int numberOfOriginalGates = numberOfGatesInModules.get(i).intValue();
//				float moduleWeight = Math.round((float) numberOfOriginalGates * (float) noOfKeyGate / (float) numberOfGates);
//				int noOfKeyGateInModule = (int) moduleWeight;
				int noOfKeyGateInModule = noOfKeyGate;
				// To generate random gate numbers
				Random randomGenerator = new Random();
				for (int j = 0; j < noOfKeyGateInModule; j++) {
					int randomGateNumber = randomGenerator.nextInt(numberOfOriginalGates);
					String newRandomGate = moduleNames.get(i) + " "
							+ gatesInModule.get(moduleNames.get(i)).get(randomGateNumber);
					if (!selectedPositions.contains(newRandomGate)) {
						selectedPositions.add(newRandomGate);
					} else {
						j--;
					}
				} 
			}
		}
		while(selectedPositions.size() > noOfKeyGate){
			selectedPositions.remove(selectedPositions.size()-1);
		}
		System.out.println( noOfKeyGate + " gates in " + selectedPositions);
		return selectedPositions;
		
	}
	
	private ArrayList<String> randomNodeSelector(int noOfKeyGate, String moduleName){
		ArrayList<String> selectedPositions = new ArrayList<String>();
		ArrayList<String> selectedNodes = new ArrayList<String>();
		
		for (int i = 0; i < moduleNames.size(); i++) {
			if (moduleNames.get(i).equals(moduleName)) {
				int numberOfOriginalGates = numberOfGatesInModules.get(i).intValue();
//				float moduleWeight = Math.round((float) numberOfOriginalGates * (float) noOfKeyGate / (float) numberOfGates);
//				int noOfKeyGateInModule = (int) moduleWeight;
				int noOfKeyGateInModule = noOfKeyGate;
				// To generate random gate numbers
				Random randomGenerator = new Random();
				for (int j = 0; j < noOfKeyGateInModule; j++) {
					int randomGateNumber = randomGenerator.nextInt(numberOfOriginalGates);
					String newRandomGate = moduleNames.get(i) + " "
							+ gatesInModule.get(moduleNames.get(i)).get(randomGateNumber);
					if (!selectedPositions.contains(newRandomGate)) {
						selectedPositions.add(newRandomGate);
					} else {
						j--;
					}
				} 
			}
		}
		while(selectedPositions.size() > noOfKeyGate){
			selectedPositions.remove(selectedPositions.size()-1);
		}
		
		int thisModule = moduleNames.indexOf(moduleName);
		for (int j = moduleTracker.get(thisModule)-1; j < endmoduleTracker.get(thisModule); j++){
			String line = netlist.get(j).trim();
			String[] wordsOfObfNetlLines = line.split(" ",2);
			String firstWord = wordsOfObfNetlLines[0].trim().split("\\(",2)[0];
			
			switch (firstWord) {
			
			case "not":
			case "and":
			case "nand":
			case "or":
			case "nor":
			case "xor":
			case "xnor":
				String gateName = wordsOfObfNetlLines[1].split("\\(",2)[0].trim();
				if (selectedPositions.contains(gateName)){
					String outputOfThatGate = netlist.get(j).trim().split(" ",2)[1].split("\\(",2)[1].split(",",2)[0].trim();
					selectedNodes.add(outputOfThatGate);
				}
			}
		}
		System.out.println( noOfKeyGate + " gates in " + selectedNodes);
		return selectedNodes;		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ModuleInterConnect modCon = new ModuleInterConnect(args[0], args[1], args[2], args[3]);

	}

}
