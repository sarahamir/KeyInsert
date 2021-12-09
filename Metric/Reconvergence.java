package Metric;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Analysis.FaninAndFanout;
import Analysis.NetlistAnalyzer;
import Analysis.ReadWrite;

public class Reconvergence {
	
	public float reconvergence = (float)0;
	
	public Reconvergence(){} // blank constructor
	
	public Reconvergence (String inputFileName){
		computeRecon(inputFileName);
	}

	public void computeRecon(String inputFileName) {
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlist = rw.fileReader(inputFileName);
		NetlistAnalyzer na = new NetlistAnalyzer(netlist);
		ArrayList<String> gates = na.gates;
		FaninAndFanout  fnf = new FaninAndFanout(netlist);
		HashMap<String, List<String>> fanoutGatesOfGate = fnf.fanoutGatesOfGate;		
		HashMap<String, List<String>> immediateFanoutGates = fnf.immediateFanoutGates;
		HashMap<String, List<String>> immediateFaninGates = fnf.immediateFaninGates;
		HashMap<String, Float> reconv = new HashMap<String, Float>();
		float totalRecon = (float)0;
		for (String gate : gates) {
			int fanoutSize = fanoutGatesOfGate.get(gate).size();
			List<String> immFO = immediateFanoutGates.get(gate);
			ArrayList<String> reconvergingGates = new ArrayList<String>();
			Float fl = (float)0; 
			if((immFO.size() != 0) & (immFO.size() != 1)){
				//System.out.println("For " + gate + ", size of immFO = " + immFO.size());
				for (int i = 0; i < immFO.size()-1; i++) {
					List<String> fo1 = fanoutGatesOfGate.get(immFO.get(i));
					for (int j = i+1 ; j < immFO.size(); j++) {
						List<String> fo2 = fanoutGatesOfGate.get(immFO.get(j));
						for (String gate1 : fo1) {
//							if ( (fo2.contains(gate1)) & (!commonGates.contains(gate1)) ){
//								commonGates.add(gate1);
//							}
							List<String> immfaningate1 = immediateFaninGates.get(gate1); // inputs comes from these gates
							if (immfaningate1.size()>1){ // single input gates, like not, cannot be converging gate
								// atleast one of immfaningate1 exists in fo1 or =immFO.get(i) & not in fo2 && one of immfaningate1 exists in fo2 or =immFO.get(j) & not in fo1
								int flag1 = 0;
								int flag2 = 0;
								for (int k = 0; k < immfaningate1.size(); k++) {
									if (((immfaningate1.get(k).equals(immFO.get(i)))|(fo1.contains(immfaningate1.get(k))))&!(fo2.contains(immfaningate1.get(k)))){
										flag1 = 1;
									}
									if (((immfaningate1.get(k).equals(immFO.get(j)))|(fo2.contains(immfaningate1.get(k))))&!(fo1.contains(immfaningate1.get(k)))){
										flag2 = 1;
									}
								}
								if (flag1 == 1 & flag2 ==1){
									if (! reconvergingGates.contains(gate1)) {
										reconvergingGates.add(gate1);
									}
								}
							}
						}
					}
				}
				fl = (float) reconvergingGates.size() / (float) fanoutSize;
			} else if (immFO.size() == 1) {
				fl = (float) 0; // TODO : What should be this value?
			} else if (immFO.size() == 0){
				fl = (float) 0;
			}
			reconv.put(gate, fl);
			totalRecon += fl;
		}
		reconvergence = totalRecon / (float) gates.size();
		System.out.println("reconvergence = " + reconvergence);
		rw.fileWriter("Reconvergence of "+ inputFileName + " = " + reconvergence, inputFileName.replace(".v","")+"_recon.txt");
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Reconvergence reco = new Reconvergence(args[0]);
	}

}
