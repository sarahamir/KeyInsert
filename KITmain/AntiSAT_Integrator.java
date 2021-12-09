package KITmain;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.xml.crypto.dsig.keyinfo.KeyInfo;

import Analysis.NetlistAnalyzer;
import Analysis.PortFinder;
import Analysis.ReadWrite;
import website.PHPmodifierForTrustHub;

public class AntiSAT_Integrator {
	String version = "0";
	private ArrayList<Integer> numberOfGatesInModules = new ArrayList<Integer>();
	private ArrayList<String> moduleNames = new ArrayList<String>();
//	private int numberOfModules = 0;
	private int numberOfGates = 0;
	private HashMap<String, ArrayList<String>> gatesInModule = new HashMap<String, ArrayList<String>>();
	public boolean debug = true;
	public boolean errorFlag = false;
	public boolean generateFiles = true;
	public boolean writeLogFile = true;
	public boolean tb = false;
	public boolean readme = true;
	public boolean writePHP = false;
	String key = "";
	String key1 = "";
	String key2 = "";
	public float keyRatioOriginalByAll = (float)0.75;
	public float keyRatioNTgenByNTObf = (float)0.5;
	public ArrayList<String> logfile = new ArrayList<String>();
	public String outputFileName = "";
	private String parentpath = null;
	public String outputDirectory = "";
	static public float keyType = (float) 1.0;
	
	public AntiSAT_Integrator(){} // null constructor
	
	public AntiSAT_Integrator(String inputFileName, String moduleName, String methodName, String keySizeForOrgn, String keySizeForAntiSAT){
//		String keyInfo = "" + (Integer.parseInt(keySizeForAntiSAT)+Integer.parseInt(keySizeForOrgn));
		key1 = keySizeForOrgn;
		RandomKeyGenerator randKey = new RandomKeyGenerator(Integer.parseInt(keySizeForAntiSAT));
		key2 = randKey.key;
		//key = key1 + key2; // TODO
		integratorBasic(inputFileName, moduleName, methodName);	
	}
	
	public AntiSAT_Integrator(String inputFileName, String moduleName, String methodName, String keyInfo){
		callFromGui(inputFileName, moduleName, methodName, keyInfo);	
	}

	public void callFromGui(String inputFileName, String moduleName, String methodName, String keyInfo) {
		keyProcessing(inputFileName, keyInfo);
		integratorBasic(inputFileName, moduleName, methodName);
	}

	private void keyProcessing(String inputFileName, String keyInfo) {
		ReadWrite rw = new ReadWrite();
		ArrayList<String> nl = rw.fileReader(inputFileName);
		parentpath = rw.parentPath;
 		NetlistAnalyzer na = new NetlistAnalyzer(nl);
 		numberOfGates = na.numberOfGates;
		if (keyInfo.contains(".txt")) {
			key = readKeyFile(keyInfo);
			//int key1Size = (int)((float)key.length()*keyRatioOriginalByAll); // TODO
			int key1Size = key.length();
			if (numberOfGates < key1Size){
				key1Size = numberOfGates;			
			}
			int key2Size = key.length() - key1Size;
			String key1b = key.substring(0, key1Size);
			key1 = "org_" + keyInfo;
			rw.fileWriter(key1b, key1);
			key2 = key.substring(key1Size, key1Size+key2Size);
		} else if (keyInfo.contains("%")) {
			String keyPercent = keyInfo.replace("%", "");
			float percent = Float.parseFloat(keyPercent);
			int keyBits = (int) Math.round((float) numberOfGates * percent / 100);
			//int key1Size = (int)((float)keyBits*keyRatioOriginalByAll); //TODO
			int key1Size = keyBits;
			if (numberOfGates < key1Size){
				key1Size = numberOfGates;			
			}
			key1 = "" + key1Size;
			int key2Size = keyBits - key1Size;		// ??	
			RandomKeyGenerator keygen = new RandomKeyGenerator(key2Size);
			key2 = keygen.key;
		} else {
			int keyBits = Integer.parseInt(keyInfo);
			//int key1Size = (int)((float)keyBits*keyRatioOriginalByAll); // TODO:
			int key1Size = keyBits;
			if (numberOfGates < key1Size){
				key1Size = numberOfGates;			
			}
			key1 = "" + key1Size;
			int key2Size = keyBits - key1Size;
			RandomKeyGenerator keygen = new RandomKeyGenerator(key2Size);
			key2 = keygen.key;		
		}
	}

