package Trojan;

import java.util.ArrayList;

import Analysis.ReadWrite;

public class TrojanSimDetect {
	public TrojanSimDetect() {
		// TODO Auto-generated constructor stub
	}
	
	public TrojanSimDetect(String inputFileName) {
		ReadWrite rw = new ReadWrite();
		ArrayList<String> file = rw.fileReader(inputFileName);
		int totalCount = 0;
		int patternCount = 0;
		int bitCount = 0;
		int patternSize = 0;
		for (int i = 4; i < file.size(); i++) {
			if (file.get(i).contains("0")|file.get(i).contains("1")|!file.get(i).contains("x")) {
				totalCount++;
				patternSize = file.get(i).length();
				if (file.get(i).contains("1")) {
					// Pattern detected Trojan
					patternCount++;
					for (int j = 0; j < file.get(i).length(); j++) {
						if (file.get(i).substring(j,j+1).equals("1")) {
							bitCount++;
						}
					}
				} 
			}
		}
		System.out.println(inputFileName.replace(".v","")+": Trojan detected patterns: "+patternCount+"/"+totalCount+" , Bits faulted: "+bitCount+"/"+totalCount*patternSize);
	}

	public static void main(String[] args) {
		if (args.length>0) {
			TrojanSimDetect tsd = new TrojanSimDetect(args[0]);
		}

	}

}
