package KITmain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import Analysis.FaninAndFanout;
import Analysis.NetlistAnalyzer;
import Analysis.ReadWrite;


public class NonMutableKeyPositions {

	/**
	 * @param ArrayList<String> inputNetlist/String inputFileName, int keySize, int percentRand
	 */
	ArrayList<String> selectedPositions = new ArrayList<String>();
	ArrayList<String> moduleNames = new ArrayList<String>();
	ArrayList<String> gates = new ArrayList<String>();
	ArrayList<String> gatesAndMods = new ArrayList<String>();
	HashMap<String, List<String>> faninGates = new HashMap<String, List<String>>();
	HashMap<String, List<String>> fanoutGates = new HashMap<String, List<String>>();
	HashMap<String, List<String>> faninNodes = new HashMap<String, List<String>>();
	HashMap<String, List<String>> fanoutNodes = new HashMap<String, List<String>>();
	ArrayList<String> outPorts = new ArrayList<String>();
	public int convergentWeight = 10;
	public int dominantWeight = 5;
	public int partconvergentWeight = 2;
	public int partdominantWeight = 3;
	public int isolatedWeight = 0;
	boolean errorFlag = false;
	boolean debug = false;
	boolean randomInit = true;
	// boolean seq = false;
	
	public NonMutableKeyPositions(String inputFileName, int keySize, int percentRand) {
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlistLines = rw.fileReader(inputFileName);
		errorFlag = rw.errorFlag;
		ArrayList<String> keyPositions = positionSelecter(netlistLines, keySize, percentRand);
		rw.fileWriter(keyPositions, "NMKP_" + inputFileName.replace(".v", ".txt"));
	}
	
	public NonMutableKeyPositions(ArrayList<String> inputNetlist, int keySize, int percentRand) {
		selectedPositions = positionSelecter(inputNetlist, keySize, percentRand);
	}
	
	public NonMutableKeyPositions() {
	}

