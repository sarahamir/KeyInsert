package Metric;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import Analysis.FaninAndFanout;
import Analysis.NetlistAnalyzer;
import Analysis.ReadWrite;

public class ReconVersion2 {
	
    ArrayList<Integer> sortedValues = new ArrayList<Integer>();
    ArrayList<String> sortedGates = new ArrayList<String>();
	public float reconvergence = (float)0;
	boolean debug = false;
	int n=0;
	public ReconVersion2(){} // blank constructor
	
	public ReconVersion2 (String inputFileName){
		computeRecon(inputFileName);
	}

	public void computeRecon(String inputFileName) {
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlist = rw.fileReader(inputFileName);
		NetlistAnalyzer na = new NetlistAnalyzer(netlist);
		String moduleName = na.moduleNames.get(0);
		if (debug) {
			System.out.println("Rv2.NA");
		}
		ArrayList<String> gates = na.gates;
		FaninAndFanout  fnf = new FaninAndFanout(netlist);
		if (debug) {
			System.out.println("Rv2.FN");
		}
//		HashMap<String, List<String>> fanoutGatesOfGate = fnf.fanoutGatesOfGate;
		HashMap<String, List<String>> faninGatesOfGate = fnf.faninGatesOfGate;
//		HashMap<String, List<String>> immediateFanoutGates = fnf.immediateFanoutGates;
		HashMap<String, List<String>> immediateFaninGates = fnf.immediateFaninGates;
		HashMap<String, Float> reconv = new HashMap<String, Float>();
		float totalRecon = (float)0;
		
		if (debug) {
			System.out.println("Rv2.P1");
		}
		
		for (String gate : gates) {
//			int fanoutSize = fanoutGatesOfGate.get(gate).size();
			int faninSize = faninGatesOfGate.get(gate).size();
//			List<String> immFO = immediateFanoutGates.get(gate);
			List<String> immFI = immediateFaninGates.get(gate);
			Float fl = (float)0; 
			if((immFI.size() != 0) & (immFI.size() != 1)){
				ArrayList<String> commonGates = new ArrayList<String>();
				for (int i = 0; i < immFI.size()-1; i++) {
					for (int j = i+1; j < immFI.size(); j++) { // These 2 for loops are just doing nC2
						List<String> commonsFound = findCommonEntries(faninGatesOfGate.get(immFI.get(i)), faninGatesOfGate.get(immFI.get(j)));
						for (String commonEntry : commonsFound) {
							if ( ! commonGates.contains(commonEntry)){
								commonGates.add(commonEntry);
							}
						}
					}
				}	
//				System.out.println(commonGates.size() + " / " + faninSize);
				fl = (float) commonGates.size() / (float) faninSize; // if I devide by (float) fanoutGatesOfGate.get(commonEntry).size() individually and add, I would get original reconvergence
			} else if (immFI.size() == 1) {
				fl = (float) -1; // TODO : What should be this value?
			} else if (immFI.size() == 0){
				fl = (float) -1;
			}
			
			reconv.put(gate, fl);
			if (fl != -1) {
				totalRecon += fl;
				n++;
			}
		}
		
		if (debug) {
			System.out.println("Rv2.P2");
		}
		
//		System.out.println(totalRecon +" / "+ (float) gates.size());
		reconvergence = totalRecon / ((float) gates.size()-n);
		System.out.println("reconvergence = " + reconvergence);
		rw.fileWriter("Reconvergence of "+ inputFileName + " = " + reconvergence, inputFileName.replace(".v","")+"_recon.txt");
		
		float number = 100000;
		HashMap<String,Integer> extendedReconv = new HashMap<String, Integer>();
		for (String key : reconv.keySet()) {
			extendedReconv.put(key, Math.round(reconv.get(key)*number));
		}
		sortByValue(extendedReconv);
//		System.out.println(sortedExtendedReconv);
		float maxReconv = (float) sortedValues.get(sortedValues.size()-1) / number;
		System.out.println("Maximum Reconvergence = " + maxReconv);
		
		int noOfTopNodes = 10;
		ArrayList<Float> topReconv = new ArrayList<Float>();
		for (int i = 0; i < noOfTopNodes; i++) {
			topReconv.add((float) sortedValues.get(sortedValues.size()-1-i) /number);
		}
		System.out.println("Top "+ noOfTopNodes + " high Reconvergence values: " + topReconv);
		
		ArrayList<String> allReconv = new ArrayList<String>();
		for (int i = 0; i < sortedValues.size(); i++) {
			float aR = (float) sortedValues.get(sortedValues.size()-1-i) /number;
			if (aR != (float) -1) {
				allReconv.add("" + aR);
			}
		}
		rw.fileWriter(allReconv, inputFileName.replace(".v","")+"_reconAll.txt");
		ArrayList<String> hiReconGates = new ArrayList<String>();
		for (int i = sortedGates.size()-1; i >= 0 ; i--) {
			hiReconGates.add(moduleName + " " + sortedGates.get(i));
		}
		rw.fileWriter(hiReconGates, inputFileName.replace(".v","")+"_hiReconGates.txt");
	}
	
	public List<String> findCommonEntries (List<String> list1, List<String> list2){
		ArrayList<String> commons = new ArrayList<String>();
		for (String entry : list1) {
			if (list2.contains(entry)){
				commons.add(entry);
			}
		}		
		return commons;
	}
	
	private void sortByValue( Map<String,Integer> map ){
	    List<Map.Entry<String,Integer>> list = new LinkedList<>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<String,Integer>>(){
	    	public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 ){
	            return (o1.getValue()).compareTo( o2.getValue() );
	        }
	    } );	
	    	
	    for (Map.Entry<String, Integer> entry : list){
	    	sortedValues.add(map.get(entry.getKey()));
	    	sortedGates.add(entry.getKey());
	    }
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ReconVersion2 reco = new ReconVersion2(args[0]);
	}

}
