package Metric;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Analysis.FaninAndFanout;
import Analysis.NetlistAnalyzer;
import Analysis.ReadWrite;


public class KeyRel {

	/**
	 * @param ArrayList<String> inputNetlist/String inputFileName, int keySize, int percentRand
	 */
	int keySize = 0;
//	int noOfIsolatedKey = 0;
	ArrayList<String> keyGates = new ArrayList<String>();
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
	public int partconvergentWeight = 1;
	public int partdominantWeight = 1;
	public int isolatedWeight = 0;
	boolean errorFlag = false;
	boolean debug = true;
	boolean randomInit = true;
	ArrayList<String> log = new ArrayList<String>();
	// boolean seq = false;
	
	public KeyRel(String inputFileName, String keyName) {
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlistLines = rw.fileReader(inputFileName);
		errorFlag = rw.errorFlag;
		HashMap<String, Float> values = metricCalculator(netlistLines, keyName);
		float cktValue = 0;
		for (String gate : values.keySet()) {
			cktValue += values.get(gate);
		}
		if (debug) {
			System.out.println("Key relation factor of " + inputFileName.replace(".v", "") + " is = " + cktValue);
			log.add("Key relation factor of " + inputFileName.replace(".v", "") + " is = " + cktValue);
		}
//		System.out.println(cktValue + " " + keySize + " " + heigherWeight);
		float normaFactor = (float) cktValue / (float) keySize;
		System.out.println("Normalized Key relation factor of " + inputFileName.replace(".v","") + " is = " + normaFactor);
		log.add("Normalized Key relation factor of " + inputFileName.replace(".v","") + " is = " + normaFactor);
		String line = "Normalized Key relation factor of " + inputFileName.replace(".v","") + " is = " + normaFactor;
		rw.fileWriter(line, "KeyRel_"+inputFileName.replace(".v",".txt"));
//		float ratio = (float) keySize / (float) outPorts.size();
//		System.out.println("Ratio = " + ratio);
//		System.out.println("No of Isolated Keys = " + noOfIsolatedKey);
		rw.fileWriter(log, "log_KeyRel_"+inputFileName.replace(".v",".txt"));
	} 
	
	public KeyRel() {
	}

	public HashMap<String, Float> metricCalculator (ArrayList<String> netlistLines, String keyName){		
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		gates = na.gates;
		ArrayList<String> detailedGates = na.detailedGates;
		moduleNames = na.moduleNames;
		gatesAndMods = na.gatesAndMods;
		FaninAndFanout fnf = new FaninAndFanout(netlistLines, na);
		faninGates = fnf.faninGatesOfGate;
		fanoutGates = fnf.fanoutGatesOfGate;
		outPorts = fnf.outputPorts;
		faninNodes = fnf.faninNodeOfGate;
		fanoutNodes = fnf.fanoutNodeOfGate;
		HashMap<String, List<String>> faninGatesOfOutPorts = fnf.faninGatesOfOutPorts;
//		ArrayList<String> keyValues = new ArrayList<String>();

		HashMap<String, Float> totalWeight = new HashMap<String, Float>(); 
		
		
		for (int i = 0; i < gates.size(); i++) {
			if (detailedGates.get(i).contains(keyName)){
				keyGates.add(gates.get(i));
			}
		}
		keySize = keyGates.size();
		int ind = 0;
		for (String output : outPorts) {
			int counter = 0;
			for (int i = 0; i < keyGates.size(); i++) {
				if(faninGatesOfOutPorts.get(output).contains(keyGates.get(i))){
					counter++;
					ind = i;
				}
			}
			if(counter==1){
				if (debug) {
					System.out.println("Only one key  " + keyGates.get(ind) + " in fanin of " + output);
				}
				totalWeight.put(keyGates.get(ind), (float)0);
//				keyGates.remove(ind);
			}
		}
		for (int i = 0; i < keyGates.size(); i++) {
			float weight = 0;
			for (int j = 0; j < keyGates.size(); j++) {
				if (i != j){ 
					weight += findMetric(keyGates.get(i), keyGates.get(j)); // weight between random keys
				}
			}
			float normaWeight = weight / (float) (keySize-1);
			totalWeight.put(keyGates.get(i), normaWeight);
			if (weight == (float) 0) {
				if (debug) {
					System.out.println("Isolated key found : " + keyGates.get(i));
				}
//				noOfIsolatedKey ++ ;
			}
		}
		
		if (debug) { 
			System.out.println("TotalWeight in the end = "+totalWeight);
		}
		
//		for (String gate : keyGates) {
//		keyValues.add(gatesAndMods.get(gates.indexOf(gate)));
//		}
		
		// TODO : here put the non-mutable parser
		return totalWeight;
	}
	
	private Float findMetric (String gate1, String gate2){
		gate1 = gate1.trim();
		gate2 = gate2.trim();
		float weight = 0;
		int heigherWeight = dominantWeight > convergentWeight ? dominantWeight : convergentWeight;
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
					weight = (float) dominantWeight / (float) heigherWeight;
					if (debug) {
						System.out.println("dominant key pair = " + gate1 + ", " + gate2);
						log.add("dominant key pair = " + gate1 + ", " + gate2);
					} 
				} else {
					if (debug) {
						System.out.println("convergent key pair = "+ gate1 + ", " + gate2);
						log.add("convergent key pair = "+ gate1 + ", " + gate2);
					}
					weight = convergentWeight / (float) heigherWeight;
				}
			} else {
				if (debug) {
					System.out.println("convergent key pair = "+ gate1 + ", " + gate2);
					log.add("convergent key pair = "+ gate1 + ", " + gate2);
				}
				weight = convergentWeight / (float) heigherWeight;
			}
		} else {
			if (fanoutGates.get(gate2).contains(gate1) | fanoutGates.get(gate1).contains(gate2)) { // partial dominant
				weight = partdominantWeight / (float) heigherWeight;
				if (debug) {
					System.out.println("partially dominant key pair = "+ gate1 + ", " + gate2);
					log.add("partially dominant key pair = "+ gate1 + ", " + gate2);
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
						log.add("partially convergent key pair = " + gate1 + ", " + gate2);
					}
					weight = partconvergentWeight / (float) heigherWeight;
				} else { // Isolated Key Gates
					if (debug) {
						System.out.println("isolated key pair = " + gate1 + ", " + gate2);
						log.add("isolated key pair = " + gate1 + ", " + gate2);
					}
					weight = isolatedWeight / (float) heigherWeight;
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
	
	public static void main(String[] args) {
		if (args.length < 2){
			System.out.println("USAGE : <inputFileName> <keyInputName>");
		}
		String keyName = args[1];
		KeyRel mke = new KeyRel(args[0], keyName);
	}
}
