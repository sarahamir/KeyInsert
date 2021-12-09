package Metric;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Analysis.FaninAndFanout;
import Analysis.NetlistAnalyzer;
import Analysis.ReadWrite;


public class KeyRel2 {

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
	public int nonmutableWeight = 20;
	int heigherWeight = 0;
	boolean errorFlag = false;
	boolean debug = true;
//	boolean randomInit = true;
//	ArrayList<String> log = new ArrayList<String>();
	// boolean seq = false;
	
	public KeyRel2(String inputFileName, String detectionReportName, String keyName) {
		maxWeight();
		HashMap<String, Float> values = metricCalculator(inputFileName, detectionReportName, keyName);
		float cktValue = 0;
		for (String gate : values.keySet()) {
			cktValue += values.get(gate);
		}
		if (debug) {
			System.out.println("Key relation factor of " + inputFileName.replace(".v", "") + " is = " + cktValue);
//			log.add("Key relation factor of " + inputFileName.replace(".v", "") + " is = " + cktValue);
		}
		float metric = (float) cktValue / (float) keySize;
		
//		log.add("Normalized Key relation factor of " + inputFileName.replace(".v","") + " is = " + normaFactor);
		String line = "KeyRel2 of " + inputFileName.replace(".v","") + " is = " + metric;
		System.out.println(line);
		ReadWrite rw = new ReadWrite();
		rw.fileWriter(line, "KeyRel2_"+inputFileName.replace(".v",".txt"));
//		rw.fileWriter(log, "log_KeyRel_"+inputFileName.replace(".v",".txt"));
	} 
	
	public KeyRel2() {
	}
	
	void maxWeight (){
		 int a = dominantWeight > convergentWeight ? dominantWeight : convergentWeight;
		 int b = partdominantWeight > partconvergentWeight ? partdominantWeight : partconvergentWeight;
		 int c = isolatedWeight > nonmutableWeight ? isolatedWeight : nonmutableWeight;
		 int d = a > b ? a : b ;
		 heigherWeight = c > d ? c : d ;
	}

	public HashMap<String, Float> metricCalculator (String inputFileName, String detectionReportName, String keyName){	
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlistLines = rw.fileReader(inputFileName);
		errorFlag = rw.errorFlag;
		NetlistAnalyzer na = new NetlistAnalyzer(netlistLines);
		gates = na.gates;
		ArrayList<String> detailedGates = na.detailedGates;
		moduleNames = na.moduleNames;
		gatesAndMods = na.gatesAndMods;
		FaninAndFanout fnf = new FaninAndFanout(netlistLines, na);
		faninGates = fnf.faninGatesOfGate;
		fanoutGates = fnf.fanoutGatesOfGate;
		outPorts = fnf.outputPorts;
		ArrayList<String> inputPorts = fnf.inputPorts;
		faninNodes = fnf.faninNodeOfGate;
		fanoutNodes = fnf.fanoutNodeOfGate;
		HashMap<String, List<String>> faninGatesOfOutPorts = fnf.faninGatesOfOutPorts;

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
		
		File detectedFile = new File(detectionReportName);
		if (detectedFile.exists()){
			ArrayList<String> keyPorts = new ArrayList<String>();
			ArrayList<String> detectedKeys = new ArrayList<String>();
			ArrayList<String> undetectedKeys = new ArrayList<String>();
			for (String port : inputPorts) {
				if (port.contains(keyName)){
					keyPorts.add(port.trim());
				}
			}
			ArrayList<String> tempAL = rw.fileReader(detectionReportName);
			for (String string : tempAL) {
				detectedKeys.add(string.split(" ",2)[0]);
			}
			for (String keyPort : keyPorts) {
				if ( ! detectedKeys.contains(keyPort)){
					undetectedKeys.add(keyPort);
				}
			}
			for (int i = 0; i < gates.size(); i++) {
				for (String undetectedKey : undetectedKeys) {			
					if (detailedGates.get(i).contains("("+undetectedKey+")")){
						totalWeight.put(gates.get(i), ((float)nonmutableWeight/(float)heigherWeight));
					}
				}
			}
		} else {
			System.err.println("File does not exist : " + detectionReportName );
		}

		if (debug) { 
			System.out.println("TotalWeight in the end = "+totalWeight);
		}
		
		return totalWeight;
	}
	
	private Float findMetric (String gate1, String gate2){
		gate1 = gate1.trim();
		gate2 = gate2.trim();
		float weight = 0;
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
//						log.add("dominant key pair = " + gate1 + ", " + gate2);
					} 
				} else {
					if (debug) {
						System.out.println("convergent key pair = "+ gate1 + ", " + gate2);
//						log.add("convergent key pair = "+ gate1 + ", " + gate2);
					}
					weight = convergentWeight / (float) heigherWeight;
				}
			} else {
				if (debug) {
					System.out.println("convergent key pair = "+ gate1 + ", " + gate2);
//					log.add("convergent key pair = "+ gate1 + ", " + gate2);
				}
				weight = convergentWeight / (float) heigherWeight;
			}
		} else {
			if (fanoutGates.get(gate2).contains(gate1) | fanoutGates.get(gate1).contains(gate2)) { // partial dominant
				weight = partdominantWeight / (float) heigherWeight;
				if (debug) {
					System.out.println("partially dominant key pair = "+ gate1 + ", " + gate2);
//					log.add("partially dominant key pair = "+ gate1 + ", " + gate2);
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
//						log.add("partially convergent key pair = " + gate1 + ", " + gate2);
					}
					weight = partconvergentWeight / (float) heigherWeight;
				} else { // Isolated Key Gates
					if (debug) {
						System.out.println("isolated key pair = " + gate1 + ", " + gate2);
//						log.add("isolated key pair = " + gate1 + ", " + gate2);
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
		if (args.length < 3){
			System.out.println("USAGE : <inputFileName> <detectionReportName> <keyInputName>");
		}
		String keyName = args[2];
		KeyRel2 mke = new KeyRel2(args[0], args[1], keyName);
	}
}
