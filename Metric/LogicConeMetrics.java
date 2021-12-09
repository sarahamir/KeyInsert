package Metric;

import java.util.ArrayList;

import Analysis.FaninAndFanout;
import Analysis.ReadWrite;

public class LogicConeMetrics {
//	public LogicConeMetrics (){}
	
	public LogicConeMetrics (String inputFileName){
		ReadWrite rw = new ReadWrite();
		ArrayList<String> file = rw.fileReader(inputFileName);
		FaninAndFanout fnf = new FaninAndFanout(file);
//		fnf.detailedGates;
//		fnf.faninGatesOfGate;
//		fnf.fanoutGatesOfGate;
//		System.out.println(fnf.listOfGates);
		int sum1 = 0; int sum2 = 0;
		for (int i = 0; i < fnf.listOfGates.size(); i++) {
			sum1 = sum1 + fnf.faninGatesOfGate.get(fnf.listOfGates.get(i)).size();
			sum2 = sum2 + fnf.fanoutGatesOfGate.get(fnf.listOfGates.get(i)).size();
		}
		float avgFaninFactor = (float)sum1/(float)(fnf.listOfGates.size())/(float)(fnf.listOfGates.size());
		float avgFanoutFactor = (float)sum2/(float)(fnf.listOfGates.size())/(float)(fnf.listOfGates.size());
		float avgFactor = (float)(sum1+sum2)/(float)(fnf.listOfGates.size())/(float)(fnf.listOfGates.size());
		System.out.println("Avg normalized cone size of "+inputFileName+": "+avgFactor);	
		rw.fileWriter("Avg normalized cone size : "+avgFactor, inputFileName.replace(".v","_ConeMetric.txt"));
		System.out.println("File created : " + inputFileName.replace(".v","_ConeMetric.txt"));
	} 
	
	public static void main(String[] args) {
		if(args.length>0){
			LogicConeMetrics met = new LogicConeMetrics (args[0]);
		}
//		String[] files = {"c17.v","c432.v","c499.v","c880.v","c1355.v","c1908.v","c2670.v","c3540.v","c5315.v","c6288.v","c7552.v"};
//		for (int i = 0; i < files.length; i++) {
//			LogicConeMetrics met = new LogicConeMetrics (files[i]);
//		}
	}

}