	private void integratorBasic(String inputFileName, String moduleName, String methodName) {
		ReadWrite rw = new ReadWrite();
		ArrayList<String> netlist = rw.fileReader(inputFileName);
		//FaninAndFanout fnf0 = new FaninAndFanout(netlist);
		PortFinder pf0 = new PortFinder(netlist);
		KeyInsert ki = new KeyInsert();
		ki.keyTypeRatio = keyType;
		ki.debug = false; 
		ki.generateOutputFiles = false;
		ki.writeLogFile = false;
		errorFlag = ki.errorFlag;
//		ki.modnameReplace = false;
		ki.keyInserter(inputFileName, key1, methodName);
		key1 = ki.key;
		netlist = ki.obfuscatedNetlist;
		// TODO : implement flip gate
		
		// randomly choose an output. from end, look for endmodule. then insert a new xor gate.
			
		for (String string : ki.logfile) {
			logfile.add(string);
		}
//		rw.fileWriter(netlist, inputFileName+"_ches16a.v");
		if (debug) {
			System.out.println("Step 1 : " + " keys inserted in original circuit.");
		}
		
		/*
		 * Calling AntiSAT
		 */
		//AntiSAT nts = new AntiSAT(netlist, moduleName, key2.substring(0,keySizeNTsatGen)); // fixed size key
		AntiSAT nts = new AntiSAT(netlist, moduleName); // PI size key
		key2 = nts.AntiSATkey;
		netlist = nts.modifiedNetlistLines;	
		int NTsatModuleStartIndex = nts.NTsatModuleStartIndex;
//		rw.fileWriter(netlist, inputFileName+"_ches16b.v");
		if (debug) {
			System.out.println("Step 2 : " + key2.length() + " key AntiSAT block inserted with " + moduleName + " ");
		}
//		rw.fileWriter(netlist, inputFileName+"_ches16.v");
		
		
		NetlistAnalyzer na = new NetlistAnalyzer(netlist);
		numberOfGatesInModules = na.numberOfGatesInModules;
		// TODO : include subcircuit gates to main circuit list
//		numberOfModules = na.numberOfModules;
		moduleNames = na.moduleNames;
//		numberOfGates = na.numberOfGates;
		gatesInModule.putAll(na.gatesInModule);
		
		String antiSATmoduleName = "AntiSAT";
		rw.fileWriter(netlist, "temp_"+inputFileName);
		int keySizeNTsatGen = (int)(keyRatioNTgenByNTObf*(float)(key2.length())); //TODO
		int keySizeNTsatObf = key2.length() - keySizeNTsatGen;
		ArrayList<String> keyPos = randomGatePositionSelector(keySizeNTsatObf , antiSATmoduleName);
		rw.fileWriter(keyPos, "antiSATkeyPos.txt");
		//System.out.println("key2 = "+key2+" keySizeNTsatGen = "+keySizeNTsatGen + " keySizeNTsatObf = "+keySizeNTsatObf);
		//String key3 = key2.substring(keySizeNTsatGen,keySizeNTsatObf+keySizeNTsatGen); // TODO
		//rw.fileWriter(key3, "temp_NTobf_key3.txt");
//		KeyInsert ki2 = new KeyInsert("temp_"+inputFileName, randKey2.key, "antiSATkeyPos.txt", 1);
		KeyInsert ki2 = new KeyInsert();
		ki2.keyInputName = "keyNTin";
		ki2.keyTypeRatio = (float) 1.0;
		ki2.debug = false;
		ki2.generateOutputFiles = false;
		ki2.writeLogFile = false;
		ki2.modnameReplace = false;

		//ki2.keyInserter("temp_"+inputFileName, "temp_NTobf_key3.txt", "antiSATkeyPos.txt");
		ki2.keyInserter("temp_"+inputFileName, ""+keySizeNTsatObf, "antiSATkeyPos.txt");
		netlist = ki2.obfuscatedNetlist;
		String key3 = ki2.key;
		if (debug) {
			System.out.println("AntiSAT obfuscation key = " + key3);
		}
		for (String string : ki2.logfile) {
			logfile.add(string);
		}
		//antiSATmoduleName += "_Obf";
		if (debug) {
			System.out.println("Step 3 : " + key3.length() + " keys inserted in AntiSAT block.");
		}	
		
		key = key1 + key2 + key3;
		 
		// write a code to inter-connect two modules
		String outputMethodNaming = "";
		switch(methodName){
		case "RN": outputMethodNaming = "NR";
			break;
		case "SL": outputMethodNaming = "NS";
			break;
		case "CS": outputMethodNaming = "NC";
			break;
			default: outputMethodNaming = "NT";
		}
		int keySize = key.length();
		
		for (int i = 0; i < moduleNames.size(); i++) {
			if (moduleNames.get(i).equals(moduleName)){
				int index = na.moduleTracker.get(i);
				String line = netlist.get(index);
				String part1 = line.split("module",2)[0];
				String modulename = line.split("module",2)[1].split("\\(",2)[0].trim();
				String part3 = line.split("\\(",2)[1];
				modulename = inputFileName.replace(".v","") + "_" + outputMethodNaming + keySize + "0";
				line = part1 + "module " + modulename + " (" + part3; 
				netlist.remove(index);
				netlist.add(index, line);
			}
		}
		
//		while(!netlist.get(NTsatModuleStartIndex).startsWith("module")){
//			NTsatModuleStartIndex++;
//		}
//		String linexx = netlist.get(NTsatModuleStartIndex);
//		System.out.println(linexx);
//		String part1 = linexx.split("module",2)[0];
//		String NTmodulename = linexx.split("module",2)[1].split("\\(",2)[0].trim();
//		String part3 = linexx.split("\\(",2)[1];
//		NTmodulename = "AntiSAT_"+inputFileName.replace(".v","") + "_" + outputMethodNaming + keySize + "0";
//		linexx = part1 + "module " + NTmodulename + " (" + part3; 
//		netlist.remove(NTsatModuleStartIndex);
//		netlist.add(NTsatModuleStartIndex, linexx);
		
		//
		String outPath = "";
		if (parentpath != null){
			String name = rw.name;
			String outdirName = parentpath + "_Out/" + name + "/" + outputMethodNaming + "/" + keySize;
			File outDir = new File(outdirName);
			if(!outDir.exists()){
				outDir.mkdirs();
			}
			outPath = outdirName + "/";
		} else {
			String outdirName;
			if (outputDirectory.equals("")) {
				outdirName = "Generated_Files";
			} else {
				if ( ! outputDirectory.endsWith("/")) {
					outdirName = outputDirectory;
				} else {
					outdirName = outputDirectory.substring(0,outputDirectory.length()-2);
				}				
			}
			File outDir = new File (outdirName);
			if(!outDir.exists()){
				outDir.mkdir();
			}
			outPath = outdirName + "/";
		}
		if (outputFileName.equals("")) {
			outputFileName = inputFileName.replace(".v", "") + "-" + outputMethodNaming + keySize + version
					+ ".v";
		} else if ( ! outputFileName.contains(".v")){
			outputFileName += ".v";
		}
		key = key1+key2;
		if (!errorFlag & generateFiles) {
			String keyFileName = outputFileName.replaceAll(".v", "") + "_key.txt";
			rw.fileWriter(key, outPath + keyFileName);
			System.out.println("File created : " + outPath + keyFileName);
//			rw.fileWriter(netlist, outPath + outputFileName);
//			System.out.println("File created : " + outPath + outputFileName);
			String foldername = outputFileName.replaceAll(".v","");
			File outDir = new File(foldername);
			if(!outDir.exists()){
				outDir.mkdirs();
			}
//			rw.fileWriter(netlist, outputFileName);
			rw.fileWriter(netlist, foldername+"/"+outputFileName);
			System.out.println("File created : " + outputFileName);
		}
		//	
		logfile.add("File created : " + outPath + outputFileName);
		logfile.add("The antiSAT block key is "+ key2);
		if (tb) {
			TestbenchWriter testbench = new TestbenchWriter(outPath + outputFileName,	1000, 1000, key);
			System.out.println("Created testbench file " + outPath + "tb_"+outputFileName);
			logfile.add("Created testbench file " + outPath + "tb_"+outputFileName);
		}
		if (writeLogFile) {
			logfile.add("File created : " + outPath+"log_"+outputFileName.replaceAll(".v", ".txt"));
			rw.fileWriter(logfile, outPath+"log_"+outputFileName.replaceAll(".v", ".txt"));
			System.out.println("File created : " + outPath+"log_"+outputFileName.replaceAll(".v", ".txt"));
		}
		
		if (readme & generateFiles) {
			ArrayList<String> Read_Me = new ArrayList<String>();
			String line = "";
			Read_Me.add("");
			Read_Me.add(outputFileName.replaceAll(".v",""));
			Read_Me.add("_______________________________________");
			Read_Me.add("");
			Read_Me.add("DESCRIPTION:");
			line = "Original Circuit: ";
			String name = inputFileName.replaceAll(".v", "");
			if (name.equals("c432")|name.equals("c499")|name.equals("c880")|name.equals("c1355")|name.equals("c2670")
					|name.equals("c3540")|name.equals("c5315")|name.equals("c1908")|name.equals("c7552")|name.equals("c6288")){
				line += "ISCAS85 benchmark ";
			}
			line = line + name + " [1]";
			Read_Me.add(line);
			NetlistAnalyzer na2 = new NetlistAnalyzer(netlist);
			Read_Me.add("Number of Gates: " + na2.gates.size() );
			//FaninAndFanout fnf = new FaninAndFanout(netlist);
			PortFinder pf = new PortFinder(netlist);
			int n1 = pf.inputPorts.size();
			int n2 = pf0.inputPorts.size();
			Read_Me.add("Number of Input: " + n1 + "( " + n2 + " original inputs, " + (n1-n2) + " key ports)");
			Read_Me.add("Number of Output: " + pf.outputPorts.size());
			Read_Me.add("Key size = " + keySize);
			Read_Me.add("          ("+key1.length()+" keys to obfuscate original circuit,");
			Read_Me.add("           "+key2.length()+" key AntiSAT block,");
			Read_Me.add("           "+key3.length()+" keys to obfuscate AntiSAT block)");
			Read_Me.add("Key input naming: 'Key_In_[module number]_[input number in module]' for unsynthesized file and 'keyinput[input number]' for synthesized file");
			Read_Me.add("Synthesis Library: SAED90nm_typ Library (version 2013)");
			Read_Me.add("");
			Read_Me.add("");
			
			Read_Me.add("OBFUSCATION TECHNIQUE(S) IMPLEMENTED:");
			switch(methodName){
			case "RN": Read_Me.add("Obfuscation method: Random key gate insertion"); 
				break;
			case "SL": Read_Me.add("Obfuscation method: Secure logic locking [2]"); 
				break;
			case "CS": Read_Me.add("Obfuscation method: Logic cone size based [2]"); 
				break;
			}
			switch(outputMethodNaming){
			case "NR":
			case "NS":
			case "NC": Read_Me.add("SAT-Attack defense: AntiSAT [3]"); 
			}
			Read_Me.add("");
			Read_Me.add("");
			
			Read_Me.add("TAXONOMY:");
			line = "Obfuscation Method: Combinational -> Combinational Hybrid -> SAT Defense -> AntiSAT";
			switch(outputMethodNaming){
			case "NR": line += " -> Random"; 
				break;
			case "NS": line += " -> SLL"; 
				break;
			case "NC": line += " -> Logic cone size based"; 
				break;
			}
			Read_Me.add(line);
			
			line = "Physical Characteristics -> Obfuscated Netlist Size";
			if (na.gates.size()<1000){
				line += " -> #gates<1000";
			} else if (na.gates.size()>5000){
				line += " -> #gates>5000";
			} else {
				line += " -> 1000<#gates<5000";
			}
			Read_Me.add(line);
			line = "                            Key Size";
			switch(keySize){
//			switch(key1.length()){
			case 32: line += " -> 32 bit";
				break;
			case 64: line += " -> 64 bit";
				break;
			case 128: line += " -> 128 bit";
				break;
			case 256: line += " -> 256 bit";
				break;
				default: line += " -> Other";
			}
			Read_Me.add(line);
			Read_Me.add("");
			Read_Me.add("");
			
			Read_Me.add("CONTACT:");
			Read_Me.add("          Sarah Amir");
			Read_Me.add("          Florida Institute of Cyber Security (FICS)");
			Read_Me.add("          University of Florida");
			Read_Me.add("          sarah.amir@ufl.edu, prema_buet@gmail.com");
			Read_Me.add("");
			Read_Me.add("");
			
			Read_Me.add("REFERENCES:");
			if (name.equals("c432")|name.equals("c499")|name.equals("c880")|name.equals("c1355")|name.equals("c2670")
					|name.equals("c3540")|name.equals("c5315")|name.equals("c1908")|name.equals("c7552")|name.equals("c6288")){
				Read_Me.add("[1] ISCAS85 benchmarks http://www.pld.ttu.ee/~maksim/benchmarks/iscas85/verilog/");
			}
			switch(methodName){
			case "RN":
				break;
			case "SL": Read_Me.add("[2]	Yasin, Muhammad, et al. \"On improving the security of logic locking.\" IEEE Transactions on Computer-Aided Design of Integrated Circuits and Systems 35.9 (2016): 1411-1424");
				break;
			case "CS": Read_Me.add("[2] Narasimhan, Seetharam, Rajat Subhra Chakraborty, and Swarup Chakraborty. \"Hardware IP protection during evaluation using embedded sequential trojan.\" IEEE Design & Test of Computers 29.3 (2012): 70-79.");
				break;
			}
			switch(outputMethodNaming){
			case "NR": 
			case "NS": 
			case "NC": Read_Me.add("[3]	Xie, Yang, and Ankur Srivastava. \"Mitigating sat attack on logic locking.\" International Conference on Cryptographic Hardware and Embedded Systems. Springer Berlin Heidelberg, 2016."); 
			}

			String foldername = outputFileName.replaceAll(".v","");
			File outDir = new File(foldername);
			if(!outDir.exists()){
				outDir.mkdirs();
			}
			rw.fileWriter(Read_Me, foldername+"/"+"ReadMe_"+foldername+".txt");
			System.out.println("File created : " + "ReadMe_"+outputFileName.replace(".v",".txt"));
			
			if (writePHP) {
				PHPmodifierForTrustHub php = new PHPmodifierForTrustHub(outputFileName.replace(".v", ""), Read_Me);
			}
		}
		
	}
	
