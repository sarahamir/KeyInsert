/**
 * 
 */
package Metric;

import java.util.ArrayList;

import Analysis.FaninAndFanout;
import Analysis.ReadWrite;

/**
 * @author Sarah Amir
 *
 */
public class Recon3 {

	/**
	 * @param args
	 */
	
	public Recon3(){}
	
	public Recon3(String inputFileName){
		ReadWrite rw = new ReadWrite();
		ArrayList<String> file = rw.fileReader(inputFileName);
		FaninAndFanout fnf = new FaninAndFanout(file);
//		fnf.detailedGates;
//		fnf.faninGatesOfGate;
//		fnf.fanoutGatesOfGate;
		
	}
	
	public static void main(String[] args) {
		if(args.length>0){
			Recon3 rec = new Recon3(args[0]);
		}

	}

}
