package KITmain;
import java.util.ArrayList;

import Analysis.ReadWrite;

public class KITMUX {

	public KITMUX(){
		
	}
	
	public KITMUX(String inputFileName, int keySize, String methodCode){
//		String inputFileName = "c3540.v";
		String outputFileName = inputFileName.replace(".v","")+"-"+ methodCode + keySize + "1"+".v";
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlist = rw.fileReader(inputFileName);
		rw.fileWriter(netlist, outputFileName);
		KeyInsert ki = new KeyInsert();
		for (int i = 0; i < keySize; i++) {
			ki.keyInputName = "keyinput" + i;
			ki.keyTypeRatio = (float) 0.0;
			ki.debug = false;
			ki.generateOutputFiles = false;
			ki.writeLogFile = false;
			ki.modnameReplace = false;
			ki.keyInserter(outputFileName, "1", methodCode);
			netlist = ki.obfuscatedNetlist;
			rw.fileWriter(netlist, outputFileName);
		}
	}
	public static void main(String[] args) {
		if(args.length<3){
			System.out.println("Usage: <inputFileName> <keySizeInBits> <methodCode>");
		}

		String inputFileName = args[0];
		int keySize = Integer.parseInt(args[1]);
		String methodCode = args[2];
		
		KITMUX muxKI = new KITMUX(inputFileName, keySize, methodCode);
		
		
	}

}
