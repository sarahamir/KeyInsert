package Trojan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Analysis.FaninAndFanout;
import Analysis.NetlistAnalyzer;
import Analysis.ReadWrite;

public class TrojanRandom {
	/*
	 * Author: Sarah Amir
	 * Version: 1.0
	 * Date: July 30, 2020
	 * 
	 * Reads Probability file. 
	 * Inserts n number of Trojan in random places, with least P(1) as trigger
	 * The payload is placed at random
	 * 
	 * Supports Cadence Library 180nm (GSCLib3.0)
	 */
	boolean debug = false;
	
	public TrojanRandom() {	}
	
	public TrojanRandom(String inputFileName, String probFileName, int numberOfTrojans) {
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlist = rw.fileReader(inputFileName);		
		NetlistAnalyzer na = new NetlistAnalyzer(netlist);
//		
//		ArrayList<String>  detailedGates = na.detailedGates;
		
		/*
		 * NODE SELECTION
		 * ----------------------------------------------------------------------
		 */
		
		//Probabilistically select first input (trigger)
		ArrayList<String> probFile = rw.fileReader(probFileName);
		HashMap<String, Float> P1 = new HashMap<String, Float>();
		for (int i = 1; i < probFile.size()-1; i++) {
			if (probFile.get(i).split("\\s+").length == 3) {
				if(Float.parseFloat(probFile.get(i).split("\\s+")[2]) != -1){
					if (probFile.get(i).split("\\s+")[0].contains("[")) {
						P1.put("\\"+probFile.get(i).split("\\s+")[0],
								Float.parseFloat(probFile.get(i).split("\\s+")[2]));
					}else{
						P1.put(probFile.get(i).split("\\s+")[0],
								Float.parseFloat(probFile.get(i).split("\\s+")[2]));
					}
				}
			}
		}
		
		//Random node selections
		Random rn = new Random();
		FaninAndFanout fnf = new FaninAndFanout(netlist);
		HashMap<String, List<String>> faninNodeOfNode = fnf.faninNodeOfNode;
		int endModuleTracker = na.endmoduleTracker.get(0);
		ArrayList<String> sortedP1 = sortByValue(P1);	
		for (int i = 0; i < sortedP1.size(); i++) {
			if (!faninNodeOfNode.keySet().contains(sortedP1.get(i))) {
				System.out.println("Node "+ sortedP1.get(i) + " not found in design.");
				sortedP1.remove(i);
				i--;
			} 
		}
		ArrayList<String> nodeList = new ArrayList<String>(faninNodeOfNode.keySet());//P1.keySet()
//		if(numberOfTrojans>sortedP1.size()){
//			numberOfTrojans = sortedP1.size();
//		}
		for (int i = 0; i < numberOfTrojans; i++) {
			String trigger = sortedP1.get(i);
			if (debug) System.out.println(i+ "th low P(1) node = " + trigger);
			
//			ArrayList<String> nodePairs = new ArrayList<String>();
//			for (int j = 0; j < nodeList.size(); j++) {
//				for (int k = 0; k < nodeList.size(); k++) {
//					if (k != j){
//						String randIn = nodeList.get(j);
//						String payload = nodeList.get(k);
//						nodePairs.add(randIn+","+payload);
//					}
//				}
//			}
//			
//			String randIn = "";
//			String payload = "";
//			while(nodePairs.size()>0){
//				int randPairInd = rn.nextInt(nodePairs.size());
//				randIn = nodePairs.get(randPairInd).split(",",2)[0];
//				payload = nodePairs.get(randPairInd).split(",",2)[1];
//				if ((faninNodeOfNode.get(payload).contains(randIn))|(faninNodeOfNode.get(payload).contains(trigger))) {
//					// Combinational loop between random nodes
//					nodePairs.remove(randPairInd); 
//					randIn = "";
//					payload = "";
//				}
//			}
//			if ((randIn == "")|(payload == "")){
//				System.err.println("No independent pair of node found. Terminating program...");
//				return;
//			}
			
			
			boolean tryNextPair = true;
			String randIn = "";
			String payload = "";
			while (tryNextPair) {
				// Randomly select second input
				int index1 = rn.nextInt(faninNodeOfNode.keySet().size());
				randIn = nodeList.get(index1);
				// Randomly select output (payload)
				int index2 = rn.nextInt(faninNodeOfNode.keySet().size());
				payload = nodeList.get(index2);
				tryNextPair = false; // No more search needed 
//				if (faninNodeOfNode.get(randIn).contains(payload)) {
//					tryNextPair = true; // Combinational loop between random nodes
//				}
				if (faninNodeOfNode.get(payload).contains(randIn)) {
					tryNextPair = true; // Combinational loop between random nodes
				}
				if (faninNodeOfNode.get(payload).contains(trigger)) {
					tryNextPair = true; // Combinational loop between random nodes
				}
//				if (faninNodeOfNode.get(trigger).contains(payload)
//						| faninNodeOfNode.get(payload).contains(trigger)) {
//					tryNextPair = true; // Combinational loop between trigger and output (payload)
//				} 
			} 
			
			/*
			 * TROJAN INSERTION IN FILE
			 * ----------------------------------------------------------------------
			 */
			String triggerReplaceNode = "newNodeB"+i;
			// Insert new payload gate
			String triggerPayloadNode = "newNodeA" + i;
			String triggerGateName = "newGateA" + i;
			String line = "AND2X1  "+triggerGateName+" (.A("+trigger+"), .B("+randIn+"), .Y("+triggerPayloadNode+"));";
			netlist.add(endModuleTracker, line);
			endModuleTracker++;
			if (debug) System.out.println("Trigger gate  = "+line);
			
			
			
			// Modify existing payload coupling gate
			for (int j = 0; j < netlist.size(); j++) {
				if (netlist.get(j).contains(payload)) { // narrowing searchspace
					String otpt = "";
					// pick output
					if (netlist.get(j).contains(".Y")) {
						otpt = netlist.get(j).split(".Y\\(",2)[1].split("\\)",2)[0];
					}
					if (netlist.get(j).contains(".Q" ) && !netlist.get(j).contains(".QN")) {
						otpt = netlist.get(j).split(".Q\\(",2)[1].split("\\)",2)[0];
					}
					if (netlist.get(j).contains(".QN") && !netlist.get(j).contains(".Q" )) {
						otpt = netlist.get(j).split(".QN\\(",2)[1].split("\\)",2)[0];
					}
					if (netlist.get(j).contains(".Q" ) &&  netlist.get(j).contains(".QN")) {
						otpt = netlist.get(j).split(".Q\\(",2)[1].split("\\)",2)[0];
					}
					if (netlist.get(j).contains(".S" ) && !netlist.get(j).contains(".CO")) {
						otpt = netlist.get(j).split(".S\\(",2)[1].split("\\)",2)[0];
					}
					if (netlist.get(j).contains(".CO") && !netlist.get(j).contains(".S" )) {
						otpt = netlist.get(j).split(".CO\\(",2)[1].split("\\)",2)[0];
					}
					if (netlist.get(j).contains(".S" ) &&  netlist.get(j).contains(".CO")) {
						otpt = netlist.get(j).split(".S\\(",2)[1].split("\\)",2)[0];
					}
					
					if(otpt.equals(payload)){  // found gate where to insert payload
						String origLine = netlist.get(j);
						if (debug) System.out.println("Existing gate = "+origLine);
						
						String modLine = netlist.get(j).split(otpt,2)[0] + triggerReplaceNode + netlist.get(j).split(otpt,2)[1];
						if (debug) System.out.println("Modified gate = "+modLine);
						
						netlist.remove(j);
						netlist.add(j, modLine);
						break;						
					}
				}
			}
					
			String payloadGateName = "newGateB" + i;
			line = "XOR2X1  "+payloadGateName+" (.A("+triggerPayloadNode+"), .B("+triggerReplaceNode+"), .Y("+payload+"));";
			netlist.add(endModuleTracker, line);
			endModuleTracker++;
			if (debug) System.out.println("Payload gate  = "+line);
		}
//		for (int j = 0; j < netlist.size(); j++) {
//			System.out.println(netlist.get(j));
//		}
		
		/*
		 * module rename
		 */
		int moduleStart = na.moduleTracker.get(0);
		String line = netlist.get(moduleStart);
		String modName = line.split("\\s+",2)[1].split("\\(",2)[0].trim();
//		line = line.split(modName, 2)[0]+modName+"_tr"+numberOfTrojans+line.split(modName, 2)[1];
		line = line.split(modName, 2)[0]+modName+"_tr"+line.split(modName, 2)[1];
		netlist.remove(moduleStart);
		netlist.add(moduleStart, line);
		
		rw.fileWriter(netlist, inputFileName.replace(".v","_tr"+numberOfTrojans+".v"));
		System.out.println("File created "+inputFileName.replace(".v","_tr"+numberOfTrojans+".v"));
		
		TrojanDetectionTestbench ttb = new TrojanDetectionTestbench(inputFileName, 10000);
	}
	
	private ArrayList<String> sortByValue( Map<String,Float> map ){
	    List<Map.Entry<String,Float>> list = new LinkedList<>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<String,Float>>(){
	    	public int compare( Map.Entry<String, Float> o1, Map.Entry<String, Float> o2 ){
	            return (o1.getValue()).compareTo( o2.getValue() );
	        }
	    } );	
	    ArrayList<String> result = new ArrayList<String>();
	    for (Map.Entry<String,Float> entry : list){
	    	result.add(entry.getKey());
	    }
	    return result;
	}
	
	public static void main(String[] args) {
		if(args.length>2){
			TrojanRandom tr = new TrojanRandom(args[0], args[1], Integer.parseInt(args[2]));
		} else{
			System.out.println("usage: java -jar TrojanRandom <inputFileName> <probabilityFileName> <numberOfTrojan>");
		}
	}

}
