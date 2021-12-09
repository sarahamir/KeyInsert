package Metric;
import java.util.ArrayList;

import Analysis.ReadWrite;


public class HammingDistance {
	/**
	 * @param inputFileName
	 */
	public HammingDistance(String inputFileName) {
		
		ReadWrite rw = new ReadWrite();
		ArrayList<String> lines = rw.fileReader(inputFileName);
		String fileName = rw.name;
		String parentPath = rw.parentPath;
		
		float totalBit  = 0;
		float highBit = 0;
		for (String line : lines) {
			if (!((line.trim().startsWith("/*"))|(line.trim().startsWith("*"))|(line.trim().startsWith("//")))) {
				for (int i = 0; i < line.length(); i++) {
					if (! line.substring(i,i+1).equals("x")) {
						highBit += Integer.parseInt(line.substring(i, i + 1));
						totalBit++;
					}
				}
			}			
		}
		
		float hd = (Math.round(highBit/totalBit*100*100))/100;
		System.out.println("total = " + totalBit + ", ones = " + highBit + ", HD = " + hd + "%");
		
		// Write output file
		
		String outputFileName =  "hd_" + fileName;
		if (parentPath != null) {
			outputFileName = parentPath + "/" + outputFileName;
		}
		String lineToWrite = "total = " + totalBit + ", ones = " + highBit + ", HD = " + hd + "%";
		rw.fileWriter(lineToWrite, outputFileName);  		
  		if (rw.errorFlag) {
			System.out.println("Created " + outputFileName);
		}
	}
	
	public static void main(String[] args) {
		HammingDistance hd = new HammingDistance(args[0]);
	}

}
