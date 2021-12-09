package Extra;
import java.util.ArrayList;
import java.util.HashMap;

import Analysis.ReadWrite;


public class Read_Probablt {

	/**
	 * @param args
	 */
	boolean errorFlag = false;
	HashMap<String, Float> probabilityOfOne = new HashMap<String, Float>();
	ArrayList<String> toWriteProb = new ArrayList<String>();
	boolean debug = false;
	
	public Read_Probablt(String inputFileName) {
		// TODO Auto-generated constructor stub
		ReadWrite rw = new ReadWrite();
		ArrayList<String> inputLines = rw.fileReader(inputFileName);
		errorFlag = rw.errorFlag;
		probabilityOfOne = convertValue(inputLines);
		if (debug) {
			System.out.println(probabilityOfOne.entrySet());
		}

		String outputFileName;
		if (rw.parentPath != null) {
			outputFileName = rw.parentPath + "/P(1)_" + rw.name + ".txt";
		} else {
			outputFileName = "P(1)_" + rw.name;
		}
		rw.fileWriter(toWriteProb, outputFileName);
		System.out.println("Created file named " + outputFileName);
	}
	
	private HashMap<String, Float> convertValue (ArrayList<String> lines){
		HashMap<String, Float> prob = new HashMap<String, Float>();
		boolean flag = false;
		// toWriteProb.add("* Node P(1) P(0)");
		for (int i = 2; i < lines.size()-1; i++) {
			if (lines.get(i-2).trim().split(" ",2)[0].equals("Net")){
				flag = true;
			}
			if (flag) {
				String nodeName = lines.get(i).trim().split(" ", 2)[0];
				String probblt = lines.get(i).trim().split(" ", 2)[1].trim()
						.split(" ", 2)[1].trim().split(" ", 2)[0];
				float probOfOne = Float.parseFloat(probblt);
				if (!nodeName.startsWith("*")) {
					prob.put(nodeName, probOfOne);
					// float probOfZero = 1 - probOfOne;
					// toWriteProb.add(nodeName + " " + probblt + " " + probOfZero);
					toWriteProb.add(nodeName + " " + probblt);
				}
			}
			if (flag & (lines.get(i+1).trim().startsWith("-"))){
				flag = false;
			}
		}			
		return prob;
	}
	
	public static void main(String[] args) {
		Read_Probablt prb = new Read_Probablt(args[0]);
	}

}