	public ArrayList<String> positionSelecter (ArrayList<String> netlistLines, int keySize, int percentRand){		
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		gates = na.gates;
		moduleNames = na.moduleNames;
		gatesAndMods = na.gatesAndMods;
		FaninAndFanout fnf = new FaninAndFanout(netlistLines, na);
		faninGates = fnf.faninGatesOfGate;
		fanoutGates = fnf.fanoutGatesOfGate;
		outPorts = fnf.outputPorts;
		faninNodes = fnf.faninNodeOfGate;
		fanoutNodes = fnf.fanoutNodeOfGate;
		
		ArrayList<String> keyPositions = new ArrayList<String>();
		int noOfInitialKey = (int)Math.round((float)keySize*(float)percentRand/100);
		HashMap<String, Long> totalWeight = new HashMap<String, Long>(); 
		if (randomInit) {
			for (String gateName : randomGatePositionSelector(noOfInitialKey)) {
				selectedPositions.add(gateName);
			}
		} else {
			for (String gateInfo : gatePositionSelector("5randGates_c499.txt")) {
				String gateName = gateInfo.trim().split(" ", 2)[1];
				selectedPositions.add(gateName);
			}
		}
//		for (String gateName : selectedPositions) {
//			totalWeight.put(gateName, (long) 0);
//		}
		for (int i = 0; i < selectedPositions.size(); i++) {
			String gateName = selectedPositions.get(i);
			long weight = 0;
			for (int j = 0; j < selectedPositions.size(); j++) {
				if (i != j){ 
					weight += findMetric(selectedPositions.get(i), selectedPositions.get(j)); // weight between random keys
				}
			}
			totalWeight.put(gateName, weight);
		}
		
		if (debug) {
			System.out.println("Random gate weights = " + totalWeight);
		}
		int temp = selectedPositions.size();
		
		for (int i = temp; i < keySize; i++) { // from #randomKeys to keySize
			List<Long> weights = new ArrayList<Long>();
			long weight2 = 0;
			for (int i1 = 0; i1 < selectedPositions.size(); i1++) {
				for (int j1 = 0; j1 < selectedPositions.size(); j1++) {
					if (i1 != j1){ 
						weight2 += findMetric(selectedPositions.get(i1), selectedPositions.get(j1)); // weight between random keys
					}
				}
				totalWeight.put(selectedPositions.get(i1), weight2);
			}
			for (int j = 0; j < gates.size(); j++) { // for all gates
				long weight = 0;
				if ( ! selectedPositions.contains(gates.get(j))){ // if this gate is not already selected
					for (int k = 0; k < selectedPositions.size(); k++){
						weight += totalWeight.get(selectedPositions.get(k));  // TODO
					}
					for (int k = 0; k < selectedPositions.size(); k++) { // findMetric of this gate and all existing key positions
						int metric = findMetric(gates.get(j), selectedPositions.get(k)); 
						weight += metric;
					}
				}
				weights.add(weight);
			}
			if (debug) {
				System.out.println("Weights = "+weights);
			} 
			int flag = 0;
			long maxWeight = Collections.max(weights);
			while (flag != 1 & flag != 2){	
				ArrayList<Integer> maxIndexes = new ArrayList<Integer>();
				ArrayList<Long> nonMaxWeights = new ArrayList<Long>();
				for (int j = 0; j < gates.size(); j++) {
					if (weights.get(j) == maxWeight) {
						maxIndexes.add(j);
					} else if (weights.get(j) < maxWeight) {
						nonMaxWeights.add(weights.get(j));
					}
	
				}
				Random rand = new Random();
				int index;
//				for (int j = 0; j < maxIndexes.size(); j++) {
				while(maxIndexes.size() != 0){
					index = rand.nextInt(maxIndexes.size());
					if (!selectedPositions.contains(gates.get(maxIndexes.get(index)))) { // search if there is any high weight gate that is not already counted in 
						selectedPositions.add(gates.get(maxIndexes.get(index)));
						totalWeight.put(gates.get(maxIndexes.get(index)), maxWeight);
						flag = 1;
						break;
					} else {
						maxIndexes.remove(index);
						if (debug) {
							System.out.println("index = "+index+", Get another gate with weight = " + maxWeight);
						}
					}
				} 
				if (flag != 1) {
					if (nonMaxWeights.size() != 0) {
						// TODO : get the next highest maxWeight and run the loop again
						maxWeight = Collections.max(nonMaxWeights);
					} else {
						flag = 2;
					}
					if (debug) {
						System.out.println("No gate found. New maxWeight " + maxWeight);
					}
				}
			}
			if (debug) {
				System.out.println(selectedPositions.get(selectedPositions.size()-1) + " : " +  maxWeight);
				System.out.println("TotalWeight = " + totalWeight );
			}
			
		}
		
		if (debug) {
			System.out.println("TotalWeight in the end = "+totalWeight);
		}
		
		for (String gate : selectedPositions) {
		keyPositions.add(gatesAndMods.get(gates.indexOf(gate)));
		}
		return keyPositions;
	}
	
