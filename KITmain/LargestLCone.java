package KITmain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import Analysis.FaninAndFanout;
import Analysis.ReadWrite;

public class LargestLCone {
	
	public LargestLCone (String inputFileName){
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlist = rw.fileReader(inputFileName);
//		NetlistAnalyzer na = new NetlistAnalyzer(netlist);
		FaninAndFanout fnf = new FaninAndFanout(netlist);
		int totalGates = fnf.detailedGates.size();		
		HashMap<String, List<String>> faninGatesOfGate = fnf.faninGatesOfGate;
		HashMap<String, List<String>> fanoutGatesOfGate = fnf.fanoutGatesOfGate;
		HashMap<String, Integer> faninSizes = new HashMap<String, Integer>();
		HashMap<String, Integer> fanoutSizes = new HashMap<String, Integer>();
		for (String gate : faninGatesOfGate.keySet()) {
			faninSizes.put(gate,faninGatesOfGate.get(gate).size());
			fanoutSizes.put(gate, fanoutGatesOfGate.get(gate).size());
		}
		ArrayList<Integer> sortedFIsize = sortByValue(faninSizes);
		ArrayList<Integer> sortedFOsize = sortByValue(fanoutSizes);
		

		int maxFIsize = sortedFIsize.get(sortedFIsize.size()-1);
		int maxFOsize = sortedFOsize.get(sortedFOsize.size()-1);
		float FImetric = (float)maxFIsize/(float)totalGates;
		float FOmetric = (float)maxFOsize/(float)totalGates;
		System.out.println("Largest FI/Gates = " + FImetric);
		System.out.println("Largest FO/Gates = " + FOmetric);
	}

	private ArrayList<Integer> sortByValue( Map<String,Integer> map ){
	    List<Map.Entry<String,Integer>> list = new LinkedList<>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<String,Integer>>(){
	    	public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 ){
	            return (o1.getValue()).compareTo( o2.getValue() );
	        }
	    } );	
	    ArrayList<Integer> result = new ArrayList<Integer>();
	    for (Map.Entry<String, Integer> entry : list){
	    	result.add(map.get(entry.getKey()));
	    }
	    return result;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LargestLCone llc = new LargestLCone(args[0]);

	}

}