	private ArrayList<String> randomGatePositionSelector(int noOfKeyGate, String moduleName){
		ArrayList<String> selectedPositions = new ArrayList<String>();
		
		for (int i = 0; i < moduleNames.size(); i++) {
			if (moduleNames.get(i).equals(moduleName)) {
				int numberOfOriginalGates = numberOfGatesInModules.get(i).intValue();
//				float moduleWeight = Math.round((float) numberOfOriginalGates * (float) noOfKeyGate / (float) numberOfGates);
//				int noOfKeyGateInModule = (int) moduleWeight;
				int noOfKeyGateInModule = noOfKeyGate;
				// To generate random gate numbers
				Random randomGenerator = new Random();
				for (int j = 0; j < noOfKeyGateInModule; j++) {
					int randomGateNumber = randomGenerator.nextInt(numberOfOriginalGates);
					String newRandomGate = moduleNames.get(i) + " "
							+ gatesInModule.get(moduleNames.get(i)).get(randomGateNumber);
					if (!selectedPositions.contains(newRandomGate)) {
						selectedPositions.add(newRandomGate);
					} else {
						j--;
					}
				} 
			}
		}
		while(selectedPositions.size() > noOfKeyGate){
			selectedPositions.remove(selectedPositions.size()-1);
		}
		
		return selectedPositions;
	}
	
	private String readKeyFile(String keyFileName) {
		ReadWrite rw = new ReadWrite();
		String key = rw.fileReader(keyFileName).get(0).replace(" ","");
		if (!key.matches("[01]+")) {
			System.err.println("Key file contains invalid characters.");
			System.out.println("Key file must only contain 0 or 1");
//			logfile.add("Key file contains invalid characters.");
//			logfile.add("Key file must only contain 0 or 1");
			errorFlag = true;
			return "";
		}
		return key;
	}

	public static void main(String[] args) {
		if (args.length < 5){
			System.out.println("Usage: <fileName> <moduleName> <methodName> <keySize> <keyType>");
		}
		// TODO Auto-generated method stub
		String inputFileName = args[0];
		String moduleName = args[1];
		String methodName = args[2];
		String keySizeForOrgn = args[3]; // m%
		//String keySizeForAntiSAT = args[4]; // 4n
		// total key 4n + m%
		keyType = Float.parseFloat(args[4]);
		
		
		
		AntiSAT_Integrator Intergator = new AntiSAT_Integrator(inputFileName, moduleName, methodName, keySizeForOrgn);
	}

}