	private Integer findMetric (String gate1, String gate2){
		gate1 = gate1.trim();
		gate2 = gate2.trim();
		int weight = 0;
		boolean convergeflag = true;
		boolean partconvergeflag = false;
		for (String output : outPorts) { // fully convergent key
			if (fanoutNodes.get(gate1).contains(output)) {
				if(!fanoutNodes.get(gate2).contains(output)){
					convergeflag = false;
				} else{
//					if (debug) {
//						System.out.println("Common output " + output);
//					}
				}
			}
		}
		if (convergeflag){ // fully convergent key
			if (fanoutGates.get(gate2).contains(gate1)) { // possible dominant key
				List<String> lhs = fanoutGates.get(gate2);
				List<String> rhs = faninGates.get(gate1);
				lhs.removeAll(fanoutGates.get(gate1));
				rhs.removeAll(faninGates.get(gate2));
				lhs.remove(gate1);
				rhs.remove(gate2);
				if (lhs.equals(rhs)) { // dominant key
					weight = dominantWeight;
					if (debug) {
						System.out.println("dominant key pair = " + gate1 + ", " + gate2);
					} 
				} else {
					if (debug) {
						System.out.println("convergent key pair = "+ gate1 + ", " + gate2);
					}
					weight = convergentWeight;
				}
			} else {
				if (debug) {
					System.out.println("convergent key pair = "+ gate1 + ", " + gate2);
				}
				weight = convergentWeight;
			}
		} else {
			if (fanoutGates.get(gate2).contains(gate1) | fanoutGates.get(gate1).contains(gate2)) { // partial dominant
				weight = partdominantWeight;
				if (debug) {
					System.out.println("partially dominant key pair = "+ gate1 + ", " + gate2);
				}
			} else {
				for (String fanoutG1 : fanoutGates.get(gate1)) {
					if (fanoutGates.get(gate2).contains(fanoutG1)) { // partially convergent key
						partconvergeflag = true;
					}
				}
				if (partconvergeflag) { // partially convergent key
					if (debug) {
						System.out.println("partially convergent key pair = " + gate1 + ", " + gate2);
					}
					weight = partconvergentWeight;
				} else { // Isolated Key Gates
					if (debug) {
						System.out.println("isolated key pair = " + gate1 + ", " + gate2);
					}
					weight = isolatedWeight;
				} 
			}
		}
				
		/*
		if (weight != 0){
			if ( goldenPattern(gate1, gate2) == false ){
				weight = nonMutableWeight;
			}
		} 
		 */

		return weight;
	}
	
	private ArrayList<String> randomGatePositionSelector(int noOfKeyGate){
		ArrayList<String> selectedPositions = new ArrayList<String>();

		// To generate random gate numbers
		Random randomGenerator = new Random();
		for (int j = 0; j < noOfKeyGate; j++) {
			int randomGateNumber = randomGenerator.nextInt(gates.size());
//			String newRandomGate = moduleNames.get(i) + " " + listOfGatesInModule.get(moduleNames.get(i)).get(randomGateNumber);
			String newRandomGate = gates.get(randomGateNumber);
			if (! selectedPositions.contains(newRandomGate)) {
				selectedPositions.add(newRandomGate);
			} else {
				j--;
			}
		}
		return selectedPositions;
	}
	
	private ArrayList<String> gatePositionSelector(String inputFileName){
		ArrayList<String> selectedPositions = new ArrayList<String>();
		ReadWrite rw = new ReadWrite();
		ArrayList<String> inputLines = rw.fileReader(inputFileName);
		errorFlag = rw.errorFlag;
		
		String firstword = "";
		String secondword = "";
		for (String line : inputLines) {
			String[] lineSegments = line.trim().split(",");
			for (int i = 0; i < lineSegments.length; i++) {
				firstword = lineSegments[i].trim().split(" ", 2)[0];
				secondword = lineSegments[i].trim().split(" ", 2)[1];
				if ( ! moduleNames.contains(firstword)){
					errorFlag = true;
					System.err.println("Key-gate position file is not in valid form. No obfuscation done.");
					System.out.println("Each gate position has to be accompanied by module name.");
					System.out.println("Valid position format: module_name<single_space>gate_name.");
					System.out.println("Positions may be separated by comma or indivisual lines.");
					System.out.println("Example:");
					System.out.println("        moduleA andX");
					System.out.println("        moduleB orY");
					System.out.println("                ...");
					System.out.println("OR : moduleA andX, moduleB orY, ...");
				} else {
					if ( ! gates.contains(secondword)){
						errorFlag = true;
						System.out.println("Key-gate position file contains gates that are not found in the circuit. This gate would be omitted");
					} else {
						selectedPositions.add( firstword + " " + secondword);
					}
				}
				
			}
		}
		return selectedPositions;
	}
	
	public static void main(String[] args) {
		if (args.length < 3){
			System.out.println("USAGE : <inputFileName> <keysize> <%initialRandomKey>");
		}
		int keySize = Integer.parseInt(args[1]);
		int percentRand = Integer.parseInt(args[2]);
		NonMutableKeyPositions nmkp = new NonMutableKeyPositions(args[0], keySize, percentRand);
	}
}
