package Metric;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Analysis.FaninAndFanout;
import Analysis.ReadWrite;

public class Connectivity {
	boolean errorFlag = false;
	
	public Connectivity(String inputFileName) {
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlistLines = rw.fileReader(inputFileName);
		errorFlag = rw.errorFlag;

		FaninAndFanout fnf = new FaninAndFanout(netlistLines);
		HashMap<String, List<String>> faninGatesOfGate = fnf.faninGatesOfGate;
		HashMap<String, List<String>> fanoutGatesOfGate = fnf.fanoutGatesOfGate;
		
		HashMap<String, Float> normalizedConn = new HashMap<String, Float>();
		ArrayList<String> toWrite = new ArrayList<String>();
		int noOfGate = faninGatesOfGate.keySet().size();
		float sum = 0;
		for (String gate : faninGatesOfGate.keySet()) {
			int faninSize = faninGatesOfGate.get(gate).size();
			int fanoutSize = fanoutGatesOfGate.get(gate).size();
			int totalConeSize = faninSize + fanoutSize;
			float normalized = (float)totalConeSize / (float)noOfGate;
			System.out.println(gate + " : " + normalized);
			normalizedConn.put(gate, normalized);
			toWrite.add(gate + "," + normalized);
			sum = sum + normalized;
		}
		
		float mean = sum/noOfGate;
		
		// stander daviation:
		sum = 0;
		for (String gate : normalizedConn.keySet()) {
			sum = sum + ( (normalizedConn.get(gate) - mean ) * (normalizedConn.get(gate) - mean ) );
		}
		float sd = (float) Math.sqrt(sum);
		
		toWrite.add("Mean = " + mean );
		toWrite.add("SD = " + sd);
		System.out.println("Mean = " + mean );
		System.out.println("SD = " + sd);
		
		rw.fileWriter(toWrite, inputFileName + "_connectivity.csv");
	}

	public static void main(String[] args) {
		Connectivity cnvt = new Connectivity(args[0]);
	}

}
